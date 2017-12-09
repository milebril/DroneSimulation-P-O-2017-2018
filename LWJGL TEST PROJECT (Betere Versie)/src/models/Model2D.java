package models;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.BufferUnderflowException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Model2D {
	
	private int drawCount;
	private int vaoID;
	
	public Model2D(int vaoID, int drawCount) {
		this.vaoID = vaoID;
		this.drawCount = drawCount;
	}
	
	public void render() {
		GL30.glBindVertexArray(vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, drawCount);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		
		System.out.println("hier");
	}
}
