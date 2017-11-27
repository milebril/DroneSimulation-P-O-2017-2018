package autoPilotJar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.lwjgl.opengl.AMDBlendMinmaxFactor;
import org.lwjgl.util.vector.Vector3f;

import autopilot.AutopilotConfigReader;
import interfaces.Autopilot;
import interfaces.AutopilotConfig;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;
import openCV.ImageProcessor;
import openCV.RedCubeLocator;

public class SimpleAutopilot implements Autopilot, AutopilotOutputs{	
	
	private AutopilotConfig configAP;
	private AutopilotInputs inputAP;
	
	private Vector3f currentPosition;
	private Vector3f prevPosition;
	
	private Vector3f oldSpeed;
	
	//Aanpassen als we naar nieuwe cubus moeten gaan
	private Vector3f stablePosition;
	private Vector3f cubePos = new Vector3f(0,3,-10);
	
	//TODO ook heading bijhouden?.	
	
	private float elapsedTime;
	private float prevElapsedTime;
	private float dt = 0;
	
	private ImageProcessor cubeLocator;
	private PIDController pidHorizontalStabilisation;
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
		//this.pidHorizontalStabilisation = new PIDController(10.0f,1.0f,5.0f);
		this.pidHorizontalStabilisation = new PIDController(5.0f,0.0f,3.0f, (float) -(Math.PI / 180), 0);
		//Initialize AP with configfile
		
		//Initialize PIDController for Thrust
		//PIDController(float K-Proportional, float K-Integral, float K-Derivative, float changeFactor, float goal)
		this.pidThrust = new PIDController(1.0f, 0.0f, 3.0f, -10, 10);
		//initialize();
		
		stablePosition = new Vector3f(0, 0, 0);
	}
	
	/**
	 * Start of the simulation
	 * Sets AutopilotConfig
	 */
	@Override
	public AutopilotOutputs simulationStarted(AutopilotConfig config, AutopilotInputs inputs) {
		this.configAP = config;
		this.inputAP = inputs;
		
		return this;
	}

	/**
	 * Function called by the Testbed to get instructions from the autopilot
	 */
	@Override
	public AutopilotOutputs timePassed(AutopilotInputs inputs) {
		this.inputAP = inputs;
		if (this.inputAP.getElapsedTime() > 0.0000001) {
			//TODO Heb dit gekopieerd uit communicateWithDrone();
			
//			//Set Variables for this iteration
//			//cubeLocator = new ImageProcessor(this);
//			cubeLocator = new ImageProcessor(this);
//			
//			currentPosition = new Vector3f(inputAP.getX(), inputAP.getY(), inputAP.getZ());
//			//TODO Heading?
//			elapsedTime = inputAP.getElapsedTime();
//			dt = elapsedTime - prevElapsedTime;
//			
//			makeData();
//			
//			
////			newLeftWingInclination = newHorizontalInclinations[0];
////			newRightWingInclination = newHorizontalInclinations[1];
//			
//			//Save droneData we need in nextIteration
//			saveData();
			
			currentPosition = new Vector3f(inputAP.getX(), inputAP.getY(), inputAP.getZ());

			newLeftWingInclination = (float) ((float) 10*(Math.PI/180));
			newRightWingInclination = (float) ((float) 10*(Math.PI/180));
			
			newHorStabInclination = 0;
			newVerStabInclination = 0;
		}
		
		return this;
	}

	/**
	 * is called when the simulation Stops
	 */
	@Override
	public void simulationEnded() {
		//Do nothing?
	}
	
	private void makeData() {
		//Horizontal Wings
		float incChange = this.pidHorizontalStabilisation.calculateChange(currentPosition.y, this.dt);
		float[] wingChange = new float[] {incChange,incChange};
		
		if(newLeftWingInclination + wingChange[0] >= 1) newLeftWingInclination = (float) (1);
		else if(newLeftWingInclination + wingChange[0] <= -1) newLeftWingInclination = (float) -(1);	
		else newLeftWingInclination += wingChange[0];
		
		if(newRightWingInclination + wingChange[1] >= 1)  newRightWingInclination = (float) (1);
		else if(newRightWingInclination + wingChange[1] <= -1) newRightWingInclination = (float) -(1);
		else newRightWingInclination += wingChange[1];
		
		//NewThrust
//		float speed = this.calculateSpeedVector().length();
//		if(speed > 8.5 || speed < 7.5){
//			float thrustChange = this.pidThrust.calculateChange(speed, this.dt);
//			this.setThrust(this.getThrust() + thrustChange);
//			//System.out.println("New Thrust: " + this.getThrust());
//		}
		
		
		newHorStabInclination = 0;
		newVerStabInclination = 0;
		//Vector3f difference = calculateDiffVector();
		
		//TODO ctrl c + ctrl v fysica voor setThrust = vertraging
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
	
	/*
	private Vector3f calculateLeftWingLift(){
		Vector3f normal = new Vector3f(0,0,0);
		Vector3f attackVector = new Vector3f(0,(float)Math.sin(newLeftWingInclination), (float) -Math.cos(newLeftWingInclination));
		Vector3f.cross(new Vector3f(1,0,0), attackVector, normal); // normal = rotationAxis x attackVector
		float liftSlope = configAP.getWingLiftSlope();
		float AoA = (float) - Math.atan2(Vector3f.dot(calculateSpeedVector(),normal), Vector3f.dot(calculateSpeedVector(),attackVector));
		float speed = (float) Math.pow(calculateSpeedVector().length(), 2);
		Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
									   (float)(normal.y*liftSlope*AoA*speed),
									   (float)(normal.z*liftSlope*AoA*speed));
		return result;
	}
	
	private Vector3f calculateRightWingLift(){
		Vector3f normal = new Vector3f(0,0,0);
		Vector3f attackVector = new Vector3f(0,(float)Math.sin(newRightWingInclination), (float) -Math.cos(newRightWingInclination));
		Vector3f.cross(new Vector3f(1,0,0), attackVector, normal); // normal = rotationAxis x attackVector
		float liftSlope = configAP.getWingLiftSlope();
		float AoA = (float) - Math.atan2(Vector3f.dot(calculateSpeedVector(),normal), Vector3f.dot(calculateSpeedVector(),attackVector)); 
		float speed = (float) Math.pow(calculateSpeedVector().length(), 2);
		Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
									   (float)(normal.y*liftSlope*AoA*speed),
									   (float)(normal.z*liftSlope*AoA*speed));
		return result;
	} 
*/
}