package models;

public class RawCubeModel {
	
	private int vaoID;
	private int vertexCount;
	
	public RawCubeModel(int vaoID, int vertexCount){
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

