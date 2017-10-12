package entities;

import org.lwjgl.util.vector.Vector3f;

public class Wing {
	
	private Vector3f centerOfMass;
	private float rotX; //Wings can only rotate around the x-axis
	private float wingMass;
	private float wingLiftSlope;
	
	public Wing(Vector3f centerOfMass, float wingMass, float wingLiftSlope) {
		this.centerOfMass = centerOfMass;
		this.wingMass = wingMass;
		this.wingLiftSlope = wingLiftSlope;
	}
	
	//TODO Angle Calculation
}
