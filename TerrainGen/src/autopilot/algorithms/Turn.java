package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.algorithmHandler.AlgorithmHandler;

/**
 * An algorithm aligning the drone with the given heading orientation.
 */
public class Turn implements Algorithm {
	
	public Turn(Algorithm nextAlgorithm, float headingDest) {
		this.nextAlgorithm = nextAlgorithm;
		this.headingDest = headingDest;
	}
	
	private final float headingDest;
	private float getHeadingDest() {
		return this.headingDest;
	}
	
	private final Algorithm nextAlgorithm;
	private Algorithm getNextAlgorithm() {
		return this.nextAlgorithm;
	}

	@Override
	public void cycle(AlgorithmHandler handler) {
		// Pitch op 0 houden
		
		// Roll op theta houden
		
		// tot gewenste heading is bereikt
		
	}

	@Override
	public String getName() {
		return "Turn";
	}

}
