package testClasses;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.opencv.core.Core;

import autopilot.algorithmHandler.AutopilotAlain;
import autopilot.algorithms.Aanloop;
import autopilot.algorithms.FlyToHeight;
import autopilot.algorithms.Land;
import autopilot.interfaces.Autopilot;
import autopilot.interfaces.AutopilotConfig;
import autopilot.interfaces.AutopilotFactory;
import autopilot.interfaces.AutopilotInputs;
import autopilot.interfaces.AutopilotOutputs;
import autopilot.interfaces.config.AutopilotConfigReader;
import autopilotModule.Module;
import autopilotModule.Testbed;
import entities.Drone;
import entities.Entity;
import physicsEngine.DroneCrashException;
import physicsEngine.MaxAoAException;
import physicsEngine.PhysicsEngine;
import physicsEngine.approximationMethods.EulerPrediction;
import prevAutopilot.SimpleAutopilot;
import renderEngine.DisplayManager;
import renderEngine.MasterRenderer;

public class LandingTest {

	private static final float STEP_TIME = 0.001f;
	private static final int DRONE_COUNT = 20;

	private static AutopilotConfig autopilotConfig;

	static float[] heights = new float[DRONE_COUNT];

	private static Testbed testbed = new Testbed();
	private static Module module = new Module(testbed);
	private static List<Drone> finished = new ArrayList<>();

	private static ExecutorService pool = Executors.newFixedThreadPool(20);

	public static void main(String[] args) throws InterruptedException, ExecutionException {
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

		// ***INITIALIZE DRONEVIEW***
		// RawModel droneModel = OBJLoader.loadObjModel("tree", loader);
		// TexturedModel staticDroneModel = new TexturedModel(droneModel, new
		// ModelTexture(loader.loadTexture("tree")));
		System.out.println(
				(int) PhysicsEngine.groundLevel - autopilotConfig.getWheelY() + autopilotConfig.getTyreRadius());

		module = new Module(testbed);
		module.defineAirport(0, 0, 20, 20);
		module.defineAirport(0, -400, 1, 50);
		module.defineAirport(0, -100, 50, 50);
		module.defineAirport(0, -200, 50, 50);
		for (int i = 0; i < DRONE_COUNT; i++) {
			module.defineDrone(2, 1, 1, autopilotConfig);

			AutopilotAlain autopilot = (AutopilotAlain) testbed.getActiveDrones().get(i).getAutopilot();

			float height = i * 5 + 10;
			heights[i] = height;

			autopilot.setAlgorithm(new FlyToHeight(i * 5 + 10));
			autopilot.addAlgorithm(new Land());
		}

		DisplayManager.start();
		MasterRenderer m = new MasterRenderer();

		while (!Display.isCloseRequested()) {

			ArrayList<Future<?>> futureList = new ArrayList<Future<?>>();
			float dt = DisplayManager.getFrameTimeSeconds();
			if (dt > 0.00001) {
				for (Drone drone : testbed.getActiveDrones()) {
					Runnable toRun = new Runnable() {
						@Override
						public void run() {
							if (!((AutopilotAlain) drone.getAutopilot()).isFinished()
									&& !((AutopilotAlain) drone.getAutopilot()).crashed) {
								try {
									PhysicsEngine.applyPhysics(drone, dt);
								} catch (DroneCrashException e) {
									((AutopilotAlain) drone.getAutopilot()).crashed = true;
									int index = testbed.getActiveDrones().indexOf(drone);
									System.out.println("Drone with startheight " + heights[index] + " crashed");
									System.out.println(e);
								} catch (MaxAoAException e) {
									e.printStackTrace();
								}
							}
						}

					};
					Future<?> fut = pool.submit(toRun);
					futureList.add(fut);

				}

				for (Future<?> fut : futureList) {
					fut.get();
				}
				
				ArrayList<Future<?>> futureList2 = new ArrayList<Future<?>>();
				// Maak een thread aan voor elke drone
				for (Drone d : testbed.getActiveDrones()) {
					Runnable toRun = new Runnable() {
						@Override
						public void run() {
							module.startTimeHasPassed(d.getId(), d.getAutoPilotInputs());
							AutopilotOutputs outputs = module.completeTimeHasPassed(d.getId());
							d.setAutopilotOutputs(outputs);
						}
					};

					Future<?> fut = pool.submit(toRun);
					futureList2.add(fut);

				}
			}

			m.prepare();
			DisplayManager.updateDisplay();

			for (Drone d : testbed.getActiveDrones()) {
				if (((AutopilotAlain) d.getAutopilot()).isFinished() && !finished.contains(d)) {
					finished.add(d);
					System.out.println("======================================================");
					System.out.println("Finished");
					System.out.println("Time to land: " + ((AutopilotAlain) d.getAutopilot()).timeToLand);
					System.out.println("Length to land: " + ((AutopilotAlain) d.getAutopilot()).lengthToLand);
					System.out.println("Length on ground to brake: " + ((AutopilotAlain) d.getAutopilot()).groundLength);
					int index = testbed.getActiveDrones().indexOf(d);
					System.out.println("Start Height: " + heights[index]);
					System.out.println("======================================================");
				}
			}

			if (allFinished()) {
				System.exit(0);
			}
		}
	}

	private static boolean allFinished() {
		for (Drone d : testbed.getActiveDrones()) {
			if (!((AutopilotAlain) d.getAutopilot()).isFinished() && !((AutopilotAlain) d.getAutopilot()).crashed) {
				return false;
			}
		}

		return true;
	}
}
