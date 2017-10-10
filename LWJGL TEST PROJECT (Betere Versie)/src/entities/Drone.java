package entities;

import org.lwjgl.util.vector.Vector3f;

import autopilot.AutopilotConfig;
import models.TexturedModel;

public class Drone extends Entity /* implements AutopilotConfig */ {

	private Wing leftWing;
	private Wing rightWing;
	
	private Stabilizer horizontalStabilizer;
	private Stabilizer verticalStabilizer;
	
	private float maxThrust;
	private float maxAOA;
	
	public Drone(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(model, position, rotX, rotY, rotZ, scale);
		
	}
	
}
