package engineTester;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.opencv.core.Core;

import autoPilotJar.SimpleAutopilot;
import autopilot.AutopilotConfigReader;
import entities.Drone;
import entities.Entity;
import fontRendering.TextMaster;
import interfaces.Autopilot;
import interfaces.AutopilotConfig;
import interfaces.AutopilotFactory;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;
import models.RawModel;
import models.TexturedModel;
import physicsEngine.DroneCrashException;
import physicsEngine.PhysicsEngine;
import physicsEngine.approximationMethods.EulerPrediction;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.OBJLoader;
import textures.ModelTexture;

public class PIDFineTuner {
	
	private static final float STEP_TIME = 0.001f;
	
	private static AutopilotConfig autopilotConfig;
	private static List<Entity> entities;
	private static List<SimpleAutopilot> autopilots;
	private static Autopilot autopilot;

	public static void main(String[] args) {
		// Needed to load openCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// ***INITIALIZE CONFIG***
		try {
			File config = new File("res/AutopilotConfig.cfg");
			DataInputStream inputStream = new DataInputStream(new FileInputStream(config));
			autopilotConfig = AutopilotConfigReader.read(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ***INITIALIZE LOADERS & SCREEN***
		DisplayManager.createDisplay();
		Loader loader = new Loader();
		TextMaster.init(loader);
		entities = new ArrayList<>();

		// ***INITIALIZE DRONEVIEW***
		RawModel droneModel = OBJLoader.loadObjModel("tree", loader);
		TexturedModel staticDroneModel = new TexturedModel(droneModel, new ModelTexture(loader.loadTexture("tree")));
		
		for (int i = 0; i < 50; i++) {
			Drone drone = new Drone(staticDroneModel, new Matrix4f().translate(new Vector3f(0,
					(int) PhysicsEngine.groundLevel - autopilotConfig.getWheelY() + autopilotConfig.getTyreRadius(), -30)),
					1, autopilotConfig, new EulerPrediction(STEP_TIME));
			Autopilot autopilot = AutopilotFactory.createAutopilot();
			autopilot.simulationStarted(autopilotConfig, drone.getAutoPilotInputs());
			
			entities.add(drone);
			autopilots.add((SimpleAutopilot) autopilot);
		}
		
		while(!Display.isCloseRequested()){
			for (int i = 0; i < 50; i++) {
				float dt = DisplayManager.getFrameTimeSeconds();
				Drone drone = (Drone) entities.get(i);
				SimpleAutopilot autopilot = autopilots.get(i);
				if(!entities.isEmpty() && dt > 0.00001) {
					try {
						PhysicsEngine.applyPhysics(drone, dt);
					} catch (DroneCrashException e) {
						System.out.println(e);
					}
					AutopilotInputs inputs = drone.getAutoPilotInputs();
					AutopilotOutputs outputs = autopilot.timePassed(inputs);
					drone.setAutopilotOutouts(outputs);
				}
			}
		}
	}

}
