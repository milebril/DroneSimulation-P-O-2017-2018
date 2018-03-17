package autoPilotJar;

import org.lwjgl.util.vector.Vector3f;

import interfaces.Autopilot;
import interfaces.AutopilotConfig;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;
import openCV.ImageProcessor;
import path.MyPath;

public class FlyingAutoPilot implements Autopilot, AutopilotOutputs {

	private PIDController pidHorStab;
	private PIDController pidVerStab;

	// Aanpassen als we naar nieuwe cubus moeten gaan
	private Vector3f stubCube = new Vector3f(0, 0, -40);
	private Vector3f cubePos = stubCube;
	
	private ImageProcessor cubeLocator;

	private MyPath path;

	private float newThrust = 0;
	private float newLeftWingInclination = 0;
	private float newRightWingInclination = 0;
	private float newHorStabInclination = 0;
	private float newVerStabInclination = 0;
	private AutopilotConfig configAP;
	private AutopilotInputs inputAP;
	private float newLeftBrake = 0;
	private float newRightBrake = 0;
	private float newFrontBrake = 0;

	private float prevElapsedTime;
	private float dt = 0;

	private Vector3f currentPosition;
	private Vector3f prevPosition;

	// AUTOMATED PID STUFF?

	private boolean isFinished = false;
	public boolean failed = false;
	private int checkpoint = -80;
	public float minY = 20;
	public float maxY = 20;

	public FlyingAutoPilot() {
		this.pidHorStab = new PIDController(1.0532867f, 0.033028185f, 1.0589304f, (float) (Math.PI / 180), 0);
		this.pidVerStab = new PIDController(2.5f, 0.0f, 2.0f, (float) (Math.PI / 180), 0);
	}

	@Override
	public AutopilotOutputs timePassed(AutopilotInputs inputs) {
		setDroneProperties(inputs);

		if (getProperties().getVelocity().length() > 80) // als de drone sneller vliegt dan 60m/s zet de thrust dan
			this.newThrust = 0;
		else
			this.newThrust = configAP.getMaxThrust();

		// Set the horizontal stabilizer inclination
		newHorStabInclination += pidHorStab.calculateChange(inputAP.getPitch() + getVerAngle(),
				getProperties().getDeltaTime());
		if (newHorStabInclination > Math.PI / 6)
			newHorStabInclination = (float) (Math.PI / 6);
		else if (newHorStabInclination < -Math.PI / 6)
			newHorStabInclination = (float) -(Math.PI / 6);

		newLeftWingInclination = 0;
		newRightWingInclination = 0;

		// if (getProperties().getVelocity().length() > 40 && inputs.getY() > 10) {
		// newLeftWingInclination = (float) Math.toRadians(4);
		// newRightWingInclination = (float) Math.toRadians(4);
		// }

		if (getEuclidDist(getProperties().getPosition(), cubePos) <= 5) {
			if (path.getIndex() <= 4) {
				this.path.setIndex(this.path.getIndex() + 1);
				this.cubePos = new Vector3f(path.getCurrentX(), path.getCurrentY(), path.getCurrentZ());
				this.pidHorStab.reset();
				this.pidVerStab.reset();
				System.out.println("Reached cube at: " + -80 * path.getIndex());
			} else {
				System.out.println("Fininshed");
				isFinished = true;
				System.out.println(maxY + " " + minY);
			}
			checkpoint = -80 * (path.getIndex() + 1);
		}

		if (properties.getPosition().z < (checkpoint)) {
			failed = true;
			System.out.println("Path failed");
		}
		return this;
	}

	@Override
	public float getThrust() {
		return this.newThrust;
	}

	@Override
	public float getLeftWingInclination() {
		return this.newLeftWingInclination;
	}

	@Override
	public float getRightWingInclination() {
		return this.newRightWingInclination;
	}

	@Override
	public float getHorStabInclination() {
		return this.newHorStabInclination;
	}

	@Override
	public float getVerStabInclination() {
		return this.newVerStabInclination;
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

	@Override
	public AutopilotOutputs simulationStarted(AutopilotConfig config, AutopilotInputs inputs) {
		this.configAP = config;
		this.inputAP = inputs;

		cubeLocator = new ImageProcessor(this); //help

		return this;
	}

	@Override
	public void simulationEnded() {
		// TODO Auto-generated method stub

	}

	private float getVerAngle() {
		float overstaande = cubePos.getY() - getProperties().getPosition().getY();
		float aanliggende = cubePos.getZ() - getProperties().getPosition().getZ();
		return (float) Math.atan(overstaande / aanliggende);
	}

	private float getEuclidDist(Vector3f vec1, Vector3f vec2) {
		Vector3f temp = new Vector3f(0, 0, 0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
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

}
