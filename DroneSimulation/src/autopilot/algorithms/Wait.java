package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import prevAutopilot.PIDController;

public class Wait implements Algorithm {

	public Wait() {
	}

	@Override
	public void cycle(AlgorithmHandler handler) {
		handler.setFrontBrakeForce(handler.getProperties().getRMax());
		handler.setHorStabInclination(0);
		handler.setLeftBrakeForce(handler.getProperties().getRMax());
		handler.setLeftWingInclination(0);
		handler.setRightBrakeForce(handler.getProperties().getRMax());
		handler.setRightWingInclination(0);
		handler.setThrust(0);
		handler.setVerStabInclination(0);
	}

	@Override
	public String getName() {
		return "Wait";
	}
}
