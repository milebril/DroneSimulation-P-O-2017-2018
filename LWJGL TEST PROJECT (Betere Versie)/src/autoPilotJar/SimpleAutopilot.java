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
import org.lwjgl.util.vector.Matrix3f;
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
		
		Matrix3f currentOrientation = getCurrentOrientation();
		
		Matrix3f prevOrientation = this.getPrevOrientation();
		
		//oppassen want 4x4 matrix is niet zomaar inverteerbaar om tggestelde orientatie te krijgen
		prevOrientation.transpose(); 
		Matrix3f diff = new Matrix3f();
		
		Matrix3f.mul(currentOrientation, prevOrientation, diff);
		
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
		
		result[3] /= this.dt;
		
		Vector3f speed = new Vector3f(result[0], result[1], result[2]);
		speed.scale(result[3]);
		
		return speed;
	}

	private void setCurrentOrientation() {
		
		// de huidige doorschuiven
		prevOrientation = this.currentOrientation;
		//lwjgl matrix
		Matrix3f currentOrientation = new Matrix3f();
		//pitchrotatie
		Matrix3f xRot = new Matrix3f();
		xRot.m11 = (float) Math.cos(currentPitch);
		xRot.m22 = (float) Math.cos(currentPitch);
		xRot.m21 = (float) - Math.sin(currentPitch);
		xRot.m12 = (float) Math.sin(currentPitch);
		//headingrotatie rond y
		Matrix3f yRot = new Matrix3f();
		yRot.m00 = (float) Math.cos(currentHeading);
		yRot.m22 = (float) Math.cos(currentHeading);
		yRot.m20 = (float) Math.sin(currentHeading);
		yRot.m02 = (float) - Math.sin(currentHeading);
		//roll rond z-as
		Matrix3f zRot = new Matrix3f();
		zRot.m00 = (float) Math.cos(currentRoll);
		zRot.m11 = (float) Math.cos(currentRoll);
		zRot.m10 = (float) - Math.sin(currentRoll);
		zRot.m01 = (float) Math.sin(currentRoll);
		
		Matrix3f temp = new Matrix3f();
		Matrix3f.mul(zRot, xRot, temp);
		Matrix3f.mul(temp, yRot, currentOrientation);
		
		// de nieuwe setten
		this.currentOrientation = currentOrientation;
	}
	
	private Matrix3f currentOrientation = new Matrix3f();
	public Matrix3f getCurrentOrientation() {
		return currentOrientation;
	}

	public Matrix3f getPrevOrientation() {
		return prevOrientation;
	}

	private Matrix3f prevOrientation = new Matrix3f();
	
	/**
	 *  We define its angle of attack as -atan2(S . N, S . A), where S
	 * is the projected airspeed vector, N is the normal, and A is the attack vector
	 * @param inclination
	 * @param rotationAxis : in drone frame	
	 * @param wingCentreOfMass: in drone frame
	 * @param attackVectorDroneFrame: in drone frame
	 * @return the current estimated aoa of the left wing with given inclination
	 */
	private float getAOA(float inclination, Vector3f rotationAxis, Vector3f wingCentreOfMass, Vector3f attackVectorDroneFrame){

		Vector3f wingRotationSpeed = new Vector3f();
		//de hefboomsafstand in wereldassenstelsel
		Vector3f leftWingLever = new Vector3f();
		Matrix3f.transform(this.getCurrentOrientation(), wingCentreOfMass, leftWingLever);
		// v_rot = omega x hefboomstafstand (in Wereldassenstelsel)
		Vector3f.cross(this.getCurrentRotationSpeed(), leftWingLever , wingRotationSpeed);
		//totale snelheid is de som van de rotatie en de drone snelheid
		Vector3f totalSpeed = new Vector3f();
		Vector3f.add(this.calculateSpeedVector(), wingRotationSpeed, totalSpeed); 
		
		//The left wing's attack vector is (0, sin(leftWingInclination), -cos(leftWingInclination)
		Vector3f attackVector = new Vector3f();
		Matrix3f.transform(this.getCurrentOrientation(), attackVectorDroneFrame, attackVector);
		
		//We define an airfoil's normal as the cross product of its axis vector and its attack vector.
		Vector3f normal = new Vector3f();
		Vector3f.cross(rotationAxis, attackVector, normal);
		
		Vector3f projAirspeedVector = new Vector3f();
		//We define an airfoil's projected airspeed vector as its airspeed vector (its
//		velocity minus the wind velocity) projected onto the plane perpendicular to its
//		axis vector.
		Vector3f.sub(totalSpeed, (Vector3f) rotationAxis.scale(Vector3f.dot(totalSpeed, rotationAxis)), projAirspeedVector);
		
		float aoa = - (float) Math.atan2(Vector3f.dot(projAirspeedVector, normal), Vector3f.dot(projAirspeedVector, attackVector));
		
		return aoa;		
	}
	
	private Vector3f oldSpeed;
	
	//Aanpassen als we naar nieuwe cubus moeten gaan
	private Vector3f cubePos;
	private List<Vector3f> cubePositions = new ArrayList<>();
	
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
//	
//	 The left wing's attack vector is (0, sin(leftWingInclination), -cos(leftWingInclination)).
//	 - The right wing's attack vector is (0, sin(rightWingInclination), -cos(rightWingInclination)).
//	 - The horizontal stabilizer's attack vector is (0, sin(horStabInclination), -cos(horStabInclination)).
//	 - The vertical stabilizer's attack vector is (-sin(verStabInclination), 0, -cos(verStabInclination)).

//	- The axis vector of both wings and of the horizontal stabilizer is (1, 0, 0).
//	- The axis vector of the vertical stabilizer is (0, 1, 0).
	
//	 the left wing is at (-wingX, 0, 0)
//	 the right wing is at (wingX, 0, 0)
//	 - the horizontal and vertical stabilizers are at (0, 0, tailSize)
	//TODO de eerste iteratie is de heading volgens z-as ipv negatieve z-as

	private float getMaxInclinationLeftWing(){
		
		Vector3f rotationAxis = new Vector3f(1, 0, 0);
		Vector3f wingCentreOfMass = new Vector3f(-this.configAP.getWingX(), 0, 0);
		float inclination = 0;
		
		float maxAOA = configAP.getMaxAOA();
		
		//eerst ruwe benadering van de maximale inclination maken zonder rotatiesnelheid en benaderingen van cos en sin
		inclination =  (float) ((-	this.calculateSpeedVector().y + this.calculateSpeedVector().z*Math.tan(maxAOA))/
						 			this.calculateSpeedVector().z + this.calculateSpeedVector().y*Math.tan(maxAOA));		

		Vector3f attackVectorDroneFrame = new Vector3f(0.0f, (float)Math.sin(inclination), - (float)Math.cos(inclination));		
		float aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
		
		//checken of de aoa al in het gewenste interval ligt.
		// zo nee: vergroot de inclinatie of verklein de inclinatie, bereken opnieuw de angle of attack en voer lus opnieuw uit
		while((aoa < 0.9 *maxAOA)|| (aoa > maxAOA)){
			if (aoa > maxAOA){
				inclination -= Math.abs(aoa- maxAOA);
			}
			else{
				inclination += Math.abs(aoa - maxAOA);
			}
			
			attackVectorDroneFrame = new Vector3f(0.0f, (float)Math.sin(inclination), - (float)Math.cos(inclination));
			aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
		}
		
		return inclination;
	}
	
	private float getMaxInclinationRightWing(){
		
		Vector3f rotationAxis = new Vector3f(1, 0, 0);
		Vector3f wingCentreOfMass = new Vector3f(this.configAP.getWingX(), 0, 0);
		float inclination = 0;
		
		float maxAOA = configAP.getMaxAOA();
		
		//eerst ruwe benadering van de maximale inclination maken zonder rotatiesnelheid en benaderingen van cos en sin
		inclination =  (float) ((-	this.calculateSpeedVector().y + this.calculateSpeedVector().z*Math.tan(maxAOA))/
						 			this.calculateSpeedVector().z + this.calculateSpeedVector().y*Math.tan(maxAOA));		

		Vector3f attackVectorDroneFrame = new Vector3f(0.0f, (float)Math.sin(inclination), - (float)Math.cos(inclination));		
		float aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
		
		//checken of de aoa al in het gewenste interval ligt.
		// zo nee: vergroot de inclinatie of verklein de inclinatie, bereken opnieuw de angle of attack en voer lus opnieuw uit
		while((aoa < 0.9 *maxAOA)|| (aoa > maxAOA)){
			if (aoa > maxAOA){
				inclination -= Math.abs(aoa- maxAOA);
			}
			else{
				inclination += Math.abs(aoa - maxAOA);
			}
			
			attackVectorDroneFrame = new Vector3f(0.0f, (float)Math.sin(inclination), - (float)Math.cos(inclination));
			aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
		}
		
		return inclination;
	}
	
	private float getMaxInclinationVertStab(){	
//		 - the horizontal and vertical stabilizers are at (0, 0, tailSize)
//		 - The vertical stabilizer's attack vector is (-sin(verStabInclination), 0, -cos(verStabInclination)).
//		The axis vector of the vertical stabilizer is (0, 1, 0).		

		Vector3f rotationAxis = new Vector3f(0, 1, 0);
		Vector3f wingCentreOfMass = new Vector3f(0, 0, this.configAP.getTailSize());
		float inclination = 0;
		
		float maxAOA = configAP.getMaxAOA();
		
		Vector3f attackVectorDroneFrame = new Vector3f(- (float)Math.sin(inclination), 0f, - (float)Math.cos(inclination));		
		float aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
		
		//checken of de aoa al in het gewenste interval ligt.
		// zo nee: vergroot de inclinatie of verklein de inclinatie, bereken opnieuw de angle of attack en voer lus opnieuw uit
		while((aoa < 0.9 *maxAOA)|| (aoa > maxAOA)){
			if (aoa > maxAOA){
				inclination -= Math.abs(aoa- maxAOA);
			}
			else{
				inclination += Math.abs(aoa - maxAOA);
			}
			
			attackVectorDroneFrame = new Vector3f(- (float)Math.sin(inclination), 0f, - (float)Math.cos(inclination));		
			aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
		}
		
		return inclination;
		
	}
	
	private float getMaxInclinationHorStab(){
		//		 - the horizontal and vertical stabilizers are at (0, 0, tailSize)
//		- The axis vector of both wings and of the horizontal stabilizer is (1, 0, 0).
//		 - The horizontal stabilizer's attack vector is (0, sin(horStabInclination), -cos(horStabInclination)).

		Vector3f rotationAxis = new Vector3f(1, 0, 0);
		Vector3f wingCentreOfMass = new Vector3f(0, 0, this.configAP.getTailSize());
		float inclination = 0;
		
		float maxAOA = configAP.getMaxAOA();
		
		Vector3f attackVectorDroneFrame = new Vector3f(0f, (float)Math.sin(inclination), - (float)Math.cos(inclination));		
		float aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
		
		//checken of de aoa al in het gewenste interval ligt.
		// zo nee: vergroot de inclinatie of verklein de inclinatie, bereken opnieuw de angle of attack en voer lus opnieuw uit
		while((aoa < 0.9 *maxAOA)|| (aoa > maxAOA)){
			if (aoa > maxAOA){
				inclination -= Math.abs(aoa- maxAOA);
			}
			else{
				inclination += Math.abs(aoa - maxAOA);
			}
			
			attackVectorDroneFrame = new Vector3f(0f, (float)Math.sin(inclination), - (float)Math.cos(inclination));		
			aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
		}
		
		return inclination;		
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
			setCurrentOrientation();
			
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