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
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import textures.ModelTexture;

public class PIDFineTuner {

	private static final float STEP_TIME = 0.001f;
	private static final int DRONE_COUNT = 50;

	private static AutopilotConfig autopilotConfig;
	private static List<Entity> entities;
	private static List<SimpleAutopilot> autopilots;
	
	private static float p, i, d;
	private static float minY = 0, maxY = 100;

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
		entities = new ArrayList<>();
		autopilots = new ArrayList<>();

		// ***INITIALIZE DRONEVIEW***
		RawModel droneModel = OBJLoader.loadObjModel("tree", loader);
		TexturedModel staticDroneModel = new TexturedModel(droneModel, new ModelTexture(loader.loadTexture("tree")));
        System.out.println((int)PhysicsEngine.groundLevel -autopilotConfig.getWheelY() + autopilotConfig.getTyreRadius());
		for (int i = 0; i < DRONE_COUNT; i++) {
			Drone drone = new Drone(staticDroneModel, new Matrix4f().translate(new Vector3f(0, (int)PhysicsEngine.groundLevel -autopilotConfig.getWheelY() + autopilotConfig.getTyreRadius() + 20, 0)), 1,
					autopilotConfig, new EulerPrediction(STEP_TIME));
			Autopilot autopilot = AutopilotFactory.createAutopilot();
			autopilot.simulationStarted(autopilotConfig, drone.getAutoPilotInputs());
			System.out.println("New AP: PID = " + ((SimpleAutopilot) autopilot).p + " " +
					 ((SimpleAutopilot) autopilot).i + " " +  ((SimpleAutopilot) autopilot).d);
			entities.add(drone);
			autopilots.add((SimpleAutopilot) autopilot);
		}
		
		DisplayManager.start();
		MasterRenderer m = new MasterRenderer();
		
		while (!Display.isCloseRequested()) {
			for (int i = 0; i < DRONE_COUNT; i++) {
				float dt = DisplayManager.getFrameTimeSeconds();
				Drone drone = (Drone) entities.get(i);
				SimpleAutopilot autopilot = autopilots.get(i);
				if (dt > 0.00001 && !autopilot.isFinished() && !autopilot.failed) {
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

			for (int j = 0; j < DRONE_COUNT; j++) {
				SimpleAutopilot autopilot = autopilots.get(j);
				if (autopilot.isFinished()) {
					if (autopilot.maxY < maxY && autopilot.minY > minY) {
						maxY = autopilot.maxY;
						minY = autopilot.minY;
						p = autopilot.p;
						i = autopilot.i;
						d = autopilot.d;
						
						System.out.println("New Best: Y:" + minY + " " + maxY + " PID: " + p + " " + i + " " + d);
					}
					
//					System.out.println(autopilot.minY);
				}
			}
			
			if (allFinished()) {
				System.exit(0);
			}
			
			m.prepare();
			DisplayManager.updateDisplay();
		}
	}

	private static boolean allFinished() {
		for (SimpleAutopilot ap : autopilots) {
			if (!ap.isFinished() && !ap.failed) {
				return false;
			}
		}
		
		return true;
	}

}
