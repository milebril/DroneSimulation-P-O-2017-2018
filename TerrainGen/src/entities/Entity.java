package entities;

import models.RawCubeModel;
import models.RawModel;
import models.TexturedModel;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * position of entity is stored in a 4x4 pose matrix
 * @author Jakob
 *
 */
public class Entity {

	private Matrix4f pose;

	public Entity(RawModel model, Matrix4f pose, float scale) {
		this.model = model;
		this.setPose(pose);
		this.scale = scale;
	}
	
	public Entity(TexturedModel model, Matrix4f pose, float scale) {
		this.texModel = model;
		this.setPose(pose);
		this.scale = scale;
	}
	
	public Entity(RawCubeModel model, Matrix4f pose, float scale) {
		this.cubeModel = model;
		this.setPose(pose);
		this.scale = scale;
	}

	// The model of the entity
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
	
	public Vector3f getPosition() {
		return new Vector3f(this.getPose().m30, this.getPose().m31, this.getPose().m32);
	}

	public void translate(Vector3f vector) {
		this.pose.translate(vector);
	}

//	public void increasePosition(Vector3f increment) {
//		Vector3f newPosition = new Vector3f();
//		Vector3f.add(this.getPosition(), increment, newPosition);
//		setPosition(newPosition);
//	}
	
	
	// the orientation of the entity
//	private Matrix4f orientation;
	
//	public Vector3f getOrientation() {
//		return new Vector3f(this.orientation.x, this.orientation.y, this.orientation.z);
//	}
//
//	public void setOrientation(Vector3f vector) {
//		this.orientation.set(vector.x, vector.y, vector.z);
//	}
	
//	public void increaseOrientation(Vector3f increment) {
//		Vector3f newOrientation = new Vector3f();
//		Vector3f.add(this.getOrientation(), increment, newOrientation);
//		setOrientation(newOrientation);
//	}
	
	
	// the scale of the entity
	private float scale;

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public Matrix4f getPose() {
		return pose;
	}
	
	/** 
	 * rotates the entity given angles around the x- y- z axis of the world frame
	 * @param angles
	 */
	public void rotate(float angle, Vector3f axis){
		//System.out.println("entity rotate angle: " + angle + " axis: " + axis);
		//System.out.println(this.getPose());
		this.getPose().rotate(angle , axis);	
	}

	public void setPose(Matrix4f pose) {
		this.pose = pose;
	}

}
