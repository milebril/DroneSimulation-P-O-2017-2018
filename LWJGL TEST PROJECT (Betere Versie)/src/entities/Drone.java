package entities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import java.lang.Math;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import autopilot.AutopilotConfig;
import models.RawModel;
import models.TexturedModel;

public class Drone extends Entity /* implements AutopilotConfig */ {

	private Wing leftWing;
	private Wing rightWing;
	
	private Stabilizer horizontalStabilizer;
	private Stabilizer verticalStabilizer;
	
	private float engineMass;
	private Vector3f enginePosition;
	
	private float tailMass;
	private float tailSize;
	
	private float maxThrust;
	private float maxAOA;
	
	private Vector3f speedVector;
	
	private float distance = 0.2f;
	private float mouseSensitivity = 0.1f;
	
	private Camera camera;
	
	public Drone(RawModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale,
			AutopilotConfig cfg) {
		super(model, position, rotX, rotY, rotZ, scale);
		
		this.speedVector = new Vector3f(0,0,0);
		
		//TODO load drone settings through Config
		//TODO updater for inclination (in wing and stab)
		//TODO updater for speedVector
		
		//Read from config:
		this.leftWing = new Wing(new Vector3f(-cfg.getWingX(),0,0), cfg.getWingMass(), cfg.getWingLiftSlope(), new Vector3f(1,0,0));   //horizontal rotation Axis
		this.rightWing = new Wing(new Vector3f(cfg.getWingX(),0,0), cfg.getWingMass(), cfg.getWingLiftSlope(), new Vector3f(1,0,0));   //horizontal rotation Axis
		
		this.horizontalStabilizer = new Stabilizer(new Vector3f(0,0,cfg.getTailSize()),cfg.getHorStabLiftSlope(),new Vector3f(1,0,0)); //horizontal rotation Axis
		this.verticalStabilizer = new Stabilizer(new Vector3f(0,0,cfg.getTailSize()),cfg.getVerStabLiftSlope(),new Vector3f(0,1,0));   //vertical rotation Axis
		
		// this.verticalStabilizer = new Stabilizer(...) + Set rotationAxis to Y;
		this.maxThrust = cfg.getMaxThrust();
		this.maxAOA = cfg.getMaxAOA();
		this.engineMass = cfg.getEngineMass();
		this.enginePosition = calculateEnginePosition(cfg.getWingX());
		this.tailMass = cfg.getTailMass();
		this.tailSize = cfg.getTailSize();
		
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
		Vector3f attackVector = new Vector3f(0,(float)Math.sin(this.leftWing.getInclination()), (float) -Math.cos(this.leftWing.getInclination()));
		Vector3f.cross(this.leftWing.getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
		
		float liftSlope = this.leftWing.getLiftSlope();
		
		//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
		
		float AoA = (float) - Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
		float speed = (float) Math.pow(this.getSpeed(),2);
		Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*Math.pow(speed, 2)),
									   (float)(normal.y*liftSlope*AoA*Math.pow(speed, 2)),
									   (float)(normal.z*liftSlope*AoA*Math.pow(speed, 2)));
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
		Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*Math.pow(speed, 2)),
									   (float)(normal.y*liftSlope*AoA*Math.pow(speed, 2)),
									   (float)(normal.z*liftSlope*AoA*Math.pow(speed, 2)));
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
			Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*Math.pow(speed, 2)),
										   (float)(normal.y*liftSlope*AoA*Math.pow(speed, 2)),
										   (float)(normal.z*liftSlope*AoA*Math.pow(speed, 2)));
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
		Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*Math.pow(speed, 2)),
									   (float)(normal.y*liftSlope*AoA*Math.pow(speed, 2)),
									   (float)(normal.z*liftSlope*AoA*Math.pow(speed, 2)));
		return result;
	}	
		
	
	private Vector3f getSpeedVector(){
		return this.speedVector;
	}
	@Override
	public void increasePosition(float dx, float dy, float dz) {
		super.increasePosition(dx, dy, dz);
		camera.increasePosition(dx, dy, dz);
		//TODO increase pos as of speed
	}
	
	public void applyForces() {
		//Gravity
		//in the 2 wings
		//in the engine          //Can we do this in the massCenter? -> gewoon in de berekende engine positie
		//in the stabilizer
		//Lift Forces, ...
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
	
	//functie die de snelheid van de drone berekent uit de speedVector
	public float getSpeed() {
		float x = this.getSpeedVector().x;
		float y = this.getSpeedVector().y;
		float z = this.getSpeedVector().z;
		return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
	}
}
