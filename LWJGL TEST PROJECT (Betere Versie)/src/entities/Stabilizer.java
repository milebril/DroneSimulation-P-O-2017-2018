package entities;

import org.lwjgl.util.vector.Vector3f;

public class Stabilizer {

	private Vector3f relPosition;
	private Vector3f rotAx;
	private float liftSlope;
	
	public Stabilizer(Vector3f position, float liftSlope, Vector3f rotationAxis) {
		this.relPosition = position;
		this.liftSlope = liftSlope;
		this.rotAx = rotationAxis;
	}
	//TODO Set rotationAxis here
	
}	
