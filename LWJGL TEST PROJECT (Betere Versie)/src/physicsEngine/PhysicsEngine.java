package physicsEngine;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import entities.AirFoil;
import entities.Drone;

public class PhysicsEngine {
	
	public static void applyPhysics (Drone drone, float dt) {
		
		// get force and torque
		Vector3f[] forces = calculateForces(drone);
		
		// calculate the new properties
		Vector3f[] newAccelerations = calculateAccelerations(drone, forces); // (in drone frame)
		Vector3f[] newVelocities = calculateVelocities(drone, newAccelerations, dt); // (in drone frame)
		Vector3f[] newPositions = calculatePositions(drone, newVelocities, dt); // (in world frame)
		
		// set the new properties
		drone.setLinearAcceleration(drone.transformToWorldFrame(newAccelerations[0]));
		drone.setAngularAcceleration(drone.transformToWorldFrame(newVelocities[1]));
		
		drone.setLinearVelocity(drone.transformToWorldFrame(newVelocities[0]));
		drone.setAngularVelocity(drone.transformToWorldFrame(newVelocities[1]));
		
		//de positie wordt opgeslagen in een posematrix
		drone.translate(newPositions[0]);
		drone.rotate(newPositions[1]);
	}
	
	/**
	 * Returns the wind velocity.
	 */
	private static Vector3f getWindVelocity(){
		return new Vector3f(0,0,0);
	}

	/**
	 * All the forces and torques exercised on the drone are calculated, added together and then
	 * returned in an array.
	 * uses: the AirFoils liftForce and gravitational force, the engine thrust and gravitational force,
	 * the tail mass gravitational force
	 * @return array with force and torque on the drone (in drone frame)
	 * 		 | Vector3f[]{total force, total torque}
	 */
	private static Vector3f[] calculateForces(Drone drone){
		
		// The total force and torque that are exercised on the given Drone (in drone frame)
		Vector3f force = new Vector3f(0, 0, 0);
		Vector3f torque = new Vector3f(0, 0, 0);
		
		
		// calculate the forces applied by the airFoils (liftForce + gravity)
		for (int i = 0; i < drone.getAirFoils().length; i++) {
			
			// get the current AirFoil
			AirFoil currentAirFoil = drone.getAirFoils()[i];
			
			
			// calculate the airspeed the airfoil experiences
			Vector3f airSpeedW = new Vector3f(0, 0, 0);
			
			// velocity of the airfoil caused by the drones rotation (omega x r = v)
			Vector3f rotationalVelocityW = new Vector3f();
			Vector3f.cross(drone.getAngularVelocity(), drone.transformToWorldFrame(currentAirFoil.getCenterOfMass()), rotationalVelocityW);
			
			// velocity of the airfoil caused by the drones linear velocity
			Vector3f linearVelocityW = drone.getLinearVelocity();
			
			// wind velocity in the world
			Vector3f windW = getWindVelocity();
			
			// airspeed = wind - airfoil velocity
			Vector3f.sub(airSpeedW, rotationalVelocityW, airSpeedW);
			Vector3f.sub(airSpeedW, linearVelocityW, airSpeedW);
			Vector3f.add(airSpeedW, windW, airSpeedW);
			
			// transform the airSpeed vector to the drone frame
			Vector3f airSpeedD = drone.transformToDroneFrame(airSpeedW);
			
			
			// project airSpeedD on the surface, perpendicular to the rotationAxis of the AirFoil
			Vector3f rotationAxisD = currentAirFoil.getRotAxis();
			Vector3f projectedAirspeedVectorD = new Vector3f(0, 0, 0);
			Vector3f.sub(airSpeedD, (Vector3f) rotationAxisD.scale(Vector3f.dot(airSpeedD, rotationAxisD)), projectedAirspeedVectorD);
			
			// calculate the angle of attack, defined as -atan2(S . N, S . A), where S
			// is the projected airspeed vector, N is the normal, and A is the attack vector
			Vector3f normalD = currentAirFoil.calculateNormal(); // N
			Vector3f attackVectorD = currentAirFoil.calculateAttackVector(); // A
			float aoa = (float) - Math.atan2(Vector3f.dot(projectedAirspeedVectorD, normalD), 
												Vector3f.dot(projectedAirspeedVectorD, attackVectorD));					
			
			// calculate the lift force N . liftSlope . AOA . s^2, where N is the
			// normal, AOA is the angle of attack, and s is the projected airspeed
			float airspeedSquared = projectedAirspeedVectorD.lengthSquared();
			Vector3f liftForceD = (Vector3f) normalD.scale(currentAirFoil.getLiftSlope() * aoa * airspeedSquared);
			
			
			// calculate the gravitational force exercised on the AirFoil
			Vector3f gravitationalForceW = new Vector3f(0, -drone.getGravity() * currentAirFoil.getMass(), 0);
			Vector3f gravitationalForceD = drone.transformToDroneFrame(gravitationalForceW);
			
			
			// total force exercised on the AirFoil
			Vector3f currentAirFoilForceD = new Vector3f(0, 0, 0);
			Vector3f.add(liftForceD, gravitationalForceD, currentAirFoilForceD);
			
			
			// calculate torque
			Vector3f currentAirFoilTorqueD = new Vector3f(0, 0, 0);
			Vector3f.cross(currentAirFoil.getCenterOfMass(), currentAirFoilForceD, currentAirFoilTorqueD);
			
			
			// add the calculated force and torque to the total force and torque
			Vector3f.add(force, currentAirFoilForceD, force);
			Vector3f.add(torque, currentAirFoilTorqueD, torque);
		}
		
		
		// The force exercised by the engine
		Vector3f gravitationalEngineForceW = new Vector3f(0, -drone.getGravity() * drone.getEngineMass(), 0);
		Vector3f gravitationalEngineForceD = drone.transformToDroneFrame(gravitationalEngineForceW);
		Vector3f thrustForceD = new Vector3f(0, 0, drone.getThrustForce());
		Vector3f totalEngineForceD = new Vector3f(0, 0, 0);
		Vector3f.add(gravitationalEngineForceD, thrustForceD, totalEngineForceD);
		
		Vector3f.add(force, totalEngineForceD, force);
		
		// The torque exercised by the engine
		Vector3f engineTorqueD = new Vector3f(0, 0, 0);
		Vector3f.cross(drone.getEnginePosition(), totalEngineForceD, engineTorqueD);
		
		Vector3f.add(torque, engineTorqueD, torque);
		
		
		// The force exercised by the tail mass
		Vector3f gravitationalTailForceW = new Vector3f(0, -drone.getGravity() * drone.getTailMass(), 0);
		Vector3f gravitationalTailForceD = drone.transformToDroneFrame(gravitationalTailForceW);
		
		Vector3f.add(force, gravitationalTailForceD, force);
		
		// The torque exercised by the tail mass
		Vector3f tailTorqueD = new Vector3f(0, 0, 0);
		Vector3f.cross(drone.getTailMassPosition(), gravitationalTailForceD, tailTorqueD);
		
		Vector3f.add(torque, tailTorqueD, torque);
		
		
		return new Vector3f[]{force, torque};
	}
	
	/**
	 * Calculates and returns the linear and angular accelerations of the drone (in drone frame). 
	 * The given forces Vector3f[] array is assumed to contain the forces at index 0 and 
	 * torque at index 1, both in drone frame.
	 * @return The linear and angular accelerations of the drone (in drone frame)
	 */
	private static Vector3f[] calculateAccelerations(Drone drone, Vector3f[] forces) {
		// linear acceleration (F = m.a -> a = F/m)
		Vector3f linearAccelerationD = new Vector3f(forces[0].x / drone.getMass(), 
				forces[0].y / drone.getMass(), forces[0].z / drone.getMass());
		
		Vector3f angularAccelerationD = new Vector3f(0, 0, 0);
		float iXx = drone.getInertiaMatrix().m00;
		float iYy = drone.getInertiaMatrix().m11;
		float iZz = drone.getInertiaMatrix().m22;
		Vector3f omega = drone.transformToDroneFrame(drone.getAngularVelocity());
		
		angularAccelerationD.x = (forces[1].x + (iYy - iZz)*omega.y*omega.z)/iXx;
		angularAccelerationD.y = (forces[1].y + (iZz - iXx)*omega.x*omega.z)/iYy;
		angularAccelerationD.z = (forces[1].z + (iXx - iYy)*omega.x*omega.y)/iZz;
		
		return new Vector3f[]{linearAccelerationD, angularAccelerationD};
	}
	
	/**
	 * Calculates and returns the linear and angular velocities of the drone (in drone frame).
	 * The given newAcclerations Vector3f[] array is assumed to contain the linear at index 0 and 
	 * angular at index 1, both in drone frame.
	 * Note that this function also uses the previous accelerations of the drone and thus should
	 * be called before the given newAcclerations are saved in the drone.
	 * @return the linear and angular velocities of the drone (in drone frame)
	 */
	private static Vector3f[] calculateVelocities(Drone drone, Vector3f[] accelerations, float dt) {

		// save current and previous linear accelerations in local variables
//		Vector3f prevLinearAccelerationD = drone.transformToDroneFrame(drone.getLinearAcceleration());
//		Vector3f newLinearAccelerationD = new Vector3f(newAccelerations[0].x, newAccelerations[0].y, newAccelerations[0].z);
		
		// calculate the linear velocity difference
		Vector3f deltaLinearVelocityD = (Vector3f) accelerations[0].scale(dt);
		
		// add the linear velocity difference to the current velocity
		Vector3f linearVelocityD = drone.transformToDroneFrame(drone.getLinearVelocity());
		Vector3f.add(deltaLinearVelocityD, linearVelocityD, linearVelocityD);		
		
		// TODO: angular velocity
		Vector3f deltaAngularVelocityD = (Vector3f) accelerations[1].scale(dt);
		Vector3f angularVelocityD = drone.transformToDroneFrame(drone.getAngularVelocity());
		Vector3f.add(deltaAngularVelocityD, angularVelocityD, angularVelocityD);
		
		return new Vector3f[]{linearVelocityD, angularVelocityD};
	}
	
	/**
	 * Calculates and returns the change in position and orientation of the drone (in world frame).
	 * The given newVelocities Vector3f[] array is assumed to contain the linear at index 0 and
	 * the angular velocity at index 1, both in drone frame.
	 * Note that this function also uses the previous velocities of the drone and thus should
	 * be called before the given newVelocities are saved in the drone.
	 * @return the new position and orientation of the drone (in world frame)
	 */
	private static Vector3f[] calculatePositions(Drone drone, Vector3f[] newVelocities, float dt) {
		// save current and previous linear velocities in local variables
		Vector3f prevLinearVelocityD = drone.transformToDroneFrame(drone.getLinearVelocity());
		Vector3f newLinearVelocityD = new Vector3f(newVelocities[0].x, newVelocities[0].y, newVelocities[0].z);
		
		// calculate the average linear velocity
		Vector3f avgLinearVelocityD = average(prevLinearVelocityD, newLinearVelocityD);
		
		// calculate the position difference
		Vector3f deltaPositionW = drone.transformToWorldFrame((Vector3f) avgLinearVelocityD.scale(dt));
		
		// add the position difference to the current position
//		Vector3f positionW = drone.getPosition();
//		Vector3f.add(deltaPositionW, positionW, positionW);		
		
		// TODO: orientation
		Vector3f prevAngularVelocityD = drone.transformToDroneFrame(drone.getAngularVelocity());
		Vector3f newAngularVelocityD = newVelocities[1];
		Vector3f avgAngularVelocityD = average(prevAngularVelocityD, newAngularVelocityD);
		
		Vector3f deltarotationW = drone.transformToWorldFrame((Vector3f) avgAngularVelocityD.scale(dt));
		
		return new Vector3f[]{deltaPositionW, deltarotationW};
	}
	
	/**
	 * Returns the average of the two given vectors.
	 * @return the average of the two given vectors
	 */
	private static Vector3f average(Vector3f a, Vector3f b) {
		return new Vector3f(a.x + (b.x - a.x) / 2, a.y + (b.y - a.y) / 2, a.z + (b.z - a.z) / 2);
	}
}
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	




