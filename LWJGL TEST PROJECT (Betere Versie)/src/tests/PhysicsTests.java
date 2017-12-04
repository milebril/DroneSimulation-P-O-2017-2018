package tests;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import autopilot.AutopilotConfigReader;
import entities.Drone;
import interfaces.AutopilotConfig;

public class PhysicsTests {
	
	
	static float EPSILON = 0.005f;
	
	static AutopilotConfig autopilotConfig;
	@BeforeClass
	public static void getConfig() throws IOException {
		File config = new File("res/AutopilotConfig.cfg");
		DataInputStream inputStream = new DataInputStream(new FileInputStream(config));
		AutopilotConfig con = AutopilotConfigReader.read(inputStream);
		autopilotConfig = con;
	}
	
	
	
	// Drone(Matrix4f pose, AutopilotConfig autopilotConfig, Vector3f velocity, Vector3f rotVel)
	Drone droneX;
	@Before
	public void createXDrone() {
		Matrix4f pose = new Matrix4f();
		droneX = new Drone(pose, autopilotConfig, new Vector3f(15.0f, 0, 0), new Vector3f(0, 0, 0));
	}
	
	Drone droneY;
	@Before
	public void createYDrone() {
		Matrix4f pose = new Matrix4f();
		Matrix4f.rotate((float) Math.PI/2, new Vector3f(0, 0, 1), pose, pose);
		droneY = new Drone(pose, autopilotConfig, new Vector3f(0, 15.0f, 0), new Vector3f(0, 0, 0));
	}
	
	Drone droneZ;
	@Before
	public void createZDrone() {
		Matrix4f pose = new Matrix4f();
		Matrix4f.rotate((float) -Math.PI/2, new Vector3f(0, 1, 0), pose, pose);
		droneZ = new Drone(pose, autopilotConfig, new Vector3f(0, 0, 15.0f), new Vector3f(0, 0, 0));
	}
	

	Vector3f xVector;
	Vector3f yVector;
	Vector3f zVector;
	@Before
	public void createVectors() {
		xVector = new Vector3f(1, 0, 0);
		yVector = new Vector3f(0, 1, 0);
		zVector = new Vector3f(0, 0, 1);
	}
	
	
	@Test
	public void testYDroneRotation() {
		Vector3f transV = droneY.transformToWorldFrame(xVector);
		assertEquals(transV.x, 0, EPSILON);
		assertEquals(transV.y, 1, EPSILON);
		assertEquals(transV.z, 0, EPSILON);
	}

}
