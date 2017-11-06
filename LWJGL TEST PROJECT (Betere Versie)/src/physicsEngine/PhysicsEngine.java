package physicsEngine;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import entities.AirFoil;
import entities.Drone;

public class PhysicsEngine {	
	

	private Vector3f speedVector;
	private Vector3f speedChangeVector;
	
	public static void applyPhysics (Drone drone, double dt){
		Vector3f[] forces = calculateForces(drone);
		Vector3f[] accelerations = calculateAccelerations(drone, forces);
		Vector3f[] velocities = calculateVelocities(drone, accelerations);
		drone.setVelocities(velocities);
		Vector3f[] positions = calculatePositions(drone, accelerations);
		drone.setPos
		
		
		
	}
	
	private static Vector3f[] calculatePositions(Drone drone, Vector3f[] accelerations) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Vector3f[] calculateVelocities(Drone drone, Vector3f[] accelerations) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Vector3f[] calculateAccelerations(Drone drone, Vector3f[] forces) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @param drone
	 * @return array with force and torque on the drone
	 */
	private static Vector3f[] calculateForces(Drone drone){
		//TODO torque 
		
		Vector3f force = new Vector3f();
		Vector3f torque = new Vector3f();
		//loopen over alle airfoils en bij elke de kracht uitrekenen en optellen bij force
		//idem vr torque
		for(AirFoil currentWing : drone.getAirFoils() ){
			Vector3f currentWingLiftForceD = new Vector3f();
			
			Vector3f centerOfMassVectorD = currentWing.getCenterOfMass();
			float liftSlope = currentWing.getLiftSlope();
			Vector3f projectedAirspeedVectorD = new Vector3f();
			Vector3f airspeedVectorW = new Vector3f();
			Vector3f airspeedVectorD = new Vector3f();
			Vector3f rotationAxisD = currentWing.getRotAxis();
			Vector3f normalD = currentWing.calculateNormal();
			Vector3f attackVectorD = currentWing.calculateAttackVector();			

			//difference between speedvector of drone and windspeed
			Vector3f.sub(drone.getSpeedVector(), getWindVelocity(), airspeedVectorW);
			//transformation of airspeedvector to drone fraem
			airspeedVectorD = drone.transformToDroneFrame(airspeedVectorW);
			//projection of airpseed vector to surface perpendicular to rotation axis, stored to projectedairspeedvector
			Vector3f.sub(airspeedVectorD, (Vector3f) rotationAxisD.scale(Vector3f.dot(airspeedVectorD, rotationAxisD)), projectedAirspeedVectorD);
			
			float airspeed = projectedAirspeedVectorD.lengthSquared();
//			We define its angle of attack as -atan2(S . N, S . A), where S
//			is the projected airspeed vector, N is the normal, and A is the attack vector
			float aoa = (float) - Math.atan2(Vector3f.dot(projectedAirspeedVectorD, normalD), Vector3f.dot(projectedAirspeedVectorD, attackVectorD));					
			
			//Each airfoil generates a lift force N . liftSlope . AOA . s^2, where N is the
//			  normal, AOA is the angle of attack, and s is the projected airspeed.  No
//			  other forces operate on the drone; in particular, there is no drag.
			currentWingLiftForceD = (Vector3f) normalD.scale(liftSlope * aoa * airspeed);
			Vector3f.add(currentWingLiftForceD, force, force);
		}
		
		return new Vector3f[]{force, torque};
		
	}
	
	private static Vector3f getWindVelocity(){
		return new Vector3f(0,0,0);
	}
	
	
//	public float getSpeed() {
//		return this.getSpeedVector().length(); 
//	}
//	
//	public void setSpeedVector(Vector3f speedVector) {
//		this.speedVector = speedVector;
//	}
//
//	public Vector3f getSpeedVector(){
//		return this.speedVector;
//	}
//	
//	public void setSpeedChangeVector(Vector3f vector){
//		this.speedChangeVector = vector;
//	}
//	
//	public Vector3f getSpeedChangeVector(){
//		return this.speedChangeVector;
//	}
//	
//}
//	
//	
//	
////Calculates vertical stabilizer liftforce
//private Vector3f calculateVerStabLift(){
//	Vector3f normal = new Vector3f(0,0,0);
//		
//	// The vertical stabilizer's attack vector is (-sin(verStabInclination), 0, -cos(verStabInclination)).
//	Vector3f attackVector = new Vector3f((float) -Math.sin(this.getVerticalStabilizer().getInclination()), 0, (float) -Math.cos(this.verticalStabilizer.getInclination()));
//	
//	Vector3f.cross(this.getVerticalStabilizer().getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
//	float liftSlope = this.getVerticalStabilizer().getLiftSlope();
//		
//	//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
//	float AoA = (float) - Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
//	float speed = (float) Math.pow(this.getSpeed(),2);
//	Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
//								   (float)(normal.y*liftSlope*AoA*speed),
//			
//								   
//								   (float)(normal.z*liftSlope*AoA*speed));
//	return result;
//}		
//	
//	
////Calculates left wing liftforce
//private Vector3f calculateLeftWingLift(){
//	//TODO: normal wordt gedeclareerd als een 0 vector omdat Vector3f.cross iets moet opslaan in vector.
//	//alternatief is mss vector declareren in Vector3f.cross?
//	Vector3f normal = new Vector3f(0,0,0);
//	
//	// The left wing's attack vector is (0, sin(leftWingInclination), -cos(leftWingInclination)).
//	Vector3f attackVector = new Vector3f(0,(float)Math.sin(this.getLeftWing().getInclination()), (float) -Math.cos(this.getLeftWing().getInclination()));
//	Vector3f.cross(this.getLeftWing().getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
//
//	float liftSlope = this.getLeftWing().getLiftSlope();
//	
//	//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
//	
//	float AoA = (float) - Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
//	float speed = (float) Math.pow(this.getSpeed(),2);
//	Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
//								   (float)(normal.y*liftSlope*AoA*speed),
//								   (float)(normal.z*liftSlope*AoA*speed));
//	
//	return result;
//}
//
////Calculates right wing liftforce
//private Vector3f calculateRightWingLift(){
//	Vector3f normal = new Vector3f(0,0,0);
//	
//	// The right wing's attack vector is (0, sin(rightWingInclination), -cos(rightWingInclination)).
//	Vector3f attackVector = new Vector3f(0,(float)Math.sin(this.getRightWing().getInclination()), (float) -Math.cos(this.getRightWing().getInclination()));
//	Vector3f.cross(this.getRightWing().getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
//	
//	float liftSlope = this.getRightWing().getLiftSlope();
//	
//	//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
//	float AoA = (float) - Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
//	float speed = (float) Math.pow(this.getSpeed(),2);
//	
//	Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
//								   (float)(normal.y*liftSlope*AoA*speed),
//								   (float)(normal.z*liftSlope*AoA*speed));
//	return result;
//}
//
////Calculates horizontal stabilizer liftforce
//private Vector3f calculateHorStabLift(){
//	Vector3f normal = new Vector3f(0,0,0);
//	
//	// The horizontal stabilizer's attack vector is (0, sin(horStabInclination), -cos(horStabInclination)).
//	Vector3f attackVector = new Vector3f(0,(float)Math.sin(this.getHorizontalStabilizer().getInclination()), (float) -Math.cos(this.horizontalStabilizer.getInclination()));
//	
//	Vector3f.cross(this.getHorizontalStabilizer().getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
//	float liftSlope = this.getHorizontalStabilizer().getLiftSlope();
//	
//	//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
//	float AoA = (float) -Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
//	float speed = (float) Math.pow(this.getSpeed(),2);
//	
//	Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
//								   (float)(normal.y*liftSlope*AoA*speed),
//								   (float)(normal.z*liftSlope*AoA*speed));
//	
//	return result;
//}
//
////Calculates vertical stabilizer liftforce
//private Vector3f calculateVerStabLift(){
//	Vector3f normal = new Vector3f(0,0,0);
//		
//	// The vertical stabilizer's attack vector is (-sin(verStabInclination), 0, -cos(verStabInclination)).
//	Vector3f attackVector = new Vector3f((float) -Math.sin(this.getVerticalStabilizer().getInclination()), 0, (float) -Math.cos(this.verticalStabilizer.getInclination()));
//	
//	Vector3f.cross(this.getVerticalStabilizer().getRotAxis(), attackVector, normal); // normal = rotationAxis x attackVector
//	float liftSlope = this.getVerticalStabilizer().getLiftSlope();
//		
//	//angle of attack = -atan2(speedVector*normal ; speedVector*attackVector)
//	float AoA = (float) - Math.atan2(Vector3f.dot(this.getSpeedVector(),normal), Vector3f.dot(this.getSpeedVector(),attackVector)); 
//	float speed = (float) Math.pow(this.getSpeed(),2);
//	Vector3f result = new Vector3f((float)(normal.x*liftSlope*AoA*speed),
//								   (float)(normal.y*liftSlope*AoA*speed),
//								   (float)(normal.z*liftSlope*AoA*speed));
//	return result;
//}	
//
//private Vector3f calculateVerStabTorque() {
//	Vector3f lift = calculateVerStabLift();
//	Vector3f torque = new Vector3f(0,0,0);
//	Vector3f.cross(getVerticalStabilizer().getCenterOfMass(), lift, torque);
//	return torque;
//}
//
//public void increasePosition(float dt) {
//	float dx = dt * this.getSpeedVector().x;
//	float dy = dt * this.getSpeedVector().y;
//	float dz = dt * this.getSpeedVector().z;
//	
//	super.increasePosition(dx, dy, dz);
//
//	this.getCamera().increasePosition(dx, dy, dz);
//	this.getCamera().increaseRotation(this.getHeadingVector());
//	super.setRotation(0, -this.getCamera().getPitch(), 0);
//}
//
//public void applyForces(float dt) {
//	//Checks:
////	if (this.getThrustForce() > this.getMaxThrust()) { //TODO: deze check is overbodig omdat de setter van Thrust deze check doet?
////		this.setThrustForce(this.getMaxThrust());
////	}
//	
//	//Gravity
//	//Check voor maximale valversnelling
//	if (Math.abs(this.getSpeedVector().y) < 200) {
//		this.getSpeedChangeVector().y += gravity * dt; // v = v0 + a*t, a = F/m
//	}
//	
//	//Engine = Speed
////	if (this.getSpeed() < 100)
//	
//	applyEngineForce(dt);		
//	applyLiftForces(dt);
//	applyTorqueForces(dt);
//	
//	System.out.println("headingVector: " + this.getHeadingVector());
//	/*
//	if (!flying) {
//		getLeftWing().setInclination(0);
//		getRightWing().setInclination(0);
//		getVerticalStabilizer().setInclination(0);
//	}
//	**/
//	
//	/*if (Math.abs(this.getHeadingVector().y) > 0.1 && !flying) {
//		if (this.getHeadingVector().y > 0) {
//			getLeftWing().setInclination(getLeftWing().getInclination() - 0.01f);
//			getRightWing().setInclination(getRightWing().getInclination() - 0.01f);
//		} else if (this.getHeadingVector().y < 0) {
//			getLeftWing().setInclination(getLeftWing().getInclination() + 0.01f);
//			getRightWing().setInclination(getRightWing().getInclination() + 0.01f);
//		} else {
//			//Do nothing
//		}
//	} 	*/	
//	
////	flyMode();
//	
//	Vector3f.add(this.getSpeedVector(), this.getSpeedChangeVector(), this.getSpeedVector());
//	Vector3f.add(rotationSpeedVectorW, rotationAccelerationW, rotationSpeedVectorW);
//	
//	deepCopySpeedVector();
//	this.setSpeedChangeVector(new Vector3f(0,0,0));
//	rotationAccelerationW = new Vector3f(0,0,0);
//
//	//setHeadingVector();
//	this.forwardVectorW = rotate(rotationSpeedVectorW.y);
//	
//	getHeadingVector().normalise();
//	
//	updateTailPosition();
//}
//
//private void updateTailPosition() {
//	Vector3f centerOfMass = new Vector3f(0,0,0);
//	
//	centerOfMass.x = -forwardVectorW.x * tailSize;
//	centerOfMass.y = -forwardVectorW.y * tailSize;
//	centerOfMass.z = -forwardVectorW.z * tailSize;
//	
//	getVerticalStabilizer().setCenterOfMass(centerOfMass);
//}
//
//private Vector3f rotate(float angles) {
//	Vector3f newH = new Vector3f(0,0,0);
//	float angle = (float) Math.toRadians(angles);
//	
//	Matrix3f matrix = new Matrix3f();
//	
//	matrix.m00 = (float) Math.cos(angle);
//	matrix.m11 = 1;
//	matrix.m22 = (float) Math.cos(angle);
//	matrix.m12 = (float) Math.sin(angle);
//	matrix.m21 = - (float) Math.sin(angle);
//	
//	newH.x = (float) (this.forwardVectorW.x * Math.cos(angle) - this.forwardVectorW.z * Math.sin(angle));
//	newH.y = this.forwardVectorW.y;
//	newH.z = (float) (+this.forwardVectorW.x * Math.sin(angle) + this.forwardVectorW.z * Math.cos(angle));
//	
//	//Matrix3f.mul(matrix, getHeadingVector(), newH);
//	
//	return newH;
//}
//
//private void deepCopySpeedVector() {
//	//DeepCopy the vector!!!
//	this.speedVectorOld.x = this.speedVectorW.x;
//	this.speedVectorOld.y = this.speedVectorW.y;
//	this.speedVectorOld.z = this.speedVectorW.z;
//}
//
//private void applyEngineForce(float dt) {
//	//v = v0 + a*t -> a = thrustForce / droneMass
//	//speedVectorNew = speedVectorOld + (thrustForce / droneMass)*dt
//	Vector3f engineVector = new Vector3f(this.getHeadingVector().x*(this.getThrustForce() / this.getDroneMass())*dt,
//										 this.getHeadingVector().y*(this.getThrustForce() / this.getDroneMass())*dt,
//										 this.getHeadingVector().z*(this.getThrustForce() / this.getDroneMass())*dt);
//	Vector3f.add(engineVector, this.getSpeedChangeVector(), this.getSpeedChangeVector());
//}
//
//private void applyLiftForces(float dt){
//	//Left Wing
//	Vector3f leftWingLiftForce = calculateLeftWingLift(); 			// = F
//	leftWingLiftForce.scale(1/getDroneMass() * dt); 			  			// = a = F/Mass
//	this.getSpeedChangeVector().y += leftWingLiftForce.y;
//	
//	//Right Wing
//	Vector3f rightWing = calculateRightWingLift(); 		 // = F
//	rightWing.scale(1/getDroneMass() * dt); 			  		 // = a = F/Mass
//	this.getSpeedChangeVector().y += rightWing.y;
//	
////	Vector3f horStab= calculateHorStabLift();
////	horStab.scale(1/getDroneMass() * dt);
////	this.getSpeedChangeVector().y += horStab.y;
//	
//	Vector3f verStab= calculateVerStabLift();
//	verStab.scale(1/getDroneMass() * dt);
//	Vector3f.add(verStab, this.accelerationVectorW, this.accelerationVectorW);
//}
//
//private void applyTorqueForces(float dt) {
//	Vector3f verStabTorque = null;
//	verStabTorque = calculateVerStabTorque();
//	verStabTorque.scale(1/getInertionY() * dt);
//	Vector3f.add(verStabTorque, rotationAccelerationW, rotationAccelerationW);
//	
//}

	
}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	




