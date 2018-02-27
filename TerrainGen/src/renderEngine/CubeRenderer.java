package renderEngine;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import entities.Entity;
import models.RawCubeModel;
import models.RawModel;
import models.TexturedModel;
import shaders.StaticShader;
import shaders.cubes.CubeShader;

public class CubeRenderer {

	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 1000;
	
	private static float FOVX;
	private static float FOVY;
	
	private Matrix4f projectionMatrix;
	private Matrix4f orthoMatrix;
	private CubeShader shader;
	
	public CubeRenderer(CubeShader shader, float fovx, float fovy){
		this.FOVX = fovx;
		this.FOVY = fovy;
		this.shader = shader;
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		createProjectionMatrix();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	public void prepare() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(1, 1, 1, 1);
	}
	
	public void prepareDroneCamera() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(1, 1, 1, 1);
	}
	
	public void prepareText() {
		//GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0, 0, 0, 1);
	}

	public void render(Entity entity, CubeShader shader) {
		RawCubeModel rawModel = entity.getCubeModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0); //Vertices
		GL20.glEnableVertexAttribArray(1); //Colors
		Matrix4f transformationMatrix = entity.getPose();
		shader.loadTransformationMatrix(transformationMatrix);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, rawModel.getVertexCount());
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	
	private void createProjectionMatrix(){
		//float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float aspectRatio = FOVX / FOVY;
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOVY / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix = new Matrix4f();
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		projectionMatrix.m33 = 0;
	}
	
}
