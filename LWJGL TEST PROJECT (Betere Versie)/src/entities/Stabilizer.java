package entities;

import org.lwjgl.util.vector.Vector3f;

public class Stabilizer {

	private Vector3f relPosition;
	private Vector3f rotAx;
	private float liftSlope;
	private float inclination;
	
	public Stabilizer(Vector3f position, float liftSlope, Vector3f rotationAxis) {
		this.relPosition = position;
		this.liftSlope = liftSlope;
		this.rotAx = rotationAxis;
	}
	
	public Vector3f getRotAxis(){
		return this.rotAx;
	}
	
	public float setInclination(){
		return this.inclination;
	}
	
	public float getInclination(){
		return this.inclination;
	}
	
	public void setLiftSlope(float slope){
		this.liftSlope = slope;
	}
	
	public float getLiftSlope(){
		return this.liftSlope;
	}
	//TODO Set rotationAxis here
	
}	
