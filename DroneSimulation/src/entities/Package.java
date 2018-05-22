package entities;

import org.lwjgl.util.vector.Matrix4f;

import models.Airport;
import models.TexturedModel;

public class Package extends Entity{

	private Airport startAirport, destAirport;
	private int startGate, destGate;
	
	public Package(TexturedModel model, Matrix4f pose, float scale, Airport startAirport, int startGate, Airport destAirport, int destGate) {
		super(model, pose, scale);
		
		this.startAirport = startAirport;
		this.destAirport = destAirport;
		this.startGate = startGate;
		this.destGate = destGate;
	}

	public Airport getStartAirport() {
		return startAirport;
	}

	public Airport getDestAirport() {
		return destAirport;
	}

	public int getStartGate() {
		return startGate;
	}

	public int getDestGate() {
		return destGate;
	}

}
