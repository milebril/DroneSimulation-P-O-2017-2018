package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;

/**
 * An algorithm aligning the drone with the given heading orientation.
 */
public class Turn implements Algorithm {
	
	public Turn(float headingDest) {
		this.headingDest = headingDest;
	}
	
	private final float headingDest;

	private float maxRoll = (float) Math.toRadians(40);
	private float wingBaseIncl = (float) Math.toRadians(2);
	private float smoothness = 9.4f; // max inclination change per dt
	private float height = -1;
	
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		if (height == -1) {
			height = handler.getProperties().getY();
		}
		float heightError = handler.getProperties().getY() - height;
		
		float dt = handler.getProperties().getDeltaTime();

		float roll = handler.getProperties().getRoll();
		float heading = handler.getProperties().getHeading();
		float pitch = handler.getProperties().getPitch();
		
		float feedback;
		
		// HEADING
		float headingError = headingDest - handler.getProperties().getHeading();
		
		// ROLL
		float rollFeedback = rollPID.getFeedback(headingError, dt);
		float rollError = (rollFeedback - roll);
		
		// REAR WINGS
		
		float wingFeedback = rearWingPID.getFeedback(rollError, dt);
		handler.setLeftWingInclination(-wingFeedback+0.15f);
		handler.setRightWingInclination(wingFeedback+0.15f);
		handler.setVerStabInclination(-0.7f*wingFeedback);
		
		// HOR STAB
		feedback = horstabPID.getFeedback(handler.getProperties().getPitch(), dt);
		handler.setHorStabInclination(-0.07f);
		
		

		
		System.out.println("--------------------");
		System.out.println("heading error: " + headingError + " rad");
		System.out.println("rollPID: " + rollFeedback);
		System.out.println("roll: " + roll + " -> error: " + rollError);
		System.out.println("wingPID: " + wingFeedback);
		System.out.println("HEightERROR " + heightError);
		
		
		
		// cruisesnelheid houden
		float cruiseForce = handler.getProperties().getGravity();
		feedback = thrustPID.getFeedback(AutopilotAlain.CRUISESPEED - handler.getProperties().getVelocity().length(), dt);
		handler.setThrust(Math.max(0, cruiseForce + feedback));
		
		
		if (Math.abs(headingError) < 0.05) {
			handler.nextAlgorithm();
		}
	}

	private PID rollPID = new PID(0.5f, 0.2f, 0.05f, maxRoll);
	private PID rearWingPID = new PID(0.5f, 0.05f, 0f, 0.1f);
	private PID heightPID = new PID(0.5f, 0.05f, 0f, 0.1f);
	
	private PID thrustPID = new PID(1000, 400, 50, 2000);
	
	private PID horstabPID = new PID(1f, 0.1f, 0.1f, 1f);
	

	@Override
	public String getName() {
		return "Turn";
	}

}
