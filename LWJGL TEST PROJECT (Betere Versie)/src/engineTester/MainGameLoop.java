package engineTester;

import models.Model2D;
import models.RawModel;
import models.TexturedModel;
import physicsEngine.PhysicsEngine;
import physicsEngine.approximationMethods.EulerPrediction;
import physicsEngine.approximationMethods.PredictionMethod;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.opencv.core.Core;

import autoPilotJar.SimpleAutopilot;
import autopilot.AutopilotConfigReader;
import autopilot.AutopilotConfigValues;
import autopilot.AutopilotConfigWriter;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.Renderer;
import shaders.StaticShader;
import testObjects.Cube;
import testObjects.Cuboid;
import textures.ModelTexture;
import entities.Camera;
import entities.Drone;
import entities.Entity;
import entities.cubeTestPlayer;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRendering.TextMaster;
import guis.GuiRenderer;
import guis.GuiTexture;
import interfaces.Autopilot;
import interfaces.AutopilotConfig;
import interfaces.AutopilotFactory;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

public class MainGameLoop {

	private static final float STEP_TIME = 0.001f;

	public static AutopilotConfig autopilotConfig;
	
	private static Drone drone;
	
	private static Camera freeRoamCamera;
	private static boolean freeRoamCameraLocked = true;
	
	private static boolean oLock = false;
	private static boolean lLock = false;
	private static boolean sLock = false;
	
	private static Entity redCube;
	private static List<Entity> entities;
	
	public static Loader loader;
	
	//TODO main opruimen, code eruit halen
	
	private static ViewStates viewState = ViewStates.CHASE;
	private static enum ViewStates {
			CHASE, ORTHO
	};
	
	public static void main(String[] args) throws IOException {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		/*
		 * Start reading AutopilotConfig.cfg
		 */
		File config = new File("res/AutopilotConfig.cfg");
		
		try {
			//Read the config file
			DataInputStream inputStream = new DataInputStream(new FileInputStream(config));
			autopilotConfig = AutopilotConfigReader.read(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Create the display AKA the screen
		DisplayManager.createDisplay();
		//Loader is used to load models using VAO's and VBO's
		loader = new Loader();
		TextMaster.init(loader);
		
		StaticShader shader = new StaticShader();
		StaticShader shaderFreeCam = new StaticShader();
		StaticShader shaderText = new StaticShader();
		StaticShader shaderTopDown = new StaticShader();
		StaticShader shaderSideView = new StaticShader();
		// Renderer based on FOVX and FOVY
		Renderer renderer = new Renderer(shader, autopilotConfig.getHorizontalAngleOfView(), autopilotConfig.getVerticalAngleOfView());
		Renderer rendererFreeCam = new Renderer(shaderFreeCam, 50, 50);
		Renderer rendererText = new Renderer(shaderText, 50, 50);
		Renderer renderTopDown = new Renderer(shaderTopDown, 40, 40);
		Renderer renderSideView = new Renderer(shaderSideView, 40, 40);
		
		//FreeRoam Camera
		freeRoamCamera = new Camera();
		freeRoamCamera.setPosition(new Vector3f(0, 100, 0));
		//freeRoamCamera.setYaw(-45);
		
		//TopDown camera
		Camera topDownCamera = new Camera();
		topDownCamera.setPosition(new Vector3f(0, 300, -100));
		topDownCamera.setRotation((float) -(Math.PI / 2), 0, 0);
		
		//Sideview Camera
		Camera sideViewCamera = new Camera();
		sideViewCamera.setPosition(new Vector3f(300,0,-100));
		sideViewCamera.setRotation(0, (float) -(Math.PI / 2), 0);
		
		//Creating 10 test cubes
		entities = new ArrayList<>();
//		Random r = new Random();
//		for (int i = 0; i < 10; i++) {
//			Cube c = new Cube(r.nextFloat(), r.nextFloat(), r.nextFloat());
//			RawModel model = loader.loadToVAO(c.positions, c.colors, null);
//			//TexturedModel staticModel = new TexturedModel(model,new ModelTexture(loader.loadTexture("image")));
//			entities.add(new cubeTestPlayer(model, 
//					new Vector3f(r.nextFloat()*20-10,r.nextFloat()*10,r.nextFloat()*-90-10),0, 0, 0, 1));
//		}
		
		Cube c = new Cube(1, 0, 0);
		RawModel redCubeModel = loader.loadToVAO(c.positions, c.colors, null);
		redCube = new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(-10,30,-50)) , 1);
		
		//entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(-10,0,-40)), 1));
		
		//WORKING DEMO
		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(0,-10,-40)), 1));
		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(0,0,-80)), 1));
		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(0,-5,-120)), 1));
		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(0,8,-160)), 1));
		entities.add(new Entity(redCubeModel, new Matrix4f().translate(new Vector3f(0,-2,-200)), 1));
		
		Cuboid droneCube = new Cuboid(0, 0, 0);
		drone = new Drone(loader.loadToVAO(droneCube.positions, droneCube.colors, null),
				new Matrix4f().translate(new Vector3f(0, 0, 0)), 1, autopilotConfig, new EulerPrediction(STEP_TIME));
		
		//Autopilot stuff
		Autopilot autopilot = AutopilotFactory.createAutopilot();
		autopilot.simulationStarted(autopilotConfig, drone.getAutoPilotInputs());
		
		//GUI
		List<GuiTexture> guis = new ArrayList<>();
		guis.add(new GuiTexture(loader.loadTexture("openfile"), new Vector2f(0.95f, 0.95f),  new Vector2f(0.05f, 0.05f)));
		
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		
		while(!Display.isCloseRequested()){
			//RENDER BUTTONS

			//Drone Camera View
			drone.getCamera().setPosition(drone.getPosition());	
			GL11.glViewport(0, 0, 200, 200);
			GL11.glScissor(0,0,200,200);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			renderer.prepare();
			shader.start();
			shader.loadViewMatrix(drone.getCamera());
			renderView(renderer, shader);
			
			if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
				drone.getCamera().takeSnapshot();
			}
			
			switch (viewState) {
			case CHASE:
				//3rd Person View (FreeCam)
				GL11.glViewport(200+1, 0, Display.getWidth() - 201, Display.getHeight());
				GL11.glScissor(200+1, 0, Display.getWidth()- 201, Display.getHeight());
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				renderSideView.prepareText();
				shaderFreeCam.start();
				shaderFreeCam.loadViewMatrix(freeRoamCamera);
				renderView(rendererFreeCam, shaderFreeCam);
				guiRenderer.render(guis);

				break;
			case ORTHO:
				GL11.glViewport(200+1, 0, Display.getWidth() - 201, Display.getHeight());
				GL11.glScissor(200+1, 0, Display.getWidth()- 201, Display.getHeight());
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				renderTopDown.prepareText();
				renderTopDown.prepare();
				
				//TopDown View
				GL11.glViewport(200 + 1, Display.getHeight()/2 + 1 ,Display.getWidth() - 201, Display.getHeight()/2);
				GL11.glScissor(200 + 1, Display.getHeight()/2  + 1, Display.getWidth() - 201, Display.getHeight()/2);
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				renderTopDown.prepare();
				shaderTopDown.start();
				shaderTopDown.loadViewMatrix(topDownCamera);
				
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				GL11.glOrtho(200+1, Display.getWidth(), Display.getHeight(), Display.getHeight()/2 + 1, 1, -1);
				GL11.glMatrixMode(GL11.GL_MODELVIEW);

				renderView(renderTopDown, shaderTopDown);
				
				//SideView
				GL11.glViewport(200 + 1, 0,Display.getWidth() - 201, Display.getHeight()/2);
				GL11.glScissor(200 + 1, 0 ,Display.getWidth() - 201, Display.getHeight()/2);
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				renderSideView.prepareText();
				shaderSideView.start();
				shaderSideView.loadViewMatrix(sideViewCamera);
				
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				GL11.glOrtho(200+1, Display.getWidth(), Display.getHeight()/2, 0, 1, -1);
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				
				renderView(renderSideView, shaderSideView);
				break;
				default:
					System.out.println("ERROR");
					break;
			}
			
			
			//GUI View
			GL11.glViewport(0, 200, 200, Display.getHeight() - 200);
			GL11.glScissor(0, 200, 200, Display.getHeight() - 200);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			rendererText.prepareDroneCamera();
			
			// snelheid van de drone
			String speed = String.valueOf(Math.round(drone.getAbsVelocity()));
			FontType font = new FontType(loader.loadTexture("verdana"), new File("res/verdana.fnt"));
			GUIText textSpeed = new GUIText("Speed = " + speed + "m/s", 5, font, new Vector2f(0.01f,0), 1, true);
			textSpeed.setColour(1, 1, 1);
			
			// positie van de drone
			Vector3f Dposition = drone.getPosition();
			String xpos = String.valueOf(Math.round(Dposition.x));
			String ypos = String.valueOf(Math.round(Dposition.y));
			String zpos = String.valueOf(Math.round(Dposition.z));
			GUIText textPosition = new GUIText("Position = ("+xpos+" , "+ypos+" , "+zpos +")" , 5, font, new Vector2f(0.01f,0.2f), 1, true);
			textPosition.setColour(1, 1, 1);
			
			float dt = DisplayManager.getFrameTimeSeconds();
			System.out.println(DisplayManager.getElapsedTime());
			if(!entities.isEmpty()) {
				
				//applyphysics rekent de krachten uit en gaat dan de kinematische waarden van de drone
				// aanpassen op basis daarvan 
				PhysicsEngine.applyPhysics(drone, dt);
				
				//Autopilot stuff
				AutopilotInputs inputs = drone.getAutoPilotInputs();
				AutopilotOutputs outputs = autopilot.timePassed(inputs);
				drone.setAutopilotOutouts(outputs);
			}
			
			TextMaster.render();
			
			// de tekst moet telkens worden verwijderd, anders wordt er elke loop nieuwe tekst overgeprint (=> onleesbaar)
			TextMaster.removeText(textSpeed);
			TextMaster.removeText(textPosition);
			
			keyInputs();
			removeCubes();
			
			shader.stop();
			DisplayManager.updateDisplay();
		}

		guiRenderer.cleanUp();
		TextMaster.cleanUp();
		shader.cleanUp();
		shaderFreeCam.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}
	
	private static void removeCubes() {
		List<Entity> toRemove = new ArrayList<>();
		
		for (Entity e : entities) {
			if ( Math.abs(Math.sqrt(Math.pow(drone.getPosition().x - e.getPosition().x, 2) +
					Math.pow(drone.getPosition().y - e.getPosition().y, 2) +
					Math.pow(drone.getPosition().z - e.getPosition().z, 2))) <= 4) {
				toRemove.add(e);
			}
		}
		
		entities.removeAll(toRemove);
	}
	
	public static void renderView(Renderer renderer, StaticShader shader) {
		for (Entity entity : entities) {
			renderer.render(entity,shader);
		} 
		
		renderer.render(drone, shader);
	}
	
	public static void keyInputs() {
		if (Keyboard.isKeyDown(Keyboard.KEY_Y)) {
			Vector3f.add(drone.getPosition(), new Vector3f(0, 150, -50), freeRoamCamera.getPosition());
			freeRoamCamera.setRotation((float) -(Math.PI / 2), 0, 0);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
			Vector3f.add(drone.getPosition(), new Vector3f(100, 0, 0), freeRoamCamera.getPosition());
			freeRoamCamera.setRotation(0, (float) -(Math.PI / 2), 0);
		} else if(Keyboard.isKeyDown(Keyboard.KEY_L)) {
			/* Lock/Unlock on Third Person Camera */
			if (!lLock) {
				freeRoamCameraLocked = !freeRoamCameraLocked;
			}
			lLock = true;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
			
			if (!oLock)
				if (viewState == ViewStates.ORTHO) {
					viewState = ViewStates.CHASE;
				} else {
					viewState = ViewStates.ORTHO;
				}
			
			oLock = true;
		} else if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
			if (!sLock) {
				DisplayManager.start();
			}
			
			sLock = true;
		}else {
			if (freeRoamCameraLocked) {
				Vector3f.add(drone.getPosition(), new Vector3f(0, 0, 30), freeRoamCamera.getPosition());
				//freeRoamCamera.setRotation((float) -(Math.PI/6), 0, 0);
			} else {
				freeRoamCamera.roam();
			}
			
			lLock = false;
			oLock = false;
			sLock = false;
		}
	}
}
