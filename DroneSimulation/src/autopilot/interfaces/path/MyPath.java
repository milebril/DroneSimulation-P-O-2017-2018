package autopilot.interfaces.path;

import java.util.LinkedList;
import org.lwjgl.util.vector.Vector3f;

import autopilot.interfaces.Path;

public class MyPath extends LinkedList<Vector3f> implements Path {
	
	public void addInaccurate(Vector3f pos, float margin) {
		add(new Vector3f(pos.x + (float) (2*Math.random()-1)*margin, pos.x + (float) (2*Math.random()-1)*margin,pos.x + (float) (2*Math.random()-1)*margin));
	}
	
	@Override
	public float[] getX() {
		float[] x = new float[size()];
		for (int i = 0; i<size();i++) {
			x[i] = this.get(i).x;
		}
		return x;
	}

	@Override
	public float[] getY() {
		float[] y = new float[size()];
		for (int i = 0; i<size();i++) {
			y[i] = this.get(i).y;
		}
		return y;
	}

	@Override
	public float[] getZ() {
		float[] z = new float[size()];
		for (int i = 0; i<size();i++) {
			z[i] = this.get(i).z;
		}
		return z;
	}

}
