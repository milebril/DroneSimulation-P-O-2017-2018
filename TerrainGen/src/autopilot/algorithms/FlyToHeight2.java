package autopilot.algorithms;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;

public class FlyToHeight2 implements Algorithm {

	public FlyToHeight2(float height, float z) {
		this.height = height;
		this.z = z;
	}
	
	private float height;
	private float z;
	
	private float prevP = -100f;
	private float prevH = -100f;
	
	private float cruiseSpeed = AutopilotAlain.CRUISESPEED;
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		
		float dt = handler.getProperties().getDeltaTime();
		
		// SNELHEID ~ THRUST
		float feedback = thrustPID.getFeedback(cruiseSpeed - handler.getProperties().getVelocity().length(), dt);
		handler.setThrust(Math.max(0, feedback));
		
		// STIJGEN/DALEN ~ ZIJVLEUGELS
		float heightError = this.height - handler.getProperties().getY();
		feedback = heightPID.getFeedback(heightError, dt);
		
		handler.setLeftWingInclination(0.1f+feedback);
		handler.setRightWingInclination(0.1f+feedback);
		
		// PITCH ~ HOR STAB
		feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), dt);
		handler.setHorStabInclination(feedback);
		
		
		// CONTROLEREN OF DOEL (stabiliteit op gevraagde hoogte) BEREIKT IS
		if (prevP == -100f)	prevP = handler.getProperties().getPitch();
		if (prevH == -100f)	prevH = handler.getProperties().getY();
		float deltaP = (handler.getProperties().getPitch() - prevP)/dt;
		prevP = handler.getProperties().getPitch();
		float deltaH = (handler.getProperties().getY() - prevH)/dt;
		prevH = handler.getProperties().getY();
		
		// als hoogte behaalt is en drone gestabiliseert is, volgend algoritme
		if (handler.getProperties().getZ() < this.z) {
			handler.nextAlgorithm();
		}
		
	}

	private PID thrustPID = new PID(1000, 400, 50, 2000);
	
	private PID rollPID = new PID(1f, 0f, 0f, 0.3f);
	
	private PID heightPID = new PID(0.5f, 0.5f, 0.1f, 0.2f);
	private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);

	@Override
	public String getName() {
		return "FlyToHeight("+height+")";
	}
	
}
