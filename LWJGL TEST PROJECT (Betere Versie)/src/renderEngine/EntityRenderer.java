package renderEngine;

import models.RawModel;
import models.RawOBJModel;
import models.TexturedModel;

import java.util.List;

import org.hamcrest.core.IsInstanceOf;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import shaders.StaticShader;
import toolbox.Maths;

import entities.Entity;

public class EntityRenderer {
	
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 1000;
	
	private static float FOVX;
	private static float FOVY;
	
	private Matrix4f projectionMatrix;
	private Matrix4f orthoMatrix;
	private StaticShader shader;
	
	public EntityRenderer(StaticShader shader, float fovx, float fovy){
		this.FOVX = fovx;
		this.FOVY = fovy;
		this.shader = shader;
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		createProjectionMatrix();
		createOrthoMatrix();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.loadOrthogonalViewMatrix(orthoMatrix);
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
	
	public void render(List<Entity> entities) {
		prepareRawModel(entities.get(0).getModel());
		for (Entity e : entities) {
			prepareInstance(e);
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, e.getModel().getVertexCount());
		}
		
		unbindRawModel();
	}
	
	private void prepareRawModel(RawModel rawModel) {
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
	}
	
	private void unbindRawModel() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	
	private void prepareInstance(Entity entity) {
//		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(),
//				entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		Matrix4f transformationMatrix = entity.getPose();
		shader.loadTransformationMatrix(transformationMatrix);
	}

	public void render(Entity entity, StaticShader shader) {
		if(entity.getModel() != null) {
			RawModel rawModel = entity.getModel();
			GL30.glBindVertexArray(rawModel.getVaoID());
			GL20.glEnableVertexAttribArray(0); //Vertices
			GL20.glEnableVertexAttribArray(1); //Colors
			Matrix4f transformationMatrix = entity.getPose();
			shader.loadTransformationMatrix(transformationMatrix);
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, rawModel.getVertexCount());
			GL20.glDisableVertexAttribArray(0);
			GL20.glDisableVertexAttribArray(1);
			GL30.glBindVertexArray(0);
		} else if (entity.getTexModel() != null) {
			System.out.println("e");
			//TexturedModel model = entity.getTexModel();
			RawOBJModel rawModel = entity.getOBJModel();
			GL30.glBindVertexArray(rawModel.getVaoID());
			GL20.glEnableVertexAttribArray(0); //Vertices
			GL20.glEnableVertexAttribArray(1); //Colors
			Matrix4f transformationMatrix = entity.getPose();
			shader.loadTransformationMatrix(transformationMatrix);
			//GL13.glActiveTexture(GL13.GL_TEXTURE0);
			//GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
			GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			GL20.glDisableVertexAttribArray(0);
			GL20.glDisableVertexAttribArray(1);
			GL30.glBindVertexArray(0);
		}
	}
	
	public void renderObject(Entity entity, StaticShader shader) {
		RawModel rawModel = entity.getModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0); //Vertices
		GL20.glEnableVertexAttribArray(1); //Colors
//		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(),
//				entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		Matrix4f transformationMatrix = entity.getPose();
		shader.loadTransformationMatrix(transformationMatrix);
		//GL13.glActiveTexture(GL13.GL_TEXTURE0);
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, rawModel.getVertexCount());
		//GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(), //Voor models
		//		GL11.GL_UNSIGNED_INT, 0);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	
	public void render(RawModel model) {
		GL30.glBindVertexArray(model.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.getVertexCount());
		GL20.glDisableVertexAttribArray(0);
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
	
	private void createOrthoMatrix(){
		
		float t =  0;
		float b = Display.getHeight();
		float l = 0;
		float r = Display.getWidth();
		float f = -1;
		float n = 1;
		
		orthoMatrix = new Matrix4f();
		orthoMatrix.setIdentity();
		orthoMatrix.m00 = 2 / (r - l); 
		orthoMatrix.m11 = 2 / (t - b); 
	 
	    orthoMatrix.m22 = -2 / (f - n); 
	    orthoMatrix.m30 = -(r + l) / (r - l); 
	    orthoMatrix.m31 = -(t + b) / (t - b); 
	    orthoMatrix.m32 = -(f + n) / (f - n); 
	}
}
