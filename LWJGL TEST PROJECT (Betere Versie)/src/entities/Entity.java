package entities;

import models.RawModel;
import models.TexturedModel;

import org.lwjgl.util.vector.Vector3f;

public class Entity {

	private RawModel model;
	private Vector3f position;
	private Vector3f orientation;
	private float scale;

	public Entity(RawModel model, Vector3f position, Vector3f orientation, float scale) {
		this.model = model;
		this.position = position;
		this.orientation = orientation;
		this.scale = scale;
	}

	public void increasePosition(Vector3f increment) {
		Vector3f newPosition = new Vector3f();
		Vector3f.add(this.getPosition(), increment, newPosition);
		setPosition(newPosition);
	}
	
	public void increaseOrientation(Vector3f increment) {
		Vector3f newOrientation = new Vector3f();
		Vector3f.add(this.getOrientation(), increment, newOrientation);
		setOrientation(newOrientation);
	}
	
	public Vector3f getOrientation() {
		return orientation;
	}

	public void setOrientation(Vector3f orientation) {
		this.orientation = orientation;
	}

	public RawModel getModel() {
		return model;
	}

	public void setModel(RawModel model) {
		this.model = model;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

}
