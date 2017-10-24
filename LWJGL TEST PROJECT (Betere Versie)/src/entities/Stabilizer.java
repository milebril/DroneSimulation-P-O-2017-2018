package entities;

import org.lwjgl.util.vector.Vector3f;

public class Stabilizer extends Wing{

	private int rotationAcceleration = 0;
	
	public Stabilizer(Vector3f centerOfMass, float wingMass, float wingLiftSlope, Vector3f rotAx) {
		super(centerOfMass, wingMass, wingLiftSlope, rotAx);
	}
	
	public int getRotationAcceleration() {
		return rotationAcceleration;
	}

	public void setRotationAcceleration(int rotationAcceleration) {
		this.rotationAcceleration = rotationAcceleration;
	}
}	
