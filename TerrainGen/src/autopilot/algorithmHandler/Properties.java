package autopilot.algorithmHandler;

import prevAutopilot.DroneProperties;
import prevAutopilot.SimpleAutopilot.AirfoilOrientation;

import javax.vecmath.AxisAngle4f;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import interfaces.AutopilotConfig;
import interfaces.AutopilotInputs;

public class Properties implements AutopilotConfig, AutopilotInputs {
	
	/**
	 * A class for saving the properties of the drone. These consists of the autopilotConfig, the
	 * AutopilotInputs and some other properties that can be derived.
	 */
	public Properties(AutopilotConfig config, AutopilotInputs inputs) {
		
		// save config
		this.droneID = config.getDroneID();
		this.gravity = config.getGravity();
		this.wingX = config.getWingX();
		this.tailSize = config.getTailSize();
		this.wheelY = config.getWheelY();
		this.frontWheelZ = config.getFrontWheelZ();
		this.rearWheelZ = config.getRearWheelZ();
		this.rearWheelX = config.getRearWheelX();
		this.tyreSlope = config.getTyreSlope();
		this.dampSlope = config.getDampSlope();
		this.tyreRadius = config.getTyreRadius();
		this.rMax = config.getRMax();
		this.fcMax = config.getFcMax();
		this.engineMass = config.getEngineMass();
		this.wingMass = config.getWingMass();
		this.tailMass = config.getTailMass();
		this.maxThrust = config.getMaxThrust();
		this.maxAOA = config.getMaxAOA();
		this.wingLiftSlope = config.getWingLiftSlope();
		this.horStabLiftSlope = config.getHorStabLiftSlope();
		this.verStabLiftSlope = config.getVerStabLiftSlope();
		this.horizontalAngleOfView = config.getHorizontalAngleOfView();
		this.verticalAngleOfView = config.getVerticalAngleOfView();
		this.nbColumns = config.getNbColumns();
		this.nbRows = config.getNbRows();
		
		// first inputs
		setImage(inputs.getImage());
		setPosition(new Vector3f(inputs.getX(), inputs.getY(), inputs.getZ()));
		setHeading(inputs.getHeading());
		setPitch(inputs.getPitch());
		setRoll(inputs.getRoll());
		setElapsedTime(inputs.getElapsedTime());
		
		// set starting values for the calculated properties
		setDeltaTime(0);
		setVelocity(new Vector3f(0, 0, 0));
		setAcceleration(new Vector3f(0, 0, 0));
		setOrientationMatrix(new Matrix3f());
	}
	
	// AutopilotInputs
	
	private byte[] image;
	public byte[] getImage() {
		return this.image;
	}
	private void setImage(byte[] image) {
		this.image = image;
	}
	
	private Vector3f position;
	public Vector3f getPosition() {
		return this.position;
	}
	private void setPosition(Vector3f position) {
		this.position = position;
	}
	public float getX() {
		return getPosition().x;
	}
	public float getY() {
		return getPosition().y;
	}
	public float getZ() {
		return getPosition().z;
	}

	private float heading;
	public float getHeading() {
		return this.heading;
	}
	private void setHeading(float heading) {
		this.heading = heading;
	}
	
	private float pitch;
	public float getPitch() {
		return this.pitch;
	}
	private void setPitch(float pitch) {
		this.pitch = pitch;
	}
	
	private float roll;
	private void setRoll(float roll) {
		this.roll = roll;
	}
	public float getRoll() {
		return this.roll;
	}
	
	private float elapsedTime;
	public float getElapsedTime() {
		return this.elapsedTime;
	}
	private void setElapsedTime(float elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	
	// AutopilotConfig
	
	private final String droneID;
	@Override
	public String getDroneID() {
		return this.droneID;
	}
	
	private final float gravity;
	@Override
	public float getGravity() {
		return this.gravity;
	}

	private final float wingX;
	@Override
	public float getWingX() {
		return this.wingX;
	}

	private final float tailSize;
	@Override
	public float getTailSize() {
		return this.tailSize;
	}

	private final float wheelY;
	@Override
	public float getWheelY() {
		return this.wheelY;
	}

	private final float frontWheelZ;
	@Override
	public float getFrontWheelZ() {
		return this.frontWheelZ;
	}

	private final float rearWheelZ;
	@Override
	public float getRearWheelZ() {
		return this.rearWheelZ;
	}

	private final float rearWheelX;
	@Override
	public float getRearWheelX() {
		return this.rearWheelX;
	}

	private final float tyreSlope;
	@Override
	public float getTyreSlope() {
		return this.tyreSlope;
	}

	private final float dampSlope;
	@Override
	public float getDampSlope() {
		return this.dampSlope;
	}

	private final float tyreRadius;
	@Override
	public float getTyreRadius() {
		return this.tyreRadius;
	}

	private final float rMax;
	@Override
	public float getRMax() {
		return this.rMax;
	}

	private final float fcMax;
	@Override
	public float getFcMax() {
		return this.fcMax;
	}

	private final float engineMass;
	@Override
	public float getEngineMass() {
		return this.engineMass;
	}

	private final float wingMass;
	@Override
	public float getWingMass() {
		return this.wingMass;
	}

	private final float tailMass;
	@Override
	public float getTailMass() {
		return this.tailMass;
	}

	private final float maxThrust;
	@Override
	public float getMaxThrust() {
		return this.maxThrust;
	}

	private final float maxAOA;
	@Override
	public float getMaxAOA() {
		return this.maxAOA;
	}

	private final float wingLiftSlope;
	@Override
	public float getWingLiftSlope() {
		return this.wingLiftSlope;
	}

	private final float horStabLiftSlope;
	@Override
	public float getHorStabLiftSlope() {
		return this.horStabLiftSlope;
	}

	private final float verStabLiftSlope;
	
	@Override
	public float getVerStabLiftSlope() {
		return this.verStabLiftSlope;
	}

	private final float horizontalAngleOfView;
	@Override
	public float getHorizontalAngleOfView() {
		return this.horizontalAngleOfView;
	}

	private final float verticalAngleOfView;
	@Override
	public float getVerticalAngleOfView() {
		return this.verticalAngleOfView;
	}

	private final int nbColumns;
	@Override
	public int getNbColumns() {
		return this.nbColumns;
	}

	private final int nbRows;
	@Override
	public int getNbRows() {
		return this.nbRows;
	}

	// Calculated properties
	
	private float deltaTime;
	public float getDeltaTime() {
		return this.deltaTime;
	}
	private void setDeltaTime(float deltaTime) {
		this.deltaTime = deltaTime;
	}
	
	private Vector3f velocity;
	public Vector3f getVelocity() {
		return new Vector3f(velocity.x, velocity.y, velocity.z);
	}
	private void setVelocity(Vector3f velocity) {
		this.velocity = velocity;
	}
	/**
	 * The velocity of the drone in world frame.
	 */
	private Vector3f calculateVelocity(Vector3f position, Vector3f previousPosition, float deltaTime) {
		Vector3f diff = new Vector3f();
		Vector3f.sub(position, previousPosition, diff);
		if (deltaTime != 0) diff.scale(1/deltaTime);
		else diff = new Vector3f(0, 0, 0);
		return diff;
	}
	
	private Vector3f acceleration;
	public Vector3f getAcceleration() {
		return this.acceleration;
	}
	private void setAcceleration(Vector3f acceleration) {
		this.acceleration = acceleration;
	}
	private Vector3f calculateAcceleration(Vector3f velocity, Vector3f previousVelocity, float deltaTime) {
		Vector3f diff = new Vector3f();
		Vector3f.sub(velocity, previousVelocity, diff);
		if (deltaTime != 0) diff.scale(1/deltaTime);
		else diff = new Vector3f(0, 0, 0);
		return diff;
	}
	
	private Matrix3f orientationMatrix;
	public Matrix3f getOrientationMatrix() {
		Matrix3f matrixCopy = new Matrix3f();
		matrixCopy.m00 = orientationMatrix.m00;
		matrixCopy.m01 = orientationMatrix.m01;
		matrixCopy.m02 = orientationMatrix.m02;
		matrixCopy.m10 = orientationMatrix.m10;
		matrixCopy.m11 = orientationMatrix.m11;
		matrixCopy.m12 = orientationMatrix.m12;
		matrixCopy.m20 = orientationMatrix.m20;
		matrixCopy.m21 = orientationMatrix.m21;
		matrixCopy.m22 = orientationMatrix.m22;
		return matrixCopy;
	}
	private void setOrientationMatrix(Matrix3f orientationMatrix) {
		this.orientationMatrix = orientationMatrix;
	}
	private Matrix3f calculateOrientationMatrix(float heading, float pitch, float roll) {
		
		Matrix3f xRot = new Matrix3f();
		xRot.m11 = (float) Math.cos(pitch);
		xRot.m22 = (float) Math.cos(pitch);
		xRot.m21 = (float) - Math.sin(pitch);
		xRot.m12 = (float) Math.sin(pitch);
		
		Matrix3f yRot = new Matrix3f();
		yRot.m00 = (float) Math.cos(heading);
		yRot.m22 = (float) Math.cos(heading);
		yRot.m20 = (float) Math.sin(heading);
		yRot.m02 = (float) - Math.sin(heading);
		
		Matrix3f zRot = new Matrix3f();
		zRot.m00 = (float) Math.cos(roll);
		zRot.m11 = (float) Math.cos(roll);
		zRot.m10 = (float) - Math.sin(roll);
		zRot.m01 = (float) Math.sin(roll);
		
		// rot = yRot . xRot . zRot -> 1st roll, dan pitch, dan heading
		Matrix3f orientation = new Matrix3f();
		Matrix3f.mul(xRot, zRot, orientation);
		Matrix3f.mul(yRot, orientation, orientation);
		
		return orientation;
	}
	
	// Rotation speed
	
	private Vector3f rotationSpeed = new Vector3f(0, 0, 0);
	public Vector3f getRotationSpeed() {
		return new Vector3f(rotationSpeed.x, rotationSpeed.y, rotationSpeed.z);
	}
	private void setRotationSpeed(Vector3f rotationSpeed) {
		this.rotationSpeed = rotationSpeed;
	}
	
	// TODO: bevestigen/controleren dat deze functie juist is
	private Vector3f calculateRotationSpeed(Matrix3f orientation, Matrix3f previousOrientation, float deltaTime){
		
		//oppassen want 4x4 matrix is niet zomaar inverteerbaar om tegenstelde orientatie te krijgen
		previousOrientation.transpose(); 
		Matrix3f diff = new Matrix3f();
		
		Matrix3f.mul(orientation, previousOrientation, diff);
		
		//enkel hier de brakke library gebruiken:
		AxisAngle4f rotation = new AxisAngle4f();
		// eerst omzetten naar andere Matrixtype, lwjgl is column major, javax row major
		javax.vecmath.Matrix4f javaxCopy = new javax.vecmath.Matrix4f(	diff.m00, diff.m10, diff.m20, 0,
											diff.m01, diff.m11, diff.m21, 0,
											diff.m02, diff.m12, diff.m22, 0,
											0, 		  0, 		0, 		  1);
		rotation.set(javaxCopy);
		float[] result = new float[4];
		rotation.get(result);
		//hier er terug uit in array result
		
		result[3] /= deltaTime;
		
		Vector3f speed = new Vector3f(result[0], result[1], result[2]);
		speed.scale(result[3]);
		
		return speed;
	}
	
	public Vector3f transformToWorldFrame(Vector3f vector) {
		Matrix3f transformation = getOrientationMatrix();

		Vector3f result = new Vector3f();
		Matrix3f.transform(transformation, vector, result);
		
		return result;
	}
	
	public Vector3f transformToDroneFrame(Vector3f vector) {
		Matrix3f transformation = new Matrix3f();
		getOrientationMatrix().transpose(transformation);
		
		Vector3f result = new Vector3f();
		Matrix3f.transform(transformation, vector, result);
		
		return result;
	}
	
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
		Matrix3f.transform(getOrientationMatrix(), point, hefboom);

		// v_rot = omega x hefboomstafstand (world frame)
		Vector3f rotation = new Vector3f();
		Vector3f.cross(getRotationSpeed(), hefboom, rotation);
		
		// totale snelheid is de som van de rotatie en de drone snelheid
		Vector3f totalSpeed = new Vector3f();
		Vector3f.add(getVelocity(), rotation, totalSpeed);

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

		// snelheid vd airfoil
		Vector3f S = transformToDroneFrame(getVelocityOfPoint(wingCentreOfMass));
		
		float angle = 0;
		float max = (float) Math.toRadians(getMaxAOA());
		
		switch (orientation) {
			case HORIZONTAL:
				angle = (float) Math.atan2(S.y, -S.z);
				break;
			case VERTICAL:
				angle = (float) Math.atan2(-S.x, -S.z);
				break;
		}
		if (angle < -Math.PI) angle += Math.PI;
		if (Math.PI < angle) angle -= Math.PI;
		
		return new float[]{angle-max, angle+max};
	}

	/**
	 * Returns the current min and max inclinations for the left wing. The min and
	 * max inclinations correspont respectivly with the negative and positive max
	 * angle of attack.
	 * 
	 * @return float[2] {min, max}
	 */
	public float[] getMaxInclinationLeftWing() {
		return getMaxInclination(new Vector3f(-getWingX(), 0, 0), AirfoilOrientation.HORIZONTAL);
	}

	/**
	 * Returns the current min and max inclinations for the right wing. The min and
	 * max inclinations correspont respectivly with the negative and positive max
	 * angle of attack.
	 * 
	 * @return float[2] {min, max}
	 */
	public float[] getMaxInclinationRightWing() {
		return getMaxInclination(new Vector3f(getWingX(), 0, 0), AirfoilOrientation.HORIZONTAL);
	}

	/**
	 * Returns the current min and max inclinations for the horizontal stabiliser.
	 * The min and max inclinations correspont respectivly with the negative and
	 * positive max angle of attack.
	 * 
	 * @return float[2] {min, max}
	 */
	public float[] getMaxInclinationHorStab() {
		return getMaxInclination(new Vector3f(0, 0, getTailSize()), AirfoilOrientation.HORIZONTAL);
	}

	/**
	 * Returns the current min and max inclinations for the vertical stabiliser. The
	 * min and max inclinations correspont respectivly with the negative and
	 * positive max angle of attack.
	 * 
	 * @return float[2] {min, max}
	 */
	public float[] getMaxInclinationVertStab() {
		return getMaxInclination(new Vector3f(0, 0, getTailSize()), AirfoilOrientation.VERTICAL);
	}
	
	// Airfoil Forces
	
	/**
	 * Returns the linear force the left wing will apply (in drone frame)
	 */
	public Vector3f getAirfoilForce(Vector3f airfoilPosition, AirfoilOrientation orientation, float liftSlope, float inclination) {
		
		// snelheid vd airfoil
		Vector3f S = transformToDroneFrame(getVelocityOfPoint(airfoilPosition));
		float s2 = S.lengthSquared();
		
		// normal and aoa depend on airfoil orientation
		float aoa;
		float size;
		Vector3f normal = new Vector3f();
		switch (orientation) {
			case HORIZONTAL:
				aoa = (float) Math.atan2(S.y, -S.z);
				size = liftSlope * aoa * s2;
				normal = new Vector3f(0f, (float) Math.cos(inclination) * size, (float) Math.sin(inclination) * size);
				break;
			case VERTICAL:
				aoa = (float) Math.atan2(-S.x, -S.z);
				size = liftSlope * aoa * s2;
				normal = new Vector3f((float) - Math.cos(inclination) * size, 0f, (float) Math.sin(inclination) * size);
				break;
		}
		
		return normal;
	}
	
	/**
	 * Returns the force the left wing would apply under the given inclination (in drone frame)
	 */
	public Vector3f getLeftWingForce(float inclination) {
		return getAirfoilForce(new Vector3f(-getWingX(), 0, 0), AirfoilOrientation.HORIZONTAL, getWingLiftSlope(), inclination);
	}
	
	/**
	 * Returns the force the right wing would apply under the given inclination (in drone frame)
	 */
	public Vector3f getRightWingForce(float inclination) {
		return getAirfoilForce(new Vector3f(getWingX(), 0, 0), AirfoilOrientation.HORIZONTAL, getWingLiftSlope(), inclination);
	}
	
	/**
	 * Returns the force the horizontal stabilizer would apply under the given inclination (in drone frame)
	 */
	public Vector3f getHorStabForce(float inclination) {
		return getAirfoilForce(new Vector3f(0, 0, getTailSize()), AirfoilOrientation.HORIZONTAL, getHorStabLiftSlope(), inclination);
	}
	
	/**
	 * Returns the force the vertical stabilizer would apply under the given inclination (in drone frame)
	 */
	public Vector3f getVertStabForce(float inclination) {
		return getAirfoilForce(new Vector3f(0, 0, getTailSize()), AirfoilOrientation.VERTICAL, getVerStabLiftSlope(), inclination);
	}
	
	// Update properties
	
	public void update(AutopilotInputs inputs) {
		
		// update the calculated properties
		setDeltaTime(inputs.getElapsedTime() - getElapsedTime());
		Vector3f newPosition = new Vector3f(inputs.getX(), inputs.getY(), inputs.getZ());
		Vector3f newVelocity = calculateVelocity(newPosition, getPosition(), getDeltaTime());
		Vector3f newAcceleration = calculateAcceleration(newVelocity, getVelocity(), getDeltaTime());
		Matrix3f newOrientation = calculateOrientationMatrix(inputs.getHeading(), inputs.getPitch(), inputs.getRoll());
		Vector3f newRotationSpeed = calculateRotationSpeed(newOrientation, getOrientationMatrix(), getDeltaTime());
		setVelocity(newVelocity);
		setAcceleration(newAcceleration);
		setOrientationMatrix(newOrientation);
		setRotationSpeed(newRotationSpeed);
		
		// save inputs
		setImage(inputs.getImage());
		setPosition(new Vector3f(inputs.getX(), inputs.getY(), inputs.getZ()));
		setHeading(inputs.getHeading());
		setPitch(inputs.getPitch());
		setRoll(inputs.getRoll());
		setElapsedTime(inputs.getElapsedTime());
	}
}