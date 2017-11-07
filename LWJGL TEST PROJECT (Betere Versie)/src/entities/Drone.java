package entities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Math;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import autoPilotJar.AutopilotInputs;
import autoPilotJar.AutopilotInputsWriter;
import autoPilotJar.AutopilotOutputs;
import autoPilotJar.AutopilotOutputsReader;
import autopilot.AutopilotConfig;
import models.RawModel;
import renderEngine.DisplayManager;
import toolbox.ImageConverter;

/**
 * Vectornames gevolgd door een D staan in het drone assenstelsel, gevolgd door een W staan in het wereld
 * uitgedrukt
 * @author Jakob
 *
 */
public class Drone extends Entity /* implements AutopilotConfig */ {

	public Drone(RawModel model, Vector3f position, Vector3f orientation, float scale,
				AutopilotConfig cfg) {
		super(model, position, orientation, scale);

		this.forwardVectorW = new Vector3f(0.0f,0.0f,-1.0f);
		
		this.linearVelocityW = new Vector3f(0.0f,0.0f, -15.0f);
		this.linearAccelerationW = new Vector3f(0.0f,0.0f,0.0f);

		this.angularVelocityW = new Vector3f(0f, 0f, 0f);
		this.angularAccelerationW = new Vector3f(0f, 0f, 0f);
		
		// Left wing, Right wing, Horizontal stabilizer, Vertical stabilizer
		// vertical rotation axis: (0,1,0) (= y-axis) // horizontal rotation axis: (1,0,0) (= x-axis)
		this.airFoils[0] = new AirFoil(new Vector3f(-cfg.getWingX(), 0, 0), cfg.getWingMass(), 
														cfg.getWingLiftSlope(), new Vector3f(1,0,0));
		this.airFoils[1] = new AirFoil(new Vector3f(cfg.getWingX(), 0, 0), cfg.getWingMass(), 
														cfg.getWingLiftSlope(), new Vector3f(1,0,0));
		this.airFoils[2] = new AirFoil(new Vector3f(0, 0, cfg.getTailSize()), 0, 
														cfg.getHorStabLiftSlope(), new Vector3f(1,0,0));
		this.airFoils[3] = new AirFoil(new Vector3f(0, 0, cfg.getTailSize()), 0, 
														cfg.getVerStabLiftSlope(), new Vector3f(0,1,0));
		
		this.maxThrust = cfg.getMaxThrust();
		this.maxAOA = cfg.getMaxAOA();
		
		
		// tail properties
		this.setTailMass(cfg.getTailMass());
		this.setTailSize(cfg.getTailSize());
		this.tailMassPosition = new Vector3f(0, 0, getTailSize());
		
		// engine properties
		this.setEngineMass(cfg.getEngineMass());
		float z = (-this.getTailMass() * this.getTailSize()) / this.getEngineMass(); // so the drones center of mass is at (0, 0, 0)
		this.enginePosition = new Vector3f(0,0,z);

		// calculate and save the drones inertia matrix
		this.setInertiaMatrix(this.calculateInertiaMatrix());
		
		
		camera = new Camera(cfg.getNbColumns(), cfg.getNbRows());
		camera.increasePosition(this.getPosition().x, this.getPosition().y, this.getPosition().z);
	}


	// AIRFOILS
	
	/**
	 * The airFoils of the drone (in order [leftwing, rightwing, hor. stabilizer, vert. stabilizer])
	 */
	private AirFoil[] airFoils = new AirFoil[4];
	
	/**
	 * Returns an array of the airFoils of the drone.
	 * (in order [leftwing, rightwing, hor. stabilizer, vert. stabilizer])
	 * @return
	 */
	public AirFoil[] getAirFoils(){
		return this.airFoils.clone();
	}
	
	/**
	 * Returns the left wing of the Drone.
	 */
	public AirFoil getLeftWing(){
		return this.airFoils[0];
	}

	/**
	 * Returns the right wing of the Drone.
	 */
	public AirFoil getRightWing(){
		return this.airFoils[1];
	}

	/**
	 * Returns the horizontal stabilizer of the Drone.
	 */
	public AirFoil getHorizStabilizer(){
		return this.airFoils[2];
	}

	/**
	 * Returns the vertical stabilizer of the Drone.
	 */
	public AirFoil getVertStabilizer(){
		return this.airFoils[3];
	}
	
	
	// FORWARD VECTOR
	
	/**
	 * The forward vector of the drone in world frame.
	 */
	private Vector3f forwardVectorW;
	
	/**
	 * Returns the forward vector of the drone in world frame.
	 */
	public Vector3f getForwardVector() {
		return new Vector3f(this.forwardVectorW.x, this.forwardVectorW.y, this.forwardVectorW.z);
	}
	
	/**
	 * Set the forward vector of the drone.
	 */
	public void setForwardVector(Vector3f vector) {
		vector.normalise(this.forwardVectorW);
	}
	
	/** 
	 * Returns the heading vector of the Drone in world frame.
	 * The heading vector is a normalized version of the forward vector of the 
	 * drone, projected onto the world xz-plane.
	 */
	private Vector3f getHeadingVector() {
		Vector3f forwardVectorW = getForwardVector();
		
		// project the forward vector onto the xz-plane
		Vector3f headingVector = new Vector3f(forwardVectorW.x, 0, forwardVectorW.z);
		
		// normalize the projected vector
		headingVector.normalise();
		
		return headingVector;
	}
	
	
	// LINEAR VELOCITY
	
	/**
	 * The linear velocity of the drone in world frame.
	 */
	private Vector3f linearVelocityW;
	
	/**
	 * Returns the linear velocity vector of the drone in world frame.
	 */
	public Vector3f getLinearVelocity(){
		return new Vector3f(this.linearVelocityW.x, this.linearVelocityW.y, this.linearVelocityW.z);
	}
	
	/**
	 * Set the linear velocity vector of the drone.
	 */
	public void setLinearVelocity(Vector3f vector) {
		this.linearVelocityW.set(vector.x, vector.y, vector.z);
	}

	/**
	 * Returns the velocity of the drone.
	 */
	public float getAbsVelocity() {
		return this.getLinearVelocity().length(); 
	}


	// LINEAR ACCELERATION
	
	/**
	 * The linear acceleration of the drone in world frame.
	 */
	private Vector3f linearAccelerationW;
	
	/**
	 * Returns the linear acceleration vector of the drone in world frame.
	 */
	public Vector3f getLinearAcceleration() {
		return new Vector3f(this.linearAccelerationW.x, this.linearAccelerationW.y, this.linearAccelerationW.z);
	}
	
	/**
	 * Set the linear acceleration vector of the drone.
	 */
	public void setLinearAcceleration(Vector3f vector) {
		this.linearAccelerationW.set(vector.x, vector.y, vector.z);
	}
	
	
	// ANGULAR VELOCITY
	
	/**
	 * The angular velocity of the drone in world frame.
	 */
	private Vector3f angularVelocityW;
	
	/**
	 * Returns the angular velocity vector of the drone in world frame.
	 */
	public Vector3f getAngularVelocity() {
		return new Vector3f(this.angularVelocityW.x, this.angularVelocityW.y, this.angularVelocityW.z);
	}
	
	/**
	 * Set the angular velocity vector of the drone.
	 */
	public void setAngularVelocity(Vector3f vector) {
		this.angularVelocityW.set(vector.x, vector.y, vector.z);
	}
	
	
	// ANGULAR ACCELERATION
	
	/**
	 * The angular acceleration of the drone in world frame.
	 */
	private Vector3f angularAccelerationW;
	
	/**
	 * Returns the angular acceleration vector of the drone in world frame.
	 */
	public Vector3f getAngularAcceleration() {
		return new Vector3f(this.angularAccelerationW.x, this.angularAccelerationW.y, this.angularAccelerationW.z);
	}
	
	/**
	 * Set the angular acceleration vector of the drone.
	 */
	public void setAngularAcceleration(Vector3f vector) {
			this.angularAccelerationW.set(vector.x, vector.y, vector.z);
		}
		
	
	// TAILSIZE
	
	/**
	 * The size of the Drones tail.
	 * (the mass of the tail and the stabilizers are located at the end of this tail)
	 */
	private float tailSize;
	
	/**
	 * Returns the size of the Drones tail.
	 */
	public float getTailSize() {
		return this.tailSize;
	}
	
	/**
	 * Set the size of the Drones tail.
	 * @throws IllegalArgumentException if the given size is smaller than or equal to 0
	 */
	public void setTailSize(float tailSize) throws IllegalArgumentException {
		if (tailSize <= 0) throw new IllegalArgumentException("TailSize must be larger than zero.");
		this.tailSize = tailSize;
	}
	
	
	// TAILMASS
	
	/**
	 * The mass of the Drones tail.
	 */
	private float tailMass;

	/**
	 * Returns the mass of the Drones tail.
	 * @return
	 */
	public float getTailMass(){
		return this.tailMass;
	}
	
	/**
	 * Set the mass of the Drones tail.
	 * @throws IllegalArgumentException if the given mass is smaller than or equal to 0
	 */
	public void setTailMass(float tailMass) throws IllegalArgumentException {
		if (tailMass <= 0) throw new IllegalArgumentException("TailMass must be larger than zero.");
		this.tailMass = tailMass;
	}
		
	
	// TAILMASS POSITION
	
	/**
	 * The position of the Drones tailmass (in drone frame)
	 */
	private final Vector3f tailMassPosition;
	
	/**
	 * Returns the position of the Drones tailmass.
	 */
	public Vector3f getTailMassPosition() {
		return new Vector3f(this.tailMassPosition.x, this.tailMassPosition.y, this.tailMassPosition.z);
	}
	
	
	// ENGINE POSITION
	
	/**
	 * The Drones engine position (in drone frame)
	 */
	private final Vector3f enginePosition;
	
	/**
	 * Returns the Drones engine position.
	 */
	public Vector3f getEnginePosition() {
		return new Vector3f(this.enginePosition.x, this.enginePosition.y, this.enginePosition.z);
	}
	
	
	
	
	// ENGINE MASS
	
	/**
	 * The mass of the Drones engine.
	 */
	private float engineMass;
	
	/**
	 * Returns the mass of the Drones engine.
	 */
	public float getEngineMass(){
		return this.engineMass;
	}
	
	/**
	 * Set the mass of the Drones engine.
	 * @throws IllegalArgumentException if the given mass is smaller than or equal to 0
	 */
	public void setEngineMass(float engineMass) throws IllegalArgumentException {
		if(engineMass <= 0) throw new IllegalArgumentException("EngineMass must be larger than zero.");
		this.engineMass = engineMass;
	}
	
	
	// DRONE MASS
	
	/**
	 * Returns the total mass of the Drone.
	 */
	// mass of the drone
	public float getDroneMass() {
		return getEngineMass() + getTailMass() + getLeftWing().getMass() + getRightWing().getMass();
	}
	
	
	// GRAVITY
	
	/**
	 * The gravity that is applied to the drone.
	 */
	private float gravity;
	
	/**
	 * Returns the gravity that is applied to the drone.
	 */
	public float getGravity() {
		return this.gravity;
	}
	
	/**
	 * Set the gravity that is applied to the drone.
	 */
	public void setGravity(float g) {
		this.gravity = g;
	}
		
	
	// INERTIA MATRIX
	
	/**
	 * The inertia matrix of the Drone (in drone frame)
	 */
	private Matrix3f inertiaMatrix;
	
	/**
	 * Returns a copy of the Drones inertia matrix
	 */
	public Matrix3f getInertiaMatrix() {
		Matrix3f result = new Matrix3f();
		result.m00 = this.inertiaMatrix.m00;
		result.m11 = this.inertiaMatrix.m11;
		result.m22 = this.inertiaMatrix.m22;
		return result;
	}
	
	/**
	 * Sets the inertia matrix of the Drone
	 */
	public void setInertiaMatrix(Matrix3f inertiaMatrix) {
		this.inertiaMatrix = inertiaMatrix;
	}

	/**
	 * Calculates the inertia matrix of the Drone
	 */
	private Matrix3f calculateInertiaMatrix(){
		Matrix3f result = new Matrix3f();
		// x inertion
		result.m00 = (float) (getTailMass() *  Math.pow(getTailSize(), 2) + 
				getEngineMass() * Math.pow(this.getEnginePosition().z, 2));
		// y inertion
		result.m11 = (float) (getTailMass() * Math.pow(getTailSize(), 2) + 
				2 * getLeftWing().getMass() * Math.pow(getLeftWing().getCenterOfMass().x, 2) +
				getEngineMass() * Math.pow(this.getEnginePosition().z, 2));
		// z inertion
		result.m22 = (float) (2 * getLeftWing().getMass() * 
				Math.pow(getLeftWing().getCenterOfMass().x, 2));
		return result;
	}
	
	
	// MAXIMUM THRUST
	
	/**
	 * The maximum thrust of the Drones engine.
	 */
	private final float maxThrust;
	
	/**
	 * Returns the maximum thrust of the Drones engine.
	 */
	public float getMaxThrust() {
		return this.maxThrust;
	}
	
	
	// THRUST FORCE
	
	/**
	 * The current thrust force of the Drones engine.
	 */
	private float thrustForce;
	
	/**
	 * Returns the current thrust force of the Drones engine.
	 */
	public float getThrustForce() {
		return this.thrustForce;
	}
	
	/**
	 * Set the current thrust force of the Drones engine.
	 */
	public void setThrustForce(float thrustForce) {
		if(thrustForce <= this.getMaxThrust())
			this.thrustForce = thrustForce;
		else
			this.thrustForce = this.getMaxThrust();
	}
	
	
	// MAXIMUM ANGLE OF ATTACK
	
	/**
	 * The Drones maximum angle of attack
	 */
	private final float maxAOA;
	
	/**
	 * Returns the Drones maximum angle of attack.
	 */
	public float getMaxAOA() {
		return this.maxAOA;
	}
	
	
	// CAMERA
	
	/**
	 * The Drones camera
	 */
	private Camera camera;
	
	/**
	 * Returns the Drones camera.
	 */
	public Camera getCamera() {
		return this.camera;
	}
	
	/**
	 * Set the roll of the Drones camera.
	 */
	public void setRoll(float roll) {
		this.getCamera().setRoll(roll);
	}
	
	/**
	 * Increase the roll of the Drones camera.
	 */
	public void increaseCameraRoll(float roll) {
		this.getCamera().increaseRoll(roll);
	}

	
	
	
	
	
	
	
	
	
	
	
	

	// Autopilot communication
	/**
	 * Sends the properties of the drone to the Autopilot
	 * @throws IOException
	 */
	public void sendToAutopilot() throws IOException {
		DataOutputStream s = new DataOutputStream(new FileOutputStream("res/APInputs.cfg"));
		
		AutopilotInputs value = new AutopilotInputs() {
			public byte[] getImage() { return ImageConverter.bufferedImageToByteArray(camera.takeSnapshot()); /* TODO !!!!*/}
			
			public float getX() { return getPosition().x; }
			public float getY() { return getPosition().y; }
			public float getZ() { return getPosition().z; }
			
			// TODO: zie getHeadingVector()
			public float getHeading() { return getHeadingVector().x; }
			public float getPitch() { return getHeadingVector().y; }
			public float getRoll() { return getHeadingVector().z; }
			
			public float getElapsedTime() { return DisplayManager.getElapsedTime(); }
		};
		
		AutopilotInputsWriter.write(s, value);
		
		s.close();
	}
	
	/**
	 * Receives the input controls from the Autopilot
	 * @throws IOException
	 */
	public void getFromAutopilot() throws IOException {
		DataInputStream i = new DataInputStream(new FileInputStream("res/APOutputs.cfg"));
		
		AutopilotOutputs settings = AutopilotOutputsReader.read(i);
		
		setThrustForce(settings.getThrust());
		
		this.getLeftWing().setInclination(settings.getLeftWingInclination());
		this.getRightWing().setInclination(settings.getRightWingInclination());		
		this.getHorizStabilizer().setInclination(settings.getHorStabInclination());
		this.getVertStabilizer().setInclination(settings.getVerStabInclination());
	}
	

	
	
	
	
	

	
	
	
	
	
	/**
	 * Transforms the given vector from the drone frame to the world frame.
	 */
	public Vector3f transformToWorldFrame(Vector3f originalD){
		//TODO
		return null;
		
	}
	
	/**
	 * Transforms the given vector from the world frame to the drone frame.
	 */
	public Vector3f transformToDroneFrame(Vector3f originalW){
		//TODO
		return null;
	}
	
	
	
	
	/*
	 * DEBUG
	 */
	
//	public void moveHeadingVector() {
//		if(Keyboard.isKeyDown(Keyboard.KEY_Z)){
//			this.forwardVectorW.y += 0.01f;
//			this.forwardVectorW.normalise();
//		}
//		if(Keyboard.isKeyDown(Keyboard.KEY_S)){
//			this.forwardVectorW.y -= 0.01f;
//			this.forwardVectorW.normalise();
//		}
//		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
//			this.forwardVectorW = rotate(0.2f);
//			this.forwardVectorW.normalise();
//		}
//		if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
//			this.forwardVectorW.x -= 0.01f;
//			this.forwardVectorW.normalise();
//		}
//  
//		camera.increaseRotation(this.forwardVectorW);
//	}
//	
//	//	private static final float SPEED_SCALE = 10.0f;
//	
//	
//	/*
//	 * DEBUG VARS
//	 */
//	private boolean flying = false;
//
//	private void flyMode() {		
//		flying = true;
//		
//		if(Keyboard.isKeyDown(Keyboard.KEY_Z)){
//			getLeftWing().setInclination((float) Math.PI / 10);
//			getRightWing().setInclination((float) Math.PI / 10);
//		} else if(Keyboard.isKeyDown(Keyboard.KEY_S)){
//			getLeftWing().setInclination(- (float) Math.PI / 10);
//			getRightWing().setInclination(- (float) Math.PI / 10);
//		} else if(Keyboard.isKeyDown(Keyboard.KEY_D)){
//			getVerticalStabilizer().setInclination((float)-Math.PI/20);
//		} else if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
//			getVerticalStabilizer().setInclination((float)Math.PI/20);
//		} else {
//			flying = false;
//		}
//	}
}
