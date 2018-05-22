package engineTester;

import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import entities.Drone;
import guis.GuiRenderer;
import guis.GuiTexture;
import renderEngine.Loader;

public class MiniMap {
	List<GuiTexture> droneTextures;
	
	public MiniMap() {
		droneTextures = new ArrayList<>();
	}
	
	public void render(List<Drone> drones, GuiRenderer guiRenderer, Loader loader) {
		//MiniMap
		GL11.glViewport(580, 0, 700, Display.getHeight());
		GL11.glScissor(580, 0, 700, Display.getHeight());
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		//renderer.prepare();
		glClearColor(1, 1, 0, 1);
		droneTextures.clear();
			for (Drone drone : drones) {
			float normalizedX = 2.0f * (float) (drone.getPosition().x * Display.getWidth() / 4000) / (float) Display.getWidth();
			float normalizedY = -2.0f * (float) (drone.getPosition().z * Display.getHeight() / 4000) / (float) Display.getHeight();
			System.out.println(new Vector2f(normalizedX, normalizedY));
			GuiTexture e = new GuiTexture(loader.loadTexture("grass"), new Vector2f(normalizedX, normalizedY),
					new Vector2f(0.01f, 0.01f));
			droneTextures.add(e);
		}
		
		guiRenderer.render(droneTextures);
		
		//Drone List
		GL11.glViewport(0, 0, 580, Display.getHeight());
		GL11.glScissor(0, 0, 580, Display.getHeight());
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		glClearColor(1, 1, 1, 1);
	}
}
