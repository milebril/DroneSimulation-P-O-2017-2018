package autopilot.algorithms;

import autopilot.Algorithm;
import autopilot.AlgorithmHandler;

public class SpeedUp  implements Algorithm {

	@Override
	public void cycle(AlgorithmHandler handler) {
		
		handler.setThrust(handler.getProperties().getMaxThrust());
		handler.setFrontBrakeForce(0);
		handler.setLeftBrakeForce(0);
		handler.setRightBrakeForce(0);
		
		// if takeoff velocity is reached, switch to takeoff algorithm
		if (Takeoff.takeoffVelocity <= handler.getProperties().getVelocity().length()) {
			handler.setAlgorithm(new Takeoff());
		}
	}

	@Override
	public String getName() {
		return "SpeedUp";
	}

}
