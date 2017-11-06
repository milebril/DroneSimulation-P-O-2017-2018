package entities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.Math;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import autoPilotJar.AutoPilot;
import autoPilotJar.AutopilotInputs;
import autoPilotJar.AutopilotInputsWriter;
import autoPilotJar.AutopilotOutputs;
import autoPilotJar.AutopilotOutputsReader;
import autopilot.AutopilotConfig;
import models.RawModel;
import openCV.RedCubeLocator;
import renderEngine.DisplayManager;
import toolbox.ImageConverter;

/**
 * Vectornames gevolgd door een D staan in het drone assenstelsel, gevolgd door een W staan in het wereld
 * uitgedrukt
 * @author Jakob
 *
 */
public class Drone extends Entity /* implements AutopilotConfig */ {

/**
 * 0: leftwing
 * 1: rightwing
 * 2: horizontalstab
 * 3: verticstab
 * 
 */
	private AirFoil[] airFoils = new AirFoil[4];
	public AirFoil getLeftWing(){
		return this.getAirFoils()[0];
	}
	
	public AirFoil getRightWing(){
		return this.getAirFoils()[1];
	}
	
	public AirFoil getHorizStabilizer(){
		return this.getAirFoils()[2];
	}
	
	public AirFoil getVertStabilizer(){
		return this.getAirFoils()[3];
	}
//	private AirFoil rightWing;
//	
//	private AirFoil horizontalStabilizer;
//	private AirFoil verticalStabilizer;
	
	private Camera camera;
	
	private float engineMass;
	private Vector3f enginePositionD;
	private float thrustForce;
	
	private float tailMass;
	private float tailSize;
	
	private float maxThrust;
	private float maxAOA;
	
	private Vector3f speedVectorW;
	private Vector3f accelerationVectorW;
//	private Vector3f speedVectorOld;
	private Vector3f forwardVectorW;

	private Vector3f rotationSpeedVectorW;
	private Vector3f rotationAccelerationW;
	
	private float gravity;
//	private static final float SPEED_SCALE = 10.0f;
	
	
	public Drone(RawModel model, Vector3f position, Vector3f orientation, float scale,
			AutopilotConfig cfg) {

		super(model, position, orientation, scale);
		
		this.speedVectorW = new Vector3f(0.0f,0.0f, -15.0f);
		this.accelerationVectorW = new Vector3f(0.0f,0.0f,0.0f);
//		this.speedVectorOld = new Vector3f(0.0f,0.0f,0.0f);
		this.forwardVectorW = new Vector3f(0.0f,0.0f,-1.0f);
		
		this.rotationSpeedVectorW = new Vector3f(0f, 0f, 0f);
		this.rotationAccelerationW = new Vector3f(0f, 0f, 0f);
		//Vector3f orig = new Vector3f(10,10,10);
		
		//TODO updater for inclination (in wing and stab)
		//TODO updater for speedVector
		
		//Read from config:
		this.airFoils[0] = new AirFoil(new Vector3f(-cfg.getWingX(),0,0), cfg.getWingMass(), cfg.getWingLiftSlope(), new Vector3f(1,0,0));  //horizontal rotation Axis = (1,0,0)
		this.airFoils[1] = new AirFoil(new Vector3f(cfg.getWingX(),0,0), cfg.getWingMass(), cfg.getWingLiftSlope(), new Vector3f(1,0,0));   //horizontal rotation Axis = (1,0,0)
		
		this.airFoils[2] = new AirFoil(new Vector3f(0,0,cfg.getTailSize()), cfg.getTailMass(),cfg.getHorStabLiftSlope(),new Vector3f(1,0,0)); //horizontal rotation Axis = (1,0,0)
		this.airFoils[3] = new AirFoil(new Vector3f(0,0,cfg.getTailSize()), cfg.getTailMass(),cfg.getVerStabLiftSlope(),new Vector3f(0,1,0)); //vertical rotation Axis = (0,1,0)
		
		this.maxThrust = cfg.getMaxThrust();
		this.maxAOA = cfg.getMaxAOA();
		this.setEngineMass(cfg.getEngineMass());
		this.enginePositionD = calculateEnginePosition();
		this.setTailMass(cfg.getTailMass());
		this.setTailSize(cfg.getTailSize());
		
		this.setInertiaMatrix(this.calculateInertiaMatrix());
		
//		this.thrustForce = 1000;
		
		camera = new Camera(cfg.getNbColumns(), cfg.getNbRows());
		camera.increasePosition(this.getPosition().x, this.getPosition().y, this.getPosition().z);
	}
	
	//Calculates engine position
	private Vector3f calculateEnginePosition(){
		float z = (-this.getTailMass()*this.getTailSize()) / this.getEngineMass();
		return new Vector3f(0,0,z);
	}


	public void sendToAutopilot() throws IOException {
		DataOutputStream s = new DataOutputStream(new FileOutputStream("res/APInputs.cfg"));
		
		AutopilotInputs value = new AutopilotInputs() {
			public byte[] getImage() { return ImageConverter.bufferedImageToByteArray(camera.takeSnapshot()); /* TODO !!!!*/}
			
			public float getX() { return getPosition().x; }
			public float getY() { return getPosition().y; }
			public float getZ() { return getPosition().z; }
			
			public float getHeading() { return getHeadingVector().x; }
			public float getPitch() { return getHeadingVector().y; }
			public float getRoll() { return getHeadingVector().z; }
			
			public float getElapsedTime() { return DisplayManager.getElapsedTime(); }
		};
		
		AutopilotInputsWriter.write(s, value);
		
		s.close();
	}

	public void getFromAutopilot() throws IOException {
		DataInputStream i = new DataInputStream(new FileInputStream("res/APOutputs.cfg"));
		
		AutopilotOutputs settings = AutopilotOutputsReader.read(i);
		
		setThrustForce(settings.getThrust());
		
		this.getLeftWing().setInclination(settings.getLeftWingInclination());
		this.getRightWing().setInclination(settings.getRightWingInclination());		
		this.getHorizStabilizer().setInclination(settings.getHorStabInclination());
		this.getVertStabilizer().setInclination(settings.getVerStabInclination());
	}
	

	public void setRoll(float roll) {
		this.getCamera().setRoll(roll);
	}
	
	public void increaseCameraRoll(float roll) {
		this.getCamera().increaseRoll(roll);
	}

	public Camera getCamera() {
		return this.camera;
	}
	
	public float getDroneMass() {
		return this.getEngineMass() + this.tailMass + this.getLeftWing().getMass() + this.getRightWing().getMass();
	}
	
	public void setHeadingVector() {
		if(this.speedVectorW.getX() == 0 && this.speedVectorW.getY() == 0 && this.speedVectorW.getZ() == 0)
			return;
		this.speedVectorW.normalise(this.forwardVectorW);
	}
	
	private Vector3f getHeadingVector() {
		return this.forwardVectorW;
	}
	
	/**
	 * functie die de snelheid van de drone berekent uit de speedVector
	 */
	public float getSpeed() {
		return this.getSpeedVector().length(); 
	}
	
	public void setSpeedVector(Vector3f speedVector) {
		this.speedVectorW = speedVector;
	}

	public Vector3f getSpeedVector(){
		return this.speedVectorW;
	}
	
	public void setSpeedChangeVector(Vector3f vector){
		this.accelerationVectorW = vector;
	}
	
	public Vector3f getSpeedChangeVector(){
		return this.accelerationVectorW;
	}
	
	public void setEngineMass(float engineMass) throws IllegalArgumentException {
		if(engineMass <= 0) throw new IllegalArgumentException("EngineMass must be larger than zero.");
		this.engineMass = engineMass;
	}
	
	
	
	public float getEngineMass(){
		return this.engineMass;
	}
	
	/**
	 * 
	 * @return the echte airfoils en geen kopie, dus opletten met wat je aanpast!!!!!
	 */
	public AirFoil[] getAirFoils(){
		return this.airFoils;
	}
	
	public void setTailMass(float tailMass) throws IllegalArgumentException {
		if(tailMass <= 0) throw new IllegalArgumentException("TailMass must be larger than zero.");
		this.tailMass = tailMass;
	}
	
	public float getTailMass(){
		return this.tailMass;
	}
	
	public void setTailSize(float tailSize) throws IllegalArgumentException {
		if(tailSize <= 0) throw new IllegalArgumentException("TailSize must be larger than zero.");
		this.tailSize = tailSize;
	}
	
	public float getTailSize() {
		return this.tailSize;
	}
	
	public float getThrustForce() {
		return this.thrustForce;
	}
	
	public float getMaxThrust() {
		return this.maxThrust;
	}
	
	public void setThrustForce(float thrustForce) {
		if(thrustForce <= this.getMaxThrust())
			this.thrustForce = thrustForce;
		else
			this.thrustForce = this.getMaxThrust();
	}
	
	private float getInertionY() {
		float inertia = (float) (getTailMass() * Math.pow(getTailSize(), 2) + 
				2 * getLeftWing().getMass() * Math.pow(getLeftWing().getCenterOfMass().x, 2) +
				getEngineMass() * Math.pow(this.enginePositionD.z, 2));
		
		return inertia;
	}
	
	private float getInertionX() {
		float inertia = (float) (getTailMass() *  Math.pow(getTailSize(), 2) + 
				getEngineMass() * Math.pow(this.enginePositionD.z, 2));
		
		return inertia;
	}
	
	private float getInertionZ() {
		return (float) (2 * getLeftWing().getMass() * Math.pow(getLeftWing().getCenterOfMass().x, 2));
	}
	
	private Matrix3f inertiaMatrix;
	
	public Matrix3f getInertiaMatrix() {
		return inertiaMatrix;
	}

	public void setInertiaMatrix(Matrix3f inertiaMatrix) {
		this.inertiaMatrix = inertiaMatrix;
	}

	private Matrix3f calculateInertiaMatrix(){
		
		Matrix3f result = new Matrix3f();
		result.m00 = this.getInertionX();
		result.m11 = this.getInertionY();
		result.m22 = this.getInertionZ();
		
		return result;
	}
	

	public Vector3f transformToWorldFrame(Vector3f originalD){
		//TODO
		return null;
		
	}
	
	public Vector3f transformToDroneFrame(Vector3f originalW){
		//TODO
		return null;
	}
	
	
	
	
	/*
	 * DEBUG
	 */
	/**
	 * 
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
