package physicsEngine;

import org.lwjgl.util.vector.Vector3f;
import entities.AirFoil;
import entities.Drone;
import entities.Tyre;

public class PhysicsEngine {

	/**
	 * At which y-value the ground level is.
	 */
	public static double groundLevel = 1;

	// MAIN

	/**
	 * Applies physics to the given drone for dt seconds, translating the drone
	 * dt seconds into the future.
	 * 
	 * @throws DroneCrashException if the Drone crashes
	 * @throws MaxAoAException if the max angle of attack is exceeded while the lift force of
	 * 						   the airfoil is greater than 50N
	 */
	public static void applyPhysics(Drone drone, float dt) throws DroneCrashException, MaxAoAException {
		
		// stepsize bepalen
		float h;
		if (dt - drone.getPredictionMethod().getStepSize() >= 0) {
			h = drone.getPredictionMethod().getStepSize();
		} else if (dt > 0) {
			h = dt;
		} else {
			return;
		}
		

		// huidige versnellingen bepalen
		Vector3f[] currentAccelerationsW = calculateAccelerations(drone, h);
		
		// snelheid voorspellen in functie van de huidige vernsellingen en posities
		Vector3f[] newVelocities = drone.getPredictionMethod().predictVelocity(drone.getLinearVelocity(), drone.getAngularVelocity(), 
																				currentAccelerationsW[0], currentAccelerationsW[1], h);
		
		// nieuwe positie berekenen aan de hand van de nieuwe snelheid
		Vector3f[] deltaPositions = calculatePositionDifferences(drone, newVelocities, h);

		// nieuwe snelheid opslaan
		drone.setLinearVelocity(newVelocities[0]);
		drone.setAngularVelocity(newVelocities[1]);

		// translatie en rotatie uitvoeren
		drone.translate(deltaPositions[0]);

		if (!deltaPositions[1].equals(new Vector3f(0, 0, 0))) {
			Vector3f rotationAxis = new Vector3f(0, 0, 0);
			deltaPositions[1].normalise(rotationAxis);
			drone.rotate(deltaPositions[1].length(), rotationAxis);
		}

		// checken of de drone crasht
		Vector3f leftWingCenterOfMass = new Vector3f(0,0,0);
		Vector3f.add(drone.transformToWorldFrame(drone.getLeftWing().getCenterOfMass()), drone.getPosition(), leftWingCenterOfMass);
		Vector3f rightWingCenterOfMass = new Vector3f(0,0,0);
		Vector3f.add(drone.transformToWorldFrame(drone.getRightWing().getCenterOfMass()),drone.getPosition(), rightWingCenterOfMass);
		
		if (drone.transformToWorldFrame(drone.getEnginePosition()).y + drone.getPosition().y <=	groundLevel) {
			throw new DroneCrashException("Drone Crashed: the engine hit the ground!");
		} if (drone.transformToWorldFrame(drone.getTailMassPosition()).y  + drone.getPosition().y <= groundLevel) {
			throw new DroneCrashException("Drone Crashed: the tail hit the ground!");
		} if (leftWingCenterOfMass.y  + drone.getPosition().y <= groundLevel) {
			throw new DroneCrashException("Drone Crashed: the left wing hit the	ground!");
		} if (rightWingCenterOfMass.y  + drone.getPosition().y <= groundLevel) {
			throw new DroneCrashException("Drone Crashed: the right wing hit the ground!");
		}
		for (Tyre tyre : drone.getTyres()) {
			if (tyre.getRadius() < tyre.getCompression()) {
				throw new DroneCrashException("Drone Crashed: tyre compressed too much!");
			}
		}
		
		// recursieve oproep
		PhysicsEngine.applyPhysics(drone, (dt - h));
	}

	// ACCELERATIONS
	
	public static String p(Vector3f a) {
		return "(" + s(a.x) + ", " + s(a.y) + ", " + s(a.z) + ")";
	}
	
	public static String s(double a) {
		return Double.toString(Math.round(a*1000)/1000);
	}
	
	/**
	 * All the forces and torques exercised on the drone are calculated, added
	 * together and then returned in an array. uses: the AirFoils liftForce and
	 * gravitational force, the engine thrust and gravitational force, the tail
	 * mass gravitational force
	 * 
	 * @return array with force and torque on the drone (in world frame) |
	 *         Vector3f[]{total force, total torque} (in world frame)
	 */
	private static Vector3f[] calculateForces(Drone drone, float stepsize) throws MaxAoAException {

		boolean printAirfoils = false;
		boolean printCompression = false;
		boolean printFriction = false;
		boolean printTotal = false;
		
		// The total force and torque that are exercised on the given Drone (in
		// drone frame)
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
			
			if (printAirfoils) System.out.println(currentAirFoil.name);
			if (printAirfoils) System.out.println("  liftForceD: " + liftForceD);
			if (printAirfoils) System.out.println("  torqueD: " + currentAirFoilTorqueD);
		}
		
		// force exercised by the engine
		Vector3f thrustForceD = new Vector3f(0, 0, -drone.getThrustForce());
		Vector3f.add(force, thrustForceD, force);
		
		// gravitational force exercised by the mass
		Vector3f gravitationD = drone.transformToDroneFrame(new Vector3f(0, -drone.getMass() * drone.getGravity(), 0));
		Vector3f.add(force, gravitationD, force);

		// forces excersised by the Tyre compression and deltacompression
		double[] compressionForces = new double[] { 0, 0, 0 };
		int i = 0;
		
		for (Tyre tyre : drone.getTyres()) {
			// if the tyre is not grounded, there will be no compression forces
			// -> start next iteration of for loop
			if (!tyre.isGrounded())
				continue;

			double oldCompression = tyre.getSavedCompression();
			double compression = tyre.getCompression();
			tyre.saveCompression(compression);

			double deltaCompression = (compression - oldCompression) / stepsize;

			double comporessionForceSize = tyre.getTyreSlope() * compression + tyre.getDampSlope() * deltaCompression;
			Vector3f compressionForce = new Vector3f(0, 0, 0);
			compressionForce.y = (float) Math.abs(comporessionForceSize);
			compressionForce = drone.transformToDroneFrame(compressionForce);

			Vector3f compressionTorque = new Vector3f();
			Vector3f.cross(tyre.getGroundedPosition(), compressionForce, compressionTorque);
			
			if (printCompression) System.out.println(tyre.name + " compressionF: " + p(compressionForce) + " | compressionT: " + p(compressionTorque));
			
			// optellen bij het totaal
			Vector3f.add(force, compressionForce, force);
			Vector3f.add(torque, compressionTorque, torque);
			compressionForces[i] = Math.abs(comporessionForceSize);
			i++;
		}

		// forces excersised by the front Tyre brake force
		if (drone.getFrontTyre().isGrounded()) {

			// richting van de remkracht (in wereldassenstelsel)
			Vector3f brakeForce = drone.getVelocityOfPoint(drone.getFrontTyre().getGroundedPosition());
			
			// tegengestelde richting, y-component weglaten en normaliseren
			brakeForce.x = -brakeForce.x;
			brakeForce.y = 0;
			brakeForce.z = -brakeForce.z;

			if (brakeForce.length() != 0.0) {

				brakeForce.normalise();

				// orientatie schalen met de remkracht
				brakeForce.scale((float) drone.getFrontTyre().getBrakingForce());
				
				// naar drone assenstelsel
				brakeForce = drone.transformToDroneFrame(brakeForce);

				// resulterende torque
				Vector3f brakeTorque = new Vector3f();
				Vector3f.cross(drone.getFrontTyre().getGroundedPosition(), brakeForce, brakeTorque);

				// optellen bij het totaal
				Vector3f.add(force, brakeForce, force);
				Vector3f.add(torque, brakeTorque, torque);
				
				if (printFriction) System.out.println("front wheel brakeF: " + p(brakeForce) + " | brakeT: " + p(brakeTorque));
			}
		}

		// forces excersised by the rear Tyres brake force and wrijvingskracht
		for (Tyre tyre : new Tyre[] { drone.getLeftTyre(), drone.getRightTyre() }) {

			// if the tyre is not grounded, there will be no compression forces
			// -> start next iteration of for loop
			if (!tyre.isGrounded())
				continue;
			
			// snelheid vd tyre (in drone frame)
			Vector3f tyreVelocityD = drone.transformToDroneFrame(drone.getVelocityOfPoint(tyre.getGroundedPosition()));
			
			// ZIJWAARTSE WRIJVING
			
			Vector3f frictionOrientation = new Vector3f(0, 0, 0);
			if (0 < tyreVelocityD.x) frictionOrientation.x = -1;
			else frictionOrientation.x = 1;
			
			// projecteren op het grondvlak + normaliseren
			frictionOrientation = drone.transformToWorldFrame(frictionOrientation);
			frictionOrientation.y = 0;
			frictionOrientation = drone.transformToDroneFrame(frictionOrientation);
			frictionOrientation.normalise();

			// grootte vd wrijvingskracht
			float N = (float) compressionForces[1];
			double frictionForceSize = -Vector3f.dot(frictionOrientation, tyreVelocityD) * N * tyre.getMaxFrictionCoeff();

			frictionOrientation.scale((float) frictionForceSize);
			
			// REMKRACHT

			Vector3f rollingOrientation = new Vector3f(0, 0, 0);
			if (0 < tyreVelocityD.z) rollingOrientation.z = -1;
			else rollingOrientation.z = 1;
			
			// projecteren op het grondvlak + normaliseren
			rollingOrientation = drone.transformToWorldFrame(rollingOrientation);
			rollingOrientation.y = 0;
			rollingOrientation = drone.transformToDroneFrame(rollingOrientation);
			rollingOrientation.normalise();
			
			// grootte vd kracht
			rollingOrientation.scale((float) tyre.getBrakingForce());
			
			// BEIDE KRACHTEN OPTELLEN
			Vector3f totalTyreForce = new Vector3f();
			Vector3f.add(rollingOrientation, frictionOrientation, totalTyreForce);
			
			if (printFriction) System.out.println(tyre.name + " frictionF: " + p(frictionOrientation) + " | brakeF: " + p(rollingOrientation));
			
			// resulterende torque
			Vector3f brakeTorque = new Vector3f();
			Vector3f.cross(tyre.getGroundedPosition(), totalTyreForce, brakeTorque);

			// optellen bij het totaal
			Vector3f.add(force, totalTyreForce, force);
			Vector3f.add(torque, brakeTorque, torque);
			
		}

		if (printTotal) System.out.println("force (D): " + p(force));
		if (printTotal) System.out.println("torque (D): " + p(torque));
		
		// to world frame
		force = drone.transformToWorldFrame(force);
		torque = drone.transformToWorldFrame(torque);

		if (printTotal) System.out.println("force (W): " + p(force));
		if (printTotal) System.out.println("torque (W): " + p(torque));
		
		// return the results
		return new Vector3f[] { force, torque };
	}

	/**
	 * Calculates and returns the linear and angular accelerations of the drone
	 * (in world frame).
	 * 
	 * @return The linear and angular accelerations of the drone (in world
	 *         frame)
	 */
	private static Vector3f[] calculateAccelerations(Drone drone, float stepsize) throws MaxAoAException {
		return calculateAccelerations(drone, calculateForces(drone, stepsize));
	}

	/**
	 * Calculates and returns the linear and angular accelerations of the drone
	 * (in world frame). The given forces Vector3f[] array is assumed to contain
	 * the forces at index 0 and torque at index 1, both in drone frame.
	 * 
	 * @return The linear and angular accelerations of the drone (in world
	 *         frame)
	 */
	private static Vector3f[] calculateAccelerations(Drone drone, Vector3f[] forces) {
		// linear acceleration
		Vector3f linearAccelerationW = new Vector3f(forces[0].x / drone.getMass(), forces[0].y / drone.getMass(),
				forces[0].z / drone.getMass());

		// rotational inertia values
		float iXx = drone.getInertiaMatrix().m00;
		float iYy = drone.getInertiaMatrix().m11;
		float iZz = drone.getInertiaMatrix().m22;

		// angular velocity
		Vector3f omega = drone.getAngularVelocity();

		// angular acceleration
		Vector3f angularAccelerationW = new Vector3f();
		angularAccelerationW.x = (forces[1].x + (iYy - iZz) * omega.y * omega.z) / iXx;
		angularAccelerationW.y = (forces[1].y + (iZz - iXx) * omega.x * omega.z) / iYy;
		angularAccelerationW.z = (forces[1].z + (iXx - iYy) * omega.x * omega.y) / iZz;

		// return the results
		return new Vector3f[] { linearAccelerationW, angularAccelerationW };
	}

	// POSITION

	/**
	 * Calculates and returns the translation and rotation of the drone given
	 * its new velocities (in world frame). The given Vector3f[] array is
	 * assumed to contain the new linear velocity at index 0 and the new angular
	 * velocity at index 1, both in world frame. Note that this function also
	 * uses the previous velocities of the drone and thus should be called
	 * before the given newVelocities are saved in the drone.
	 * 
	 * @return the translation and rotation of the drone given its new
	 *         velocities (in world frame)
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

		return new Vector3f[] { deltaPositionW, deltarotationW };
	}

	/**
	 * Returns the average of the two given vectors.
	 * 
	 * @return the average of the two given vectors
	 */
	private static Vector3f average(Vector3f a, Vector3f b) {
		return new Vector3f(a.x + (b.x - a.x) / 2, a.y + (b.y - a.y) / 2, a.z + (b.z - a.z) / 2);
	}
	
	
	public static String vectorToIntString(Vector3f vector) {
		return "(" + (vector.x) + ", " + (vector.y) + ", "+ (vector.z) + ")"; 
	}
}