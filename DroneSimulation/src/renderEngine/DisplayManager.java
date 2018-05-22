package renderEngine;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector2f;

public class DisplayManager {
	
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 700;
	private static final int FPS_CAP = 40;
	
	public static float elapsedTime;
	private static long lastFrameTime;
	private static float delta;
	
	public static boolean started = false;
	
	public static void createDisplay(){		
		ContextAttribs attribs = new ContextAttribs(3,2)
		.withForwardCompatible(true)
		.withProfileCore(true);
		
		try {
			Display.setDisplayMode(new DisplayMode(WIDTH,HEIGHT));
			Display.create(new PixelFormat(), attribs);
			Display.setTitle("Drone Simulation");
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
		//GL11.glViewport(0,0, WIDTH, HEIGHT);
		lastFrameTime = getCurrentTime();
	}
	
	public static void updateDisplay(){
		Display.sync(FPS_CAP);
		Display.update();
		
		long currentFrameTime = getCurrentTime();
		delta = (currentFrameTime - lastFrameTime) / 1000f; // In seconds
		lastFrameTime = currentFrameTime;
		if (delta > 0.01) {
			delta = 0.01f;
		}
//		if (delta > 0.006) {
//			delta = 0.006f;
//		}
		elapsedTime += delta;
	}
	
	public static float getFrameTimeSeconds() {
		if (started) {
			return (float) (delta);
		} else {
			return 0;
		}
	}
	
	public static void closeDisplay(){
		Display.destroy();
	}

	private static long getCurrentTime() {
		return Sys.getTime() * 1000 / Sys.getTimerResolution();
	}

	public static float getElapsedTime() {
		return elapsedTime;
	}

	public static void start() {
		started = true;
	}

	public static Vector2f getNormailzedMouseCoordinates() {
		float normalizedX = -1.0f + 2.0f * (float) Mouse.getX() / (float) Display.getWidth();
		float normalizedY = 1.0f - 2.0f * (float) Mouse.getY() / (float) Display.getHeight();
		
		return new Vector2f(normalizedX, normalizedY);
	}

	public static void reset() {
		started = false;
		elapsedTime = 0;
		lastFrameTime = 0;
	}

	public static void pauze() {
		if (started) {
			started = false;
		} else {
			started = true;
		}
		
	}

}
