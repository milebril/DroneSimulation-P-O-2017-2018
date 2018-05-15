package autopilot.algorithms;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;

public class Stabilize implements Algorithm {

	@Override
	public void cycle(AlgorithmHandler handler) {
		
		float dt = handler.getProperties().getDeltaTime();
		
		// PITCH OP 0
		float feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), dt);
		handler.setHorStabInclination(feedback);
		System.out.println("pitchfeedback: " + feedback);
		
		// ROLL op 0
		feedback = rollPID.getFeedback(-handler.getProperties().getRoll(), dt);
		handler.setLeftWingInclination(-feedback+0.15f);
		handler.setRightWingInclination(feedback+0.15f);
		System.out.println("wingfeedback: " + feedback);
		
		// cruisesnelheid houden
		float cruiseForce = handler.getProperties().getGravity();
		feedback = thrustPID.getFeedback(AutopilotAlain.CRUISESPEED - handler.getProperties().getVelocity().length(), dt);
		handler.setThrust(Math.max(0, cruiseForce + feedback));
	}

	private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);
	private PID rollPID = new PID(1f, 0f, 0f, 0.3f);
	private PID thrustPID = new PID(1000, 400, 50, 2000);
	
	@Override
	public String getName() {
		return "Stabilize";
	}

}
