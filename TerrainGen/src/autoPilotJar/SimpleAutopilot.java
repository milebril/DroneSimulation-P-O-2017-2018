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
import path.MyPath;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;

public class SimpleAutopilot implements Autopilot, AutopilotOutputs {
	
	private List<Vector3f> cubePositions = new ArrayList<>();
	private MyPath path;
	
	public SimpleAutopilot() {
		float[] pathX = {  5,   0,  -5, -10,  -5};
		float[] pathY = {  20,   20,   20,   20,   20};
		float[] pathZ = {-80,-160,-240,-320,-400};
		this.path = new MyPath(pathX,pathY,pathZ);
		this.path.setIndex(0);
		
		this.cubePos = new Vector3f(path.getCurrentX(), path.getCurrentY(),path.getCurrentZ());
		
		//Initialize PIDController for horizontalflight
		//PIDController(float K-Proportional, float K-Integral, float K-Derivative, float changeFactor, float goal)
		this.pidHorStab = new PIDController(1.0f,0.0f,1.0f, (float) (Math.PI / 180), 0);
		this.pidVerStab = new PIDController(2.5f,0.0f,2.0f, (float) (Math.PI / 180), 0);
		
		//PID for Roll (als we dat ooit gaan gebruiken)
		//this.pidWings = new PIDController(1.0f,0.0f,5.0f,(float) Math.toRadians(1),0);
        this.pidRoll = new PIDController(0.0f,0.5f,1.0f,(float) Math.toRadians(1),0);

		
		//Initialize AP with configfile TODO: mag deze lijn weg?
		
		//Initialize PIDController for Thrust
		//PIDController(float K-Proportional, float K-Integral, float K-Derivative, float changeFactor, float goal)
		//this.pidThrust = new PIDController(1.0f, 0.0f, 3.0f, -10, 10);
	}
	
	
	private static float INCLINATIONINCREMENT = 0.01f;
	
	private boolean heightGoalReached = false;
	
	
	// Autopilot communication
	
	/**
	 * The Autopilot config
	 */
	private AutopilotConfig configAP;

	/**
	 * Getter for the Autopilot config
	 */
	public AutopilotConfig getConfig() {
		return this.configAP;
	}
	
	/**
	 * The Autopilot input
	 */
	private AutopilotInputs inputAP;
	
	/**
	 * Getter for the Autopilot input
	 */
	public AutopilotInputs getInput() {
		return this.inputAP;
	}
	
	
	@Override
	public AutopilotOutputs timePassed(AutopilotInputs inputs) {
		this.inputAP = inputs;
		//System.out.println("Roll: " + inputs.getRoll());
		
		if (this.inputAP.getElapsedTime() > 0.0001) {
			setDroneProperties(inputs);
			//System.out.println("Goal: " + this.cubePos);
			//Set the horizontal stabilizer inclination
			newHorStabInclination += pidHorStab.calculateChange(inputAP.getPitch() + getVerAngle(), getProperties().getDeltaTime());
			if(newHorStabInclination > Math.PI/6) newHorStabInclination = (float) (Math.PI/6);
			else if(newHorStabInclination < - Math.PI/6) newHorStabInclination = (float) -(Math.PI/6);
			//System.out.println("Inclination horizontal stabiliser: " + newHorStabInclination);
			
			//Set the vertical stabilizer inclination
			newVerStabInclination += pidVerStab.calculateChange(inputAP.getHeading() - getHorAngle(), getProperties().getDeltaTime());
			if(newVerStabInclination > Math.PI/6) newVerStabInclination = (float) (Math.PI/6);
			else if(newVerStabInclination < - Math.PI/6) newVerStabInclination = (float) -(Math.PI/6);
			
			//Set the wing inclination
			//newLeftWingInclination = (float) Math.toRadians(4); //met een inclination van 4graden stijgt hij 5 meter over 200 meter
//			newLeftWingInclination += pidRoll.calculateChange(inputAP.getRoll(), getProperties().getDeltaTime());
//			if(newLeftWingInclination > Math.PI/6) newLeftWingInclination = (float) (Math.PI/6);
//			else if(newLeftWingInclination < - Math.PI/6) newLeftWingInclination = (float) -(Math.PI/6);
			
			//newRightWingInclination = (float) Math.toRadians(4);
			
			//Set the thrust force
			//if (getProperties().getVelocity().length() > 60) //als de drone sneller vliegt dan 60m/s zet de thrust dan uit
	        //  this.newThrust = 0;
			//else
	    		this.newThrust = configAP.getMaxThrust();
			
			System.out.println("Velocity: " + getProperties().getVelocity().length());
		//	System.out.println("Thrust: " + newThrust);
			
//			cubePositions = cubeLocator.getCoordinatesOfCube();
//			cubePositions.sort(new Comparator<Vector3f>() {
//				@Override
//				public int compare(Vector3f o1, Vector3f o2) {
//					return -Float.compare(o1.z, o2.z);
//				}
//			});
			
			//Lock next target
//			if (cubePositions.size() > 0) {
//				Vector3f temp = cubePositions.get(0);
//				if ((int) (temp.z / -40) > blockCount) {
//					blockCount++;
//					cubePos = new Vector3f(Math.round(temp.x), Math.round(temp.y), ((int) (temp.z / 40)) * 40);
//					System.out.println("Schatting: " + cubePos);
//					System.out.println("Z POS: " + getProperties().getPosition().z);
//					System.out.println(inputAP.getElapsedTime());
//					System.out.println(blockCount);
//				}
								
//				if (!lockedOnTarget && getEuclidDist(getProperties().getPosition(), cubePos) <= 15) {
//					lockedOnTarget = true;
//					cubePos = new Vector3f((cubePositions.get(0).x + cubePos.x) / 2f, 
//							(cubePositions.get(0).y + cubePos.y) / 2f, ((int) (cubePos.z / 40)) * 40 ) ;
//					cubePos = cubePositions.get(0); 
//		          	cubePos.z = ((int) (cubePos.z / 40)) * 40; 
//					System.out.println("Lock: " + cubePos);
//				}
//			} 
			
//			if (cubePositions.size() > 0) {
//				cubePos = cubePositions.get(0);
//			}
			
			//CUBE REACHED
			System.out.println("Size: " + this.cubePositions.size());
			if(getEuclidDist(getProperties().getPosition(),cubePos) <= 4){
				this.path.setIndex(this.path.getIndex() + 1);
				this.cubePos = new Vector3f(path.getCurrentX(), path.getCurrentY(), path.getCurrentZ());
//				this.cubePos = stubCube.translate(0, 0, -40);
//				lockedOnTarget = false;
				this.pidVerStab.reset();
//	            this.pidWings.reset();
			}
			
			//REMOVE THIS AFTER TESTING:
			//this.newThrust = configAP.getMaxThrust();
		}

		return this;
	}
	
	@Override
	public AutopilotOutputs simulationStarted(AutopilotConfig config, AutopilotInputs inputs) {
		this.configAP = config;
		this.inputAP = inputs;
		
		cubeLocator = new ImageProcessor(this);
		
		return this;
	}
	
	
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

		Vector3f wingRotationSpeed = new Vector3f();
		//de hefboomsafstand in wereldassenstelsel
		Vector3f leftWingLever = new Vector3f();
		Matrix3f.transform(getProperties().getOrientationMatrix(), wingCentreOfMass, leftWingLever);
		// v_rot = omega x hefboomstafstand (in Wereldassenstelsel)
		Vector3f.cross(getProperties().getRotationSpeed(), leftWingLever , wingRotationSpeed);
		//totale snelheid is de som van de rotatie en de drone snelheid
		Vector3f totalSpeed = new Vector3f();
		Vector3f.add(getProperties().getVelocity(), wingRotationSpeed, totalSpeed); 
		
		
		//The left wing's attack vector is (0, sin(leftWingInclination), -cos(leftWingInclination)
		Vector3f attackVector = new Vector3f();
		Matrix3f.transform(getProperties().getOrientationMatrix(), attackVectorDroneFrame, attackVector);
		
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
	
	
	//Aanpassen als we naar nieuwe cubus moeten gaan
	private Vector3f stubCube = new Vector3f(0, 0, -40);
	private Vector3f cubePos = stubCube;
	
	private float heightGoal = 1;
	
	private ImageProcessor cubeLocator;
	private PIDController pidHorStab;
	private PIDController pidHorWing;
	private PIDController pidHorGoal;
	private PIDController pidVerStab;
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
	
	

	
	private float getVerAngle(){
		float overstaande = cubePos.getY() - getProperties().getPosition().getY();
		float aanliggende = cubePos.getZ() - getProperties().getPosition().getZ();
		return (float) Math.atan(overstaande/aanliggende);
	}
	
	private float getHorAngle(){
		float overstaande = cubePos.getX() - getProperties().getPosition().getX();
		float aanliggende = cubePos.getZ() - getProperties().getPosition().getZ();
		return (float) Math.atan(overstaande/aanliggende);
	}
	
//	
//	 - The left wing's attack vector is (0, sin(leftWingInclination), -cos(leftWingInclination)). => rot axis = (1 , 0, 0)
//	 - The right wing's attack vector is (0, sin(rightWingInclination), -cos(rightWingInclination)). => rot axis = (1 , 0, 0)
//	 - The horizontal stabilizer's attack vector is (0, sin(horStabInclination), -cos(horStabInclination)). => rot axis = (1 , 0, 0)
//	 - The vertical stabilizer's attack vector is (-sin(verStabInclination), 0, -cos(verStabInclination)).  => rot axis = (0 , 1, 0)

//	- The axis vector of both wings and of the horizontal stabilizer is (1, 0, 0).
//	- The axis vector of the vertical stabilizer is (0, 1, 0).
	
//	 the left wing is at (-wingX, 0, 0)
//	 the right wing is at (wingX, 0, 0)
//	 - the horizontal and vertical stabilizers are at (0, 0, tailSize)
	//TODO de eerste iteratie is de heading volgens z-as ipv negatieve z-as
	
	
	private float getMaxInclination(Vector3f rotationAxis, Vector3f wingCentreOfMass) {
		float maxInclination;
		
		
		return 0;
	}
	
	private float getMaxInclinationLeftWing(){
		
		Vector3f rotationAxis = new Vector3f(1, 0, 0);
		Vector3f wingCentreOfMass = new Vector3f(-this.configAP.getWingX(), 0, 0);
		float inclination = 0;

		float maxAOA = configAP.getMaxAOA();
		
		//eerst ruwe benadering van de maximale inclination maken zonder rotatiesnelheid en benaderingen van cos en sin
		inclination =  (float) ((-	getProperties().getVelocity().y + getProperties().getVelocity().z*Math.tan(maxAOA))/
								getProperties().getVelocity().z + getProperties().getVelocity().y*Math.tan(maxAOA));		

		float aoa = Float.NaN;
		
		//checken of de aoa al in het gewenste interval ligt.
		// zo nee: vergroot de inclinatie of verklein de inclinatie, bereken opnieuw de angle of attack en voer lus opnieuw uit
		if(getProperties().getVelocity().length() == 0){
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
		inclination =  (float) ((-	getProperties().getVelocity().y + getProperties().getVelocity().z*Math.tan(maxAOA))/
				getProperties().getVelocity().z + getProperties().getVelocity().y*Math.tan(maxAOA));		

		float aoa = Float.NaN;
		
		//checken of de aoa al in het gewenste interval ligt.
		// zo nee: vergroot de inclinatie of verklein de inclinatie, bereken opnieuw de angle of attack en voer lus opnieuw uit
		if(getProperties().getVelocity().length() == 0){
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
		if(getProperties().getVelocity().length() == 0){
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
		if(getProperties().getVelocity().length() == 0){
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
	
	// DRONE PROPERTIES
	
	/**
	 * Value class in which the properties of the drone are saved
	 */
	private DroneProperties properties = new DroneProperties();
	
	/**
	 * Returns the value class in which the properties of the drone are saved
	 */
	public DroneProperties getProperties() {
		return this.properties;
	}
	
	/**
	 * Updates the drone properties and previous drone properties according to the given data.
	 */
	private void setDroneProperties(AutopilotInputs inputs) {
		DroneProperties previousProperties = getProperties();
		DroneProperties properties = new DroneProperties(inputs, previousProperties);
		
		this.previousProperties = previousProperties;
		this.properties = properties;
	}
	
	/**
	 * Value class in which the previous properties of the drone are saved
	 */
	private DroneProperties previousProperties = new DroneProperties();
	
	/**
	 * Returns the value class in which the previous properties of the drone are saved
	 */
	public DroneProperties getPreviousProperties() {
		return this.previousProperties;
	}
	
	/**
	 * A class for saving the properties of the drone. Used for saving the properties of
	 * the current and previous iteration.
	 *
	 */
	class DroneProperties {
		
		public DroneProperties(AutopilotInputs inputs, DroneProperties previousProperties) {
			// save give properties
			this.image = inputs.getImage();
			this.elapsedTime = inputs.getElapsedTime();
			this.position = new Vector3f(inputs.getX(), inputs.getY(), inputs.getZ());
			this.heading = inputs.getHeading();
			this.pitch = inputs.getPitch();
			this.roll = inputs.getRoll();
			
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
			System.out.println("div: " + diff );
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

	@Override
	public float getFrontBrakeForce() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public float getLeftBrakeForce() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public float getRightBrakeForce() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}