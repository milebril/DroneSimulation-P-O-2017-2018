package entities;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import models.RawModel;
import models.TexturedModel;
import renderEngine.DisplayManager;

public class cubeTestPlayer extends Entity{

	private static final float GRAVITY = -1.0f;
	
	private static final float TERRAIN_HEIGHT = 0;
	
	private float downSpeed = 0;
	
	public cubeTestPlayer(RawModel model, Matrix4f pose, float scale) {
		super(model, pose, scale);
	}

//	public void applyGravity() {
//		downSpeed += GRAVITY * DisplayManager.getFrameTimeSeconds(); // v = v + a*t
//		super.increasePosition(0, downSpeed * DisplayManager.getFrameTimeSeconds(), 0);
//		if (super.getPosition().y <= TERRAIN_HEIGHT) {
//			downSpeed = 0;
//			super.getPosition().y = TERRAIN_HEIGHT;
//		}
//	}

	public float getDownSpeed() {
		// TODO Auto-generated method stub
		return this.downSpeed;
	}
	
}
