package entities;

import org.lwjgl.util.vector.Matrix4f;

import models.TexturedModel;

public class Gate extends Entity {
	
	private final int airportID;
	private final int gateID;

	public Gate(TexturedModel model, Matrix4f pose, float scale, int airportID, int gateID) {
		super(model, pose, scale);
		this.airportID = airportID;
		this.gateID = gateID;
		
	}

	public int getAirportID() {
		return airportID;
	}

	public int getGateID() {
		return gateID;
	}

}
