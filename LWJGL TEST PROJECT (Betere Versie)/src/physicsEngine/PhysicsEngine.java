package physicsEngine;

import org.lwjgl.util.vector.Vector3f;

public class PhysicsEngine {
	
	
	
	private float gravity = 9.81f;
	private float wingX = 15.0f;
	private float tailSize = 10.0f;
	private float engineMass = 50.0f;
	private float wingMass = 70.0f;
	private float tailMass = 100.0f;
	private float maxThrust = 1000.0f;
	private float maxAOA = 50.0f;
	private float wingLiftSlope = 100.0f;
	private float horStabLiftSlope = 10.0f;
	private float verStabLiftSlope = 10.0f;
	private float horizontalAngleOfView = 120.0f;
	private float verticalAngleOfView = 120.0f;
	private int nbColumns = 200;
	private int nbRows = 200;
	private Vector3f speedVector;
	private Vector3f speedChangeVector;
	
	
	
	
	
	
	
	
	
	public float getSpeed() {
		return this.getSpeedVector().length(); 
	}
	
	public void setSpeedVector(Vector3f speedVector) {
		this.speedVector = speedVector;
	}

	public Vector3f getSpeedVector(){
		return this.speedVector;
	}
	
	public void setSpeedChangeVector(Vector3f vector){
		this.speedChangeVector = vector;
	}
	
	public Vector3f getSpeedChangeVector(){
		return this.speedChangeVector;
	}
	
	
	
	
	
//Calculates vertical stabilizer liftforce
private Vector3f calculateVerStabLift(){
	Vector3f normal = new Vector3f(0,0,0);
		
	// The vertical stabilizer's attack vector is (-sin(verStabInclination), 0, -cos(verStabInclination)).
	Vector3f attackVector = new Vector3f((float) -Math.sin(this.getVerticalStabilizer().getInclination()), 0, (float) -Math.cos(this.verticalStabilizer.getInclination()));
	
	Vector3f.cross(this.getVerticalStabilizer().getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
	float liftSlope = this.getVerticalStabilizer().getLiftSlope();
		
	//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
	float AoA = (float) - Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
	float speed = (float) Math.pow(this.getSpeed(),2);
	Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
								   (float)(normal.y*liftSlope*AoA*speed),
			
								   
								   (float)(normal.z*liftSlope*AoA*speed));
	return result;
}		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	




