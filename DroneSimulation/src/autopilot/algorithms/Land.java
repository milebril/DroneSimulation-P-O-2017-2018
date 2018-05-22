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
			handler.setLeftWingInclination(-feedback);
			handler.setRightWingInclination(-feedback);

			// remmen
			handler.setFrontBrakeForce(handler.getProperties().getRMax());
			handler.setLeftBrakeForce(handler.getProperties().getRMax());
			handler.setRightBrakeForce(handler.getProperties().getRMax());
		}

		// wanneer het vliegtuig stilstaat is het geland
		if (handler.getProperties().getVelocity().length() <= 0.1) {
			handler.nextAlgorithm();
		}
	}

	private PID rollPID = new PID(0.7f, 0.01f, 0.1f, 0.8f);
	private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);

	@Override
	public String getName() {
		return "Landen";
	}

}
