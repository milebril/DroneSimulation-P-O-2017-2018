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
	private AutopilotConfig configAP;
	private AutopilotInputs inputAP;
	
	private Vector3f currentPosition;
	private Vector3f prevPosition;
	
	
	//Aanpassen als we naar nieuwe cubus moeten gaan
	private Vector3f cubePos;
	private List<Vector3f> cubePositions = new ArrayList<>();
	
	
	//TODO ook heading bijhouden?.	
	
	private float elapsedTime;
	private float prevElapsedTime;
	private float dt = 0;
	
	private PIDController pidHorStab;
	private PIDController pidVerStab;
	
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
		this.pidHorStab = new PIDController(1.0f,0.0f,0.5f,(float)Math.toRadians(1),0);
		//this.pidVerStab = new PIDController(1.0f,0.75f,0.0f,(float)Math.toRadians(1),0);
		this.pidVerStab = new PIDController(1.0f,10.0f,1.0f,(float)Math.toRadians(1),0);
		//Initialize AP with configfile
		
		
		//ADD CUBES TO LIST
//		cubePositions.add(new Vector3f(0,0, -200));
//		cubePositions.add(new Vector3f(0,0,-80));
//		cubePositions.add(new Vector3f(-2,0,-120));
//		cubePositions.add(new Vector3f(0,0,-160));
//		cubePositions.add(new Vector3f(0, 0, -80));
//		cubePositions.add(new Vector3f(-5,0,-40));
//		cubePositions.add(new Vector3f(-2.5f,0,-60));
//		cubePositions.add(new Vector3f(-5,0,-40));
		
		//TURNING DEMO
		cubePositions.add(new Vector3f(5,0, -40));
		cubePositions.add(new Vector3f(0,0, -80));
		cubePositions.add(new Vector3f(-2,0, -120));
		cubePositions.add(new Vector3f(-3,0, -160));
		cubePositions.add(new Vector3f(5,0, -200));
		
		//WORKING DEMO UP/DOWN
//		cubePositions.add(new Vector3f(0,-10,-40));
//		cubePositions.add(new Vector3f(0,0,-80));
//		cubePositions.add(new Vector3f(0,-5,-120));
//		cubePositions.add(new Vector3f(0,8,-160));
//		cubePositions.add(new Vector3f(0,-2,-200));
		
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
	
	private float getEuclidDist(Vector3f vec1, Vector3f vec2){
		Vector3f temp = new Vector3f(0,0,0);
		Vector3f.sub(vec1, vec2, temp);
		return temp.length();
	}
	
	@Override
	public AutopilotOutputs timePassed(AutopilotInputs inputs) {
		this.inputAP = inputs;
		if (this.inputAP.getElapsedTime() > 0.0000001) {
			
			//SAVE DATA 
			currentPosition = new Vector3f(inputAP.getX(), inputAP.getY(), inputAP.getZ());
			this.dt = inputs.getElapsedTime() - prevElapsedTime;
			prevElapsedTime = inputs.getElapsedTime();
			this.prevPosition = new Vector3f(currentPosition.x, currentPosition.y, currentPosition.z);
			
			
			newHorStabInclination += pidHorStab.calculateChange(inputAP.getPitch() + getVerAngle(), dt);
			if(newHorStabInclination > Math.PI/6) newHorStabInclination = (float) (Math.PI/6);
			else if(newHorStabInclination < - Math.PI/6) newHorStabInclination = (float) -(Math.PI/6);

			newVerStabInclination += pidVerStab.calculateChange(inputAP.getHeading() - getHorAngle(), dt);
			if(newVerStabInclination > Math.PI/6) newVerStabInclination = (float) (Math.PI/6);
			else if(newVerStabInclination < - Math.PI/6) newVerStabInclination = (float) -(Math.PI/6);
			
			//CUBE REACHED
			if(getEuclidDist(this.currentPosition,cubePos) <= 4 && !cubePositions.isEmpty()){
				this.cubePos = cubePositions.remove(0);
				this.pidHorStab.reset();
				this.pidVerStab.reset();
			}
			this.newThrust = this.configAP.getMaxThrust();
		      
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