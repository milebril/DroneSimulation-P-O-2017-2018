package engineTester;

import models.RawModel;
import models.TexturedModel;

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
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.opencv.core.Core;

import autoPilotJar.AutoPilot;
import autopilot.AutopilotConfig;
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

public class MainGameLoop {

	public static AutopilotConfig autopilotConfig;
	
	public static List<Renderer> renderers = new ArrayList<Renderer>();
	
	public static long elapsedTime = 0;
	
	public static void main(String[] args) throws IOException {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		/*
		 * Start reading AutopilotConfig.cfg
		 */
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
		
		//Create the display AKA the screen
		DisplayManager.createDisplay();
		//Loader is used to load models using VAO's and VBO's
		Loader loader = new Loader();
		//
		TextMaster.init(loader);
		
		
		
		StaticShader shader = new StaticShader();
		StaticShader shaderFreeCam = new StaticShader();
		StaticShader shaderText = new StaticShader();
		// Renderer based on FOVX and FOVY
		Renderer renderer = new Renderer(shader, autopilotConfig.getHorizontalAngleOfView(), autopilotConfig.getVerticalAngleOfView());
		Renderer rendererFreeCam = new Renderer(shaderFreeCam, 120, 120);
		Renderer rendererText = new Renderer(shaderText, 120, 120);
		renderers.add(renderer);
		renderers.add(rendererFreeCam);
		
		//Creating 1000 test cubes
		Random r = new Random();
		List<Entity> entities = new ArrayList<>();
		for (int i = 0; i < 2000; i++) {
			Cuboid c = new Cuboid(r.nextFloat(), r.nextFloat(), r.nextFloat());
			RawModel model = loader.loadToVAO(c.positions, c.colors, null);
			//TexturedModel staticModel = new TexturedModel(model,new ModelTexture(loader.loadTexture("image")));
			entities.add(new cubeTestPlayer(model, 
					new Vector3f(r.nextFloat()*200-100,r.nextFloat()*200-100,r.nextFloat()*-1000),0, 0, 0, 1));
		}
		
		Cube c = new Cube(1, 0, 0);
		RawModel model = loader.loadToVAO(c.positions, c.colors, null);
/*		Entity e = new Entity(model, 
				new Vector3f(10,30,-50),0, 0, 0, 1);*/
		
		Entity e = new Entity(model, 
				new Vector3f(0,30,-50),0, 0, 0, 1);
		
/*		Entity e = new Entity(model, 
				new Vector3f(-10,30,-50),0, 0, 0, 1);*/
		
		Cuboid droneCube = new Cuboid(0, 0, 0);
		Drone drone = new Drone(loader.loadToVAO(droneCube.positions, droneCube.colors, null),
				new Vector3f(0, 30, 0), 0, 0, 0, 2, autopilotConfig);
		AutoPilot ap = new AutoPilot();
		
		//kaka
		Camera camera = new Camera();
		camera.setPosition(new Vector3f(0, 300, -100));
		camera.setYaw(-45);
		
		Camera camera2 = new Camera();
		
		while(!Display.isCloseRequested()){
			GL11.glViewport(0, 0, 200, 200);
			GL11.glScissor(0,0,200,200);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			renderer.prepare();
			shader.start();
			shader.loadViewMatrix(drone.getCamera());
			
//			for (Entity entity : entities) {
//				renderer.render(entity,shader);
//			} 
			renderer.render(e, shader);
			renderer.render(drone, shader);
			
			if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
				drone.getCamera().takeSnapshot();
			}
			
			
			GL11.glViewport(200, 0, Display.getWidth() - 200, Display.getHeight());
			GL11.glScissor(200, 0, Display.getWidth() - 200, Display.getHeight());
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			rendererFreeCam.prepareText();
			shaderFreeCam.start();
			shaderFreeCam.loadViewMatrix(camera);
			/* 3rd person */


			
//			for (Entity entity : entities) {
//				rendererFreeCam.render(entity,shaderFreeCam);
//			} 
			rendererFreeCam.render(e, shaderFreeCam);
			rendererFreeCam.render(drone, shaderFreeCam);
			
			GL11.glViewport(0, 200, 200, Display.getHeight() - 200);
			GL11.glScissor(0, 200, 200, Display.getHeight() - 200);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			rendererText.prepare();
			shaderText.start();
			shaderText.loadViewMatrix(camera2);
			
			rendererText.render(e, shaderText);
			
			/* GUI */
			// GUIText: 1ste argument is de string die geprint moet worden
			// 2de argument is de fontsize
			// 3de argument is het font
			// 4de argument is de positie (x,y) tussen 0 en 1, (0,0) is links van boven
			// 5de argument is de lengte van een regel (tussen 0 en 1), 1 wilt zeggen dat de tekst over heel de lengte van het 
			// 		beeld mag
			// 6de argument is gecentreerd (true) of niet (false)
			
			// snelheid van de drone
			String speed = String.valueOf(Math.round(drone.getSpeed()));
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
			
			drone.increasePosition(dt);
			drone.sendToAutopilot(dt);
			ap.getFromDrone();
			ap.sendToDrone();
			drone.getFromAutopilot();
			drone.applyForces(dt);
			
			
			if(Math.abs(Math.sqrt(Math.pow(drone.getPosition().x - e.getPosition().x, 2) +
					Math.pow(drone.getPosition().y - e.getPosition().y, 2) +
					Math.pow(drone.getPosition().z - e.getPosition().z, 2))) < 4) {
				break;
			}
			
			/* Drone Debug */
			drone.moveHeadingVector();
			
			if (drone.getPosition().z < -1000) {
				break;
			}
			
			TextMaster.render();
			
			// de tekst moet telkens worden verwijderd, anders wordt er elke loop nieuwe tekst overgeprint (=> onleesbaar)
			TextMaster.removeText(textSpeed);
			TextMaster.removeText(textPosition);
			shader.stop();
			DisplayManager.updateDisplay();
			
		}

		TextMaster.cleanUp();
		shader.cleanUp();
		shaderFreeCam.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
