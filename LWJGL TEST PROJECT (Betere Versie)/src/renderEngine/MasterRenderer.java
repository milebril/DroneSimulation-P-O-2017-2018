package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entities.Camera;
import entities.Entity;
import models.TexturedModel;
import shaders.StaticShader;
import shaders.StaticShaderTextures;

public class MasterRenderer {
	
	private StaticShaderTextures shader = new StaticShaderTextures();
	private ObjectRenderer objectRenderer = new ObjectRenderer(shader, 120, 120);
	
	private Map<TexturedModel, List<Entity>> entities = new HashMap<TexturedModel, List<Entity>>();
	
	public void render(Camera camera) {
		objectRenderer.prepare();
		shader.start();
		shader.loadViewMatrix(camera);
		objectRenderer.render(entities);
		shader.stop();
		entities.clear();
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
	
	public void cleanUp() {
		shader.cleanUp(); 
	}
	
}
 