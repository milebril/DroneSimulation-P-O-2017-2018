package engineTester;

import models.RawModel;
import models.TexturedModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.Renderer;
import shaders.StaticShader;
import testObjects.Cube;
import textures.ModelTexture;
import toolbox.generateColors;
import entities.Camera;
import entities.Entity;

public class MainGameLoop {

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		Loader loader = new Loader();
		StaticShader shader = new StaticShader();
		Renderer renderer = new Renderer(shader);
		
		
		Random r = new Random();
		List<Entity> entities = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
		
			Cube c = new Cube(r.nextFloat(), r.nextFloat(), r.nextFloat());
			
		RawModel model = loader.loadToVAO(c.positions, c.colors, null);
		
		TexturedModel staticModel = new TexturedModel(model,new ModelTexture(loader.loadTexture("image")));
		
		entities.add(new Entity(staticModel, 
				new Vector3f(r.nextFloat()*100-50,r.nextFloat()*100-50,r.nextFloat()*-300),
				0, 0, 0, 1));
		
		}
		Camera camera = new Camera();
		
		while(!Display.isCloseRequested()){
			//entity.increaseRotation(1, 1, 0);
			camera.move();
			renderer.prepare();
			shader.start();
			shader.loadViewMatrix(camera);
			for (Entity entity : entities) {
				//entity.increaseRotation(1, 1, 0);
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
