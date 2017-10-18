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

public class Camera {
	
	private Vector3f position = new Vector3f(0,0,0);
	private float pitch;
	private float yaw;
	private float roll;
	
	private float distance = 0.2f;
	private float mouseSensitivity = 0.1f;
	
	private int snapshotWidth;
	private int snapshotHeight;
	
	public Camera(){}
	
	public Camera(int nbColumns, int nbRows) {
		this.snapshotWidth = nbColumns;
		this.snapshotHeight = nbRows;
	}

	public void move(){
		if(Keyboard.isKeyDown(Keyboard.KEY_Z)){
			position.x += distance * (float)Math.sin(Math.toRadians(yaw));
	        position.z -= distance * (float)Math.cos(Math.toRadians(yaw));
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)){
			position.x -= distance * (float)Math.sin(Math.toRadians(yaw));
	        position.z += distance * (float)Math.cos(Math.toRadians(yaw));
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

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}
	
	/*
	 * Drone Based Camera Movements
	 */
	
	public void increasePosition(float dx, float dy, float dz) {
			this.position.x += dx;
			this.position.y += dy;
			this.position.z += dz;
	}

	public void setRoll(float roll) {
		this.roll = roll;
	}

	public void increaseRoll(float roll) {
		this.roll += roll;
	}

	public void increaseRotation(Vector3f headingVector) {
		//Yaw:
		Vector3f xPLUSz = new Vector3f(headingVector.x, 0, headingVector.y);
		this.yaw = (float) Math.toRadians(Math.atan(Math.sin(headingVector.y) / Math.sin(xPLUSz.length())));
	}

}
