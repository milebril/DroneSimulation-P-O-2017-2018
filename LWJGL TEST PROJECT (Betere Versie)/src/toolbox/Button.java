package toolbox;

import java.awt.geom.Rectangle2D;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import engineTester.MainGameLoop;
import entities.Entity;
import models.Model2D;
import models.RawModel;
import renderEngine.Loader;

public class Button {
	protected Rectangle2D hitbox;
	private Entity entity;
	
	private Model2D model;
	
	public Button(float x, float y, float width, float height) {
		hitbox = new Rectangle2D.Float(x, y, width, height);
		
	    float[] vertices = {
	      -0.5f, 0.5f, 0f,
	      -0.5f, -0.5f, 0f,
	      0.5f, -0.5f, 0f,
	      0.5f, -0.5f, 0f,
	      0.5f, 0.5f, 0f,
	      -0.5f, 0.5f, 0f
	    };
	    
	    float[] colours = {
	    	0.5f, 0f, 0f,
	    	0.5f, 0f, 0f,
	    	0.5f, 0f, 0f,
	    	0.5f, 0f, 0f,
	    	0.5f, 0f, 0f,
	    	0.5f, 0f, 0f,
	    };
	    
	}

	//TODO DRAW BUTTON SQUARE (Check tuts)
	
	public void render() {
		model.render();
	}
	
	public boolean isClicked()  {
		if (hitbox.contains(Mouse.getX(), Display.getHeight() - 1 -Mouse.getY()) && Mouse.isButtonDown(0)) {
			return true;
		}
		
		return false;
	}

	public void update(Vector3f cameraPos) {
		
	}
}
