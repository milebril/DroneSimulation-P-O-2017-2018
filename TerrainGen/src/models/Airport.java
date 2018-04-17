package models;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.LandingStrip;
import terrains.Terrain;
import textures.ModelTexture;

public class Airport {

	// Airport has 2 Landing strips and 2 Gates

	private Entity leftGate;
	private Entity rightGate;

	private LandingStrip landingStrip;
	private LandingStrip landingStrip2;

	public Airport(int x, int z) {
		Loader loader = new Loader();

		RawModel gateModel = OBJLoader.loadObjModel("gate10", loader);
		TexturedModel staticGateModel = new TexturedModel(gateModel,
				new ModelTexture(loader.loadTexture("gate.blauw")));

		leftGate = new Entity(staticGateModel, new Matrix4f().translate(new Vector3f(x, 1, z)), 1);
		rightGate = new Entity(staticGateModel, new Matrix4f().translate(new Vector3f(x + 40, 1, z)), 1);

		landingStrip = new LandingStrip(x - 30, z - 400, loader, new ModelTexture(loader.loadTexture("landing")));
		landingStrip2 = new LandingStrip(x - 30, z, loader, new ModelTexture(loader.loadTexture("landing")));
	}

	public void render(MasterRenderer renderer, Camera camera, Light light) {
		renderer.processTerrain(landingStrip);
		renderer.processTerrain(landingStrip2);
		renderer.processEntity(leftGate);
		renderer.processEntity(rightGate);
	}

}
