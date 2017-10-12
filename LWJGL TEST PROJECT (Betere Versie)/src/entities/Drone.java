package entities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import autopilot.AutopilotConfig;
import models.RawModel;
import models.TexturedModel;

public class Drone extends Entity /* implements AutopilotConfig */ {

	private Wing leftWing;
	private Wing rightWing;
	
	private Stabilizer horizontalStabilizer;
	private Stabilizer verticalStabilizer;
	
	private float maxThrust;
	private float maxAOA;
	
	private float distance = 0.2f;
	private float mouseSensitivity = 0.1f;
	
	private Camera camera;
	
	public Drone(RawModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale,
			AutopilotConfig cfg) {
		super(model, position, rotX, rotY, rotZ, scale);
		
		//TODO load drone settings through Config
		//TODO Create wings & Stabilizerz
		//TODO Calculate position of the engine
		
		//Read from config:
		//TODO Create wings & Stabilizers
		this.leftWing = new Wing(new Vector3f(-cfg.getWingX(),0,0), cfg.getWingMass(), cfg.getWingLiftSlope());
		this.rightWing = new Wing(new Vector3f(cfg.getWingX(),0,0), cfg.getWingMass(), cfg.getWingLiftSlope());
		// this.horizontalStabilizer = new Stabilizer(...) + Set rotationAxis to X;
		// this.verticalStabilizer = new Stabilizer(...) + Set rotationAxis to Y;
		this.maxThrust = cfg.getMaxThrust();
		this.maxAOA = cfg.getMaxAOA();
		
		camera = new Camera(cfg.getNbColumns(), cfg.getNbRows());
		camera.increasePosition(this.getPosition().x, this.getPosition().y, this.getPosition().z);
	}
	
	@Override
	public void increasePosition(float dx, float dy, float dz) {
		super.increasePosition(dx, dy, dz);
		camera.increasePosition(dx, dy, dz);
	}
	
	public void applyForces() {
		//Gravity
		//in the 2 wings
		//in the engine          //Can we do this in the massCenter?
		//in the stabilizer
		//Lift Forces, ...
	}
	
	public void setRoll(float roll) {
		this.camera.setRoll(roll);
	}
	
	public void increaseCameraRoll(float roll) {
		this.camera.increaseRoll(roll);
	}

	public Camera getCamera() {
		return this.camera;
	}
	
	
	/*
	public void move(){
		if(Keyboard.isKeyDown(Keyboard.KEY_Z)){
			this.getPosition().x += distance * (float)Math.sin(Math.toRadians(camera.getYaw()));
			this.getPosition().z -= distance * (float)Math.cos(Math.toRadians(camera.getYaw()));
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)){
			this.getPosition().x -= distance * (float)Math.sin(Math.toRadians(yaw));
			this.getPosition().z += distance * (float)Math.cos(Math.toRadians(yaw));
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
			position.x += distance * (float)Math.sin(Math.toRadians(yaw+90));
	        position.z -= distance * (float)Math.cos(Math.toRadians(yaw+90));
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
			position.x += distance * (float)Math.sin(Math.toRadians(yaw-90));
	        position.z -= distance * (float)Math.cos(Math.toRadians(yaw-90));
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
			position.y+=0.2f;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
			position.y-=0.2f;
		}
		
		if (Mouse.isButtonDown(2)) {
			int dx = Mouse.getDX();
	        int dy = Mouse.getDY();
	        yaw += (dx * mouseSensitivity);
	        pitch -= (dy * mouseSensitivity);
		}	
		

		if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
			GL11.glReadBuffer(GL11.GL_FRONT);
			int width = Display.getWidth();
			int height= Display.getHeight();
			int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
			ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
			GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );
			
			File file = new File("res/image.png"); // The file to save to.
			String format = "PNG"; // Example: "PNG" or "JPG"
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			
			for(int x = 0; x < width; x++) 
			{
			    for(int y = 0; y < height; y++)
			    {
			        int i = (x + (width * y)) * bpp;
			        int r = buffer.get(i) & 0xFF;
			        int g = buffer.get(i + 1) & 0xFF;
			        int b = buffer.get(i + 2) & 0xFF;
			        image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
			    }
			}
			//Cropping the image
			BufferedImage dest = image.getSubimage(image.getWidth()/2 - 100, image.getHeight()/2 - 100,
					this.snapshotWidth, this.snapshotHeight);
			
			try {
			    ImageIO.write(dest, format, file);
			} catch (IOException e) { e.printStackTrace(); }
		}
		
	}
	*/
}
