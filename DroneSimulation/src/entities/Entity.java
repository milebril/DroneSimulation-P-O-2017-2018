package entities;

import models.RawCubeModel;
import models.RawModel;
import models.TexturedModel;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Entity {
	
	public Entity(RawModel model, Matrix4f pose, float scale) {
		this.model = model;
		this.setPose(pose);
		this.scale = scale;
	}
	
	public Entity(RawCubeModel model, Matrix4f pose, float scale) {
		this.cubeModel = model;
		this.setPose(pose);
//		this.orientation = orientation;
		this.scale = scale;
	}
	
	public Entity(TexturedModel model, Matrix4f pose, float scale) {
		this.texModel = model;
		this.setPose(pose);
//		this.orientation = orientation;
		this.scale = scale;
	}

	// MODEL
	
	private RawModel model;
	private RawCubeModel cubeModel;
	private TexturedModel texModel;
	
	public RawModel getModel() {
		return model;
	}
	
	public RawCubeModel getCubeModel() {
		return cubeModel;
	}
	
	public TexturedModel getTexModel() {
		return this.texModel;
	}

	public void setModel(RawModel model) {
		this.model = model;
	}
	
	// POSE
	
	/**
	 * The pose matrix of the entity. This contains the 
	 * position and orientation.
	 */
	private Matrix4f pose;
	
	/**
	 * Returns the pose matrix of this Entity.
	 */
	public Matrix4f getPose() {
		return pose;
	}
	
	/**
	 * Set the pose matrix of this Entity.
	 */
	public void setPose(Matrix4f pose) {
		this.pose = pose;
	}
	
	/**
	 * Returns the position of this Entity.
	 */
	public Vector3f getPosition() {
		return new Vector3f(this.getPose().m30, this.getPose().m31, this.getPose().m32);
	}
	
	/**
	 * Translates this entity over the given vector.
	 */
	public void translate(Vector3f vector) {
		this.pose.m30 += vector.x;
		this.pose.m31 += vector.y;
		this.pose.m32 += vector.z;
	}
	
	/** 
	 * Rotates this entity for the given angle around the x- y- z axis of the world frame
	 */
	public void rotate(float angle, Vector3f axis){
//		this.getPose().rotate(angle , axis);
		float ux = axis.x;
		float uy = axis.y;
		float uz = axis.z;
		
		float cth = (float) Math.cos(angle);
		float sth = (float) Math.sin(angle);
		Matrix3f rm = new Matrix3f();
		rm.m00 = cth + ux*ux*(1-cth);
		rm.m10 = ux*uy*(1-cth)-uz*sth;
		rm.m20 = ux*uz*(1-cth) + uy*sth;
		rm.m01 = uy*ux*(1-cth) + uz*sth;
		rm.m11 = cth + uy*uy*(1-cth);
		rm.m21 = uy*uz*(1-cth) - ux*sth;
		rm.m02 = uz*ux*(1-cth) - uy*sth;
		rm.m12 = uz*uy*(1-cth) + ux*sth;
		rm.m22 = cth + uz*uz*(1-cth);
//		
		Matrix4f p = this.getPose();
//		System.out.println("matrix p: " + p);
		Matrix3f t = new Matrix3f();
		
		t.m00 = p.m00;
		t.m01 = p.m01;
		t.m02 = p.m02;
		t.m10 = p.m10;
		t.m11 = p.m11;
		t.m12 = p.m12;
		t.m20 = p.m20;
		t.m21 = p.m21;
		t.m22 = p.m22;
		
		
		Matrix3f.mul(rm, t, t);
		
		Matrix4f newPose = new Matrix4f();
		
		newPose.m00 = t.m00;
		newPose.m01 = t.m01;
		newPose.m02 = t.m02;
		newPose.m30 = p.m30;
		newPose.m10 = t.m10;
		newPose.m11 = t.m11;
		newPose.m12 = t.m12;
		newPose.m31 = p.m31;
//		System.out.println("newPose m13: " + newPose.m13);
		newPose.m32 = p.m32;
		newPose.m20 = t.m20;
		newPose.m21 = t.m21;
		newPose.m22 = t.m22;
		
		this.setPose(newPose);
		
//		System.out.println("after rotate: \n" + this.getPose());
	}
	
	// SCALE

	/**
	 * The scale of this Entity.
	 */
	private float scale;
	
	/**
	 * Returns the scale of this Entity.
	 */
	public float getScale() {
		return scale;
	}
	
	/**
	 * Sets the scale of this Entity.
	 */
	public void setScale(float scale) {
		this.scale = scale;
	}
	// ORIENTATION
	
	private float pitch;
	public float getPitch() {
		return pitch;
	}
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	
	private float heading;
	public float getHeading() {
		return heading;
	}
	public void setHeading(float heading) {
		this.heading = heading;
	}
	
	private float roll;
	public float getRoll() {
		return roll;
	}
	public void setRoll(float roll) {
		this.roll = roll;
	}
	
	public Matrix3f getDroneToWorldRotationMatrix() {
		// 1st number is row, 2nd is column => FOUT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		Matrix3f xRot = new Matrix3f();
		xRot.m00 = 1; xRot.m01 = 0;                            xRot.m02 = 0;
		xRot.m10 = 0; xRot.m11 = (float) Math.cos(getPitch()); xRot.m12 = (float) - Math.sin(getPitch());
		xRot.m20 = 0; xRot.m21 = (float) Math.sin(getPitch()); xRot.m22 = (float) Math.cos(getPitch());
		
		Matrix3f yRot = new Matrix3f();
		yRot.m00 = (float) Math.cos(getHeading());   yRot.m01 = 0; yRot.m02 = (float) Math.sin(getHeading());
		yRot.m10 = 0;                                yRot.m11 = 1; yRot.m12 = 0;
		yRot.m20 = (float) - Math.sin(getHeading()); yRot.m21 = 0; yRot.m22 = (float) Math.cos(getHeading());
		
		Matrix3f zRot = new Matrix3f();
		zRot.m00 = (float) Math.cos(getRoll()); zRot.m01 = (float) - Math.sin(getRoll()); zRot.m02 = 0;
		zRot.m10 = (float) Math.sin(getRoll()); zRot.m11 = (float) Math.cos(getRoll());   zRot.m12 = 0;
		zRot.m20 = 0;                           zRot.m21 = 0;                             zRot.m22 = 1;
		
		// rot = yRot . xRot . zRot
		Matrix3f rot = new Matrix3f();
		Matrix3f.mul(xRot, zRot, rot);
		Matrix3f.mul(yRot, rot, rot);
		
		return rot;
	}
	
	private void changeAngles(Vector3f axis, float angle) {
		axis.normalise();
		
		Matrix3f W = new Matrix3f();
		W.m00 = 0; W.m01 = -axis.z; W.m02 = axis.y;
		W.m10 = axis.z; W.m11 = 0; W.m12 = -axis.x;
		W.m20 = -axis.y; W.m21 = axis.x; W.m22 = 0;
		
		Matrix3f W2 = new Matrix3f();
		Matrix3f.mul(W, W, W2);
		
		Matrix3f I = new Matrix3f();
		Matrix3f sin =  new Matrix3f();
		Matrix3f sin2 =  new Matrix3f();
		sin.m00 = (float) Math.sin(angle); sin.m11 = (float) Math.sin(angle); sin.m22 = (float) Math.sin(angle);
		sin2.m00 = (float) (2*Math.sin(angle/2)); sin2.m11 = (float) (2*Math.sin(angle/2)); sin2.m22 = (float) (2*Math.sin(angle/2));
		
		Matrix3f.mul(sin, W, sin);
		Matrix3f.mul(sin2, W2, sin2);
		
		Matrix3f rotation = new Matrix3f();
		Matrix3f.add(I, sin, rotation);
		Matrix3f.add(rotation, sin2, rotation);
		
		
		Matrix3f totalRot = new Matrix3f();
		Matrix3f.mul(rotation, getDroneToWorldRotationMatrix(), totalRot);
		
		this.pitch = (float) - Math.asin(totalRot.m20);
		this.roll = (float) Math.atan(totalRot.m21/totalRot.m22);
		this.heading = (float) Math.atan(totalRot.m10/totalRot.m00);
		System.out.println("pitchhhhhh: " + pitch);
	}
}
