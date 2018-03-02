package physicsEngine;

import org.lwjgl.util.vector.Vector3f;
import entities.AirFoil;
import entities.Drone;
import entities.Tyre;

public class PhysicsEngine {
	
	/**
	 * At which y-value the ground level is.
	 */
	public static double groundLevel = 0;
	
	// MAIN
	
	/**
	 * Applies physics to the given drone for dt seconds, translating the drone dt seconds into the future.
	 * @throws DroneCrashException if the Drone crashes
	 */
	public static void applyPhysics (Drone drone, float dt) throws DroneCrashException {
		
		// stepsize bepalen
		float h;
		if(dt - drone.getPredictionMethod().getStepSize() >= 0){
			h = drone.getPredictionMethod().getStepSize();
		} else if (dt > 0) {
			h = dt;
		} else {
			return;
		}
		
		// huidige versnellingen bepalen
		Vector3f[] currentAccelerationsD = calculateAccelerations(drone, h);
		
		// snelheid voorspellen in functie van de huidige vernsellingen en posities
		Vector3f[] newVelocities = drone.getPredictionMethod().predictVelocity(
				drone.transformToDroneFrame(drone.getLinearVelocity()), 
				drone.transformToDroneFrame(drone.getAngularVelocity()), 
				currentAccelerationsD[0], currentAccelerationsD[1], h);
		
		// nieuwe positie berekenen aan de hand van de nieuwe snelheid
		Vector3f[] deltaPositions = calculatePositionDifferences(drone, newVelocities, h);
		
		// nieuwe snelheid opslaan
		drone.setLinearVelocity(drone.transformToWorldFrame(newVelocities[0]));
		drone.setAngularVelocity(drone.transformToWorldFrame(newVelocities[1]));	
		
		// translatie en rotatie uitvoeren
		drone.translate(deltaPositions[0]);
		
		if (!deltaPositions[1].equals(new Vector3f(0,0,0))) {
			Vector3f rotationAxis = new Vector3f(0,0,0);
			deltaPositions[1].normalise(rotationAxis);
			drone.rotate(deltaPositions[1].length(), rotationAxis);
		}
		
		// checken of de drone crasht
		Vector3f leftWingCenterOfMass = new Vector3f(0,0,0);
		Vector3f.add(drone.transformToWorldFrame(drone.getLeftWing().getCenterOfMass()), drone.getPosition(), leftWingCenterOfMass);
		Vector3f rightWingCenterOfMass = new Vector3f(0,0,0);
		Vector3f.add(drone.transformToWorldFrame(drone.getRightWing().getCenterOfMass()), drone.getPosition(), rightWingCenterOfMass);
		if (drone.transformToWorldFrame(drone.getEnginePosition()).y <= groundLevel) {
			throw new DroneCrashException("Drone Crashed: the engine hit the ground!");
		} else if (drone.transformToWorldFrame(drone.getTailMassPosition()).y <= groundLevel) {
			throw new DroneCrashException("Drone Crashed: the tail hit the ground!");
		} else if (leftWingCenterOfMass.y <= groundLevel) {
			throw new DroneCrashException("Drone Crashed: the left wing hit the ground!");
		} else if (rightWingCenterOfMass.y <= groundLevel) {
			throw new DroneCrashException("Drone Crashed: the right wing hit the ground!");
		}
		for (Tyre tyre : drone.getTyres()) {
			if (tyre.getRadius() < tyre.getCompression()) {
				throw new DroneCrashException("Drone Crashed: tyre compressed too much!");
			}
		}
		
		
		//recursieve oproep
		PhysicsEngine.applyPhysics(drone, (dt - h));
	}
	
	// ACCELERATIONS
	
	/**
	 * All the forces and torques exercised on the drone are calculated, added together and then
	 * returned in an array.
	 * uses: the AirFoils liftForce and gravitational force, the engine thrust and gravitational force,
	 * the tail mass gravitational force
	 * @return array with force and torque on the drone (in drone frame)
	 * 		 | Vector3f[]{total force, total torque} (in drone frame)
	 */
	private static Vector3f[] calculateForces(Drone drone, float stepsize){
		
		// The total force and torque that are exercised on the given Drone (in drone frame)
		Vector3f force = new Vector3f(0, 0, 0);
		Vector3f torque = new Vector3f(0, 0, 0);
		
		// calculate the forces applied by the airFoils (liftForce + gravity)
		for (int i = 0; i < drone.getAirFoils().length; i++) {
			
			// get the current AirFoil
			AirFoil currentAirFoil = drone.getAirFoils()[i];
			
			// get the liftforce
			Vector3f liftForceD = currentAirFoil.calculateAirFoilLiftForce();

			// calculate torque
			Vector3f currentAirFoilTorqueD = new Vector3f(0, 0, 0);
			Vector3f.cross(currentAirFoil.getCenterOfMass(), liftForceD, currentAirFoilTorqueD);
			
			
			// add the calculated force and torque to the total force and torque
			Vector3f.add(force, liftForceD, force);
			Vector3f.add(torque, currentAirFoilTorqueD, torque);
			
		}
		
		// force exercised by the engine
		Vector3f thrustForceD = new Vector3f(0, 0, - drone.getThrustForce());
		Vector3f.add(force, thrustForceD, force);

		// gravitational force exercised by the mass
		Vector3f gravitationD = drone.transformToDroneFrame(new Vector3f(0, - drone.getMass()*drone.getGravity(), 0));
		Vector3f.add(force, gravitationD, force);
		
		// forces excersised by the Tyre compression and deltacompression
		for (Tyre tyre : drone.getTyres()) {
			double oldCompression = tyre.getSavedCompression();
			double compression = tyre.getCompression();
			tyre.saveCompression(compression);
			
			double deltaCompression = (compression - oldCompression) / stepsize;
			
			double comporessionForceSize = tyre.getTyreSlope()*compression + tyre.getDampSlope()*deltaCompression;
			Vector3f compressionForce = new Vector3f(0, 0, 0);
			compressionForce.y = (float) Math.abs(comporessionForceSize);
			
			Vector3f compressionTorque = new Vector3f();
			Vector3f.cross(tyre.getPosition(), compressionForce, compressionTorque);
			
			Vector3f.add(force, compressionForce, force);
			Vector3f.add(torque, compressionTorque, torque);
		}
		
		// forces excersised by the front Tyre brake force
		if (drone.getFrontTyre().isGrounded()) {
			Vector3f groundedPosition = drone.getFrontTyre().getGroundedProperties()[1];
			Vector3f appliedForceW = drone.transformToWorldFrame(getForceAppliedAtPoint(groundedPosition, force, torque));
			appliedForceW.y = 0; // kan geen rem kracht uitoefenen in de wereld y richting
			Vector3f appliedForceD = drone.transformToDroneFrame(appliedForceW);
			
			// orientatie van de rem kracht is tegengesteld aan de uitgeoefende kracht.
			Vector3f brakeForce = new Vector3f();
			appliedForceD.negate(brakeForce);
			brakeForce.normalise();
			
			// rem kracht is maximum gelijk aan de applied force
			brakeForce.scale((float)Math.min(appliedForceD.length(), drone.getFrontTyre().getBrakingForce()));
			
			// resulterende torque
			Vector3f brakeTorque = new Vector3f();
			Vector3f.cross(groundedPosition, brakeForce, brakeTorque);
			
			// optellen bij het geheel
			Vector3f.add(force, brakeForce, force);
			Vector3f.add(torque, brakeTorque, torque);
		}
		
		// forces excersised by the rear Tyres brake force and wrijvingskracht
		for (Tyre tyre : new Tyre[] {drone.getLeftTyre(), drone.getRightTyre()}) {
			Vector3f[] groundedProperties = tyre.getGroundedProperties();
			Vector3f groundedPosition = groundedProperties[0];
			Vector3f rollingOrientation = groundedProperties[1];
			Vector3f slidingOrientation = groundedProperties[2];
			
			Vector3f appliedForceD = getForceAppliedAtPoint(rollingOrientation, force, torque);
			
			// braking force
			// TODO: kracht (uit appliedForceD) berekenen die in de rollingOrientation wordt uitgeoefend
			// 			(-> appliedForceD op rollingOrientation projecteren)
			// 		 en dan de reactie-kracht hierop berekenen
			
			// wrijvingskracht
			// TODO: kracht (uit appliedForceD) berekenen die in de slidingOrientation wordt uitgeoefend
			// 			(-> appliedForceD op slidingOrientation projecteren)
			// 		 en dan de reactie-kracht hierop berekenen
			
			// optellen bij het geheel
			// TODO: braking en sliding kracht optellen (force en torque)
		}
		
		
		// return the results
		return new Vector3f[]{force, torque};
	}
	
	/**
	 * Calculates and returns the linear and angular accelerations of the drone (in drone frame).
	 * @return The linear and angular accelerations of the drone (in drone frame)
	 */
	private static Vector3f[] calculateAccelerations(Drone drone, float stepsize) {
		return calculateAccelerations(drone, calculateForces(drone, stepsize));
	}
	
	/**
	 * Calculates and returns the linear and angular accelerations of the drone (in drone frame). 
	 * The given forces Vector3f[] array is assumed to contain the forces at index 0 and 
	 * torque at index 1, both in drone frame.
	 * @return The linear and angular accelerations of the drone (in drone frame)
	 */
	private static Vector3f[] calculateAccelerations(Drone drone, Vector3f[] forces) {
		// linear acceleration
		Vector3f linearAccelerationD = new Vector3f(forces[0].x / drone.getMass(), 
				forces[0].y / drone.getMass(), forces[0].z / drone.getMass());
		
		// rotational inertia values
		float iXx = drone.getInertiaMatrix().m00;
		float iYy = drone.getInertiaMatrix().m11;
		float iZz = drone.getInertiaMatrix().m22;
		
		// angular velocity
		Vector3f omega = drone.transformToDroneFrame(drone.getAngularVelocity());
		
		// angular acceleration
		Vector3f angularAccelerationD = new Vector3f();
		angularAccelerationD.x = (forces[1].x + (iYy - iZz)*omega.y*omega.z)/iXx;
		angularAccelerationD.y = (forces[1].y + (iZz - iXx)*omega.x*omega.z)/iYy;
		angularAccelerationD.z = (forces[1].z + (iXx - iYy)*omega.x*omega.y)/iZz;
		
		// return the results
		return new Vector3f[]{linearAccelerationD, angularAccelerationD};
	}
	
	// POSITION
	
	/**
	 * Calculates and returns the translation and rotation of the drone given its new velocities (in world frame).
	 * The given Vector3f[] array is assumed to contain the new linear velocity at index 0 and
	 * the new angular velocity at index 1, both in world frame.
	 * Note that this function also uses the previous velocities of the drone and thus should
	 * be called before the given newVelocities are saved in the drone.
	 * @return the translation and rotation of the drone given its new velocities (in world frame)
	 */
	private static Vector3f[] calculatePositionDifferences(Drone drone, Vector3f[] newVelocities, float dt) {
		
		// calculate the average linear velocity
		Vector3f avgLinearVelocityW = average(drone.getLinearVelocity(), newVelocities[0]);
		
		// calculate the translation
		Vector3f deltaPositionW = (Vector3f) avgLinearVelocityW.scale(dt);
		
		// calculate the average angular velocity
		Vector3f avgAngularVelocityW = average(drone.getAngularVelocity(), newVelocities[1]);

		// calculate the rotation
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
	
	
	/**
	 * Returns the force that is applied at the given point by calculating the sum of the
	 * given force and the force applied by the given torque
	 */
	private static Vector3f getForceAppliedAtPoint(Vector3f point, Vector3f force, Vector3f torque) {
		// afstand torque axis <-> punt
		double distance = Math.abs(torque.x*point.x + torque.y*point.y + torque.z*point.z) / torque.length();
		
		// TODO: torque to force
		
		
		
		return null;
	}
	
	
}



