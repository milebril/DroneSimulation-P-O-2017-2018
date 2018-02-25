package shaders;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;

import toolbox.Maths;

import entities.Camera;

public class StaticShaderTextures extends ShaderProgram{
	
	private static final String VERTEX_FILE = "src/shaders/vertexShaderTexture.txt";
	private static final String FRAGMENT_FILE = "src/shaders/fragmentShaderTexture.txt";
	
	private int location_transformationMatrix;
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_orthoMatrix;

	public StaticShaderTextures() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoords");
	}

	@Override
	protected void getAllUniformLocations() {
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_orthoMatrix = super.getUniformLocation("orthoMatrix");
		
	}
	
	public void loadTransformationMatrix(Matrix4f matrix){
		super.loadMatrix(location_transformationMatrix, matrix);
	}
	
	public void loadViewMatrix(Camera camera){
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		super.loadMatrix(location_viewMatrix, viewMatrix);
	}
	
	public void loadOrthogonalViewMatrix(Matrix4f projection) {
		super.loadMatrix(location_orthoMatrix, projection);
	}
	
	public void loadProjectionMatrix(Matrix4f projection){
		super.loadMatrix(location_projectionMatrix, projection);
	}
	
	

}
