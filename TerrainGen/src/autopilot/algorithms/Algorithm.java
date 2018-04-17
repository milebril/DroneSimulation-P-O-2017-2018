package autopilot.algorithms;

import autopilot.algorithmHandler.AlgorithmHandler;

public interface Algorithm {
	public void cycle(AlgorithmHandler handler);
	public String getName();
}
