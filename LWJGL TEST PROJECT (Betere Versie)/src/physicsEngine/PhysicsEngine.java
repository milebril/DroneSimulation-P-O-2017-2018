package physicsEngine;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import entities.AirFoil;
import entities.Drone;

public class PhysicsEngine {
	
	public static void applyPhysics (Drone drone, double dt) {
		// get force and torque
		Vector3f[] forces = calculateForces(drone);
		
		// calculate and set the new linear and angular accelerations
		Vector3f[] accelerations = calculateAccelerations(drone, forces); // (in drone frame)
		drone.setLinearAcceleration(drone.transformToWorldFrame(accelerations[0]));
		drone.setAngularAcceleration(drone.transformToWorldFrame(accelerations[1]));
		
		// calculate and set the new linear and angular velocities
		Vector3f[] velocities = calculateVelocities(drone, accelerations, dt); // (in drone frame)
		drone.setLinearVelocity(drone.transformToWorldFrame(velocities[0]));
		drone.setAngularVelocity(drone.transformToWorldFrame(velocities[1]));
		
		// calculate and set the new position and orientation
		Vector3f[] positions = calculatePositions(drone, velocities);
		drone.setPosition(positions[0]);
		drone.setOrientation(positions[1]);
		
		
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
			
			
			
			// wind experienced by the airfoil
			// !!! hier heb ik airspeed = wind - velocity gedaan ipv airspeed = velocity - wind !!!
			Vector3f airSpeedW = new Vector3f();
			Vector3f.sub(getWindVelocity(), drone.getLinearVelocity(), airSpeedW);
			
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
		
		
		
		/*	oude loop (hield geen rekening met de zwaartekracht en deed airspeed = velocity - wind (ipv airspeed = wind - velocity) 
		
		//loopen over alle airfoils en bij elke de kracht uitrekenen en optellen bij force
		//idem vr torque
		for(AirFoil currentWing : drone.getAirFoils() ){
			Vector3f currentWingLiftForceD = new Vector3f();
			
			Vector3f centerOfMassVectorD = currentWing.getCenterOfMass();
			float liftSlope = currentWing.getLiftSlope();
			Vector3f projectedAirspeedVectorD = new Vector3f();
			Vector3f airspeedVectorW = new Vector3f();
			Vector3f airspeedVectorD = new Vector3f();
			Vector3f rotationAxisD = currentWing.getRotAxis();
			Vector3f normalD = currentWing.calculateNormal();
			Vector3f attackVectorD = currentWing.calculateAttackVector();			

			//difference between speedvector of drone and windspeed
			Vector3f.sub(drone.getLinearVelocity(), getWindVelocity(), airspeedVectorW);
			
			//transformation of airspeedvector to drone fraem
			airspeedVectorD = drone.transformToDroneFrame(airspeedVectorW);
			
			//projection of airpseed vector to surface perpendicular to rotation axis, stored to projectedairspeedvector
			Vector3f.sub(airspeedVectorD, (Vector3f) rotationAxisD.scale(Vector3f.dot(airspeedVectorD, rotationAxisD)), projectedAirspeedVectorD);
			
			float airspeed = projectedAirspeedVectorD.lengthSquared();
//			We define its angle of attack as -atan2(S . N, S . A), where S
//			is the projected airspeed vector, N is the normal, and A is the attack vector
			float aoa = (float) - Math.atan2(Vector3f.dot(projectedAirspeedVectorD, normalD), Vector3f.dot(projectedAirspeedVectorD, attackVectorD));					
			
			//Each airfoil generates a lift force N . liftSlope . AOA . s^2, where N is the
//			  normal, AOA is the angle of attack, and s is the projected airspeed.  No
//			  other forces operate on the drone; in particular, there is no drag.
			currentWingLiftForceD = (Vector3f) normalD.scale(liftSlope * aoa * airspeed);
			Vector3f.add(currentWingLiftForceD, force, force);
		}
		*/
		
		return new Vector3f[]{force, torque};
		
	}

	private static Vector3f[] calculateAccelerations(Drone drone, Vector3f[] forces) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Vector3f[] calculateVelocities(Drone drone, Vector3f[] accelerations, double dt) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Vector3f[] calculatePositions(Drone drone, Vector3f[] accelerations) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	




