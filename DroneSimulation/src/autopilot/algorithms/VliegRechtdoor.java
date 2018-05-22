package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;

public class VliegRechtdoor implements Algorithm {
	
	@Override
	public void cycle(AlgorithmHandler handler) {

		float dt = handler.getProperties().getDeltaTime();
		float feedback;
		
		handler.setVerStabInclination(0);
		
		// PITCH ~ HOR STAB
		feedback = horStabPID.getFeedback(handler.getProperties().getPitch(), dt);
		handler.setHorStabInclination(feedback);
		
		// ROLL & HEIGHT
		float rollFeedback = rollPID.getFeedback(handler.getProperties().getRoll(), dt);
		
		float heightError = handler.getProperties().getY() - handler.getProperties().getCruiseheight();
		float heightFeedback = heightPID.getFeedback(heightError, dt);
		
		handler.setLeftWingInclination(heightFeedback-rollFeedback+0.15f);
		handler.setRightWingInclination(heightFeedback+rollFeedback+0.15f);
		
		// THRUST
		float cruiseForce = handler.getProperties().getGravity();
		feedback = thrustPID.getFeedback(AutopilotAlain.CRUISESPEED - handler.getProperties().getVelocity().length(), dt);
		handler.setThrust(Math.max(0, cruiseForce + feedback));
	}
	
	private PID horStabPID = new PID(4, 1, 1, 2);
	private PID thrustPID = new PID(1000, 400, 50, 2000);
	private PID heightPID = new PID(0.1f, 1f, 0.0f, 2);
	private PID rollPID = new PID(0.7f, 0.5f, 0.16f, 2);
	private PID vertStabPID = new PID(0, 0, 0.05f, 2);

	@Override
	public String getName() {
		return "Yayo";
	}
	
	private float limitFeedback(float feedback, float min, float max) {
		if (0.75f*max < feedback) return 0.75f*max;
		else if (feedback < 0.75f*min) return 0.75f*min;
		else return feedback;
	}
}
