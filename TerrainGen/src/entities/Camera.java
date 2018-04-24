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
	public float pitch;
	public float yaw;
	private float roll;
	
	private float distance = 1.0f;
	private float mouseSensitivity = 0.001f;
	
	private int snapshotWidth;
	private int snapshotHeight;
	
	public Camera(){
		position.y = 10;
	}
	
	public Camera(int nbColumns, int nbRows) {
		position.y = 10;
		this.snapshotWidth = nbColumns;
		this.snapshotHeight = nbRows;
	}

	public Vector3f getPosition() {
		return position;
	}

	/**
	 * Set pitch of camera around y-axi
	 * @param pitch in degrees
	 */
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	
	public float getPitch() {
		return this.pitch;
	}

	public float getYaw() {
		return this.yaw;
	}

	public float getRoll() {
		return this.roll;
	}
	
	/*
	 * Drone Based Camera Movements
	 */
	
	public void increasePosition(float dx, float dy, float dz) {
			this.position.x += dx;
			this.position.y += dy;
			this.position.z += dz;
	}

	/**
	 * Roll around the z-axis
	 */
	public void setRoll(float roll) {
		this.roll = roll;
	}

	public void increaseRoll(float roll) {
		this.roll += roll;
	}

	public void increaseRotation(Vector3f headingVector) {
		this.yaw = (float) Math.asin(headingVector.y);
		if (headingVector.z < 0) {
			this.pitch = (float) -Math.atan(headingVector.x / headingVector.z);
		} else {
			this.pitch = (float) (-Math.atan(headingVector.x / headingVector.z) + Math.PI);
		}
	}
	
	public BufferedImage takeSnapshot() {
		GL11.glReadBuffer(GL11.GL_FRONT);
		int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
		ByteBuffer buffer = BufferUtils.createByteBuffer(snapshotWidth * snapshotHeight * bpp);
		GL11.glReadPixels(0, 0, snapshotWidth, snapshotHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer ); //Only take camera view
		
		File file = new File("res/imageNew.png"); // The file to save to.
		String format = "PNG"; // Example: "PNG" or "JPG"
		BufferedImage image = new BufferedImage(snapshotWidth, snapshotHeight, BufferedImage.TYPE_INT_RGB);
		
		for(int x = 0; x < snapshotWidth; x++) 
		{
		    for(int y = 0; y < snapshotHeight; y++)
		    {
		        int i = (x + (snapshotWidth * y)) * bpp;
		        int r = buffer.get(i) & 0xFF;
		        int g = buffer.get(i + 1) & 0xFF;
		        int b = buffer.get(i + 2) & 0xFF;
		        image.setRGB(x, snapshotHeight - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
		    }
		}
		
		try {
		    ImageIO.write(image, format, file);
		} catch (IOException e) { 
			e.printStackTrace(); 
		}
		
		return image;
	}
	
	/**
	 * a method like takeSnapshot() that takes a snapshot but returns it as a byte array
	 */
	public byte[] takeByteArraySnapshot() {
		GL11.glReadBuffer(GL11.GL_FRONT);
		int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
		ByteBuffer buffer = BufferUtils.createByteBuffer(snapshotWidth * snapshotHeight * bpp);
		GL11.glReadPixels(0, 0, snapshotWidth, snapshotHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer ); //Only take camera view
		
		byte[] rgbArray = new byte[3 * snapshotWidth * snapshotHeight]; // 3 color values per pixel
		
		for (int i = 0; i < 3 * snapshotWidth * snapshotHeight; i += 3)
		{
	        rgbArray[i]     = (byte) (buffer.get(i) & 0xFF);
	        rgbArray[i + 1] = (byte) (buffer.get(i + 1) & 0xFF);
	        rgbArray[i + 2] = (byte) (buffer.get(i + 2) & 0xFF);
		}
		
		return rgbArray;
	}

	/**
	 * Rotate around the x-axis
	 */
	public void setYaw(float f) {
		this.yaw = f;
		
	}
	
	public void setPosition(Vector3f position) {
		this.position = position;
	}
	
	/**
	 * Angles to rotate the camera in radians
	 * @param rx
	 * @param ry
	 * @param rz
	 */
	public void setRotation(float rx, float ry, float rz){
		this.yaw = rx;
		this.pitch = ry;
		this.roll = rz;
	}

	public void roam() {
		if(Keyboard.isKeyDown(Keyboard.KEY_Z) || Keyboard.isKeyDown(Keyboard.KEY_W)){
	      position.x += distance * (float)Math.sin(pitch);
	      position.z -= distance * (float)Math.cos(pitch);
		}
	    if(Keyboard.isKeyDown(Keyboard.KEY_S)){
	      position.x -= distance * (float)Math.sin((pitch));
	      position.z += distance * (float)Math.cos((pitch));
	    }
	    if(Keyboard.isKeyDown(Keyboard.KEY_D)){
	      position.x += distance * (float)Math.sin((pitch+90));
	      //position.z -= distance * (float)Math.cos((pitch+90));
	    }
	    if(Keyboard.isKeyDown(Keyboard.KEY_Q) || Keyboard.isKeyDown(Keyboard.KEY_A)){
	      position.x += distance * (float)Math.sin((pitch-90));
	      //position.z -= distance * (float)Math.cos((pitch-90));
	    }
	    if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
	      position.y+=0.2f;
	    }
	    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
	      position.y-=0.2f;
	    }
	    
	    if (Mouse.isButtonDown(0)) {
//	      int dx = Mouse.getDX();
//          int dy = Mouse.getDY();
//          pitch -= (dx * mouseSensitivity);
          
          float pitchChange = Mouse.getDY() * 0.2f;
          pitch -= pitchChange;
          if(pitch < 0){
              pitch = 0;
          }else if(pitch > 90){
              pitch = 90;
          }
          
          float angleChange = Mouse.getDX() * 0.3f;
          yaw -= angleChange;
          
//          yaw -= (dy * mouseSensitivity);
	    }  
	}
}
