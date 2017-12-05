package tests;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import autopilot.AutopilotConfigReader;
import autopilot.AutopilotConfigValues;
import autopilot.AutopilotConfigWriter;
import entities.*;
import interfaces.AutopilotConfig;
import models.RawModel;
import physicsEngine.PhysicsEngine;
import physicsEngine.approximationMethods.EulerPrediction;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import testObjects.Cube;


/**
 * @author Torben
 *
 */
public class DroneTests {
	
	public static AutopilotConfig autopilotConfig;
	public Drone drone;
	public Loader loader;
	public static float pi = (float) Math.PI;

	@Before
	public void createNewDrone(){
		this.loader = new Loader();
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

		this.drone = new Drone(loader.loadToVAO(droneCube.positions, droneCube.colors, null), new Matrix4f(), 1f, autopilotConfig, new EulerPrediction(0.01f));
	}
	
	/**
	 * De volgende functie test of de drone zijn positie juist verandert op basis van zijn snelheid.
	 */
	
	@Test
	public void testIncreasePosition(){
		
		//Drone Setup
		Vector3f position = new Vector3f(0,0,0);
		Vector3f speedVector = new Vector3f(5,5,5);
		drone.setLinearVelocity(speedVector);
		
		//Testing
		Vector3f expectedPosition = new Vector3f(5,5,5);
		
		assertEquals(position, drone.getPosition());
		
		PhysicsEngine.applyPhysics(drone, 1);;
		
		assertEquals(expectedPosition, drone.getPosition());
		
		resetDrone();
	}
	
	/**
	 * De volgende functie test of de thrust force naar de maxForce wordt gezet wanneer men over
	 * de maximale toegelaten waarde probeert te gaan.
	 */
	
	@Test
	public void testMaxThrustForce(){
		drone.setThrustForce(drone.getMaxThrust() + 5.0f);
		float errorMargin = 0.00001f;
		
		assertEquals(drone.getMaxThrust(),drone.getThrustForce(),errorMargin);
		
		resetDrone();
	}
	
	
	/**
	 * De volgende functie test of de linker en de rechter vleugel dezelfde kracht uitoefenen wanneer
	 * ze dezelfde inclinatie hebben.
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	
	@Test
	public void testLeftAndRightWingSameForce() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		drone.setLinearVelocity(new Vector3f(10,10,10));
		drone.getLeftWing().setInclination(pi/4);
		drone.getRightWing().setInclination(pi/4);
		
		Class c = drone.getClass();
		
		Method method1 = c.getDeclaredMethod("calculateLeftWingLift");
		method1.setAccessible(true);
		Vector3f leftForce = (Vector3f) method1.invoke(drone);
		
		Method method2 = c.getDeclaredMethod("calculateRightWingLift");
		method2.setAccessible(true);
		Vector3f rightForce = (Vector3f) method2.invoke(drone);
		
		assertEquals(leftForce,rightForce);
		
		resetDrone();
	}
	
	
	
	
	
	@After
	public void cleanUp(){
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}
	
	public void resetDrone(){
		Vector3f nulVector = new Vector3f(0,0,0);
		Matrix4f identityMatrix = new Matrix4f();
		drone.setLinearVelocity(nulVector);
//		drone.setHeadingVector();
		drone.setLinearAcceleration(nulVector);
		drone.setPose(identityMatrix);;
		drone.getCamera().setPosition(nulVector);
		drone.setThrustForce(0);
		drone.getLeftWing().setInclination(0);
		drone.getRightWing().setInclination(0);
		drone.getHorizStabilizer().setInclination(0);
		drone.getVertStabilizer().setInclination(0);
	}
	
}
