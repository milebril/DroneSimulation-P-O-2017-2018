package engineTester;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import models.Airport;
import models.RawCubeModel;
import models.RawModel;
import models.TexturedModel;
import physicsEngine.DroneCrashException;
import physicsEngine.MaxAoAException;
import physicsEngine.PhysicsEngine;
import physicsEngine.approximationMethods.EulerPrediction;
import prevAutopilot.SimpleAutopilot;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.TrueTypeFont;
import org.opencv.core.Core;

import autopilot.interfaces.Autopilot;
import autopilot.interfaces.AutopilotConfig;
import autopilot.interfaces.AutopilotFactory;
import autopilot.interfaces.AutopilotInputs;
import autopilot.interfaces.AutopilotOutputs;
import autopilot.interfaces.Path;
import autopilot.interfaces.config.AutopilotConfigReader;
import autopilot.interfaces.path.MyPath;
import renderEngine.CubeRenderer;
import renderEngine.DisplayManager;
import renderEngine.EntityRenderer;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import shaders.StaticShader;
import shaders.cubes.CubeShader;
import terrains.LandingStrip;
import terrains.Terrain;
import testObjects.Cube;
import testObjects.Cuboid;
import textures.ModelTexture;
import entities.Camera;
import entities.Drone;
import entities.Entity;
import entities.Light;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRendering.TextMaster;
import guis.Button;
import guis.GuiRenderer;
import guis.GuiTexture;
import autopilotModule.*;
import autopilotModule.Module;

public class MainGameLoop {

	private static final float STEP_TIME = 0.001f;

	// Key press lock
	private static boolean lLock;
	private static boolean sLock;
	private static boolean mLock;
	private static boolean pLock;

	public static AutopilotConfig autopilotConfig;

	// Drone Stuff
	private static Drone activeDrone;

	// Entities lists
	private static List<Terrain> terrains;
	private static List<Entity> cubes;

	// Loader
	private static Loader loader;

	// Camera Stuff
	private static Camera chaseCam;
	private static Camera extraCam;
	private static boolean chaseCameraLocked = true;

	// Shaders
	private static CubeShader cubeShader;

	// Renderers
	private static MasterRenderer renderer;
	private static CubeRenderer cubeRenderer;

	// Lights
	private static Light light;

	// ViewEnum
	private static ViewEnum currentView = ViewEnum.MAIN;

	//Module
	private static Testbed testbed = new Testbed();
	private static Module module = new Module(testbed);
	
	//
	public static ViewEnumExtra extraView = ViewEnumExtra.TOP_DOWN;

	// ThreadPool
	private static ExecutorService pool = Executors.newFixedThreadPool(20); // creating a pool of X threads

	private enum ViewEnum {
		MAIN, MINIMAP
	}
	
	//Minimap
	private static MiniMap miniMap;
	private static DroneList droneList;

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		// Needed to load openCV
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

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
		loader = new Loader();
		TextMaster.init(loader);
		terrains = new ArrayList<>();
		cubes = new ArrayList<>();
		module = new Module(testbed);

		// ***INITIALIZE DRONEVIEW***
//		RawModel droneModel = OBJLoader.loadObjModel("untitled5", loader);
//		TexturedModel staticDroneModel = new TexturedModel(droneModel,
//				new ModelTexture(loader.loadTexture("untitled")));


//		for (int i = 1; i <= 10; i++) {
			
//			Drone drone = new Drone(staticDroneModel,
//					new Matrix4f().translate(new Vector3f(-20*i,
//							(int) PhysicsEngine.groundLevel - autopilotConfig.getWheelY()
//									+ autopilotConfig.getTyreRadius() + 20,
//							20 * i)),
//					1f, autopilotConfig, new EulerPrediction(STEP_TIME));
//			drone.setName("Drone " + i);
//			drone.setId(drones.size());
//			
//			activeDrone = drone;
//			entities.add(drone);
//			drones.add(drone);
//		}
		
		module.defineAirport(0, 0, 20, 20);
		module.defineAirport(0, -400, 1, 50);
		module.defineAirport(0, -100, 50, 50);
		module.defineAirport(0, -200, 50, 50);
		for (int i = 0; i < 10; i++) {
			module.defineDrone(2, 1, 1, autopilotConfig);
		}
		//module.defineDrone(2, 1, 1, autopilotConfig);
		
		//module.defineDrone(1, 0, 0, autopilotConfig);
		
		ArrayList<Drone> drones = getTestbed().getDrones();

		
		//Drone randomValue = testbed.getInactiveDrones().get(0);
//		activeDrone = randomValue;
		
		activeDrone = getTestbed().getActiveDrones().get(0);

		// ***INITIALIZE CHASE-CAM***
		chaseCam = new Camera();
		chaseCam.setPosition(activeDrone.getPosition().translate(30, 0, 0));
		// chaseCam.setRotation(0, -1.5f, 0);
		
		extraCam = new Camera();
		extraCam.setPosition(extraView.getCameraPosition(activeDrone, extraCam));

		light = new Light(new Vector3f(20000, 20000, 2000), new Vector3f(1, 1, 1));

		terrains.add(new Terrain(0, -1, loader, new ModelTexture(loader.loadTexture("checker"))));
		terrains.add(new Terrain(-1, -1, loader, new ModelTexture(loader.loadTexture("checker"))));
		terrains.add(new Terrain(0, 0, loader, new ModelTexture(loader.loadTexture("checker"))));
		terrains.add(new Terrain(-1, 0, loader, new ModelTexture(loader.loadTexture("checker"))));
//		terrains.add(new LandingStrip(-0.5f, -1, loader, new ModelTexture(loader.loadTexture("landing"))));

		Camera camera = new Camera(200, 200);
		camera.setPosition(activeDrone.getPosition().translate(0, 0, 0));
		renderer = new MasterRenderer();

		// Cube Render
		cubeShader = new CubeShader();
		cubeRenderer = new CubeRenderer(cubeShader, 120, 120);

		Cube c = new Cube(1, 0, 0);
		RawCubeModel cube = loader.loadToVAO(c.positions, c.colors);
		
		// ***INITIALIZE BUTTONS GUI***
		GuiRenderer guiRenderer = new GuiRenderer(loader);

		BufferedImage i = null;
		try {
			i = ImageIO.read(new File("res/grass.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		miniMap = new MiniMap();
		droneList = new DroneList(drones);

		while (!Display.isCloseRequested()) {
			if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
				//camera.takeSnapshot();
			}

			drones = getTestbed().getDrones();

			switch (currentView) {
			case MINIMAP:
				miniMap.render(drones, getTestbed().getAirports(), guiRenderer, loader);
				break;
			case MAIN:
				// CAMERA VIEW
				renderer.prepareBlack();
				GL11.glViewport(0, 0, 202, 202);
				GL11.glScissor(0, 0, 202, 202);
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				glClearColor(0, 0, 0, 1);

				renderer.prepare();
				GL11.glViewport(0, 0, 200, 200);
				GL11.glScissor(0, 0, 200, 200);
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				renderEntities(camera, "Drone");
				camera.setPosition(activeDrone.getPosition().translate(0, 0, -5));

				// ***BIG SCREEN***
				// renderer.prepare();
				GL11.glViewport(580, 0, 700, Display.getHeight());
				GL11.glScissor(580, 0, 700, Display.getHeight());
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				GL11.glClearColor(135 / 255f, 206 / 255f, 235 / 255f, 0.6f);
				renderEntities(chaseCam, "3D");
				
				GL11.glViewport(0, 201, Display.getWidth() - 700, Display.getHeight() - 201);
				GL11.glScissor(0, 201, Display.getWidth() - 700, Display.getHeight() - 201);
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				//GL11.glClearColor(135 / 255f, 206 / 255f, 235 / 255f, 0.6f);
				renderEntities(extraCam, "3D");
				extraCam.setPosition(extraView.getCameraPosition(activeDrone, extraCam));

				// GUI
				GL11.glViewport(0, 0, 1280, 700);
				GL11.glScissor(0, 0, 1280, 700);
				GL11.glEnable(GL11.GL_SCISSOR_TEST);

				// ***BUTTON GUI***
				GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
				GL11.glScissor(0, 0, Display.getWidth(), Display.getHeight());
				GL11.glEnable(GL11.GL_SCISSOR_TEST);

				break;
			}

			// ***UPDATES***
			float dt = DisplayManager.getFrameTimeSeconds();
			if (dt > 0.00001) {

				// De lijst waarin alle threads gestoken worden om later te checken of ze klaar
				// zijn.
				ArrayList<Future<?>> futureList = new ArrayList<Future<?>>();

				// Maak een thread aan voor elke drone
				for (Drone d : getTestbed().getActiveDrones()) {
					Runnable toRun = new Runnable() {
						@Override
						public void run() {

							// fysica toepassen
							// applyphysics rekent de krachten uit en gaat dan de kinematische waarden van
							// de drone aanpassen op basis daarvan
							try {
								PhysicsEngine.applyPhysics(d, dt);
							} catch (DroneCrashException e) {
								System.err.println(e);
								System.exit(-1);
							} catch (MaxAoAException e) {
								e.printStackTrace();
								System.exit(-1);
							}
							//System.out.println(d.getName() + " is ready with applyPhysics.");
						}
					};
					Future<?> fut = pool.submit(toRun);
					futureList.add(fut);
				}

				// De code gaat niet verder totdat alle voordien aangemaakt threads klaar zijn
				// future.get() -> Waits if necessary for the computation to complete, and then
				// retrieves its result.
				for (Future<?> fut : futureList) {
					fut.get();
				}

				ArrayList<Future<?>> futureList2 = new ArrayList<Future<?>>();
				// Maak een thread aan voor elke drone
				for (Drone d : getTestbed().getActiveDrones()) {
					Runnable toRun = new Runnable() {
						@Override
						public void run() {
							// Autopilot stuff
							module.startTimeHasPassed(d.getId(), d.getAutoPilotInputs());
							//TODO: timePassed moet weg! foetsieee
							AutopilotOutputs outputs = module.completeTimeHasPassed(d.getId());
							d.setAutopilotOutputs(outputs);
							

							//System.out.println(d.getName() + " is ready with AP.");
						}
					};

					Future<?> fut = pool.submit(toRun);
					futureList2.add(fut);

				}

				// De code gaat niet verder totdat alle voordien aangemaakt threads klaar zijn
				// future.get() -> Waits if necessary for the computation to complete, and then
				// retrieves its result.
				for (Future<?> fut : futureList2) {
					fut.get();
				}
			}

			keyInputs();
			
			//Updates the GUI
			droneList.updateLabels(activeDrone);

			removeCubes();
			DisplayManager.updateDisplay();

		}

		renderer.cleanUp();
		loader.cleanUp();
		TextMaster.cleanUp();
		cubeShader.cleanUp();
		DisplayManager.closeDisplay();
	}

	private static void renderEntities(Camera camera, String type) throws InterruptedException, ExecutionException {
		for (Terrain t : terrains) {
			renderer.processTerrain(t);
		}

		//No need for multithreading; Already optimized; models are just displaced.
//		for (Entity entity : entities) {
//					renderer.processEntity(entity);
//		}
		
		for (Drone d : getTestbed().getDrones()) {
			renderer.processEntity(d);
		}
			
		
		for(Airport a: getTestbed().getAirports()) {
			a.render(renderer, chaseCam, light);
		}
		renderer.render(light, camera);

		cubeShader.start();
		cubeShader.loadViewMatrix(camera);
		for (Entity entity : cubes) {
			cubeRenderer.render(entity, cubeShader);
		}
		cubeShader.stop();
	}

	private static void removeCubes() {
		List<Entity> toRemove = new ArrayList<>();
		for (Entity e : cubes) {
			if (getEuclidDist(activeDrone.getPosition(), e.getPosition()) <= 3) {
				toRemove.add(e);
			}
		}

		cubes.removeAll(toRemove);
	}

	private static float getEuclidDist(Vector3f vec1, Vector3f vec2) {
		Vector3f temp = new Vector3f(0, 0, 0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
	}

	public static void keyInputs() {
		if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
			/* Lock/Unlock on Third Person Camera */
			if (!lLock) {
				chaseCameraLocked = !chaseCameraLocked;
			}
			lLock = true;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
			if (!sLock) {
				DisplayManager.start();
			}
			sLock = true;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_M)) {
			if (!mLock) {
				if (currentView == ViewEnum.MAIN) {
					currentView = ViewEnum.MINIMAP;
				} else {
					currentView = ViewEnum.MAIN;
				}
			}
			mLock = true;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
			if (!pLock) {
				DisplayManager.pauze();
			}
			pLock = true;
		} else {
			if (chaseCameraLocked) {
				Vector3f.add(activeDrone.getPosition(), new Vector3f(0, 0, 30), chaseCam.getPosition());
			} else {
				chaseCam.roam();
			}

			lLock = false;
			sLock = false;
			mLock = false;
			pLock = false;
		}

		if (chaseCameraLocked) {
			Vector3f.add(activeDrone.getPosition(), new Vector3f(0, 0, 30), chaseCam.getPosition());
		} else {
			chaseCam.roam();
		}
//		lLock = false;
//		sLock = false;
	}

	public static void setActiveDrone(Drone drone) {
		activeDrone = drone;
	}

	public static Testbed getTestbed() {
		return testbed;
	}
}
	

