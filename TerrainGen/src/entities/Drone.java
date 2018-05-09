package entities;

import java.io.IOException;
import java.lang.Math;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import autopilot.algorithmHandler.AutopilotAlain;
import autopilot.interfaces.Autopilot;
import autopilot.interfaces.AutopilotConfig;
import autopilot.interfaces.AutopilotFactory;
import autopilot.interfaces.AutopilotInputs;
import autopilot.interfaces.AutopilotOutputs;
import models.RawModel;
import models.TexturedModel;
import physicsEngine.approximationMethods.EulerPrediction;
import physicsEngine.approximationMethods.PredictionMethod;
import renderEngine.DisplayManager;
import toolbox.ImageConverter;

/**
 * Vectornames gevolgd door een D staan in het drone assenstelsel, gevolgd door een W staan in het wereld
 * uitgedrukt
 * alle vectoren bestaan uit 3 elementen omdat het crossproduct voor 4 niet gedefinieerd is in de library
 * @author Jakob
 *
 */
public class Drone extends Entity /* implements AutopilotConfig */ {

	// CONSTRUCTORS
	
	public Drone(TexturedModel model, Matrix4f pose, float scale,
				AutopilotConfig cfg, PredictionMethod predictionMethod) {
		super(model, pose, scale);
		
		
		
		this.linearVelocityW = new Vector3f(0.0f,0.0f, -0.0f);

		this.angularVelocityW = new Vector3f(0f, 0f, 0f);
		
		
		// Left wing, Right wing, Horizontal stabilizer, Vertical stabilizer
		// vertical rotation axis: (0,1,0) (= y-axis) // horizontal rotation axis: (1,0,0) (= x-axis)
		this.airFoils[0] = new AirFoil(this, new Vector3f(1,0,0), new Vector3f(-cfg.getWingX(), 0, 0), cfg.getWingMass(), 
														cfg.getWingLiftSlope());
		this.airFoils[1] = new AirFoil(this, new Vector3f(1,0,0), new Vector3f(cfg.getWingX(), 0, 0), cfg.getWingMass(), 
														cfg.getWingLiftSlope());
		this.airFoils[2] = new AirFoil(this, new Vector3f(1,0,0), new Vector3f(0, 0, cfg.getTailSize()), 0, 
														cfg.getHorStabLiftSlope());
		this.airFoils[3] = new AirFoil(this, new Vector3f(0,1,0), new Vector3f(0, 0, cfg.getTailSize()), 0, 
														cfg.getVerStabLiftSlope());
		this.airFoils[0].name = "Left wing";
		this.airFoils[1].name = "Right wing";
		this.airFoils[2].name = "Horizontal stabilizer";
		this.airFoils[3].name = "Vertical stabilizer";
		
		/* INITIALIZE AUTOPILOT */
		Autopilot autopilot = AutopilotFactory.createAutopilot();
		this.setAutopilot(autopilot);
		
		
		//set configs
		this.maxThrust = cfg.getMaxThrust();
		this.maxAOA = cfg.getMaxAOA();
		this.gravity = cfg.getGravity();		
		
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
		
		// camera
		camera = new Camera(cfg.getNbColumns(), cfg.getNbRows());
		camera.increasePosition(this.getPosition().x, this.getPosition().y, this.getPosition().z);
		
		// set the prediciton method
		this.predictionMethod = predictionMethod;
		
		// Tyres
		tyres[0] = new Tyre(this, new Vector3f(0, cfg.getWheelY(), cfg.getFrontWheelZ()), cfg.getTyreRadius(), cfg.getRMax(), cfg.getFcMax(), cfg.getTyreSlope(), cfg.getDampSlope());
		tyres[1] = new Tyre(this, new Vector3f(-cfg.getRearWheelX(), cfg.getWheelY(), cfg.getRearWheelZ()), cfg.getTyreRadius(), cfg.getRMax(), cfg.getFcMax(), cfg.getTyreSlope(), cfg.getDampSlope());
		tyres[2] = new Tyre(this, new Vector3f(cfg.getRearWheelX(), cfg.getWheelY(), cfg.getRearWheelZ()), cfg.getTyreRadius(), cfg.getRMax(), cfg.getFcMax(), cfg.getTyreSlope(), cfg.getDampSlope());
	}
	
	public Drone(Matrix4f pose, AutopilotConfig autopilotConfig, Vector3f velocity, Vector3f rotVel) {
		this(null, pose, 1f, autopilotConfig, new EulerPrediction(0.01f));
		this.setLinearVelocity(velocity);
		this.setAngularVelocity(rotVel);
	}
	
	// AUTOPILOT
	
	private int id;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	// PREDICTION METHOD
	
	/**
	 * The predicion method the PhysicsEngine has to use for this Drone
	 */
	private final PredictionMethod predictionMethod;
	
	/**
	 * Returns this Drones predictionmethod
	 */
	public PredictionMethod getPredictionMethod() {
		return predictionMethod;
	}

	// AIRFOILS
	
	/**
	 * The airFoils of the drone (in order [leftwing, rightwing, hor. stabilizer, vert. stabilizer])
	 */
	private AirFoil[] airFoils = new AirFoil[4];
	
	/**
	 * Returns a copy of the airFoils array.
	 * (in order [leftwing, rightwing, hor. stabilizer, vert. stabilizer])
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
	public AirFoil getHorStabilizer(){
		return this.airFoils[2];
	}

	/**
	 * Returns the vertical stabilizer of the Drone.
	 */
	public AirFoil getVertStabilizer(){
		return this.airFoils[3];
	}
	
	// TYRES
	
	/**
	 * The Tyres of the drone (in order (in order [front wheel, rear left wheel, rear right wheel]) 
	 */
	private Tyre[] tyres = new Tyre[3];
	
	/**
	 * Returns a copy of the tyres array.
	 * (in order [leftwing, rightwing, hor. stabilizer, vert. stabilizer])
	 */
	public Tyre[] getTyres(){
		return this.tyres.clone();
	}
	
	/**
	 * Returns the front Tyre.
	 */
	public Tyre getFrontTyre() {
		return this.tyres[0];
	}
	
	/**
	 * Returns the left Tyre.
	 */
	public Tyre getLeftTyre() {
		return this.tyres[1];
	}
	
	/**
	 * Returns the right Tyre.
	 */
	public Tyre getRightTyre() {
		return this.tyres[2];
	}
	
	// FORWARD AND HEADING VECTOR
	
	/**
	 * Returns the forward vector of the drone in world frame.
	 */
	public Vector3f getForwardVector() {
		return this.transformToWorldFrame(new Vector3f(0, 0, -1));
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
	
	  // VELOCITY OF A POINT 
	   
	  /** 
	   * Returns the velocity (in world frame) of the point at the given coordinates (in drone frame) 
	   * @param point a point in de drone frame 
	   * @return a velocity in world frame 
	   */ 
	  public Vector3f getVelocityOfPoint(Vector3f point) { 
	    Vector3f rotVelocity = new Vector3f(); 
	    Vector3f.cross(getAngularVelocity(), transformToWorldFrame(point) , rotVelocity); 
	     
	    Vector3f totalVelocity = new Vector3f(); 
	    Vector3f.add(rotVelocity, getLinearVelocity(), totalVelocity); 
	     
	    return totalVelocity; 
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
	 * Returns the Drones engine position. (in drone frame)
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
	public float getMass() {
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
	 * Returns the absolute value of current thrust force of the Drones engine.
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
	
	// AUTOPILOT
	
	/**
	 * Add drone data to Autopilots interface to send to the Autopilot
	 * @throws IOException
	 */
	public AutopilotInputs getAutoPilotInputs() {

		return new AutopilotInputs() {
			public byte[] getImage() { return null;} //TODO: is null zetten hier voldoende?
			
			public float getX() { return getPosition().x; }
			public float getY() { return getPosition().y; }
			public float getZ() { return getPosition().z; }
			
			public float getHeading() { return getHeadingFloat(); }
			public float getPitch() { return getPitchFloat(); }
			public float getRoll() { return getRollFloat(); }
			
			public float getElapsedTime() { return DisplayManager.getElapsedTime(); }
		};
		
	}
	
	/**
	 * Receives the input controls from the Autopilot
	 */
	public void setAutopilotOutputs(AutopilotOutputs outputs) {
		setThrustForce(outputs.getThrust());
		
		this.getFrontTyre().setBrakingForce(outputs.getFrontBrakeForce());
		this.getLeftTyre().setBrakingForce(outputs.getLeftBrakeForce());
		this.getRightTyre().setBrakingForce(outputs.getRightBrakeForce());
		
		this.getLeftWing().setInclination(outputs.getLeftWingInclination());
		this.getRightWing().setInclination(outputs.getRightWingInclination());		
		this.getHorStabilizer().setInclination(outputs.getHorStabInclination());
		this.getVertStabilizer().setInclination(outputs.getVerStabInclination());
	}	
	
	// FRAME TRANSFORMATIONS
	
	/**
	 * Transforms the given vector from the drone frame to the world frame.
	 */
	public Vector3f transformToWorldFrame(Vector3f originalD){
		float len = originalD.length();
		
		Matrix3f transformationMatrix = (Matrix3f) calculateDtoWTransformationMatrix();
		Vector3f resultW = new Vector3f(0,0,0);

		Matrix3f.transform(transformationMatrix, originalD, resultW);
		
		// to reduce the rekenfouten:
		if (len != 0) {
			resultW.normalise();
			resultW.scale(len);
		}
		
		return resultW;
	}
	
	/**
	 * Transforms the given vector from the world frame to the drone frame.
	 */
	public Vector3f transformToDroneFrame(Vector3f originalW){
		float len = originalW.length();
		
		Matrix3f transformationMatrix = new Matrix3f();
		calculateDtoWTransformationMatrix().transpose(transformationMatrix);
		
		Vector3f resultD = new Vector3f();
		
		Matrix3f.transform(transformationMatrix, originalW, resultD);
		
		// to reduce the rekenfouten:
		if (len != 0) {
			resultD.normalise();
			resultD.scale(len);
		}
		return resultD;
	}
	
	/**
	 * Returns the transformation matrix for transforming vectors from the drone to the world frame.
	 */
	public Matrix3f calculateDtoWTransformationMatrix(){
		Matrix4f matrix4 = this.getPose();
		Matrix3f result = new Matrix3f();
		result.m00 = matrix4.m00;
		result.m01 = matrix4.m01;
		result.m02 = matrix4.m02;
		result.m10 = matrix4.m10;
		result.m11 = matrix4.m11;
		result.m12 = matrix4.m12;
		result.m20 = matrix4.m20;
		result.m21 = matrix4.m21;
		result.m22 = matrix4.m22;
		return result;
	}

	// ORIENTATION ANGLES
	
	/**
	 * Returns the roll of the drone in radians.
	 */
	public float getRollFloat() {
		Vector3f r0 = new Vector3f();
		Vector3f u0 = new Vector3f();
		Vector3f headingVector = this.getHeadingVector();
		Vector3f forwardVector = this.getForwardVector();
		Vector3f.cross(headingVector, new Vector3f(0,1,0), r0);
		Vector3f.cross(r0, forwardVector, u0);
		Vector3f r = this.transformToWorldFrame(new Vector3f(1,0,0));
	
		return (float) Math.atan2(Vector3f.dot(r, u0), Vector3f.dot(r, r0));
	}
	
	/**
	 * Returns the pitch of the drone in radians.
	 */
	public float getPitchFloat() {
		
		Vector3f headingVector = this.getHeadingVector();
		Vector3f forwardVector = this.getForwardVector();
		return (float) (Math.atan2(forwardVector.y, Vector3f.dot(headingVector, forwardVector)));
	}

	/**
	 * Returns the heading of the drone in radians.
	 * @return atan2(H . (-1, 0, 0), H . (0, 0, -1)), where H is the drone's heading vector (which we define as H0/||H0|| where H0 is the drone's forward vector ((0, 0, -1) in drone coordinates) projected onto the world XZ plane.
	 */
	public float getHeadingFloat() {

		Vector3f headingVector = this.getHeadingVector(); 
		return (float) Math.atan2(-headingVector.x, - headingVector.z);
	}
	
	private String name;
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String s) {
		this.name = s;
	}
	
	public Autopilot getAutopilot() {
		return autopilot;
	}

	public void setAutopilot(Autopilot autopilot) {
		this.autopilot = autopilot;
	}

	private Autopilot autopilot;
	
}
