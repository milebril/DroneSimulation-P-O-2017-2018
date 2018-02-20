package models;

public class RawOBJModel {
	private int vaoID;
	private int vertexCount;
	
	public RawOBJModel(int vaoID, int vertexCount){
		this.vaoID = vaoID;
		this.vertexCount = vertexCount;
	}

	public int getVaoID() {
		return vaoID;
	}

	public int getVertexCount() {
		return vertexCount;
	}
}
