package models;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import autopilot.interfaces.AutopilotConfig;
import entities.Camera;
import entities.Entity;
import entities.Gate;
import entities.Light;
import physicsEngine.PhysicsEngine;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.LandingStrip;
import terrains.Terrain;
import textures.ModelTexture;

public class Airport {

	// Airport has 2 Landing strips and 2 Gates
	
	private final int airportID;

	private Gate leftGate;
	private Gate rightGate;

	private LandingStrip landingStrip;
	private LandingStrip landingStrip2;
	
	private int x;
	private int z;

	public Airport(int x, int z, int airportId) {
		
		this.x = x;
		this.z = z;
		airportID = airportId;
		Loader loader = new Loader();

		RawModel gateModel = OBJLoader.loadObjModel("gate10", loader);
		TexturedModel staticGateModel = new TexturedModel(gateModel,
				new ModelTexture(loader.loadTexture("gate.blauw")));

		leftGate = new Gate(staticGateModel, new Matrix4f().translate(new Vector3f(x, 1, z)), 1, airportId, 1);
		rightGate = new Gate(staticGateModel, new Matrix4f().translate(new Vector3f(x + 40, 1, z)), 1, airportId, 0);

		landingStrip = new LandingStrip(x - 30, z - 400, loader, new ModelTexture(loader.loadTexture("landing")), airportId, 0);
		landingStrip2 = new LandingStrip(x - 30, z, loader, new ModelTexture(loader.loadTexture("landing")),airportId, 1);
	}

	public void render(MasterRenderer renderer, Camera camera, Light light) {
		renderer.processTerrain(landingStrip);
		renderer.processTerrain(landingStrip2);
		renderer.processEntity(leftGate);
		renderer.processEntity(rightGate);
	}

	public int getAirportID() {
		return airportID;
	}
	
	public Gate getGate(int gate) {
		if (this.leftGate.getGateID() == gate)
			return leftGate;
		return rightGate; // default gate is rightGate
	}
	
	public Matrix4f getDronePosition(int gateID, AutopilotConfig config) {
		
		return new Matrix4f().translate(new Vector3f(0 + 40*gateID + x,(int) PhysicsEngine.groundLevel - config.getWheelY()
								+ config.getTyreRadius(),0 + 10 + z)); //TODO: juiste positie voor x en z
	}

}
