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
	private Vector3f stubCube = new Vector3f(0, 0, -40);
	private Vector3f cubePos = stubCube;
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
		//this.pidHorWing = new PIDController(1.0f,0.0f,10.0f, (float) -(Math.PI / 180), 0);
		//this.pidHorStab = new PIDController(2.0f,1.0f,10.0f, (float) (Math.PI / 180), 0);
		this.pidHorGoal = new PIDController(1.0f,0.0f,0.5f, (float) (Math.PI / 180), 0);
		
		this.pidHorStab = new PIDController(2.0f,1.0f,10.0f, (float) (Math.PI / 180), 0);
		this.pidHorGoal = new PIDController(1.0f,0.0f,0.5f, (float) (Math.PI / 180), 0);
		this.pidVerGoal = new PIDController(2.0f,0.0f,1.0f, (float) (Math.PI / 180), 0);
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
	
	private float getEuclidDist(Vector3f vec1, Vector3f vec2){
		Vector3f temp = new Vector3f(0,0,0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
	}
	
	@Override
	public AutopilotOutputs timePassed(AutopilotInputs inputs) {
		this.inputAP = inputs;
		if (this.inputAP.getElapsedTime() > 0.0000001) {
			currentPosition = new Vector3f(inputAP.getX(), inputAP.getY(), inputAP.getZ());
			this.dt = inputs.getElapsedTime() - prevElapsedTime;
			prevElapsedTime = inputs.getElapsedTime();
			
			
			newHorStabInclination += pidHorGoal.calculateChange(inputAP.getPitch() + getVerAngle(), dt);
			if(newHorStabInclination > Math.PI/6) newHorStabInclination = (float) (Math.PI/6);
			else if(newHorStabInclination < - Math.PI/6) newHorStabInclination = (float) -(Math.PI/6);

			newVerStabInclination += pidVerGoal.calculateChange(inputAP.getHeading() - getHorAngle(), dt);
			if(newVerStabInclination > Math.PI/6) newVerStabInclination = (float) (Math.PI/6);
			else if(newVerStabInclination < - Math.PI/6) newVerStabInclination = (float) -(Math.PI/6);
			
			cubePositions = cubeLocator.getCoordinatesOfCube();
			cubePositions.sort(new Comparator<Vector3f>() {
				@Override
				public int compare(Vector3f o1, Vector3f o2) {
					return -Float.compare(o1.z, o2.z);
				}
			});
			
			if (cubePositions.size() > 0) {
				Vector3f temp = new Vector3f(0,0,0);
				Vector3f.sub(cubePos, cubePositions.get(0), temp);
				if (temp.length() > 3) {
					cubePos = cubePositions.get(0);
//					System.out.println("CUBE:" + cubePos);
//					System.out.println("POS" + currentPosition);
				}
			}
			
//			if (cubePos == stubCube) {
//				//System.out.println(cubePos);
//				if (getEuclidDist(this.currentPosition, cubePos) < 4) {
//					stubCube.translate(0, 0, -40);
//					cubePos = stubCube;
//					System.out.println(cubePos);;
//				}
//				System.out.println("Size:" + cubeLocator.getCoordinatesOfCube().size());
//				if (cubePositions.size() > 0) {
//					System.out.println("Cube Found");
//					cubePos = cubePositions.get(0);
//				}
//			}
			
			//CUBE REACHED
			if(getEuclidDist(this.currentPosition,cubePos) <= 4){
				System.out.println("hier");
				this.cubePos = stubCube.translate(0, 0, -40);
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
			//REMOVE THIS AFTER TESTING:
		      this.newThrust = configAP.getMaxThrust();
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