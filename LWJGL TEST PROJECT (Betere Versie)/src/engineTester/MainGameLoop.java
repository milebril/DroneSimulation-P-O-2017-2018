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

import org.lwjgl.opengl.Display;
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
import textures.ModelTexture;
import entities.Camera;
import entities.Entity;

public class MainGameLoop {

	public static AutopilotConfig autopilotConfig;
	
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
		
		//Createinfg the display AKA the screen
		DisplayManager.createDisplay();
		//Loader is used to load models using VAO's and VBO's
		Loader loader = new Loader();
		//
		StaticShader shader = new StaticShader();
		// Renderer based on FOVX and FOVY
		Renderer renderer = new Renderer(shader, autopilotConfig.getHorizontalAngleOfView(), autopilotConfig.getVerticalAngleOfView());
		
		//Creating 1000 test cubes
		Random r = new Random();
		List<Entity> entities = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			Cube c = new Cube(r.nextFloat(), r.nextFloat(), r.nextFloat());
			RawModel model = loader.loadToVAO(c.positions, c.colors, null);
			TexturedModel staticModel = new TexturedModel(model,new ModelTexture(loader.loadTexture("image")));
			entities.add(new Entity(staticModel, 
					new Vector3f(r.nextFloat()*100-50,r.nextFloat()*100-50,r.nextFloat()*-300),0, 0, 0, 1));
		}
		
		Camera camera = new Camera(autopilotConfig.getNbColumns(), autopilotConfig.getNbRows());
		
		while(!Display.isCloseRequested()){
			//entity.increaseRotation(1, 1, 0);
			camera.move();
			renderer.prepare();
			shader.start();
			shader.loadViewMatrix(camera);
			for (Entity entity : entities) {
				//entity.increaseRotation(1, 0, 0);
				renderer.render(entity,shader);
			}
			shader.stop();
			DisplayManager.updateDisplay();
		}

		shader.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
