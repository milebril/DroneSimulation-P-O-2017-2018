package path;

public class MyPath implements Path{
	
	private float[] x;
	private float[] y;
	private float[] z;
	private int index;

	public MyPath (float[] x, float[] y, float [] z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.index = 0;
	}
	
	public float getCurrentX() {
		return getX()[getIndex()];
	}
	
	public float getCurrentY() {
		return getY()[getIndex()];
	}
	
	public float getCurrentZ() {
		return getZ()[getIndex()];
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public void setIndex(int i) {
		this.index = i;
	}
	
	@Override
	public float[] getX() {
		return this.x;
	}

	@Override
	public float[] getY() {
		return this.y;
	}

	@Override
	public float[] getZ() {
		return this.z;
	}

}
