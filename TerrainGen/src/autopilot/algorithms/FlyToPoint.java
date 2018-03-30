package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.Properties;

/**
 * An algorithm for flying to a given point.
 */
public class FlyToPoint implements Algorithm {
	
	public FlyToPoint(Algorithm nextAlgorithm, Vector3f point) {
		this.point = point;
		this.nextAlgorithm = nextAlgorithm;
	}
	
	private final Vector3f point;
	private Vector3f getPoint() {
		return this.point;
	}
	
	private final Algorithm nextAlgorithm;
	private Algorithm getNextAlgorithm() {
		return this.nextAlgorithm;
	}

	@Override
	public void cycle(AlgorithmHandler handler) {
		// TODO Auto-generated method stub
		
		// if the point is reached
		boolean reached = true;
		if (reached) {
			handler.setAlgorithm(getNextAlgorithm());
		}
	}
	
	@Override
	public String getName() {
		return "FlyToPoint";
	}
}
