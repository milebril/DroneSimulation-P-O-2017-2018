package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import entities.Camera;
import entities.Entity;
import models.TexturedModel;
import shaders.StaticShader;
import shaders.StaticShaderTextures;
import shaders.TerrainShader;
import terrains.Terrain;

public class MasterRenderer {

	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 1000;
	
	private static float FOVX;
	private static float FOVY;
	
	private Matrix4f projectionMatrix;
	
	private StaticShaderTextures shader = new StaticShaderTextures();
	private ObjectRenderer objectRenderer;
	
	private List<Terrain> terrains = new ArrayList<>();
	private TerrainRenderer terrainRenderer;
	private TerrainShader terrainShader = new TerrainShader();
	
	public MasterRenderer() {
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		createProjectionMatrix();
		objectRenderer = new ObjectRenderer(shader, 60, 60);
		terrainRenderer = new TerrainRenderer(terrainShader);
	}
	
	private Map<TexturedModel, List<Entity>> entities = new HashMap<TexturedModel, List<Entity>>();
	
	public void render(Camera camera) {
		objectRenderer.prepare();
		shader.start();
		shader.loadViewMatrix(camera);
		objectRenderer.render(entities);
		shader.stop();
		terrainShader.start();
		terrainShader.loadViewMatrix(camera);
		terrainRenderer.render(terrains);
		terrainShader.stop();
		entities.clear();
		terrains.clear();
	}
	
	public void processTerrain(Terrain terrain){
		terrains.add(terrain);
	}
	
	public void processEntity(Entity entity) {
		TexturedModel entityModel = entity.getTexModel();
		List<Entity> batch = entities.get(entityModel);
		if (batch != null) {
			batch.add(entity);
		} else {
			batch = new ArrayList<Entity>();
			batch.add(entity);
			entities.put(entityModel, batch);
		}
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
	
	
	public void cleanUp() {
		shader.cleanUp(); 
		terrainShader.cleanUp();
	}
	
}
 