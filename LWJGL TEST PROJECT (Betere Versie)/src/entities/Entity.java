package entities;

import models.RawModel;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Entity {
	
	public Entity(RawModel model, Matrix4f pose, float scale) {
		this.model = model;
		this.setPose(pose);
		this.scale = scale;
	}

	// MODEL
	
	private RawModel model;
	
	public RawModel getModel() {
		return model;
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
		this.pose.translate(vector);
	}
	
	/** 
	 * Rotates this entity for the given angle around the x- y- z axis of the world frame
	 */
	public void rotate(float angle, Vector3f axis){
		this.getPose().rotate(angle , axis);	
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
	
}
