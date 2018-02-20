package engineTester;

import java.util.ArrayList;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import fontRendering.TextMaster;
import models.RawModel;
import models.RawOBJModel;
import models.TexturedModel;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.OBJLoader;
import renderEngine.Renderer;
import shaders.StaticShader;
import textures.ModelTexture;

public class SimpleWorldTester {

	private static Renderer renderer;
	private static StaticShader shader;
	private static Camera camera;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		DisplayManager.createDisplay();
		
		shader = new StaticShader();
		renderer = new Renderer(shader, 60, 60);
		camera = new Camera();
		
		Loader loader = new Loader();
		
		RawOBJModel model = OBJLoader.loadOBJModel("stall", loader);
		
		TexturedModel tModel = new TexturedModel(model, new ModelTexture(loader.loadTexture("stallTexture")));
		Entity e = new Entity(tModel, new Matrix4f().translate(new Vector3f(0,0,-10)), 1);
		e.setModel(null);
		e.setOBJModel(model);
		
		while(!Display.isCloseRequested()){
			e.getPose().rotate((float) (Math.PI/180), new Vector3f(0,1,0));
			renderer.prepare();
			shader.start();
			shader.loadViewMatrix(camera);
			renderer.render(e, shader);
			
			shader.stop();
			DisplayManager.updateDisplay();
		}
		
		shader.cleanUp();
		DisplayManager.closeDisplay();
	}

}
