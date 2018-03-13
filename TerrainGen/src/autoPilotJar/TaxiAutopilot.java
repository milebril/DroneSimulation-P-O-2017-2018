package autoPilotJar;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import autoPilotJar.PIDController;
import interfaces.Autopilot;
import interfaces.AutopilotConfig;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

public class TaxiAutopilot implements Autopilot, AutopilotOutputs {

	private float newThrust = 0;
	private float newLeftWingInclination = 0;
	private float newRightWingInclination = 0;
	private float newHorStabInclination = 0;
	private float newVerStabInclination = 0;
	private float fcMax;
	private float newLeftBrake = 0;
	private float newRightBrake = 0;
	private float newFrontBrake = 0;


	private Vector3f goal = new Vector3f(0, 0, -200);

	private float prevElapsedTime;
	private float dt = 0;

	private PIDController pidThrust;

	private AutopilotConfig configAP;
	private AutopilotInputs inputAP;

	private Vector3f currentPosition;
	private Vector3f prevPosition;

	private Matrix3f currentOrientation = new Matrix3f();

	public Matrix3f getCurrentOrientation() {
		return currentOrientation;
	}

	private Matrix3f prevOrientation = new Matrix3f();

	public Matrix3f getPrevOrientation() {
		return prevOrientation;
	}

	private Vector3f calculateSpeedVector() {
		// Vector3f diff = new Vector3f(0,0,0);
		Vector3f posChange = new Vector3f(0.0f, 0.0f, 0.0f);
		Vector3f.sub(currentPosition, prevPosition, posChange);

		if (dt != 0)
			posChange.scale(1 / this.dt);
		return posChange;
	}

	private float getSpeed() {
		Vector3f speedVector = calculateSpeedVector();
		float speed = speedVector.length();
		return speed;
	}

	private float getHorAngle() {
		float overstaande = goal.getX() - this.currentPosition.getX();
		float aanliggende = goal.getZ() - this.currentPosition.getZ();
		return (float) Math.atan(overstaande / aanliggende);
	}

	private float getHeading() {
		return this.getInput().getHeading();
	}

	/**
	 * Getter for the AP config
	 */
	public AutopilotConfig getConfig() {
		return this.configAP;
	}

	/**
	 * Getter for the AP input
	 */
	public AutopilotInputs getInput() {
		return this.inputAP;
	}

	@Override
	public AutopilotOutputs simulationStarted(AutopilotConfig config, AutopilotInputs inputs) {
		this.configAP = config;
		this.inputAP = inputs;
		return this;
	}

	@Override
	public AutopilotOutputs timePassed(AutopilotInputs inputs) {
		this.inputAP = inputs;
		if (this.inputAP.getElapsedTime() > 0.0000001) {
			currentPosition = new Vector3f(inputAP.getX(), inputAP.getY(), inputAP.getZ());
			this.dt = inputs.getElapsedTime() - prevElapsedTime;
			prevElapsedTime = inputs.getElapsedTime();

			// load Current Orientation Of Drone
			setHeading(inputAP.getHeading());
			setPitch(inputAP.getPitch());
			setRoll(inputAP.getRoll());
			setCurrentOrientation();

			if (this.getHeading() - this.getHorAngle() > 0.01) {
				this.newLeftBrake = getFcMax();
				this.newRightBrake = 0;
				this.newFrontBrake = 0;
				this.newThrust = 200;
				
				// rechter achterrem vollenbak open en thrust ni te hoog (zodat de drone zich
				// draait naar het doel
			} else if (this.getHeading() - this.getHorAngle() < 0.01) {
				this.newLeftBrake = 0;
				this.newRightBrake = getFcMax();
				this.newFrontBrake = 0;
				this.newThrust = 200;
			} else if (this.getSpeed() > 50) {
				this.newThrust = 0;

			} else {
				this.newThrust = configAP.getMaxThrust();
			}

			this.prevPosition = new Vector3f(currentPosition.x, currentPosition.y, currentPosition.z);
			this.newLeftWingInclination = (float) 0;
			this.newRightWingInclination = (float) 0;
			this.newHorStabInclination = (float) 0;
			this.newVerStabInclination = 0;
		}

		return this;

	}

	@Override
	public void simulationEnded() {
		// nee
	}

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

	private float currentHeading;
	private float prevHeading;

	private void setHeading(float heading) {
		this.prevHeading = this.currentHeading;
		this.currentHeading = heading;
	}

	private float currentPitch;
	private float prevPitch;

	private void setPitch(float pitch) {
		this.prevPitch = this.currentPitch;
		this.currentPitch = pitch;
	}

	private float currentRoll;
	private float prevRoll;

	private void setRoll(float roll) {
		this.prevRoll = this.currentRoll;
		this.currentRoll = roll;
	}

	private void setCurrentOrientation() {

		// de huidige doorschuiven
		prevOrientation = this.currentOrientation;
		// lwjgl matrix
		Matrix3f currentOrientation = new Matrix3f();
		// pitchrotatie
		Matrix3f xRot = new Matrix3f();
		xRot.m11 = (float) Math.cos(currentPitch);
		xRot.m22 = (float) Math.cos(currentPitch);
		xRot.m21 = (float) -Math.sin(currentPitch);
		xRot.m12 = (float) Math.sin(currentPitch);
		// headingrotatie rond y
		Matrix3f yRot = new Matrix3f();
		yRot.m00 = (float) Math.cos(currentHeading);
		yRot.m22 = (float) Math.cos(currentHeading);
		yRot.m20 = (float) Math.sin(currentHeading);
		yRot.m02 = (float) -Math.sin(currentHeading);
		// roll rond z-as
		Matrix3f zRot = new Matrix3f();
		zRot.m00 = (float) Math.cos(currentRoll);
		zRot.m11 = (float) Math.cos(currentRoll);
		zRot.m10 = (float) -Math.sin(currentRoll);
		zRot.m01 = (float) Math.sin(currentRoll);

		Matrix3f temp = new Matrix3f();
		Matrix3f.mul(zRot, xRot, temp);
		Matrix3f.mul(temp, yRot, currentOrientation);

		// de nieuwe setten
		this.currentOrientation = currentOrientation;
	}

	public TaxiAutopilot() {
		// Set initial Positions
		currentPosition = new Vector3f(0, 0, 0);
		prevPosition = new Vector3f(0, 0, 0);

		// Initialize AP with configfile

		// Initialize PIDController for Thrust
		// PIDController(float K-Proportional, float K-Integral, float K-Derivative,
		// float changeFactor, float goal)
		//this.pidThrust = new PIDController(1.0f, 0.0f, 3.0f, -10, 10);
		// initialize();
	}

	@Override
	public float getFrontBrakeForce() {
		return newFrontBrake;
	}

	@Override
	public float getLeftBrakeForce() {
		return newLeftBrake;
	}

	@Override
	public float getRightBrakeForce() {
		return newRightBrake;
	}

	public float getFcMax() {
		return this.configAP.getFcMax();
	}
}
