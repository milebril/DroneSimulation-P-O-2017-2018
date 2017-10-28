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
import org.lwjgl.util.vector.Vector3f;

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

public class MainGameLoop {

	public static AutopilotConfig autopilotConfig;
	
	public static List<Renderer> renderers = new ArrayList<Renderer>();
	
	public static long elapsedTime = 0;
	
	public static void main(String[] args) {

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
		StaticShader shader = new StaticShader();
		StaticShader shaderFreeCam = new StaticShader();
		// Renderer based on FOVX and FOVY
		Renderer renderer = new Renderer(shader, autopilotConfig.getHorizontalAngleOfView(), autopilotConfig.getVerticalAngleOfView());
		Renderer rendererFreeCam = new Renderer(shaderFreeCam, 120, 120);
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
		Entity e = new Entity(model, 
				new Vector3f(0,0,-100),0, 0, 0, 1);
		
		Cube droneCube = new Cube(0, 0, 0);
		Drone drone = new Drone(loader.loadToVAO(droneCube.positions, droneCube.colors, null),
				new Vector3f(0, 30, 0), 0, 0, 0, 4, autopilotConfig);
		
		Camera camera = new Camera();
		camera.setPosition(new Vector3f(0, 300, -100));
		camera.setYaw(-45);
		
		while(!Display.isCloseRequested()){
			GL11.glViewport(0, 0, 200, 200);
			GL11.glScissor(0,0,200,200);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			renderer.prepare();
			shader.start();
			shader.loadViewMatrix(drone.getCamera());
			
			if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
				drone.getCamera().takeSnapshot();
			}
			
			for (Entity entity : entities) {
				renderer.render(entity,shader);
			} 
			renderer.render(e, shader);
			renderer.render(drone, shader);
			
			/* 3rd person */
			GL11.glViewport(200, 0, Display.getWidth() - 200, Display.getHeight());
			GL11.glScissor(200, 0, Display.getWidth() - 200, Display.getHeight());
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			rendererFreeCam.prepare();
			shaderFreeCam.start();
			shaderFreeCam.loadViewMatrix(camera);
			
			for (Entity entity : entities) {
				rendererFreeCam.render(entity,shaderFreeCam);
			} 
			rendererFreeCam.render(e, shaderFreeCam);
			rendererFreeCam.render(drone, shaderFreeCam);
			
			
			float dt = DisplayManager.getFrameTimeSeconds();
			drone.increasePosition(dt);
			drone.applyForces(dt);
			
			if(Math.abs(Math.sqrt(Math.pow(drone.getPosition().x - e.getPosition().x, 2) +
					Math.pow(drone.getPosition().y - e.getPosition().y, 2) +
					Math.pow(drone.getPosition().z - e.getPosition().z, 2))) < 4) {
				break;
			}
			
			/* Drone Debug */
			//drone.moveHeadingVector();
			
			if (drone.getPosition().z < -1000) {
				break;
			}
			
			shader.stop();
			DisplayManager.updateDisplay();
		}

		shader.cleanUp();
		shaderFreeCam.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
