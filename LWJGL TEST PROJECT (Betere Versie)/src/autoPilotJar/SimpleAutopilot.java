package autoPilotJar;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.AMDBlendMinmaxFactor;
import org.lwjgl.util.vector.Vector3f;

import autopilot.AutopilotConfigReader;
import interfaces.Autopilot;
import interfaces.AutopilotConfig;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;
import openCV.ImageProcessor;
import openCV.RedCubeLocator;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;

public class SimpleAutopilot implements Autopilot, AutopilotOutputs{	
	private boolean heightGoalReached = false;
	private AutopilotConfig configAP;
	private AutopilotInputs inputAP;
	
	private Vector3f currentPosition;
	private Vector3f prevPosition;
	
	//both will be zero during the first iteration
	private float currentHeading;
	private float prevHeading;
	
	private void setHeading(float heading){
		this.prevHeading = this.currentHeading;
		this.currentHeading = heading;
	}
	
	private float currentPitch;
	private float prevPitch;
	
	private void setPitch(float pitch){
		this.prevPitch = this.currentPitch;
		this.currentPitch = pitch;
	}
	
	private float currentRoll;
	private float prevRoll;
	
	private void setRoll(float roll){
		this.prevRoll = this.currentRoll;
		this.currentRoll = roll;
	}
	
	/**
	 * 
	 * @return an array containing the the current rotation axis and the speed around this axis in the order
	 * [x, y, z, speed] 
	 */
	private Vector3f getCurrentRotationSpeed(){
		
		Matrix4f currentOrientation = getCurrentOrientation();
		
		Matrix4f prevOrientation;
		Matrix4f xRotp = new Matrix4f();
		xRotp.rotX(prevPitch);
		Matrix4f yRotp = new Matrix4f();
		yRotp.rotY(prevHeading);
		Matrix4f zRotp = new Matrix4f();
		zRotp.rotZ(prevRoll);;
		zRotp.mul(xRotp);
		zRotp.mul(yRotp);
		prevOrientation = zRotp;
		
		prevOrientation.transpose(); 
		currentOrientation.mul(prevOrientation);
		
		AxisAngle4f rotation = new AxisAngle4f();
		rotation.set(currentOrientation);
		float[] result = new float[4];
		rotation.get(result);
		float rotSpeed = result[3] / this.dt;
		result[3] = rotSpeed;
		
		Vector3f speed = new Vector3f(result[0], result[1], result[2]);
		speed.scale(result[3]);
		
		return speed;
	}

	private Matrix4f getCurrentOrientation() {
		
		Matrix4f currentOrientation;
		Matrix4f xRot = new Matrix4f();
		xRot.rotX(currentPitch);
		Matrix4f yRot = new Matrix4f();
		yRot.rotY(currentHeading);
		Matrix4f zRot = new Matrix4f();
		zRot.rotZ(currentRoll);;
		zRot.mul(xRot);
		zRot.mul(yRot);
		currentOrientation = zRot;
		return currentOrientation;
	}

	
	private float getAOALeftWing(float inclination){
		Vector3f localRotationSpeed = new Vector3f();
		//de hefboomsafstand
		Vector3f leftWingLever = new Vector3f(-this.configAP.getWingX(), 0, 0);
		// v_rot = omega x hefboomstafstand
		Vector3f.cross(this.getCurrentRotationSpeed(), leftWingLever , localRotationSpeed);
		//totale snelheid is de som van de rotatie en de drone snelheid
		Vector3f totalSpeed = new Vector3f();
		Vector3f.add(this.calculateSpeedVector(), localRotationSpeed, totalSpeed); 
		
		//The left wing's attack vector is (0, sin(leftWingInclination), -cos(leftWingInclination)
		Vector3f attackVector = new Vector3f (0.0f, (float) Math.sin(inclination), (float)-Math.cos(inclination));
		
		this.getCurrentOrientation().transform(attackVector);
	}
	
	private Vector3f oldSpeed;
	
	//Aanpassen als we naar nieuwe cubus moeten gaan
	private Vector3f cubePos;
	private List<Vector3f> cubePositions = new ArrayList<>();
	
	
	//TODO ook heading bijhouden?.	
	
	private float elapsedTime;
	private float prevElapsedTime;
	private float dt = 0;
	
	private float heightGoal = 1;
	
	private ImageProcessor cubeLocator;
	private PIDController pidHorStab;
	private PIDController pidHorWing;
	private PIDController pidHorGoal;
	private PIDController pidVerGoal;
	private PIDController pidThrust;
	
	/* Variables to send back to drone	 
	 * Initialy All inclinations are 0
	 */
	private float newThrust = 0;
	private float newLeftWingInclination = 0;
	private float newRightWingInclination = 0;
	private float newHorStabInclination = 0;
	private float newVerStabInclination = 0;
	
	public SimpleAutopilot() {
		//Set initial Positions
		currentPosition = new Vector3f(0,0,0);
		prevPosition = new Vector3f(0,0,0);
		
		//Initialize PIDController for horizontalflight
		//PIDController(float K-Proportional, float K-Integral, float K-Derivative, float changeFactor, float goal)
		//this.pidHorStab = new PIDController(10.0f,1.0f,5.0f);
		this.pidHorWing = new PIDController(1.0f,0.0f,10.0f, (float) -(Math.PI / 180), 0);
		
		this.pidHorStab = new PIDController(2.0f,1.0f,10.0f, (float) (Math.PI / 180), 0);
		this.pidHorGoal = new PIDController(2.0f,0.0f,0.0f, (float) (Math.PI / 180), 0);
		this.pidVerGoal = new PIDController(2.0f,0.0f,1.0f, (float) (Math.PI / 180), 0);
		//Initialize AP with configfile
		
		//Initialize PIDController for Thrust
		//PIDController(float K-Proportional, float K-Integral, float K-Derivative, float changeFactor, float goal)
		this.pidThrust = new PIDController(1.0f, 0.0f, 3.0f, -10, 10);
		//initialize();
		
		//ADD CUBES TO LIST
		//cubePositions.add(new Vector3f(0,0, -200));
//		cubePositions.add(new Vector3f(0,0,-80));
//		cubePositions.add(new Vector3f(-2,0,-120));
//		cubePositions.add(new Vector3f(0,0,-160));
//		cubePositions.add(new Vector3f(0, 0, -80));
//		cubePositions.add(new Vector3f(-5,0,-40));
//		cubePositions.add(new Vector3f(-2.5f,0,-60));
		
		
		//cubePositions.add(new Vector3f(-5,0,-40));
		
		//WORKING DEMO UP/DOWN
		cubePositions.add(new Vector3f(0,-10,-40));
		cubePositions.add(new Vector3f(0,0,-80));
		cubePositions.add(new Vector3f(0,-5,-120));
		cubePositions.add(new Vector3f(0,8,-160));
		cubePositions.add(new Vector3f(0,-2,-200));
		
		cubePos = cubePositions.remove(0);
	}
	
	private Vector3f calculateSpeedVector(){
		//Vector3f diff = new Vector3f(0,0,0);
		Vector3f posChange = new Vector3f(0.0f,0.0f,0.0f);
		Vector3f.sub(currentPosition, prevPosition, posChange);
		if (dt != 0)
			posChange.scale(1/this.dt);
		return posChange;
	}
	
	/**
	 * Saves data in old... Variables
	 * 
	 * Position
	 * Elapsed Time
	 * Heading?
	 */
	private void saveData() {
		prevPosition = currentPosition;
		prevElapsedTime = elapsedTime;
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
	
	private float getVerAngle(){
		float overstaande = cubePos.getY() - this.currentPosition.getY();
		float aanliggende = cubePos.getZ() - this.currentPosition.getZ();
		return (float) Math.atan(overstaande/aanliggende);
	}
	
	private float getHorAngle(){
		float overstaande = cubePos.getX() - this.currentPosition.getX();
		float aanliggende = cubePos.getZ() - this.currentPosition.getZ();
		return (float) Math.atan(overstaande/aanliggende);
	}
	
	private float maxAOAWings(){
		
	}
	
	private float maxAOAVertStab(){
		
	}
	
	private float maxAOAHorStab(){
		
	}
	
	private float getEuclidDist(Vector3f vec1, Vector3f vec2){
		Vector3f temp = new Vector3f(0,0,0);
		Vector3f.sub(vec1, vec2, temp);
		return temp.length();
	}
	
	@Override
	public AutopilotOutputs timePassed(AutopilotInputs inputs) {
		this.inputAP = inputs;
		if (this.inputAP.getElapsedTime() > 0.0000001) {
			
			currentPosition = new Vector3f(inputAP.getX(), inputAP.getY(), inputAP.getZ());
			this.dt = inputs.getElapsedTime() - prevElapsedTime;
			prevElapsedTime = inputs.getElapsedTime();
			
			
			//load Current Orientation Of Drone
			setHeading(inputAP.getHeading());
			setPitch(inputAP.getPitch());
			setRoll(inputAP.getRoll());
			
			newHorStabInclination += pidHorGoal.calculateChange(inputAP.getPitch() + getVerAngle(), dt);
			if(newHorStabInclination > Math.PI/6) newHorStabInclination = (float) (Math.PI/6);
			else if(newHorStabInclination < - Math.PI/6) newHorStabInclination = (float) -(Math.PI/6);

			newVerStabInclination += pidVerGoal.calculateChange(inputAP.getHeading() - getHorAngle(), dt);
			if(newVerStabInclination > Math.PI/6) newVerStabInclination = (float) (Math.PI/6);
			else if(newVerStabInclination < - Math.PI/6) newVerStabInclination = (float) -(Math.PI/6);
			
			//CUBE REACHED
			if(getEuclidDist(this.currentPosition,cubePos) <= 4 && !cubePositions.isEmpty()){
				this.cubePos = cubePositions.remove(0);
			}
			
			//THRUST FORCE
		      if (this.calculateSpeedVector().length() > 20) { 
		          newThrust = 0; 
		      } else { 
		          if (Math.abs(newVerStabInclination) > 0.1) { 
		        	  newThrust = configAP.getMaxThrust() / 4; 
		          } else { 
		        	  this.newThrust = configAP.getMaxThrust(); 
		          } 
		      } 
			
		      //SAVE DATA
		      this.prevPosition = new Vector3f(currentPosition.x, currentPosition.y, currentPosition.z); 
		}
		
		return this;
	}

	@Override
	public void simulationEnded() {
		//Do nothing?
	}

	/*
	 * (non-Javadoc)
	 * @see interfaces.AutopilotOutputs
	 * 
	 * Zo kunnen we this returnen ipv altijd new AutopilotOutputs
	 */
	
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
	
}