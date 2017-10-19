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
	
	private static final float GRAVITY = -9.81f;
	private static final float SPEED_SCALE = 10.0f;
	
	//private float downSpeed = 0;
	
	private Camera camera;
	
	public Drone(RawModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale,
			AutopilotConfig cfg) {
		super(model, position, rotX, rotY, rotZ, scale);
		
		this.speedVector = new Vector3f(0.0f,0.0f, -10.0f);
		this.speedChangeVector = new Vector3f(0.0f,0.0f,0.0f);
		this.speedVectorOld = new Vector3f(0.0f,0.0f,0.0f);
		this.headingVector = new Vector3f(0.0f,0.0f,-1.0f);
		//Vector3f orig = new Vector3f(10,10,10);
		
		//TODO updater for inclination (in wing and stab)
		//TODO updater for speedVector
		
		//Read from config:
		this.leftWing  = new Wing(new Vector3f(-cfg.getWingX(),0,0), cfg.getWingMass(), cfg.getWingLiftSlope(), new Vector3f(1,0,0));  //horizontal rotation Axis = (1,0,0)
		this.rightWing = new Wing(new Vector3f(cfg.getWingX(),0,0), cfg.getWingMass(), cfg.getWingLiftSlope(), new Vector3f(1,0,0));   //horizontal rotation Axis = (1,0,0)
		
		this.horizontalStabilizer = new Stabilizer(new Vector3f(0,0,cfg.getTailSize()),cfg.getHorStabLiftSlope(),new Vector3f(1,0,0)); //horizontal rotation Axis = (1,0,0)
		this.verticalStabilizer   = new Stabilizer(new Vector3f(0,0,cfg.getTailSize()),cfg.getVerStabLiftSlope(),new Vector3f(0,1,0)); //vertical rotation Axis = (0,1,0)
		
		this.maxThrust = cfg.getMaxThrust();
		this.maxAOA = cfg.getMaxAOA();
		this.engineMass = cfg.getEngineMass();
		this.enginePosition = calculateEnginePosition(cfg.getWingX());
		this.tailMass = cfg.getTailMass();
		this.tailSize = cfg.getTailSize();
		
		this.thrustForce = 1000;
		
		camera = new Camera(cfg.getNbColumns(), cfg.getNbRows());
		camera.increasePosition(this.getPosition().x, this.getPosition().y, this.getPosition().z);
	}
	
	//Calculates engine position
	private Vector3f calculateEnginePosition(float wingX){
		float x = (this.leftWing.getMass()*wingX - this.rightWing.getMass()*wingX) / this.engineMass;
		float z = (-this.tailMass*this.tailSize) / this.engineMass;
		return new Vector3f(x,0,z);
	}
	
	//Calculates left wing liftforce
	private Vector3f calculateLeftWingLift(){
		//TODO: normal wordt gedeclareerd als een 0 vector omdat Vector3f.cross iets moet opslaan in vector.
		//alternatief is mss vector declareren in Vector3f.cross?
		Vector3f normal = new Vector3f(0,0,0);
		
		// The left wing's attack vector is (0, sin(leftWingInclination), -cos(leftWingInclination)).
		Vector3f attackVector = new Vector3f(0,(float)Math.sin(this.leftWing.getInclination()), (float) -Math.cos(this.leftWing.getInclination()));
		Vector3f.cross(this.leftWing.getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
		
		float liftSlope = this.leftWing.getLiftSlope();
		
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
		Vector3f attackVector = new Vector3f(0,(float)Math.sin(this.rightWing.getInclination()), (float) -Math.cos(this.rightWing.getInclination()));
		Vector3f.cross(this.rightWing.getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
		
		float liftSlope = this.rightWing.getLiftSlope();
		
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
		Vector3f attackVector = new Vector3f(0,(float)Math.sin(this.horizontalStabilizer.getInclination()), (float) -Math.cos(this.horizontalStabilizer.getInclination()));
		
		Vector3f.cross(this.horizontalStabilizer.getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
		float liftSlope = this.horizontalStabilizer.getLiftSlope();
		
		//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
		float AoA = (float) - Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
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
		Vector3f attackVector = new Vector3f(0,(float) -Math.sin(this.verticalStabilizer.getInclination()), (float) -Math.cos(this.verticalStabilizer.getInclination()));
			
		Vector3f.cross(this.verticalStabilizer.getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
		float liftSlope = this.verticalStabilizer.getLiftSlope();
			
		//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
		float AoA = (float) - Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
		float speed = (float) Math.pow(this.getSpeed(),2);
		
		Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
									   (float)(normal.y*liftSlope*AoA*speed),
									   (float)(normal.z*liftSlope*AoA*speed));
		return result;
	}	
		
	
	public Vector3f getSpeedVector(){
		return this.speedVector;
	}
	
	public void increasePosition(float dt) {
		float dx = this.headingVector.x * dt * Math.abs(this.speedVector.x);
		float dy = this.headingVector.y * dt * Math.abs(this.speedVector.y);
		float dz = this.headingVector.z * dt * Math.abs(this.speedVector.z);
		
		super.increasePosition(dx, dy, dz);
		camera.increasePosition(dx, dy, dz);
		
		camera.increaseRotation(this.headingVector);
	}
	
	public void applyForces(float dt) {
		//Checks:
		if (this.thrustForce > this.maxThrust) {
			this.thrustForce = maxThrust;
		}
		
		//Gravity
		if (Math.abs(this.speedVector.y) < 60) {
			this.speedChangeVector.y += GRAVITY * dt; // v = v0 + a*t, a = F/m
		}
		
		//Engine = Speed
		if (this.speedVector.length() < 100)
			applyEngineForce(dt);
		
		
		applyLiftForces(dt);
		
		if (this.headingVector.y > 0) {
			leftWing.setInclination(leftWing.getInclination() - 0.01f);
		} else if (this.headingVector.y < 0) {
			leftWing.setInclination(leftWing.getInclination() + 0.01f);
		} else {
			//Do nothing
		}
		
		//x = x0 + v*t
		setHeadingVector();
		
		Vector3f.add(speedVector, speedChangeVector, speedVector);
		System.out.println(this.getPosition());
		deepCopySpeedVector();
		speedChangeVector = new Vector3f(0,0,0);
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
		Vector3f engineVector = new Vector3f(this.getHeadingVector().x*(this.thrustForce / this.getDroneMass())*dt,
											 this.getHeadingVector().y*(this.thrustForce / this.getDroneMass())*dt,
											 this.getHeadingVector().z*(this.thrustForce / this.getDroneMass())*dt);
		Vector3f.add(engineVector, this.speedChangeVector, this.speedChangeVector);
	}

	private void applyLiftForces(float dt){
		//Left Wing
		Vector3f leftWingLiftForce = this.calculateLeftWingLift(); 			// = F
		leftWingLiftForce.scale(1/this.getDroneMass() * dt); 			  			// = a = F/Mass
		this.speedChangeVector.y += leftWingLiftForce.y;
		
		//Right Wing
		//left and right are same for now.
		//Vector3f.add(rightWing, this.speedChangeVector, this.speedChangeVector); // = v = v0 + a*t
		Vector3f rightWing = this.calculateRightWingLift(); 		 // = F
		rightWing.scale(1/this.getDroneMass() * dt); 			  		 // = a = F/Mass
		this.speedChangeVector.y += rightWing.y;
		
		//Vector3f horStab = this.calculateHorStabLift()
	}
	
	public void setRoll(float roll) {
		this.camera.setRoll(roll);
	}
	
	public void increaseCameraRoll(float roll) {
		this.camera.increaseRoll(roll);
	}

	public Camera getCamera() {
		return this.camera;
	}
	
	public float getDroneMass() {
		return this.engineMass + this.tailMass + this.leftWing.getMass() + this.rightWing.getMass();
	}
	
	private void setHeadingVector() {
		if(this.speedVector.getX() == 0 && this.speedVector.getY() == 0 && this.speedVector.getZ() == 0)
			return;
		this.speedVector.normalise(this.headingVector);
	}
	
	private Vector3f getHeadingVector() {
		return this.headingVector;
	}
	
	//functie die de snelheid van de drone berekent uit de speedVector
	public float getSpeed() {
		float x = this.getSpeedVector().x;
		float y = this.getSpeedVector().y;
		float z = this.getSpeedVector().z;
		return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
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
}
