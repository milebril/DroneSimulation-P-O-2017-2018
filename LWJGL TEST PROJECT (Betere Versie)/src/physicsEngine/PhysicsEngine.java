package physicsEngine;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import entities.AirFoil;
import entities.Drone;

public class PhysicsEngine {
	
	public static void applyPhysics (Drone drone, float dt) {
		float h;
		
		if(dt - drone.getPredictionMethod().getStepSize() >= 0){
			h = drone.getPredictionMethod().getStepSize();
		} else if (dt > 0) {
			h = dt;
		} else {
			return;
		}
			
			
		// eerst de snelheden voorspellen met de huidige snelheden en versnelling en positie
		// dan de posities voorspellen 
		Vector3f[] newVelocities = drone.getPredictionMethod().predictVelocity(drone.getLinearVelocity(), drone.getAngularVelocity(), drone.getLinearAcceleration(), drone.getAngularAcceleration(), h);
		Vector3f[] deltaPositions = calculatePositions(drone, newVelocities, h);
		
		drone.setLinearVelocity(drone.transformToWorldFrame(newVelocities[0]));
		drone.setAngularVelocity(drone.transformToWorldFrame(newVelocities[1]));	
		
		// posities updaten
		Vector3f rotationAxis = new Vector3f();
		deltaPositions[1].normalise(rotationAxis);
		drone.translate(deltaPositions[0]);
		drone.rotate(deltaPositions[1].length(), rotationAxis );
		
		// get force and torque voor de nieuw berekende situatie n bijhouden van de versnelling in de drone
		Vector3f[] forces = calculateForces(drone);
		
		// calculate the new properties
		Vector3f[] newAccelerations = calculateAccelerations(drone, forces); // (in drone frame)
		
		// set the new properties
		drone.setLinearAcceleration(drone.transformToWorldFrame(newAccelerations[0]));
		drone.setAngularAcceleration(drone.transformToWorldFrame(newAccelerations[1]));		
		
		//recursieve oproep
		PhysicsEngine.applyPhysics(drone, (dt - h));
		
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
			
			
			Vector3f liftForceD = currentAirFoil.calculateAirFoilLiftForce();	
			
//			System.out.println("PE.calculateForces : liftforceD: " + i + " " + liftForceD);

			// calculate torque
			Vector3f currentAirFoilTorqueD = new Vector3f(0, 0, 0);
			Vector3f.cross(currentAirFoil.getCenterOfMass(), liftForceD, currentAirFoilTorqueD);
			
			
			// add the calculated force and torque to the total force and torque
			Vector3f.add(force, liftForceD, force);
			Vector3f.add(torque, currentAirFoilTorqueD, torque);
		}
		
		
		// The force exercised by the engine
//		System.out.println("PE.calculateforces: thrustforce: " + drone.getThrustForce());
		Vector3f thrustForceD = new Vector3f(0, 0, - drone.getThrustForce());
		Vector3f.add(force, thrustForceD, force);

		
		// The gravitational force exercised by the mass
		Vector3f gravitationD = drone.transformToDroneFrame(new Vector3f(0, - drone.getMass()*drone.getGravity(), 0));
		Vector3f.add(force, gravitationD, force);
		
//		System.out.println("PE.calcforces gravitational: " + drone.getMass()*drone.getGravity());

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
//		System.out.println("PE.calculateAccel dronemass: " + drone.getMass());
//		System.out.println("PE.calculateAccel forces: " + forces[0]);
//		System.out.println("PE.calculateAccel linearaccel: " + linearAccelerationD);
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
		
		Vector3f deltaAngularVelocityD = (Vector3f) accelerations[1].scale(dt);
		Vector3f angularVelocityD = drone.transformToDroneFrame(drone.getAngularVelocity());
		Vector3f.add(deltaAngularVelocityD, angularVelocityD, angularVelocityD);
		
		return new Vector3f[]{linearVelocityD, angularVelocityD};
	}
	
	/**
	 * Calculates and returns the change in position and orientation of the drone (in world frame).
	 * The given newVelocities Vector3f[] array is assumed to contain the linear at index 0 and
	 * the angular velocity at index 1, both in world frame.
	 * Note that this function also uses the previous velocities of the drone and thus should
	 * be called before the given newVelocities are saved in the drone.
	 * @return the new position and orientation of the drone (in world frame)
	 */
	private static Vector3f[] calculatePositions(Drone drone, Vector3f[] newVelocities, float dt) {
		// save current and previous linear velocities in local variables
		Vector3f prevLinearVelocityW= drone.getLinearVelocity();
		Vector3f newLinearVelocityW = new Vector3f(newVelocities[0].x, newVelocities[0].y, newVelocities[0].z);
		
		// calculate the average linear velocity
		Vector3f avgLinearVelocityW = average(prevLinearVelocityW, newLinearVelocityW);
		
		// calculate the position difference
		Vector3f deltaPositionW = (Vector3f) avgLinearVelocityW.scale(dt);
		
		// add the position difference to the current position	
		
		Vector3f prevAngularVelocityW = (drone.getAngularVelocity());
		Vector3f newAngularVelocityW = newVelocities[1];
		Vector3f avgAngularVelocityW = average(prevAngularVelocityW, newAngularVelocityW);
		// lengte van omega maal de tijd is  
		Vector3f deltarotationW = ((Vector3f) avgAngularVelocityW.scale(dt));
		
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



