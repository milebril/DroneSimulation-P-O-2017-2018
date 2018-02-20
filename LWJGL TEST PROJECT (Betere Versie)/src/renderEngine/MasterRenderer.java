package renderEngine;

import shaders.StaticShader;

public class MasterRenderer {
	
	private StaticShader shader = new StaticShader();
	private EntityRenderer entityRenderer = new EntityRenderer(shader, fovx, fovy);
	

}
