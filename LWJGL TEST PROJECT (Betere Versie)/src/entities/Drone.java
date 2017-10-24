package entities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import java.lang.Math;

import javax.imageio.ImageIO;
import javax.swing.plaf.synth.SynthSplitPaneUI;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import autopilot.AutopilotConfig;
import models.RawModel;
import models.TexturedModel;
import renderEngine.DisplayManager;

public class Drone extends Entity /* implements AutopilotConfig */ {

	private Wing leftWing;
	private Wing rightWing;
	
	private Stabilizer horizontalStabilizer;
	private Stabilizer verticalStabilizer;
	
	private float engineMass;
	private Vector3f enginePosition;
	private float thrustForce;
	
	private float tailMass;
	private float tailSize;
	
	private float maxThrust;
	private float maxAOA;
	
	private Vector3f speedVector;
	private Vector3f speedChangeVector;
	private Vector3f speedVectorOld;
	private Vector3f headingVector;
	
	private Vector3f rotationSpeedVector;
	private Vector3f rotationAcceleration;
	
	private static final float GRAVITY = -9.81f;
	private static final float SPEED_SCALE = 10.0f;
	
	private Camera camera;
	
	/*
	 * DEBUG VARS
	 */
	private boolean flying = false;
	
	public Drone(RawModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale,
			AutopilotConfig cfg) {
		super(model, position, rotX, rotY, rotZ, scale);
		
		this.speedVector = new Vector3f(0.0f,0.0f, -1.0f);
		this.speedChangeVector = new Vector3f(0.0f,0.0f,0.0f);
		this.speedVectorOld = new Vector3f(0.0f,0.0f,0.0f);
		this.headingVector = new Vector3f(0.0f,0.0f,-1.0f);
		
		this.rotationSpeedVector = new Vector3f(0f, 0f, 0f);
		this.rotationAcceleration = new Vector3f(0f, 0f, 0f);
		//Vector3f orig = new Vector3f(10,10,10);
		
		//TODO updater for inclination (in wing and stab)
		//TODO updater for speedVector
		
		//Read from config:
		this.leftWing  = new Wing(new Vector3f(-cfg.getWingX(),0,0), cfg.getWingMass(), cfg.getWingLiftSlope(), new Vector3f(1,0,0));  //horizontal rotation Axis = (1,0,0)
		this.rightWing = new Wing(new Vector3f(cfg.getWingX(),0,0), cfg.getWingMass(), cfg.getWingLiftSlope(), new Vector3f(1,0,0));   //horizontal rotation Axis = (1,0,0)
		
		this.horizontalStabilizer = new Stabilizer(new Vector3f(0,0,cfg.getTailSize()), cfg.getTailMass(),cfg.getHorStabLiftSlope(),new Vector3f(1,0,0)); //horizontal rotation Axis = (1,0,0)
		this.verticalStabilizer   = new Stabilizer(new Vector3f(0,0,cfg.getTailSize()), cfg.getTailMass(),cfg.getVerStabLiftSlope(),new Vector3f(0,1,0)); //vertical rotation Axis = (0,1,0)
		
		horizontalStabilizer.setInclination(0);
		verticalStabilizer.setInclination(0);
		
		this.maxThrust = cfg.getMaxThrust();
		this.maxAOA = cfg.getMaxAOA();
		this.engineMass = cfg.getEngineMass();
		this.enginePosition = calculateEnginePosition();
		this.tailMass = cfg.getTailMass();
		this.tailSize = cfg.getTailSize();
		
		this.thrustForce = 1000;
		
		camera = new Camera(cfg.getNbColumns(), cfg.getNbRows());
		camera.increasePosition(this.getPosition().x, this.getPosition().y, this.getPosition().z);
	}
	
	//Calculates engine position
	private Vector3f calculateEnginePosition(){
		float z = (-this.getTailMass()*this.getTailSize()) / this.getEngineMass();
		return new Vector3f(0,0,z);
	}

	//Calculates left wing liftforce
	private Vector3f calculateLeftWingLift(){
		//TODO: normal wordt gedeclareerd als een 0 vector omdat Vector3f.cross iets moet opslaan in vector.
		//alternatief is mss vector declareren in Vector3f.cross?
		Vector3f normal = new Vector3f(0,0,0);
		
		// The left wing's attack vector is (0, sin(leftWingInclination), -cos(leftWingInclination)).
		Vector3f attackVector = new Vector3f(0,(float)Math.sin(this.getLeftWing().getInclination()), (float) -Math.cos(this.getLeftWing().getInclination()));
		Vector3f.cross(this.getLeftWing().getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
		
		float liftSlope = this.getLeftWing().getLiftSlope();
		
		//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
		
		float AoA = (float) - Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
		float speed = (float) Math.pow(this.getSpeed(),2);
		Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
									   (float)(normal.y*liftSlope*AoA*speed),
									   (float)(normal.z*liftSlope*AoA*speed));
		
		return result;
	}
	
	//Calculates right wing liftforce
	private Vector3f calculateRightWingLift(){
		Vector3f normal = new Vector3f(0,0,0);
		
		// The right wing's attack vector is (0, sin(rightWingInclination), -cos(rightWingInclination)).
		Vector3f attackVector = new Vector3f(0,(float)Math.sin(this.getRightWing().getInclination()), (float) -Math.cos(this.getRightWing().getInclination()));
		Vector3f.cross(this.getRightWing().getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
		
		float liftSlope = this.getRightWing().getLiftSlope();
		
		//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
		float AoA = (float) - Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
		float speed = (float) Math.pow(this.getSpeed(),2);
		
		Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
									   (float)(normal.y*liftSlope*AoA*speed),
									   (float)(normal.z*liftSlope*AoA*speed));
		return result;
	}
	
	//Calculates horizontal stabilizer liftforce
	private Vector3f calculateHorStabLift(){
		Vector3f normal = new Vector3f(0,0,0);
		
		// The horizontal stabilizer's attack vector is (0, sin(horStabInclination), -cos(horStabInclination)).
		Vector3f attackVector = new Vector3f(0,(float)Math.sin(this.getHorizontalStabilizer().getInclination()), (float) -Math.cos(this.horizontalStabilizer.getInclination()));
		
		Vector3f.cross(this.getHorizontalStabilizer().getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
		float liftSlope = this.getHorizontalStabilizer().getLiftSlope();
		
		//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
		float AoA = (float) -Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
		float speed = (float) Math.pow(this.getSpeed(),2);
		
		Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
									   (float)(normal.y*liftSlope*AoA*speed),
									   (float)(normal.z*liftSlope*AoA*speed));
		
		return result;
	}
	
	//Calculates vertical stabilizer liftforce
	private Vector3f calculateVerStabLift(){
		Vector3f normal = new Vector3f(0,0,0);
			
		// The vertical stabilizer's attack vector is (-sin(verStabInclination), 0, -cos(verStabInclination)).
		Vector3f attackVector = new Vector3f((float) -Math.sin(this.getVerticalStabilizer().getInclination()), 0, (float) -Math.cos(this.verticalStabilizer.getInclination()));
		Vector3f.cross(this.getVerticalStabilizer().getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
		float liftSlope = this.getVerticalStabilizer().getLiftSlope();
			
		//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
		float AoA = (float) - Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
		float speed = (float) Math.pow(this.getSpeed(),2);
		Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
									   (float)(normal.y*liftSlope*AoA*speed),
									   (float)(normal.z*liftSlope*AoA*speed));
		return result;
	}	
	
	private Vector3f calculateVerStabTorque() {
		Vector3f lift = calculateVerStabLift();
		Vector3f torque = new Vector3f(0,0,0);
		
		Vector3f.cross(getVerticalStabilizer().getCenterOfMass(), lift, torque);
		
		return torque;
	}
	
	public void increasePosition(float dt) {
		float dx = this.getHeadingVector().x * dt * Math.abs(this.getSpeedVector().x);
		float dy = this.getHeadingVector().y * dt * Math.abs(this.getSpeedVector().y);
		float dz = this.getHeadingVector().z * dt * Math.abs(this.getSpeedVector().z);
		
		super.increasePosition(dx, dy, dz);

		this.getCamera().increasePosition(dx, dy, dz);
		//this.getCamera().increaseRotation(this.getHeadingVector());
	}
	
	public void applyForces(float dt) {
		//Checks:
		if (this.getThrustForce() > this.getMaxThrust()) { //TODO: deze check is overbodig omdat de setter van Thrus deze check doet?
			this.setThrustForce(this.getMaxThrust());
		}
		
		//Gravity
		//Check voor maximale valversnelling
		if (Math.abs(this.getSpeedVector().y) < 200) {
			this.getSpeedChangeVector().y += GRAVITY * dt; // v = v0 + a*t, a = F/m
		}
		
		//Engine = Speed
		if (this.getSpeed() < 100)
			applyEngineForce(dt);
		
		//System.out.println("Before Left: " + speedChangeVector);
		applyLiftForces(dt);
		//System.out.println("After Left: " + speedChangeVector);
		applyTorqueForces(dt);
		
		
		if (Math.abs(this.getHeadingVector().y) > 0.1 && !flying) {
			getVerticalStabilizer().setInclination(0);
			
			if (this.getHeadingVector().y > 0) {
				getLeftWing().setInclination(getLeftWing().getInclination() - 0.01f);
				getRightWing().setInclination(getRightWing().getInclination() - 0.01f);
				//getHorizontalStabilizer().setInclination(this.horizontalStabilizer.getInclination() - 0.01f);
			} else if (this.getHeadingVector().y < 0) {
				getLeftWing().setInclination(getLeftWing().getInclination() + 0.01f);
				getRightWing().setInclination(getRightWing().getInclination() + 0.01f);
				//getHorizontalStabilizer().setInclination(this.horizontalStabilizer.getInclination() + 0.01f);
			} else {
				//Do nothing
			}
		} 
		
		
		//x = x0 + v*t
		
		flyMode();
		
		Vector3f.add(this.getSpeedVector(), this.getSpeedChangeVector(), this.getSpeedVector());
		Vector3f.add(rotationSpeedVector, rotationAcceleration, rotationSpeedVector);
		
		deepCopySpeedVector();
		this.setSpeedChangeVector(new Vector3f(0,0,0));
		rotationAcceleration = new Vector3f(0,0,0);
		
		setHeadingVector();
		
		//Vector3f.add(getHeadingVector(), rotationSpeedVector, headingVector);
		//getHeadingVector().normalise();
	}

	private void deepCopySpeedVector() {
		//DeepCopy the vector!!!
		this.speedVectorOld.x = this.speedVector.x;
		this.speedVectorOld.y = this.speedVector.y;
		this.speedVectorOld.z = this.speedVector.z;
	}
	
	private void applyEngineForce(float dt) {
		//v = v0 + a*t -> a = thrustForce / droneMass
		//speedVectorNew = speedVectorOld + (thrustForce / droneMass)*dt
		Vector3f engineVector = new Vector3f(this.getHeadingVector().x*(this.getThrustForce() / this.getDroneMass())*dt,
											 this.getHeadingVector().y*(this.getThrustForce() / this.getDroneMass())*dt,
											 this.getHeadingVector().z*(this.getThrustForce() / this.getDroneMass())*dt);
		Vector3f.add(engineVector, this.getSpeedChangeVector(), this.getSpeedChangeVector());
	}

	private void applyLiftForces(float dt){
		//Left Wing
		Vector3f leftWingLiftForce = calculateLeftWingLift(); 			// = F
		leftWingLiftForce.scale(1/getDroneMass() * dt); 			  			// = a = F/Mass
		this.getSpeedChangeVector().y += leftWingLiftForce.y;
		
		//Right Wing
		Vector3f rightWing = calculateRightWingLift(); 		 // = F
		rightWing.scale(1/getDroneMass() * dt); 			  		 // = a = F/Mass
		this.getSpeedChangeVector().y += rightWing.y;
		
		Vector3f horStab= calculateHorStabLift();
		horStab.scale(1/getDroneMass() * dt);
		this.getSpeedChangeVector().y += horStab.y;
		
		Vector3f verStab= calculateVerStabLift();
		verStab.scale(1/getDroneMass() * dt);
		Vector3f.add(verStab, this.speedChangeVector, this.speedChangeVector);
	}
	
	private void applyTorqueForces(float dt) {
		Vector3f verStabTorque = null;
		verStabTorque = calculateVerStabTorque();
		verStabTorque.scale(1/getInertionY() * dt);
		Vector3f.add(verStabTorque, rotationAcceleration, rotationAcceleration);
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
		return this.getEngineMass() + this.tailMass + this.leftWing.getMass() + this.rightWing.getMass();
	}
	
	private void setHeadingVector() {
		if(this.speedVector.getX() == 0 && this.speedVector.getY() == 0 && this.speedVector.getZ() == 0)
			return;
		this.speedVector.normalise(this.headingVector);
	}
	
	private Vector3f getHeadingVector() {
		return this.headingVector;
	}
	
	/**
	 * functie die de snelheid van de drone berekent uit de speedVector
	 */
	public float getSpeed() {
		return this.getSpeedVector().length(); 
	}
	
	public Vector3f getSpeedVector(){
		return this.speedVector;
	}
	
	public void setSpeedChangeVector(Vector3f vector){
		this.speedChangeVector = vector;
	}
	
	public Vector3f getSpeedChangeVector(){
		return this.speedChangeVector;
	}
	
	public float getEngineMass(){
		return this.engineMass;
	}
	
	public Wing getRightWing(){
		return this.rightWing;
	}
	
	public Wing getLeftWing(){
		return this.leftWing;
	}
	
	public Stabilizer getHorizontalStabilizer(){
		return this.horizontalStabilizer;
	}
	
	public Stabilizer getVerticalStabilizer(){
		return this.verticalStabilizer;
	}
	
	public float getTailMass(){
		return this.tailMass;
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
				getEngineMass() * Math.pow(this.enginePosition.z, 2));
		
		return inertia;
	}
	
	private float getInertionX() {
		float inertia = (float) (getTailMass() *  Math.pow(getTailSize(), 2) + 
				getEngineMass() * Math.pow(this.enginePosition.z, 2));
		
		return inertia;
	}
	
	private float getInertionZ() {
		return (float) (2 * getLeftWing().getMass() * Math.pow(getLeftWing().getCenterOfMass().x, 2));
	}
	
	/*
	 * DEBUG
	 */
	
	public void moveHeadingVector() {
		if(Keyboard.isKeyDown(Keyboard.KEY_Z)){
			this.headingVector.y += 0.01f;
			this.headingVector.normalise();
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)){
			this.headingVector.y -= 0.01f;
			this.headingVector.normalise();
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
			this.headingVector.x += 0.01f;
			this.headingVector.normalise();
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
			this.headingVector.x -= 0.01f;
			this.headingVector.normalise();
		}

		camera.increaseRotation(this.headingVector);
	}
	
	private void flyMode() {		
		flying = true;
		
		if(Keyboard.isKeyDown(Keyboard.KEY_Z)){
			if (this.getHorizontalStabilizer().getInclination() < 0.5)
				this.getHorizontalStabilizer().setInclination(this.horizontalStabilizer.getInclination() + 0.01f);
		} else if(Keyboard.isKeyDown(Keyboard.KEY_S)){
			if (this.getHorizontalStabilizer().getInclination() > -0.5)
				this.getHorizontalStabilizer().setInclination(this.horizontalStabilizer.getInclination() - 0.01f);
		} else if(Keyboard.isKeyDown(Keyboard.KEY_D)){
			getVerticalStabilizer().setInclination(-0.5f);
		} else if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
			this.headingVector.x -= 0.01f;
		} else {
			flying = false;
		}
	}
}
