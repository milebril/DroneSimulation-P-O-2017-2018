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
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

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
import autopilot.interfaces.config.AutopilotConfigReader;
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

public class MainGameLoop {

	private static final float STEP_TIME = 0.001f;

	// Key press lock
	private static boolean lLock;
	private static boolean sLock;
	private static boolean mLock;
	private static boolean pLock;

	public static AutopilotConfig autopilotConfig;

	// Drone Stuff
	private static List<Drone> drones;
	private static Drone activeDrone;

	// Entities lists
	private static List<Entity> entities;
	private static List<Terrain> terrains;
	private static List<Entity> cubes;

	// Loader
	private static Loader loader;

	// Autopilot
	private static Autopilot autopilot;

	// Camera Stuff
	private static Camera chaseCam;
	private static boolean chaseCameraLocked = true;

	// Shaders
	private static CubeShader cubeShader;

	// Renderers
	private static MasterRenderer renderer;
	private static CubeRenderer cubeRenderer;

	// Lights
	private static Light light;

	// Buttons
	private static Button openFile;
	private static Button randomCubes;

	// ViewEnum
	private static ViewEnum currentView = ViewEnum.MAIN;

	// ThreadPool
	private static ExecutorService pool = Executors.newFixedThreadPool(20); // creating a pool of X threads

	private enum ViewEnum {
		MAIN, MINIMAP
	}

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
		loader = new Loader();
		TextMaster.init(loader);
		entities = new ArrayList<>();
		terrains = new ArrayList<>();
		cubes = new ArrayList<>();
		drones = new ArrayList<>();

		// ***INITIALIZE DRONEVIEW***
		RawModel droneModel = OBJLoader.loadObjModel("untitled5", loader);
		TexturedModel staticDroneModel = new TexturedModel(droneModel,
				new ModelTexture(loader.loadTexture("untitled")));

		for (int i = 1; i <= 10; i++) {
			Drone drone = new Drone(staticDroneModel,
					new Matrix4f().translate(new Vector3f(-20 * i,
							(int) PhysicsEngine.groundLevel - autopilotConfig.getWheelY()
									+ autopilotConfig.getTyreRadius() + 20,
							20 * i)),
					1f, autopilotConfig, new EulerPrediction(STEP_TIME));
			drone.setName("Drone " + i);

			activeDrone = drone;
			entities.add(drone);
			drones.add(drone);
		}

		for (int i = 1; i <= 10; i++) {
			Drone drone = new Drone(staticDroneModel,
					new Matrix4f().translate(new Vector3f(-20 * i,
							(int) PhysicsEngine.groundLevel - autopilotConfig.getWheelY()
									+ autopilotConfig.getTyreRadius() + 20 + 20,
							20 * i)),
					1f, autopilotConfig, new EulerPrediction(STEP_TIME));
			drone.setName("Drone " + i);

			activeDrone = drone;
			entities.add(drone);
			drones.add(drone);
		}

		for (int i = 1; i <= 10; i++) {
			Drone drone = new Drone(staticDroneModel,
					new Matrix4f().translate(new Vector3f(20 * i,
							(int) PhysicsEngine.groundLevel - autopilotConfig.getWheelY()
									+ autopilotConfig.getTyreRadius() + 20,
							20 * i)),
					1f, autopilotConfig, new EulerPrediction(STEP_TIME));
			drone.setName("Drone " + i);

			activeDrone = drone;
			entities.add(drone);
			drones.add(drone);
		}

		for (int i = 1; i <= 10; i++) {
			Drone drone = new Drone(staticDroneModel,
					new Matrix4f().translate(new Vector3f(20 * i,
							(int) PhysicsEngine.groundLevel - autopilotConfig.getWheelY()
									+ autopilotConfig.getTyreRadius() + 20 + 20,
							20 * i)),
					1f, autopilotConfig, new EulerPrediction(STEP_TIME));
			drone.setName("Drone " + i);

			activeDrone = drone;
			entities.add(drone);
			drones.add(drone);
		}

		// ***INITIALIZE CHASE-CAM***
		chaseCam = new Camera();
		chaseCam.setPosition(activeDrone.getPosition().translate(30, 0, 0));
		// chaseCam.setRotation(0, -1.5f, 0);

		// ***INITIALIZE GUI-TEXT***
		FontType font = new FontType(loader.loadTexture("verdana"), new File("res/verdana.fnt"));

		// Speed text
		String speed = String.valueOf(Math.round(activeDrone.getAbsVelocity()));
		GUIText textSpeed = new GUIText("Speed = " + speed + "m/s", 1, font, new Vector2f(0, 0), 1, false);
		textSpeed.setColour(0, 0, 0);

		// Position text
		String xpos, ypos, zpos;
		GUIText textPosition = new GUIText("", 1, font, new Vector2f(0, 0.05f), 1, false);
		textPosition.setColour(0, 0, 0);

		// Wing inclinations text
		String leftWingInc = String.valueOf(activeDrone.getLeftWing().getInclination());
		GUIText textLeftWing = new GUIText("Left wing inclination = " + leftWingInc + "rad", 1, font,
				new Vector2f(0, 0.1f), 1, false);
		textLeftWing.setColour(1, 0, 0);

		String rightWingInc = String.valueOf(activeDrone.getRightWing().getInclination());
		GUIText textRightWing = new GUIText("Right wing inclination = " + rightWingInc + "rad", 1, font,
				new Vector2f(0, 0.15f), 1, false);
		textRightWing.setColour(1, 0, 0);

		String horzStab = String.valueOf(activeDrone.getHorStabilizer().getInclination());
		GUIText textHorzStab = new GUIText("Horizontal stabilizer inclination = " + horzStab + "rad", 1, font,
				new Vector2f(0, 0.20f), 1, false);
		textHorzStab.setColour(1, 0, 0);

		String vertStab = String.valueOf(activeDrone.getVertStabilizer().getInclination());
		GUIText textVertStab = new GUIText("Vertical stabilizer inclination = " + vertStab + "rad", 1, font,
				new Vector2f(0, 0.25f), 1f, false);
		textVertStab.setColour(1, 0, 0);

		light = new Light(new Vector3f(20000, 20000, 2000), new Vector3f(1, 1, 1));

		terrains.add(new Terrain(0, -1, loader, new ModelTexture(loader.loadTexture("checker"))));
		terrains.add(new Terrain(-1, -1, loader, new ModelTexture(loader.loadTexture("checker"))));
		terrains.add(new Terrain(0, 0, loader, new ModelTexture(loader.loadTexture("checker"))));
		terrains.add(new Terrain(-1, 0, loader, new ModelTexture(loader.loadTexture("checker"))));
		terrains.add(new LandingStrip(-0.5f, -1, loader, new ModelTexture(loader.loadTexture("landing"))));

		Camera camera = new Camera(200, 200);
		camera.setPosition(activeDrone.getPosition().translate(0, 0, 0));
		renderer = new MasterRenderer();

		// Cube Render
		cubeShader = new CubeShader();
		cubeRenderer = new CubeRenderer(cubeShader, 120, 120);

		Cube c = new Cube(1, 1, 0);
		RawCubeModel cube = loader.loadToVAO(c.positions, c.colors);
		cubes.add(new Entity(cube, new Matrix4f().translate(new Vector3f(0, 20, -480)), 1));
		cubes.add(new Entity(cube, new Matrix4f().translate(new Vector3f(0, 20, -560)), 1));
		cubes.add(new Entity(cube, new Matrix4f().translate(new Vector3f(0, 20, -640)), 1));
		cubes.add(new Entity(cube, new Matrix4f().translate(new Vector3f(0, 20, -720)), 1));
		cubes.add(new Entity(cube, new Matrix4f().translate(new Vector3f(0, 20, -800)), 1));
		cubes.add(new Entity(cube, new Matrix4f().translate(new Vector3f(0, 20, -880)), 1));

		// ***INITIALIZE BUTTONS GUI***
		List<GuiTexture> guis = new ArrayList<>();
		GuiRenderer guiRenderer = new GuiRenderer(loader);

		List<GuiTexture> droneTextures = new ArrayList<>();

		createOpenFileButton();
		openFile.show(guis);

		createRandomCubeButton();
		randomCubes.show(guis);

		BufferedImage i = null;
		try {
			i = ImageIO.read(new File("res/grass.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		float s = (60 / i.getHeight());
		s = 0.01f;

		MiniMap minimap = new MiniMap();

		while (!Display.isCloseRequested()) {
			if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
				camera.takeSnapshot();
			}

			switch (currentView) {
			case MINIMAP:
				minimap.render(drones, guiRenderer, loader);
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

				// GUI
				GL11.glViewport(0, 0, 1280, 700);
				GL11.glScissor(0, 0, 1280, 700);
				GL11.glEnable(GL11.GL_SCISSOR_TEST);

				speed = String.valueOf(Math.round(activeDrone.getAbsVelocity()));
				textSpeed.setString("Speed = " + speed + "m/s");
				TextMaster.loadText(textSpeed);

				xpos = String.valueOf(Math.round(activeDrone.getPosition().x));
				ypos = String.valueOf(Math.round(activeDrone.getPosition().y));
				zpos = String.valueOf(Math.round(activeDrone.getPosition().z));
				textPosition.setString("Position = (" + xpos + " , " + ypos + " , " + zpos + ")");
				TextMaster.loadText(textPosition);

				leftWingInc = String.valueOf(Math.round(activeDrone.getLeftWing().getInclination() * 100.0) / 100.0);
				textLeftWing.setString("Left wing inclination = " + leftWingInc + "rad");
				TextMaster.loadText(textLeftWing);

				rightWingInc = String.valueOf(Math.round(activeDrone.getRightWing().getInclination() * 100.0) / 100.0);
				textRightWing.setString("Right wing inclination = " + rightWingInc + "rad");
				TextMaster.loadText(textRightWing);

				horzStab = String.valueOf(Math.round(activeDrone.getHorStabilizer().getInclination() * 100.0) / 100.0);
				textHorzStab.setString("Horizontal stabilizer inclination = " + horzStab + "rad");
				TextMaster.loadText(textHorzStab);

				vertStab = String.valueOf(Math.round(activeDrone.getVertStabilizer().getInclination() * 100.0) / 100.0);
				textVertStab.setString("Vertical stabilizer inclination = " + vertStab + "rad");
				TextMaster.loadText(textVertStab);

				TextMaster.render();
				TextMaster.removeAll();

				// ***BUTTON GUI***
				GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
				GL11.glScissor(0, 0, Display.getWidth(), Display.getHeight());
				GL11.glEnable(GL11.GL_SCISSOR_TEST);

				guiRenderer.render(guis);
				openFile.checkHover();
				randomCubes.checkHover();
				break;
			}

			// ***UPDATES***
			float dt = DisplayManager.getFrameTimeSeconds();
			if (!entities.isEmpty() && dt > 0.00001) {

				// De lijst waarin alle threads gestoken worden om later te checken of ze klaar
				// zijn.
				ArrayList<Future<?>> futureList = new ArrayList<Future<?>>();

				// Maak een thread aan voor elke drone
				for (Drone d : drones) {
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
				for (Drone d : drones) {
					Runnable toRun = new Runnable() {
						@Override
						public void run() {
							// Autopilot stuff
							AutopilotInputs inputs = d.getAutoPilotInputs();
							AutopilotOutputs outputs = d.getAutopilot().timePassed(inputs);
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
		for (Entity entity : entities) {
					renderer.processEntity(entity);
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
		} else if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
			reset();
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
		lLock = false;
		sLock = false;
	}

	private static void reset() {
		// Reset Cubes & Display
		generateRandomCubes();
		// DisplayManager.reset();

		// Reset Cameras
		chaseCameraLocked = true;
		// viewState = ViewStates.CHASE;

		entities.remove(activeDrone);
		RawModel droneModel = OBJLoader.loadObjModel("untitled5", loader);
		TexturedModel staticDroneModel = new TexturedModel(droneModel,
				new ModelTexture(loader.loadTexture("untitled")));
		activeDrone = new Drone(staticDroneModel, new Matrix4f().translate(new Vector3f(0,
				(int) PhysicsEngine.groundLevel - autopilotConfig.getWheelY() + autopilotConfig.getTyreRadius(), 0)),
				1f, autopilotConfig, new EulerPrediction(STEP_TIME));
		// drone.getPose().rotate((float) -(Math.PI/2), new Vector3f(1,0,0));
		entities.add(activeDrone);

		// Reset AP
		autopilot = AutopilotFactory.createAutopilot();
		autopilot.simulationStarted(autopilotConfig, activeDrone.getAutoPilotInputs());
	}

	public static void generateRandomCubes() {
		Random r = new Random();
		cubes = new ArrayList<>();

		float prevX = 0.0f;
		float prevY = 0.0f;

		for (int i = 1; i <= 5; i++) {
			Cube c = null;

			switch (i) {
			case 1:
				c = new Cube(1, 0, 0);
				break;
			case 2:
				c = new Cube(0, 1, 0);
				break;
			case 3:
				c = new Cube(1, 0, 0);
				break;
			case 4:
				c = new Cube(1, 1, 0);
				break;
			case 5:
				c = new Cube(0, 1, 1);
				break;
			default:
				break;
			}

			RawCubeModel model = loader.loadToVAO(c.positions, c.colors);
			float x = r.nextFloat() * 20 - 10;
			x = 0;
			float y = ((float) r.nextInt(20));
			float z = i * -40;
			Vector3f position = new Vector3f(x, y, z);

			while (Math.sqrt(Math.pow(x - prevX, 2) + Math.pow(y - prevY, 2)) > 10) {
				x = r.nextFloat() * 20 - 10;
				// x = 0;
				y = ((float) r.nextInt(1000) / 500 - 1) * 10;
			}

			prevX = x;
			prevY = y;

			cubes.add(new Entity(model, new Matrix4f().translate(position), 1));
		}

	}

	private static void createOpenFileButton() {
		JFileChooser fc = new JFileChooser();
		float normalizedX = -1.0f + 2.0f * (float) 1200 / (float) Display.getWidth();
		float normalizedY = 1.0f - 2.0f * (float) 20 / (float) Display.getHeight();

		openFile = new Button(loader, "openfile", new Vector2f(normalizedX, normalizedY), new Vector2f(0.05f, 0.05f)) {
			@Override
			public void whileHover() {
			}

			@Override
			public void stopHover() {
				this.setScale(new Vector2f(0.05f, 0.05f));
			}

			@Override
			public void startHover() {
				this.playHoverAnimation(0.01f);
			}

			@Override
			public void onClick() {
				this.playerClickAnimation(0.02f);

				int returnVal = fc.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					// Read file and load cubes
					loadCubes(file);
				} else {
					System.out.println("Open command cancelled by user.");
				}
			}
		};
	}

	private static void createRandomCubeButton() {
		randomCubes = new Button(loader, "random", new Vector2f(0.9f, 0.75f), new Vector2f(0.05f, 0.05f)) {
			@Override
			public void whileHover() {
			}

			@Override
			public void stopHover() {
			}

			@Override
			public void startHover() {
			}

			@Override
			public void onClick() {
				generateRandomCubes();
			}
		};
	}

	public static void loadCubes(File file) {
		// reset entities first
		cubes = new ArrayList<>();

		int count = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for (String line; (line = br.readLine()) != null;) {
				String[] s = line.split(" ");
				float x = Float.parseFloat(s[0]);
				float y = Float.parseFloat(s[1]);
				float z = Float.parseFloat(s[2]);

				Cube c = null;

				switch (count) {
				case 0:
					c = new Cube(1, 0, 0);
					break;
				case 1:
					c = new Cube(0, 1, 0);
					break;
				case 2:
					c = new Cube(1, 0, 1);
					break;
				case 3:
					c = new Cube(1, 1, 0);
					break;
				case 4:
					c = new Cube(0, 1, 1);
					break;
				default:
					break;
				}
				count++;

				RawCubeModel model = loader.loadToVAO(c.positions, c.colors);

				cubes.add(new Entity(model, new Matrix4f().translate(new Vector3f(x, y, z)), 1));
			}
			// line is not visible here.
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
