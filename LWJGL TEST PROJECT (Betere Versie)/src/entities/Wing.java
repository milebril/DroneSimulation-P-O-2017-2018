package entities;

import org.lwjgl.util.vector.Vector3f;

public class Wing {
	
	private Vector3f centerOfMass;
	private Vector3f rotAx; //Wings can only rotate around the x-axis -> rotation axis = (1,0,0)
	private float wingMass;
	private float wingLiftSlope;
	private float inclination = 0;
	
	public Wing(Vector3f centerOfMass, float wingMass, float wingLiftSlope,Vector3f rotAx) {
		this.centerOfMass = centerOfMass;
		this.wingMass = wingMass;
		this.wingLiftSlope = wingLiftSlope;
		this.rotAx = rotAx;
	}
	
	public float getMass(){
		return this.wingMass;
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
		this.wingLiftSlope = slope;
	}
	
	public float getLiftSlope(){
		return this.wingLiftSlope;
	}
	//TODO Angle Calculation
}
