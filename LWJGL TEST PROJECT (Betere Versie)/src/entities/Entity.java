package entities;

import models.RawModel;

import org.lwjgl.util.vector.Vector3f;

public class Entity {

	public Entity(RawModel model, Vector3f position, Vector3f orientation, float scale) {
		this.model = model;
		this.position = position;
		this.orientation = orientation;
		this.scale = scale;
	}
	
	
	// The model of the entity
	private RawModel model;
	
	public RawModel getModel() {
		return model;
	}

	public void setModel(RawModel model) {
		this.model = model;
	}

	
	// the position of the entity
	private Vector3f position;
	
	public Vector3f getPosition() {
		return new Vector3f(this.position.x, this.position.y, this.position.z);
	}

	public void setPosition(Vector3f vector) {
		this.position.set(vector.x, vector.y, vector.z);
	}

	public void increasePosition(Vector3f increment) {
		Vector3f newPosition = new Vector3f();
		Vector3f.add(this.getPosition(), increment, newPosition);
		setPosition(newPosition);
	}
	
	
	// the orientation of the entity
	private Vector3f orientation;
	
	public Vector3f getOrientation() {
		return new Vector3f(this.orientation.x, this.orientation.y, this.orientation.z);
	}

	public void setOrientation(Vector3f vector) {
		this.orientation.set(vector.x, vector.y, vector.z);
	}
	
	public void increaseOrientation(Vector3f increment) {
		Vector3f newOrientation = new Vector3f();
		Vector3f.add(this.getOrientation(), increment, newOrientation);
		setOrientation(newOrientation);
	}
	
	
	// the scale of the entity
	private float scale;

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

}
