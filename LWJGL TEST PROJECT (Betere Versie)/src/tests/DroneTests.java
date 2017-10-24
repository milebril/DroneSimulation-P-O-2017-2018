package tests;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Vector3f;

import autopilot.AutopilotConfig;
import autopilot.AutopilotConfigReader;
import autopilot.AutopilotConfigValues;
import autopilot.AutopilotConfigWriter;
import entities.*;
import models.RawModel;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import testObjects.Cube;


public class DroneTests {
	
	public static AutopilotConfig autopilotConfig;
	public Drone drone;

	@Before
	public void createNewDrone(){
		Loader loader = new Loader();
		DisplayManager.createDisplay();
		Cube droneCube = new Cube(1, 0, 0);
		
		File config = new File("res/AutopilotConfig.cfg");
		
		try {
			
			//Create new config file with Values from AutopilotConfigValues
			if (!config.exists()) {
				DataOutputStream s = new DataOutputStream(new FileOutputStream(config));
				AutopilotConfigWriter.write(s, new AutopilotConfigValues());
			}
			
			//Read the config file
			DataInputStream inputStream = new DataInputStream(new FileInputStream(config));
			autopilotConfig = AutopilotConfigReader.read(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.drone = new Drone(loader.loadToVAO(droneCube.positions, droneCube.colors, null), new Vector3f(0,0,0), 0, 0, 0, 0, autopilotConfig);
	}
	
	@Test
	public void testIncreasePosition(){
		
		//Drone Setup
		Vector3f position = new Vector3f(0,0,0);
		Vector3f speedVector = new Vector3f(5,5,5);
		drone.setSpeedVector(speedVector);
		
		//Testing
		Vector3f expectedPosition = new Vector3f(5,5,5);
		
		assertEquals(position, drone.getPosition());
		
		drone.increasePosition(1);
		
		assertEquals(expectedPosition, drone.getPosition());
	}
	
}
