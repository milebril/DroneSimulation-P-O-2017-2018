package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.interfaces.Path;

public class HoogteKubussen implements Algorithm {
	
	float afstandOmKubusTeBereiken = 5;
	
	public HoogteKubussen(Algorithm nextAlgorithm, Path path) {
		this.path = path;
		this.nextAlgorithm = nextAlgorithm;
	}
	
	private Path path;
	private Algorithm nextAlgorithm;
	
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		// check of the kubus is bereikt 
		
	}
	
	private boolean near(AlgorithmHandler handler, float x, float y, float z) {
		float afstand = (float) Math.sqrt(Math.pow(x-handler.getProperties().getX(), 2) + Math.pow(y-handler.getProperties().getY(), 2) + Math.pow(z-handler.getProperties().getZ(), 2));
		return afstand <= afstandOmKubusTeBereiken;
	}
	
	private float getEuclidDist(Vector3f vec1, Vector3f vec2) {
		Vector3f temp = new Vector3f(0, 0, 0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
	}
	
	
	@Override
	public String getName() {
		return "HoogteKubussen";
	}
	
	
}
