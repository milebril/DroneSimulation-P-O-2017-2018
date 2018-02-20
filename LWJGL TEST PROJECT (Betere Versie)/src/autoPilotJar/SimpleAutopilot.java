package autoPilotJar;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;
import java.util.Comparator;
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
	
	private static float INCLINATIONINCREMENT = 0.1f;
	
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
	 * @param copyRotationAxis : in drone frame	
	 * @param wingCentreOfMass: in drone frame
	 * @param attackVectorDroneFrame: in drone frame
	 * @return the current estimated aoa of the left wing with given inclination
	 */
	private float getAOA(float inclination, Vector3f rotationAxis, Vector3f wingCentreOfMass, Vector3f attackVectorDroneFrame){
		
		Vector3f copyRotationAxis = new Vector3f(rotationAxis.x, rotationAxis.y, rotationAxis.z);
		
//		System.out.println("getAOA inclination : " + inclination);
//		System.out.println("getAOA rotationAxis : " + copyRotationAxis);
//		System.out.println("getAOA wingCentreOfMass : " + wingCentreOfMass);
//		System.out.println("getAOA attackVectorDroneFrame : " + attackVectorDroneFrame);		
//		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
		Vector3f.cross(copyRotationAxis, attackVector, normal);
		
		Vector3f projAirspeedVector = new Vector3f();
		//We define an airfoil's projected airspeed vector as its airspeed vector (its
//		velocity minus the wind velocity) projected onto the plane perpendicular to its
//		axis vector.
//		System.out.println("rotation axis VOOR SCALE: " + copyRotationAxis);

		Vector3f.sub(totalSpeed, (Vector3f) copyRotationAxis.scale(Vector3f.dot(totalSpeed, copyRotationAxis)), projAirspeedVector);
		
//		System.out.println("rotation axis NA SCALE: " + copyRotationAxis);

//		System.out.println("getAOA projAirspeedVector: " + projAirspeedVector);
//		System.out.println("getAOA totalSpeed: " + totalSpeed);
//		System.out.println("getAOA normal: " + normal);
//		System.out.println("getAOA projAirspeedVector: " + projAirspeedVector);
//		System.out.println("getAOA attackVector: " + attackVector);
		
		float aoa = - (float) Math.atan2(Vector3f.dot(projAirspeedVector, normal), Vector3f.dot(projAirspeedVector, attackVector));
		
		return aoa;		
	}
	
	private Vector3f oldSpeed;
	
	//Aanpassen als we naar nieuwe cubus moeten gaan
	private Vector3f stubCube = new Vector3f(0, 0, -40);
	private Vector3f cubePos = stubCube;
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
	private PIDController pidWings;
	private PIDController pidRoll;
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
		//this.pidHorWing = new PIDController(1.0f,0.0f,10.0f, (float) -(Math.PI / 180), 0);
		//this.pidHorStab = new PIDController(2.0f,1.0f,10.0f, (float) (Math.PI / 180), 0);
		//this.pidHorGoal = new PIDController(1.0f,0.0f,0.5f, (float) (Math.PI / 180), 0);
		
		//this.pidHorStab = new PIDController(2.0f,1.0f,10.0f, (float) (Math.PI / 180), 0);
		this.pidHorStab = new PIDController(1.0f,0.0f,0.5f, (float) (Math.PI / 180), 0);
		this.pidVerGoal = new PIDController(2.0f,0.0f,1.0f, (float) (Math.PI / 180), 0);
		
		
		this.pidWings = new PIDController(1.0f,0.0f,5.0f,(float) Math.toRadians(1),0);
        this.pidRoll = new PIDController(5.0f,0.0f,10.0f,(float) Math.toRadians(1),0);

		
		//Initialize AP with configfile
		
		
		
		//Initialize PIDController for Thrust
		//PIDController(float K-Proportional, float K-Integral, float K-Derivative, float changeFactor, float goal)
		this.pidThrust = new PIDController(1.0f, 0.0f, 3.0f, -10, 10);
		//initialize();
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
		
		cubeLocator = new ImageProcessor(this);
		
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

		float aoa = Float.NaN;
		
		//checken of de aoa al in het gewenste interval ligt.
		// zo nee: vergroot de inclinatie of verklein de inclinatie, bereken opnieuw de angle of attack en voer lus opnieuw uit
		if(this.calculateSpeedVector().length() == 0){
			aoa = maxAOA;
		}
		else{
			
			Vector3f attackVectorDroneFrame = new Vector3f(0.0f, (float)Math.sin(inclination), - (float)Math.cos(inclination));		
			aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
			
			while((aoa < 0.9 *maxAOA)|| (aoa > maxAOA)){
				//System.out.println("getMaxInclinationHorStab maxaoa:  " + maxAOA);
				//System.out.println("getMaxInclinationHorStab current inclination:  " + inclination);;
				if (aoa > maxAOA){
					inclination -= INCLINATIONINCREMENT;
				}
				else{
					inclination += INCLINATIONINCREMENT;
				}
				//System.out.println("getMaxInclinationHorStab current rotationAxis:  " + rotationAxis);
	
				attackVectorDroneFrame = new Vector3f(0.0f, (float)Math.sin(inclination), - (float)Math.cos(inclination));		
				aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
			}
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

		float aoa = Float.NaN;
		
		//checken of de aoa al in het gewenste interval ligt.
		// zo nee: vergroot de inclinatie of verklein de inclinatie, bereken opnieuw de angle of attack en voer lus opnieuw uit
		if(this.calculateSpeedVector().length() == 0){
			aoa = maxAOA;
		}
		else{
			
			Vector3f attackVectorDroneFrame = new Vector3f(0.0f, (float)Math.sin(inclination), - (float)Math.cos(inclination));	
			aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
			
			while((aoa < 0.9 *maxAOA)|| (aoa > maxAOA)){
				//System.out.println("getMaxInclinationHorStab maxaoa:  " + maxAOA);
				//System.out.println("getMaxInclinationHorStab current inclination:  " + inclination);;
				if (aoa > maxAOA){
					inclination -= INCLINATIONINCREMENT;
				}
				else{
					inclination += INCLINATIONINCREMENT;
				}
				//System.out.println("getMaxInclinationHorStab current rotationAxis:  " + rotationAxis);
				attackVectorDroneFrame = new Vector3f(0.0f, (float)Math.sin(inclination), - (float)Math.cos(inclination));		
				aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
			}
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
		float aoa = Float.NaN;
		
		//checken of de aoa al in het gewenste interval ligt.
		// zo nee: vergroot de inclinatie of verklein de inclinatie, bereken opnieuw de angle of attack en voer lus opnieuw uit
		if(this.calculateSpeedVector().length() == 0){
			aoa = maxAOA;
		}
		else{
			
			Vector3f attackVectorDroneFrame = new Vector3f(- (float)Math.sin(inclination), 0f, - (float)Math.cos(inclination));	
			aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
			
			while((aoa < 0.9 *maxAOA)|| (aoa > maxAOA)){
				//System.out.println("getMaxInclinationHorStab maxaoa:  " + maxAOA);
			    //System.out.println("getMaxInclinationHorStab current inclination:  " + inclination);;
				if (aoa > maxAOA){
					inclination -= INCLINATIONINCREMENT;
				}
				else{
					inclination += INCLINATIONINCREMENT;
				}
				//System.out.println("getMaxInclinationHorStab current rotationAxis:  " + rotationAxis);
	
				attackVectorDroneFrame = new Vector3f(- (float)Math.sin(inclination), 0f, - (float)Math.cos(inclination));		
				aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
			}
		}
		return inclination;
		
	}
	
	private float getMaxInclinationHorStab(){
		//		 - the horizontal and vertical stabilizers are at (0, 0, tailSize)
//		- The axis vector of both wings and of the horizontal stabilizer is (1, 0, 0).
//		 - The horizontal stabilizer's attack vector is (0, sin(horStabInclination), -cos(horStabInclination)).

//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//System.out.println("getmaxincl");
		
		Vector3f rotationAxis = new Vector3f(1, 0, 0);
		Vector3f wingCentreOfMass = new Vector3f(0, 0, this.configAP.getTailSize());
		float inclination = 0;
		
		float maxAOA = configAP.getMaxAOA();
		float aoa = Float.NaN;
		
		//checken of de aoa al in het gewenste interval ligt.
		// zo nee: vergroot de inclinatie of verklein de inclinatie, bereken opnieuw de angle of attack en voer lus opnieuw uit
		if(this.calculateSpeedVector().length() == 0){
			aoa = maxAOA;
		}
		else{
			
			Vector3f attackVectorDroneFrame = new Vector3f(0f, (float)Math.sin(inclination), - (float)Math.cos(inclination));		
			aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
			int count = 0;
			while((aoa < 0.9 *maxAOA)|| (aoa > maxAOA)){
				if (aoa > maxAOA){
					inclination -= INCLINATIONINCREMENT;
				}
				else{
					inclination += INCLINATIONINCREMENT;
				}
	
				//System.out.println(count++);
				attackVectorDroneFrame = new Vector3f(0f, (float)Math.sin(inclination), - (float)Math.cos(inclination));		
				aoa = this.getAOA(inclination, rotationAxis, wingCentreOfMass, attackVectorDroneFrame);
			}
		}
		
		//System.out.println("getMaxInclinationHorStab current aoa:  " + aoa);

		return inclination;		
	}
	
	private float getEuclidDist(Vector3f vec1, Vector3f vec2){
		Vector3f temp = new Vector3f(0,0,0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
	}
	
	private int blockCount = 0;
	private boolean lockedOnTarget = false;
	
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
			
			newHorStabInclination += pidHorStab.calculateChange(inputAP.getPitch() + getVerAngle(), dt);
			if(newHorStabInclination > Math.PI/6) newHorStabInclination = (float) (Math.PI/6);
			else if(newHorStabInclination < - Math.PI/6) newHorStabInclination = (float) -(Math.PI/6);

//			float maxHorStab = getMaxInclinationHorStab();
//			if (newHorStabInclination > maxHorStab) {
//				newHorStabInclination = maxHorStab;
//			} else if (newHorStabInclination < -maxHorStab) {
//				newHorStabInclination = -maxHorStab;
//			}
			
			newVerStabInclination += pidVerGoal.calculateChange(inputAP.getHeading() - getHorAngle(), dt);
			if(newVerStabInclination > Math.PI/6) newVerStabInclination = (float) (Math.PI/6);
			else if(newVerStabInclination < - Math.PI/6) newVerStabInclination = (float) -(Math.PI/6);
			
			System.out.println("VerStab: " + newVerStabInclination);
			System.out.println("HorStab: " + newHorStabInclination);

//			//ROLL PID
//			float changeWing = this.pidWings.calculateChange(inputAP.getHeading() - getHorAngle(), dt);
//				
//			this.newLeftWingInclination += changeWing;
//			if(this.newLeftWingInclination > Math.toRadians(20)) this.newLeftWingInclination = (float) Math.toRadians(20);
//			if(this.newLeftWingInclination < 0) this.newLeftWingInclination = 0;
//				
//			this.newRightWingInclination -= changeWing;
//			if(this.newRightWingInclination > Math.toRadians(20)) this.newRightWingInclination = (float) Math.toRadians(20);
//			if(this.newRightWingInclination < 0) this.newRightWingInclination = 0;
//
//			//Negatieve Roll (LeftWingInclination > RightWingInclination) -> NegatieveChangeWingRoll
//			if(Math.abs(this.inputAP.getRoll()) > Math.toRadians(10)){
//				float changeWingRoll = this.pidRoll.calculateChange(this.inputAP.getRoll(),dt);
//				//System.out.println("Roll | ChangeWingRoll : " + this.inputAP.getRoll() + " | " + changeWingRoll);
//				this.newLeftWingInclination += changeWingRoll;
//				if(this.newLeftWingInclination > Math.toRadians(20)) this.newLeftWingInclination = (float) Math.toRadians(20);
//				if(this.newLeftWingInclination < 0) this.newLeftWingInclination = 0;
//				
//				this.newRightWingInclination -= changeWingRoll;
//				if(this.newRightWingInclination > Math.toRadians(20)) this.newRightWingInclination = (float) Math.toRadians(20);
//				if(this.newRightWingInclination < 0) this.newRightWingInclination = 0;
//			}
			
			cubePositions = cubeLocator.getCoordinatesOfCube();
			cubePositions.sort(new Comparator<Vector3f>() {
				@Override
				public int compare(Vector3f o1, Vector3f o2) {
					return -Float.compare(o1.z, o2.z);
				}
			});
			
			//Lock next target
			if (cubePositions.size() > 0) {
				Vector3f temp = cubePositions.get(0);
				if ((int) (temp.z / -40) > blockCount) {
					blockCount++;
					cubePos = new Vector3f(Math.round(temp.x), Math.round(temp.y), ((int) (temp.z / 40)) * 40);
//					System.out.println("Schatting: " + cubePos);
//					System.out.println("Z POS: " + currentPosition.z);
//					System.out.println(inputAP.getElapsedTime());
//					System.out.println(blockCount);
				}
								
				if (!lockedOnTarget && getEuclidDist(currentPosition, cubePos) <= 15) {
					lockedOnTarget = true;
					cubePos = new Vector3f((cubePositions.get(0).x + cubePos.x) / 2f, 
							(cubePositions.get(0).y + cubePos.y) / 2f, ((int) (cubePos.z / 40)) * 40 ) ;
//					cubePos = cubePositions.get(0); 
//		          	cubePos.z = ((int) (cubePos.z / 40)) * 40; 
//					System.out.println("Lock: " + cubePos);
				}
			} 
			
//			if (cubePositions.size() > 0) {
//				cubePos = cubePositions.get(0);
//			}
			
			//CUBE REACHED
			if(getEuclidDist(this.currentPosition,cubePos) <= 4){
				this.cubePos = stubCube.translate(0, 0, -40);
				lockedOnTarget = false;
//				this.pidHorStab.reset();
//	            this.pidWings.reset();
			}
			
			//THRUST FORCE
	      if (this.calculateSpeedVector().length() > 30) { 
	          newThrust = 0; 
	      } else { 
//		          if (Math.abs(newVerStabInclination) > 0.1) { 
//		        	  newThrust = configAP.getMaxThrust() / 4; 
//		          } else { 
//	        	  this.newThrust = configAP.getMaxThrust(); 
//		          } 
	    	  
	    	this.newThrust = configAP.getMaxThrust();  
		  } 
	      //REMOVE THIS AFTER TESTING:
	      //this.newThrust = configAP.getMaxThrust();
	      
	      //SAVE DATA
	      this.prevPosition = new Vector3f(currentPosition.x, currentPosition.y, currentPosition.z); 
	      
	      this.newLeftWingInclination = (float) Math.toRadians(10);
	      this.newRightWingInclination = (float) Math.toRadians(10);
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