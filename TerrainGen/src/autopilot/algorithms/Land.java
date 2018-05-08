package autopilot.algorithms;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;

public class Land implements Algorithm {

	@Override
	public void cycle(AlgorithmHandler handler) {
		float dt = handler.getProperties().getDeltaTime();
		if (0.04 < Math.abs(handler.getProperties().getRoll())) {
			// roll fixen
			float feedback = rollPID.getFeedback(handler.getProperties().getRoll(), dt);
			handler.setLeftWingInclination(feedback);
			handler.setRightWingInclination(feedback);
		} else {
			handler.setThrust(0);
			
			// pitch op 0 houden
			float feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), dt);
			handler.setHorStabInclination(feedback);
			
			// rusting dalen
			handler.setLeftWingInclination(0);
			handler.setRightWingInclination(0);
			
			// remmen
			handler.setLeftBrakeForce(handler.getProperties().getRMax());
			handler.setRightBrakeForce(handler.getProperties().getRMax());
		}
	}
	
	
	private PID rollPID = new PID(0.7f, 0.01f, 0.1f, 0.8f);
	private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);

	@Override
	public String getName() {
		return "Landen";
	}

}
