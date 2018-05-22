package autopilot.interfaces.path;

import java.util.Iterator;
import java.util.LinkedList;

import org.lwjgl.util.vector.Vector3f;

import autopilot.interfaces.Path;

public class VectorPath extends LinkedList<Vector3f> implements Path {
	
	// Constructors
	
	public VectorPath(Path path) {
		for (int i = 0; i < path.getX().length; i++) {
			this.add(new Vector3f(path.getX()[i], path.getY()[i], path.getZ()[i]));
		}
	}
	
	public VectorPath() {}
	
	// Path interface
	
	@Override
	public float[] getX() {
		float[] X = new float[this.size()];
		Iterator<Vector3f> iterator = this.iterator();
		int i = 0;
		while (iterator.hasNext())
		{
			X[i] = iterator.next().x;
			i++;
		}
		return X;
	}
	
	@Override
	public float[] getY() {
		float[] Y = new float[this.size()];
		Iterator<Vector3f> iterator = this.iterator();
		int i = 0;
		while (iterator.hasNext())
		{
			Y[i] = iterator.next().y;
			i++;
		}
		return Y;
	}
	
	@Override
	public float[] getZ() {
		float[] Z = new float[this.size()];
		Iterator<Vector3f> iterator = this.iterator();
		int i = 0;
		while (iterator.hasNext())
		{
			Z[i] = iterator.next().z;
			i++;
		}
		return Z;
	}
	
	private static final long serialVersionUID = 2466428834918673167L; // om een eclipse warning te voorkomen
}
