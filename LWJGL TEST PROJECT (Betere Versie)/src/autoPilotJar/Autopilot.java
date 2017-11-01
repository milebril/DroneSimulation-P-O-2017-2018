package autoPilotJar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.lwjgl.util.vector.Vector3f;

import autopilot.AutopilotConfig;
import autopilot.AutopilotConfigReader;
import openCV.RedCubeLocator;

public class Autopilot {	
	private AutopilotConfig configAP;
	private AutopilotInputs inputAP;
	
	private Vector3f currentPosition;
	private Vector3f prevPosition;
	
	//TODO ook heading bijhouden?.	
	
	private float elapsedTime;
	private float prevElapsedTime;
	private float dt;
	
	private RedCubeLocator cubeLocator;
	
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
		
		//Initialiwe AP with configfile
		initialize();
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
		cubeLocator = new RedCubeLocator(inputAP.getImage());
		currentPosition = new Vector3f(inputAP.getX(), inputAP.getY(), inputAP.getZ());
		//TODO Heading?
		elapsedTime = inputAP.getElapsedTime();
		dt = elapsedTime - prevElapsedTime;
		
		//Save droneData we need in nextIteration
		saveData();
		
		//When finished send data back to the drone;
		sendToDrone();
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
	 * Elepsed Time
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

	/* TODO
	private float getDroneMass(){
		return configAP.getEngineMass()+ configAP.getTailMass() + 2* configAP.getWingMass();
	}
	
	private float getSpeed(){ 
		return (float) Math.sqrt(Math.pow((getX()-getOldX()), 2) + Math.pow((getY()-getOldY()), 2) + Math.pow((getZ()-getOldZ()), 2)) / this.getDt(); 
	}*/
}
