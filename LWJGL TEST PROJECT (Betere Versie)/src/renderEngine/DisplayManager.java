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
	
	private static boolean started = false;
	
	public static void createDisplay(){		
//		ContextAttribs attribs = new ContextAttribs(3,2)
//		.withForwardCompatible(true)
//		.withProfileCore(true);
//		
//		try {
//			Display.setDisplayMode(new DisplayMode(WIDTH,HEIGHT));
//			Display.create(new PixelFormat(), attribs);
//			Display.setTitle("Our First Display!");
//		} catch (LWJGLException e) {
//			e.printStackTrace();
//		}
		
		Display.setTitle("Basic Game");
		try {
			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
//		GL11.glMatrixMode(GL11.GL_PROJECTION);
//		GL11.glLoadIdentity();
//		GL11.glOrtho(0, WIDTH, HEIGHT, 0, 1, -1);
//		GL11.glMatrixMode(GL11.GL_MODELVIEW);
//		GL11.glEnable(GL11.GL_TEXTURE_2D);
//		GL11.glEnable(GL11.GL_BLEND);
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		
		
		GL11.glViewport(0, 0, WIDTH, HEIGHT);
		lastFrameTime = getCurrentTime();
	}
	
	public static void updateDisplay(){
		Display.sync(FPS_CAP);
		Display.update();
		
		long currentFrameTime = getCurrentTime();
		delta = (currentFrameTime - lastFrameTime) / 1000f; // In seconds
		delta = delta/4; //Slow down the simulation TODO: ik denk dat dit hier moet want anders gaat de AP verkeerde tijden berekenen.
		lastFrameTime = currentFrameTime;
		
		elapsedTime += delta;
	}
	
	public static float getFrameTimeSeconds() {
		if (started) {
			return (float) (delta) ; //Simulatie wordt trager afgespeeld zodat het duidelijker is.
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
}
