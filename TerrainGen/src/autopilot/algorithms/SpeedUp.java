package autopilot.algorithms;

import autopilot.algorithmHandler.AlgorithmHandler;

public class SpeedUp implements Algorithm {

	@Override
	public void cycle(AlgorithmHandler handler) {
		if (Takeoff.condition(handler)) {
			handler.setAlgorithm(new Takeoff());
		} else {
			handler.setThrust(handler.getProperties().getMaxThrust());
			handler.setFrontBrakeForce(0);
			handler.setLeftBrakeForce(0);
			handler.setRightBrakeForce(0);
		}
	}

	@Override
	public String getName() {
		return "SpeedUp";
	}

}
