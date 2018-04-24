package autopilot.algorithms;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;

public class FlyToHeight implements Algorithm {

	public FlyToHeight(float height) {
		this.height = height;
	}
	
	private float height;
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		float dt = handler.getProperties().getDeltaTime();
		
		// roll stabilizeren
		float rollError = handler.getProperties().getRoll();
		
		float feedback = rollPID.getFeedback(rollError, dt);
		float leftInc = -feedback;
		float rightInc = feedback;
		float vertStabInc = -0.6f * feedback;
		handler.setLeftWingInclination(leftInc);
		handler.setRightWingInclination(rightInc);
		handler.setVerStabInclination(vertStabInc);
		
		// snelheid behouden
		float cruiseForce = handler.getProperties().getGravity();
		feedback = thrustPID.getFeedback(50 - handler.getProperties().getVelocity().length(), dt);
		handler.setThrust(Math.max(0, cruiseForce + feedback));
		
		// als roll klein genoeg is, stijg
		if (Math.abs(rollError) < 0.1f) {
			float heightError = this.height - handler.getProperties().getY();
			feedback = heightPID.getFeedback(heightError, dt);
			System.out.println("heightPID feedback: " + feedback);
			feedback = pitchPID.getFeedback(feedback-handler.getProperties().getPitch(), dt);
			System.out.println("pitchPID feedback: " + -feedback);
			
			handler.setHorStabInclination(-feedback);
			
			
			
			
			
			
		}
	}

	private PID thrustPID = new PID(1000, 400, 50, 2000);
	private PID rollPID = new PID(1f, 0f, 0f, 0.3f);
	
	private PID heightPID = new PID(0.5f, 0.5f, 0.1f, 2f);
	private PID pitchPID = new PID(1f, 0.1f, 0.1f, 0.2f);

	@Override
	public String getName() {
		return "FlyToHeight";
	}
	
}
