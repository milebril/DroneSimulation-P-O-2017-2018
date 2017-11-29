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

public class SimpleAutopilot implements Autopilot, AutopilotOutputs{	
	private boolean heightGoalReached = false;
	private AutopilotConfig configAP;
	private AutopilotInputs inputAP;
	
	private Vector3f currentPosition;
	private Vector3f prevPosition;
	
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
		this.pidHorGoal = new PIDController(2.0f,0.0f,1.0f, (float) (Math.PI / 180), 0);
		//Initialize AP with configfile
		
		//Initialize PIDController for Thrust
		//PIDController(float K-Proportional, float K-Integral, float K-Derivative, float changeFactor, float goal)
		this.pidThrust = new PIDController(1.0f, 0.0f, 3.0f, -10, 10);
		//initialize();
		
		//ADD CUBES TO LIST
		cubePositions.add(new Vector3f(0,-10,-40));
		cubePositions.add(new Vector3f(0,-1,-80));
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
	
	private float getAngle(){
		float overstaande = cubePos.getY() - this.currentPosition.getY();
		float aanliggende = cubePos.getZ() - this.currentPosition.getZ();
		return (float) Math.atan(overstaande/aanliggende);
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
			
			newHorStabInclination += pidHorGoal.calculateChange(inputAP.getPitch() + getAngle(), dt);
			if(newHorStabInclination > Math.PI/6) newHorStabInclination = (float) (Math.PI/6);
			else if(newHorStabInclination < - Math.PI/6) newHorStabInclination = (float) -(Math.PI/6);
//			System.out.println("pitch : " + inputAP.getPitch());
//			System.out.println("angle : " + getAngle());
//			System.out.println("stuff : " + (inputAP.getPitch() - getAngle()));
//			System.out.println("horizontal stabiliser: " + newHorStabInclination);
			if(getEuclidDist(this.currentPosition,cubePos) <= 4){
				this.cubePos = cubePositions.remove(0);
				System.out.println("test");
			}
			
			//System.out.println();
			
			//HORIZONTAL WINGPOSITION
//			float incChange = this.pidHorWing.calculateChange(currentPosition.y, this.dt);
//			float[] wingChange = new float[] {incChange,incChange};
//			
//			newLeftWingInclination += wingChange[0];
////			newLeftWingInclination -= inputAP.getPitch();
//			if(newLeftWingInclination >= Math.PI/6) newLeftWingInclination = (float) (Math.PI/6);
//			else if(newLeftWingInclination <= -Math.PI/6) newLeftWingInclination = (float) -(Math.PI/6);	
//			
//			newRightWingInclination += wingChange[1];
////			newRightWingInclination -= inputAP.getPitch();
//			if(newRightWingInclination >= Math.PI/6)  newRightWingInclination = (float) (Math.PI/6);
//			else if(newRightWingInclination <= -Math.PI/6) newRightWingInclination = (float) -(Math.PI/6);
//			
//			System.out.println("Hor Wing Inc: " + newRightWingInclination);
			
			//HORIZONTAL STABILISER
//			if(this.currentPosition.getY() < this.heightGoal + 0.1 && this.currentPosition.getY() > this.heightGoal - 0.1)
//				this.heightGoalReached = true;
//			//Stabilise
//			if(this.heightGoalReached == true){
//				newHorStabInclination += pidHorStab.calculateChange(inputAP.getPitch(), dt);
//				if(newHorStabInclination > Math.PI/6) newHorStabInclination = (float) (Math.PI/6);
//				else if(newHorStabInclination < -Math.PI/6) newHorStabInclination = (float) -(Math.PI/6);
//			}
//			//climb/fall
//			else{
//				this.heightGoalReached = false;
//				newHorStabInclination += pidHorGoal.calculateChange(this.currentPosition.getY(), dt);
//				if(newHorStabInclination > Math.PI/6) newHorStabInclination = (float) (Math.PI/6);
//				else if(newHorStabInclination < -Math.PI/6) newHorStabInclination = (float) -(Math.PI/6);
//			}
//			System.out.println("Hor StabWing Inc: " + newHorStabInclination);
//			System.out.println();
			//THRUST FORCE
			this.newThrust = 8;
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