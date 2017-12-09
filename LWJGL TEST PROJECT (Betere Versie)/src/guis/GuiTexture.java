package guis;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;

public class GuiTexture {
	private int texture;
	private Vector2f position;
	private Vector2f scale;
	public GuiTexture(int texture, Vector2f position, Vector2f scale) {
		this.texture = texture;
		this.position = position;
		this.scale = scale;
	}
	public int getTexture() {
		return texture;
	}
	public Vector2f getPosition() {
		return position;
	}
	public Vector2f getScale() {
		return scale;
	}
	public void setColor(Vector4f color) {
		// TODO Auto-generated method stub
	}
	public void setScale(Vector2f scale) {
		this.scale = scale;
	}
	public void setPosition(Vector2f position2) {
		this.position = position;
	}
}
