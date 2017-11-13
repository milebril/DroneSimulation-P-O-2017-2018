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

import autopilot.AutopilotConfig;
import autopilot.AutopilotConfigReader;
import openCV.ImageProcessor;
import openCV.RedCubeLocator;

public class Autopilot {	
	private AutopilotConfig configAP;
	private AutopilotInputs inputAP;
	
	private Vector3f currentPosition;
	private Vector3f prevPosition;
	
	//Aanpassen als we naar nieuwe cubus moeten gaan
	private Vector3f stablePosition;
	private Vector3f cubePos = new Vector3f(0,3,-10);
	
	//TODO ook heading bijhouden?.	
	
	private float elapsedTime;
	private float prevElapsedTime;
	private float dt;
	
	private ImageProcessor cubeLocator;
	
	/* Variables to send back to drone	 */
	private float newThrust;
	private float newLeftWingInclination;
	private float newRightWingInclination;
	private float newHorStabInclination;
	private float newVerStabInclination;
	
	public Autopilot() {
		
		//Set initial inclination to 0
		newThrust = 0;
		newLeftWingInclination = 0;
		newRightWingInclination = 0;
		newHorStabInclination = 0;
		newVerStabInclination = 0;
		
		//Set initial Positions
		currentPosition = new Vector3f(0,0,0);
		prevPosition = new Vector3f(0,0,0);
		
		//Initialiwe AP with configfile
		initialize();
		
		stablePosition = new Vector3f(0, 0, 0);
	}
	
	private void initialize() {
		try {
			DataInputStream inputStream = new DataInputStream(new FileInputStream("res/AutopilotConfig.cfg"));
			configAP = AutopilotConfigReader.read(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This is the function we'll be calling from the testbed to run the AP.
	 */
	public void communicateWithDrone() {
		getFromDrone();
		
		//Set Variables for this iteration
		//cubeLocator = new ImageProcessor(this);
		cubeLocator = new ImageProcessor(this);
		
		currentPosition = new Vector3f(inputAP.getX(), inputAP.getY(), inputAP.getZ());
		//TODO Heading?
		elapsedTime = inputAP.getElapsedTime();
		dt = elapsedTime - prevElapsedTime;
		
		makeData();
		
		System.out.println("Cube: " + cubeLocator.getCoordinatesOfCube());
		
		//Save droneData we need in nextIteration
		saveData();
		
		//When finished send data back to the drone;
		sendToDrone();
	}
	
	private void makeData() {
		newHorStabInclination = 0;
		newVerStabInclination = 0;
		newThrust = 0;
		Vector3f difference = calculateDiffVector();
			
		
		
		
		//TODO ctrl c + ctrl v fysica voor setThrust = vertraging
	}
	
	private Vector3f calculateDiffVector(){
		Vector3f diff = new Vector3f(0,0,0);
		Vector3f speedVector = new Vector3f(0.0f,0.0f,0.0f);
		Vector3f.sub(currentPosition, prevPosition, speedVector);
		//TODO Vector3f.sub(cubeLocator.makeVector(), speedVector, diff);
		return diff;
	}
	
	private void getFromDrone() {
		try {
			DataInputStream i = new DataInputStream(new FileInputStream("res/APInputs.cfg"));
			inputAP = AutopilotInputsReader.read(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendToDrone() {
		try {
			DataOutputStream s = new DataOutputStream(new FileOutputStream("res/APOutputs.cfg"));
			AutopilotOutputs value = new AutopilotOutputs(){
				@Override
				public float getThrust() {return newThrust;}
				@Override
				public float getLeftWingInclination() {return newLeftWingInclination;}
				@Override
				public float getRightWingInclination() {return newRightWingInclination;}
				@Override
				public float getHorStabInclination() {return newHorStabInclination;}
				@Override
				public float getVerStabInclination() {return newVerStabInclination;}
			};
			AutopilotOutputsWriter.write(s, value);
			
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	private Vector3f calculateProportionalError() {
		float dx, dy, dz = 0;
		
		Vector3f maxError = new Vector3f(0,0,0);
		Vector3f currentError = new Vector3f(0,0,0);
		Vector3f.sub(cubePos, stablePosition, maxError);
		Vector3f.sub(cubePos, currentPosition, currentError);
		
		if (maxError.x == 0 || maxError.y == 0) {
			dx = currentError.x / maxError.x;
			dy = currentError.y / maxError.y;
		} else {
			dx = currentError.x;
			dy = currentError.y;
		}
		
		return new Vector3f(dx, dy, dz);
		
	}
	
	/* TODO
	private float getDroneMass(){
		return configAP.getEngineMass()+ configAP.getTailMass() + 2* configAP.getWingMass();
	}
	
	private float getSpeed(){ 
		return (float) Math.sqrt(Math.pow((getX()-getOldX()), 2) + Math.pow((getY()-getOldY()), 2) + Math.pow((getZ()-getOldZ()), 2)) / this.getDt(); 
	}*/
}
