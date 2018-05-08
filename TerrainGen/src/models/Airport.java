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

	private Vector3f position;

	public Airport(int x, int z, int airportId, String rotation) {
		
		this.x = x;
		this.z = z;
		airportID = airportId;
		Loader loader = new Loader();

		RawModel gateModel = OBJLoader.loadObjModel("gate10", loader);
		TexturedModel staticGateModel = new TexturedModel(gateModel,
				new ModelTexture(loader.loadTexture("gate.blauw")));

		if (rotation.equals("block")) {
			leftGate = new Gate(staticGateModel, new Matrix4f().translate(new Vector3f(x, 1, z)), 1, airportId, 0);
			rightGate = new Gate(staticGateModel, new Matrix4f().translate(new Vector3f(x, 1, z + 40)), 1, airportId, 1);
			leftGate.rotate((float) (Math.PI/2), new Vector3f(0, 1, 0));
			rightGate.rotate((float) (Math.PI/2), new Vector3f(0, 1, 0));
			
			landingStrip = new LandingStrip(x, z - 30, loader, new ModelTexture(loader.loadTexture("landing")), airportId, 0, true);
			landingStrip2 = new LandingStrip(x - 400, z - 30, loader, new ModelTexture(loader.loadTexture("landing")),airportId, 1, true);
		} else {
			leftGate = new Gate(staticGateModel, new Matrix4f().translate(new Vector3f(x, 1, z)), 1, airportId, 0);
			rightGate = new Gate(staticGateModel, new Matrix4f().translate(new Vector3f(x + 40, 1, z)), 1, airportId, 1);
			
			landingStrip = new LandingStrip(x - 30, z - 400, loader, new ModelTexture(loader.loadTexture("landing")), airportId, 0, false);
			landingStrip2 = new LandingStrip(x - 30, z, loader, new ModelTexture(loader.loadTexture("landing")),airportId, 1, false);
		}
		
		position = new Vector3f(x, 0, z);
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
								+ config.getTyreRadius(),0 + 10 + z));
	}

	public Vector3f getPackagePosition(int gateID) {
		
		return new Vector3f(0 + 40 * gateID + x , 0 , 0 + 10 + z);
	}

	public Vector3f getPosition() {
		return position;
	}

}
