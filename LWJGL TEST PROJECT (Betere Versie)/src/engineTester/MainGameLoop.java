package engineTester;

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
	private static boolean lLock = false;
	
	private static Entity redCube;
	private static List<Entity> entities;
	
	//TODO main opruimen, code eruit halen
	
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
		Loader loader = new Loader();
		TextMaster.init(loader);
		
		StaticShader shader = new StaticShader();
		StaticShader shaderFreeCam = new StaticShader();
		StaticShader shaderText = new StaticShader();
		StaticShader shaderTopDown = new StaticShader();
		StaticShader shaderSideView = new StaticShader();
		// Renderer based on FOVX and FOVY
		Renderer renderer = new Renderer(shader, autopilotConfig.getHorizontalAngleOfView(), autopilotConfig.getVerticalAngleOfView());
		Renderer rendererFreeCam = new Renderer(shaderFreeCam, 50, 50);
		Renderer rendererText = new Renderer(shaderText, 120, 120);
		Renderer renderTopDown = new Renderer(shaderTopDown, 50, 50);
		Renderer renderSideView = new Renderer(shaderSideView, 50, 50);
		
		//FreeRoam Camera
		freeRoamCamera = new Camera();
		freeRoamCamera.setPosition(new Vector3f(0, 100, 0));
		freeRoamCamera.setYaw(-45);
		
		//TopDown camera
		Camera topDownCamera = new Camera();
		topDownCamera.setPosition(new Vector3f(0, 150, -50));
		topDownCamera.setRotation((float) -(Math.PI / 2), 0, 0);
		
		//Sideview Camera
		Camera sideViewCamera = new Camera();
		sideViewCamera.setPosition(new Vector3f(150,5,-50));
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
		
		Cuboid droneCube = new Cuboid(0, 0, 0);
		drone = new Drone(loader.loadToVAO(droneCube.positions, droneCube.colors, null),
				new Matrix4f().translate(new Vector3f(0, 0, 0)), 1, autopilotConfig, new EulerPrediction(STEP_TIME));
		
		//Autopilot stuff
		Autopilot autopilot = AutopilotFactory.createAutopilot();
		autopilot.simulationStarted(autopilotConfig, drone.getAutoPilotInputs());
		
		while(!Display.isCloseRequested()){
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
			
			//3rd Person View (FreeCam)
			GL11.glViewport(200+1, 0, Display.getWidth() - 700, Display.getHeight());
			GL11.glScissor(200+1, 0, Display.getWidth() - 700, Display.getHeight());
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			rendererFreeCam.prepare();
			shaderFreeCam.start();
			shaderFreeCam.loadViewMatrix(freeRoamCamera);
			renderView(rendererFreeCam, shaderFreeCam);
			
			//TopDown View
			GL11.glViewport(Display.getWidth() - 498, Display.getHeight()/2 + 1 ,499 , Display.getHeight()/2);
			GL11.glScissor(Display.getWidth() - 498, Display.getHeight()/2  + 1, 499 , Display.getHeight()/2);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			renderTopDown.prepare();
			shaderTopDown.start();
			shaderTopDown.loadViewMatrix(topDownCamera);
			renderView(renderTopDown, shaderTopDown);
			
			//SideView
			GL11.glViewport(Display.getWidth() - 498, 0 ,500 , Display.getHeight()/2);
			GL11.glScissor(Display.getWidth() - 498, 0 ,500 , Display.getHeight()/2);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			renderSideView.prepareText();
			shaderSideView.start();
			shaderSideView.loadViewMatrix(sideViewCamera);
			renderView(renderSideView, shaderSideView);
			
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
			if(!( Math.abs(Math.sqrt(Math.pow(drone.getPosition().x - redCube.getPosition().x, 2) +
					Math.pow(drone.getPosition().y - redCube.getPosition().y, 2) +
					Math.pow(drone.getPosition().z - redCube.getPosition().z, 2))) < 4)) {
				
				//applyphysics rekent de krachten uit en gaat dan de kinematische waarden van de drone
				// aanpassen op basis daarvan 
				PhysicsEngine.applyPhysics(drone, dt);
				//System.out.println("inclination" + drone.getLeftWing().getInclination());
				//System.out.println("Speed" + drone.getAbsVelocity());
				//System.out.println("Thrustforce" + drone.getThrustForce());
				
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
			
			shader.stop();
			DisplayManager.updateDisplay();
			
		}

		TextMaster.cleanUp();
		shader.cleanUp();
		shaderFreeCam.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}
	
	public static void renderView(Renderer renderer, StaticShader shader) {
		for (Entity entity : entities) {
			renderer.render(entity,shader);
		} 
		
		renderer.render(redCube, shader);
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
		} else {
			if (freeRoamCameraLocked) {
				Vector3f.add(drone.getPosition(), new Vector3f(0, 30, 30), freeRoamCamera.getPosition());
				freeRoamCamera.setRotation((float) -(Math.PI/6), 0, 0);
			} else {
				freeRoamCamera.roam();
			}
			
			lLock = false;
		}
	}

}
