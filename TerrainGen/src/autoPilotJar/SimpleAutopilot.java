package autoPilotJar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import interfaces.Autopilot;
import interfaces.AutopilotConfig;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;
import openCV.ImageProcessor;
import path.MyPath;

public class SimpleAutopilot implements Autopilot, AutopilotOutputs {

	private List<Vector3f> cubePositions = new ArrayList<>();
	private MyPath path;
	public float minY = 20;
	public float maxY = 20;

	// Aanpassen als we naar nieuwe cubus moeten gaan
	private Vector3f stubCube = new Vector3f(0, 0, -40);
	protected Vector3f cubePos = stubCube;

	private float heightGoal = 1;

	private ImageProcessor cubeLocator;
	protected PIDController pidHorStab;
	private PIDController pidHorWing;
	private PIDController pidHorGoal;
	private PIDController pidVerStab;
	private PIDController pidWings;
	private PIDController pidRoll;
	private PIDController pidThrust;

	protected boolean isFinished = false;
	public boolean failed = false;

	/*
	 * Variables to send back to drone Initialy All inclinations are 0
	 */
	private float newThrust = 0;
	protected float newLeftWingInclination = 0;
	protected float newRightWingInclination = 0;
	protected float newHorStabInclination = 0;
	protected float newVerStabInclination = 0;	private float newLeftBrake = 0;
	private float newRightBrake = 0;
	private float newFrontBrake = 0;
	private AutopilotStages stage = AutopilotStages.FLYING;
	
	public float p, i, d;
	
	private FlyingAutopilot flyingAP;
	private TakeOffAutopilot takeOffAP;
	private TaxiAutopilot taxiAP;

	public SimpleAutopilot() {
		flyingAP = new FlyingAutopilot();
		takeOffAP = new TakeOffAutopilot(this);
		taxiAP = new TaxiAutopilot(this);
		
//		float[] pathX = { 0, 0, 0, 0, 0, 0 };
//		float[] pathY = { 25, 10, 30, 60, 30, 20 };
//		float[] pathZ = { -80, -160, -240, -320, -400, -480 };
////		float[] pathZ = { -480, -560, -640, -720, -800, -880 };
//		this.path = new MyPath(pathX, pathY, pathZ);
//		this.path.setIndex(0);

		//this.cubePos = new Vector3f(path.getCurrentX(), path.getCurrentY(), path.getCurrentZ());

		// Initialize PIDController for horizontalflight
		// PIDController(float K-Proportional, float K-Integral, float K-Derivative,
		// float changeFactor, float goal)
		Random r = new Random();
//		p = 1.1233587f + (r.nextFloat() - 0.5f) / 100;
//		i = 0.30645216f + (r.nextFloat() - 0.5f) / 100;
//		d = 1.1156111f + (r.nextFloat() - 0.5f) / 100;
		p = 1 + r.nextFloat();
		i = Math.abs(r.nextFloat() - 0.5f);
		d = 1 + r.nextFloat();
		
		this.pidHorStab = new PIDController(p, i, d, (float) (Math.PI / 180), 0);
//		this.pidHorStab = new PIDController(1.1233587f, 0.30645216f, 1.1156111f, (float) (Math.PI / 180), 0);
		
		this.pidVerStab = new PIDController(2.5f, 0.0f, 2.0f, (float) (Math.PI / 180), 0);

		// PID for Roll (als we dat ooit gaan gebruiken)
		// this.pidWings = new PIDController(1.0f,0.0f,5.0f,(float)
		// Math.toRadians(1),0);
		this.pidRoll = new PIDController(0.0f, 0.5f, 1.0f, (float) Math.toRadians(1), 0);

		// Initialize PIDController for Thrust
		// PIDController(float K-Proportional, float K-Integral, float K-Derivative,
		// float changeFactor, float goal)
		// this.pidThrust = new PIDController(1.0f, 0.0f, 3.0f, -10, 10);
	}

	// Autopilot communication

	/**
	 * The Autopilot config
	 */
	protected AutopilotConfig configAP;

	/**
	 * Getter for the Autopilot config
	 */
	public AutopilotConfig getConfig() {
		return this.configAP;
	}

	/**
	 * The Autopilot input
	 */
	protected AutopilotInputs inputAP;

	/**
	 * Getter for the Autopilot input
	 */
	public AutopilotInputs getInput() {
		return this.inputAP;
	}

	@Override
	public AutopilotOutputs simulationStarted(AutopilotConfig config, AutopilotInputs inputs) {
		this.configAP = config;
		this.inputAP = inputs;

		cubeLocator = new ImageProcessor(this);

		return this;
	}

	protected int checkpoint = -80;
	
	@Override
	public AutopilotOutputs timePassed(AutopilotInputs inputs) {
		this.inputAP = inputs;
		if (inputs.getY() > this.maxY)
			this.maxY = inputs.getY();

		if (inputs.getY() < this.minY)
			this.minY = inputs.getY();

		// System.out.println("Max X: " + this.maxY);
		// System.out.println("Min X: " + this.maxY);

		if (this.inputAP.getElapsedTime() > 0.0000001) {
			setDroneProperties(inputs);

			if (getProperties().getVelocity().length() > 80) // als de drone sneller vliegt dan 60m/s zet de thrust dan
				this.newThrust = 0;
			else
				this.newThrust = configAP.getMaxThrust();

			// newLeftWingInclination = 0;
			// newRightWingInclination = 0;
			switch (stage) {
			case TAKE_OFF:
				return takeOffAP.timePassed(properties);
			case FLYING:
				return flyingAP.timePassed(properties);
			case TAXI:
				return taxiAP.timePassed(properties);
			default:
				break;
			}

			// switch (this.stages) {
			// case FLYING:
			//
			// // Set the horizontal stabilizer inclination
			// newHorStabInclination += pidHorStab.calculateChange(inputAP.getPitch() +
			// getVerAngle(),
			// getProperties().getDeltaTime());
			// if (newHorStabInclination > Math.PI / 6)
			// newHorStabInclination = (float) (Math.PI / 6);
			// else if (newHorStabInclination < -Math.PI / 6)
			// newHorStabInclination = (float) -(Math.PI / 6);
			// newHorStabInclination);
			//
			// // Set the vertical stabilizer inclination
			// newVerStabInclination += pidVerStab.calculateChange(inputAP.getHeading() -
			// getHorAngle(),
			// getProperties().getDeltaTime());
			// if (newVerStabInclination > Math.PI / 6)
			// newVerStabInclination = (float) (Math.PI / 6);
			// else if (newVerStabInclination < -Math.PI / 6)
			// newVerStabInclination = (float) -(Math.PI / 6);
			//
			// // Set the wing inclination
			// newLeftWingInclination = (float) Math.toRadians(4); // met een inclination
			// van 4graden stijgt hij 5
			// // meter over 200 meter
			// // newLeftWingInclination += pidRoll.calculateChange(inputAP.getRoll(),
			// // getProperties().getDeltaTime());
			// // if(newLeftWingInclination > Math.PI/6) newLeftWingInclination = (float)
			// // (Math.PI/6);
			// // else if(newLeftWingInclination < - Math.PI/6) newLeftWingInclination =
			// // (float) -(Math.PI/6);
			//
			// newRightWingInclination = (float) Math.toRadians(4);
			//
			// // Set the thrust force
			// // if (getProperties().getVelocity().length() > 60) //als de drone sneller
			// // vliegt dan 60m/s zet de thrust dan uit
			// // this.newThrust = 0;
			// // else
			// this.newThrust = configAP.getMaxThrust();
			//
			// // cubePositions = cubeLocator.getCoordinatesOfCube();
			// // cubePositions.sort(new Comparator<Vector3f>() {
			// // @Override
			// // public int compare(Vector3f o1, Vector3f o2) {
			// // return -Float.compare(o1.z, o2.z);
			// // }
			// // });
			//
			// // Lock next target
			// // if (cubePositions.size() > 0) {
			// // Vector3f temp = cubePositions.get(0);
			// // if ((int) (temp.z / -40) > blockCount) {
			// // blockCount++;
			// // cubePos = new Vector3f(Math.round(temp.x), Math.round(temp.y), ((int)
			// (temp.z
			// // / 40)) * 40);
			// // }
			//
			// // if (!lockedOnTarget && getEuclidDist(getProperties().getPosition(),
			// cubePos)
			// // <= 15) {
			// // lockedOnTarget = true;
			// // cubePos = new Vector3f((cubePositions.get(0).x + cubePos.x) / 2f,
			// // (cubePositions.get(0).y + cubePos.y) / 2f, ((int) (cubePos.z / 40)) * 40 )
			// ;
			// // cubePos = cubePositions.get(0);
			// // cubePos.z = ((int) (cubePos.z / 40)) * 40;
			// // }
			// // }
			//
			// // if (cubePositions.size() > 0) {
			// // cubePos = cubePositions.get(0);
			// // }
			//
			// // CUBE REACHED
			// if (getEuclidDist(getProperties().getPosition(), cubePos) <= 4) {
			// this.path.setIndex(this.path.getIndex() + 1);
			// this.cubePos = new Vector3f(path.getCurrentX(), path.getCurrentY(),
			// path.getCurrentZ());
			// // this.cubePos = stubCube.translate(0, 0, -40);
			// // lockedOnTarget = false;
			// this.pidVerStab.reset();
			// // this.pidWings.reset();
			// }
			//
			// // REMOVE THIS AFTER TESTING:
			// // this.newThrust = configAP.getMaxThrust();
			// case TAXI:
			// if (inputs.getHeading() - this.getHorAngle() > 0.01) {
			// this.newLeftBrake = getFcMax();
			// this.newRightBrake = 0;
			// this.newFrontBrake = 0;
			// this.newThrust = 200;
			//
			// // rechter achterrem vollenbak open en thrust ni te hoog (zodat de drone zich
			// // draait naar het doel
			// } else if (inputs.getHeading() - this.getHorAngle() < 0.01) {
			// this.newLeftBrake = 0;
			// this.newRightBrake = getFcMax();
			// this.newFrontBrake = 0;
			// this.newThrust = 200;
			// } else if (getProperties().getVelocity().length() > 50) {
			// this.newThrust = 0;
			//
			// } else {
			// this.newThrust = configAP.getMaxThrust();
			// }
			//
			// this.newLeftWingInclination = (float) 0;
			// this.newRightWingInclination = (float) 0;
			// this.newHorStabInclination = (float) 0;
			// this.newVerStabInclination = 0;
			//
			// case TAKEOFF:
			// // versnel tot 60m/s en stijg dan op
			// this.newHorStabInclination = 0;
			// this.newVerStabInclination = 0;
			// this.newFrontBrake = 0;
			// this.newLeftBrake = 0;
			// this.newRightBrake = 0;
			// this.newThrust = configAP.getMaxThrust();
			// if (getProperties().getVelocity().length() < 60) {
			// this.newLeftWingInclination = (float) 0;
			// this.newRightWingInclination = (float) 0;
			// } else {
			// this.newLeftWingInclination = (float) 0.2;
			// this.newRightWingInclination = (float) 0.2;
			//
			// }
			//
			// case LANDING:
			// // nog niet ge�mplementeerd
			// }
		}

		return this;
	}

	protected float getVerAngle() {
		float overstaande = cubePos.getY() - getProperties().getPosition().getY();
		float aanliggende = cubePos.getZ() - getProperties().getPosition().getZ();
		return (float) Math.atan(overstaande / aanliggende);
	}

	private float getHorAngle() {
		float overstaande = cubePos.getX() - getProperties().getPosition().getX();
		float aanliggende = cubePos.getZ() - getProperties().getPosition().getZ();
		return (float) Math.atan(overstaande / aanliggende);
	}

	public static float getEuclidDist(Vector3f vec1, Vector3f vec2) {
		Vector3f temp = new Vector3f(0, 0, 0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
	}

	private int blockCount = 0;
	private boolean lockedOnTarget = false;

	@Override
	public void simulationEnded() {
		// Do nothing?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.AutopilotOutputs
	 * 
	 * Zo kunnen we this returnen ipv altijd new AutopilotOutputs
	 */

	@Override
	public float getThrust() {
		return newThrust;
	}

	@Override
	public float getLeftWingInclination() {
		return newLeftWingInclination;
	}

	@Override
	public float getRightWingInclination() {
		return newRightWingInclination;
	}

	@Override
	public float getHorStabInclination() {
		return newHorStabInclination;
	}

	@Override
	public float getVerStabInclination() {
		return newVerStabInclination;
	}

	// MAX AIRFOIL INCLINATION

	/**
	 * Returns the velocity (in the world frame) of a given point (in drone frame)
	 * of the drone accounting for the velocity of drone and the rotation speed of
	 * the drone.
	 * 
	 * @param a
	 *            point attached to the drone (in drone frame)
	 * @return the total velocity of the given point (in world frame)
	 */
	public Vector3f getVelocityOfPoint(Vector3f point) {

		// de hefboomsafstand vh point tov de drone (world frame)
		Vector3f hefboom = new Vector3f();
		Matrix3f.transform(getProperties().getOrientationMatrix(), point, hefboom);

		// v_rot = omega x hefboomstafstand (world frame)
		Vector3f rotation = new Vector3f();
		Vector3f.cross(getProperties().getRotationSpeed(), hefboom, rotation);

		// totale snelheid is de som van de rotatie en de drone snelheid
		Vector3f totalSpeed = new Vector3f();
		Vector3f.add(getProperties().getVelocity(), rotation, totalSpeed);

		return totalSpeed;
	}

	/**
	 * An enum class used to specify the orientation of an airfoil.
	 */
	public enum AirfoilOrientation {
		HORIZONTAL, VERTICAL
	}

	/**
	 * Returns an array of the min and max inclination of the airfoil at position
	 * centerOfMass (in drone frame) with an axis orientation either horizontal or
	 * vertical. The min and max inclinations correspond respectively with the
	 * negative and positive max angle of attack.
	 * 
	 * @return float[2] {min, max}
	 */
	public float[] getMaxInclination(Vector3f wingCentreOfMass, AirfoilOrientation orientation) {
		float maxInclination[] = new float[2];

		// tangens vd pos- en negative angle of attack
		double posTangent = Math.tan(this.getConfig().getMaxAOA());
		double negTangent = Math.tan(this.getConfig().getMaxAOA());

		// snelheid vd airfoil (world frame)
		Vector3f S = getVelocityOfPoint(wingCentreOfMass);

		// berekening vd min en max inclination
		switch (orientation) {
		case HORIZONTAL:
			maxInclination[0] = (float) Math.atan((negTangent * S.z - S.y) / (S.z + negTangent * S.y));
			maxInclination[1] = (float) Math.atan((posTangent * S.z - S.y) / (S.z + posTangent * S.y));
			break;
		case VERTICAL:
			maxInclination[0] = (float) Math.atan((-S.x - negTangent * S.z) / (negTangent * S.x - S.z));
			maxInclination[1] = (float) Math.atan((-S.x - posTangent * S.z) / (posTangent * S.x - S.z));
			break;
		}

		return maxInclination;
	}

	/**
	 * Returns the current min and max inclinations for the left wing. The min and
	 * max inclinations correspont respectivly with the negative and positive max
	 * angle of attack.
	 * 
	 * @return float[2] {min, max}
	 */
	public float[] getMaxInclinationLeftWing() {
		return getMaxInclination(new Vector3f(-getConfig().getWingX(), 0, 0), AirfoilOrientation.HORIZONTAL);
	}

	/**
	 * Returns the current min and max inclinations for the right wing. The min and
	 * max inclinations correspont respectivly with the negative and positive max
	 * angle of attack.
	 * 
	 * @return float[2] {min, max}
	 */
	public float[] getMaxInclinationRightWing() {
		return getMaxInclination(new Vector3f(getConfig().getWingX(), 0, 0), AirfoilOrientation.HORIZONTAL);
	}

	/**
	 * Returns the current min and max inclinations for the horizontal stabiliser.
	 * The min and max inclinations correspont respectivly with the negative and
	 * positive max angle of attack.
	 * 
	 * @return float[2] {min, max}
	 */
	public float[] getMaxInclinationHorStab() {
		return getMaxInclination(new Vector3f(0, 0, getConfig().getTailSize()), AirfoilOrientation.HORIZONTAL);
	}

	/**
	 * Returns the current min and max inclinations for the vertical stabiliser. The
	 * min and max inclinations correspont respectivly with the negative and
	 * positive max angle of attack.
	 * 
	 * @return float[2] {min, max}
	 */
	public float[] getMaxInclinationVertStab() {
		return getMaxInclination(new Vector3f(0, 0, getConfig().getTailSize()), AirfoilOrientation.VERTICAL);
	}

	// DRONE PROPERTIES

	/**
	 * Value class in which the properties of the drone are saved
	 */
	private DroneProperties properties = new DroneProperties();

	/**
	 * Returns the value class in which the properties of the drone are saved
	 */
	public DroneProperties getProperties() {
		return this.properties;
	}

	/**
	 * Updates the drone properties and previous drone properties according to the
	 * given data.
	 */
	private void setDroneProperties(AutopilotInputs inputs) {
		DroneProperties previousProperties = getProperties();
		DroneProperties properties = new DroneProperties(inputs, previousProperties);

		this.previousProperties = previousProperties;
		this.properties = properties;
	}

	/**
	 * Value class in which the previous properties of the drone are saved
	 */
	private DroneProperties previousProperties = new DroneProperties();

	/**
	 * Returns the value class in which the previous properties of the drone are
	 * saved
	 */
	public DroneProperties getPreviousProperties() {
		return this.previousProperties;
	}

	@Override
	public float getFrontBrakeForce() {
		return this.newFrontBrake;
	}

	@Override
	public float getLeftBrakeForce() {
		return this.newLeftBrake;
	}

	@Override
	public float getRightBrakeForce() {
		return this.newRightBrake;
	}

	public float getFcMax() {
		return this.configAP.getFcMax();
	}

	public boolean isFinished() {
		return this.isFinished;
	}

	public AutopilotStages getStage() {
		return this.stage;
	}
	
	public void setStage(AutopilotStages stage) {
		this.stage = stage;
	}
}