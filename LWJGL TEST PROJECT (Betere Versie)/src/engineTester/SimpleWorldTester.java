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
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import renderEngine.EntityRenderer;
import shaders.StaticShader;
import textures.ModelTexture;

public class SimpleWorldTester {

	private static Camera camera;
	
	public static void main(String[] args) {
		DisplayManager.createDisplay();
	
		camera = new Camera();
		
		Loader loader = new Loader();
		
		RawOBJModel model = OBJLoader.loadObjModel("dragon", loader);
		TexturedModel tModel = new TexturedModel(model, new ModelTexture(loader.loadTexture("dragonTexture")));
		
		Entity e = new Entity(tModel, new Matrix4f().translate(new Vector3f(0,0,-10)), 1);
		e.setModel(null);
		e.setOBJModel(model);
		
		MasterRenderer masterRenderer = new MasterRenderer();
		while(!Display.isCloseRequested()){
			e.getPose().rotate((float) (Math.PI/180), new Vector3f(0,1,0));
			
			masterRenderer.processEntity(e);
			
			masterRenderer.render(camera);
			DisplayManager.updateDisplay();
		}
		
		masterRenderer.cleanUp();
		//loader.cleanUp();
		DisplayManager.closeDisplay();
	}

}
