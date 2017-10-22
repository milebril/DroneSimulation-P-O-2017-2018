package entities;

import org.lwjgl.util.vector.Vector3f;

public class Stabilizer {

	private Vector3f relPosition;
	private Vector3f rotAx;
	private float liftSlope;
	private float inclination = 15;
	
	public Stabilizer(Vector3f position, float liftSlope, Vector3f rotationAxis) {
		this.setRelPosition(position);
		this.setLiftSlope(liftSlope);
		this.setRotAxis(rotationAxis);
	}
	
	private void setRelPosition(Vector3f pos){
		this.relPosition = pos;
	}
	
	public Vector3f getRelPosition(){
		return this.relPosition;
	}
	
	private void setRotAxis(Vector3f axis){
		this.rotAx = axis;
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
	
	private void setLiftSlope(float slope){
		this.liftSlope = slope;
	}
	
	public float getLiftSlope(){
		return this.liftSlope;
	}
	
	
}	
