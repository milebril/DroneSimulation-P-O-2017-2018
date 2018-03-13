package autoPilotJar;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import interfaces.AutopilotInputs;
public class DroneProperties {
	
	/**
	 * A class for saving the properties of the drone. Used for saving the properties of
	 * the current and previous iteration.
	 *
	 */
	public DroneProperties(AutopilotInputs inputs, DroneProperties previousProperties) {
		// save give properties
		this.image = inputs.getImage();
		this.elapsedTime = inputs.getElapsedTime();
		this.position = new Vector3f(inputs.getX(), inputs.getY(), inputs.getZ());
		this.heading = inputs.getHeading();
		this.pitch = inputs.getPitch();
		this.roll = inputs.getRoll();
		
		this.frontBrakeForce = previousProperties.getFrontBrakeForce();
		this.leftBrakeForce = previousProperties.getLeftBrakeForce();
		this.rightBrakeForce = previousProperties.getRightBrakeForce();
		
		// calculate other properties
		this.deltaTime = this.elapsedTime - previousProperties.getElapsedTime();
		this.velocity = calculateVelocity(this.position, previousProperties.getPosition(), this.deltaTime);
		this.orientationMatrix = calculateOrientationMatrix(heading, pitch, roll);
		this.rotationSpeed = calculateRotationSpeed(this.orientationMatrix, previousProperties.getOrientationMatrix(), this.deltaTime);
	}
	
	public DroneProperties() {
		// save give properties
		this.image = null;
		this.elapsedTime = 0;
		this.position = new Vector3f(0, 0, 0);
		this.heading = 0;
		this.pitch = 0;
		this.roll = 0;
		
		this.frontBrakeForce = 0;
		this.leftBrakeForce = 0;
		this.rightBrakeForce = 0;
		
		// calculate other properties
		this.deltaTime = 0;
		this.velocity = new Vector3f(0, 0, 0);
		this.orientationMatrix = new Matrix3f();
		this.rotationSpeed = new Vector3f(0, 0, 0);
	}
	
	
	// GIVEN PROPERTIES
	
	// Image
	
	/**
	 * The image captured by the drone
	 */
	private final byte[] image;
	
	/**
	 * Returns the image.
	 */
	public byte[] getImage() {
		return this.image;
	}
	
	// Time
	
	/**
	 * The elapsed time property
	 */
	private final float elapsedTime;
	
	/**
	 * Returns the elapsed time property
	 */
	public float getElapsedTime() {
		return this.elapsedTime;
	}
	
	// Position
	
	/**
	 * The position property
	 */
	private final Vector3f position;
	
	/**
	 * Returns the position property.
	 */
	public Vector3f getPosition() {
		return this.position;
	}
	
	// Heading, pitch and roll
	
	/**
	 * The heading property
	 */
	private final float heading;
	
	/**
	 * Returns the heading property.
	 */
	public float getHeading() {
		return this.heading;
	}
	
	/**
	 * The pitch property
	 */
	private final float pitch;
	
	/**
	 * Returns the pitch property.
	 */
	public float getPitch() {
		return this.pitch;
	}
	
	/**
	 * The roll property
	 */
	private final float roll;
	
	/**
	 * Returns the roll property.
	 */
	public float getRoll() {
		return this.roll;
	}
	
	// BRAKES
	
	private float frontBrakeForce;
	private float leftBrakeForce;
	private float rightBrakeForce;

	public float getFrontBrakeForce() {
		return this.frontBrakeForce;
	}
	
	public float getLeftBrakeForce() {
		return this.leftBrakeForce;
	}

	public float getRightBrakeForce() {
		return this.rightBrakeForce;
	}
	
	// CALCULATED PROPERTIES
	
	// Delta time
	
	/**
	 * Delta time since the previous DroneProperties object
	 */
	private final float deltaTime;
	
	/**
	 * Returns delta time.
	 */
	public float getDeltaTime() {
		return this.deltaTime;
	}
	
	// Speed vector
	
	/**
	 * The velocity property
	 */
	private final Vector3f velocity;
	
	/**
	 * Returns the velocity property.
	 */
	public Vector3f getVelocity() {
		return new Vector3f(velocity.x, velocity.y, velocity.z);
	}
	
	/**
	 * Calulates the velocity vector.
	 */
	private Vector3f calculateVelocity(Vector3f position, Vector3f previousPosition, float deltaTime) {
		Vector3f diff = new Vector3f();
		Vector3f.sub(position, previousPosition, diff);
		if (deltaTime != 0) diff.scale(1/deltaTime);
		else diff = new Vector3f(0, 0, 0);
		return diff;
	}
	
	// Orientation matrix
	
	/**
	 * The orientation matrix.
	 */
	private final Matrix3f orientationMatrix;
	
	/**
	 * Returns a copy of the orientation matrix.
	 */
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
	
	/**
	 * Calculates the orientation matrix.
	 */
	private Matrix3f calculateOrientationMatrix(float heading, float pitch, float roll) {
		
		//lwjgl matrix
		Matrix3f orientation = new Matrix3f();
		
		//pitch rotatie
		Matrix3f xRot = new Matrix3f();
		xRot.m11 = (float) Math.cos(pitch);
		xRot.m22 = (float) Math.cos(pitch);
		xRot.m21 = (float) - Math.sin(pitch);
		xRot.m12 = (float) Math.sin(pitch);
		//heading rotatie rond y-as
		Matrix3f yRot = new Matrix3f();
		yRot.m00 = (float) Math.cos(heading);
		yRot.m22 = (float) Math.cos(heading);
		yRot.m20 = (float) Math.sin(heading);
		yRot.m02 = (float) - Math.sin(heading);
		//roll rond z-as
		Matrix3f zRot = new Matrix3f();
		zRot.m00 = (float) Math.cos(roll);
		zRot.m11 = (float) Math.cos(roll);
		zRot.m10 = (float) - Math.sin(roll);
		zRot.m01 = (float) Math.sin(roll);
		
		Matrix3f temp = new Matrix3f();
		Matrix3f.mul(zRot, xRot, temp);
		Matrix3f.mul(temp, yRot, orientation);
		
		// de nieuwe setten
		return orientation;
	}
	
	// Rotation speed
	
	/**
	 * The rotation speed property.
	 */
	private final Vector3f rotationSpeed;
	
	/**
	 * Returns a copy of the rotation speed property vector.
	 */
	public Vector3f getRotationSpeed() {
		return new Vector3f(rotationSpeed.x, rotationSpeed.y, rotationSpeed.z);
	}
	
	/**
	 * Calculates the rotation speed property using the current orientation, previous orientation and delta time.
	 */
	private Vector3f calculateRotationSpeed(Matrix3f orientation, Matrix3f previousOrientation, float deltaTime){
		
		//oppassen want 4x4 matrix is niet zomaar inverteerbaar om tegenstelde orientatie te krijgen
		previousOrientation.transpose(); 
		Matrix3f diff = new Matrix3f();
		
		Matrix3f.mul(orientation, previousOrientation, diff);
		
		//enkel hier de brakke library gebruiken:
		AxisAngle4f rotation = new AxisAngle4f();
		// eerst omzetten naar andere Matrixtype, lwjgl is column major, javax row major
		Matrix4f javaxCopy = new Matrix4f(	diff.m00, diff.m10, diff.m20, 0,
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
}