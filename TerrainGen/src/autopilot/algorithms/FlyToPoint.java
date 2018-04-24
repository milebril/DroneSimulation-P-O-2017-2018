package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.Properties;

/**
 * An algorithm for flying to a given point.
 */
public class FlyToPoint implements Algorithm {
	
	public FlyToPoint(Algorithm nextAlgorithm, Vector3f point) {
		this.point = point;
		this.nextAlgorithm = nextAlgorithm;
	}
	
	private final Vector3f point;
	private Vector3f getPoint() {
		return this.point;
	}
	
	private final Algorithm nextAlgorithm;
	private Algorithm getNextAlgorithm() {
		return this.nextAlgorithm;
	}

	
	private float maxRoll = (float) Math.toRadians(30);
	private float wingBaseIncl = (float) Math.toRadians(2);
	private float smoothness = 9.4f; // max inclination change per dt
	private float height = -1;
	
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		if (height == -1) {
			height = handler.getProperties().getY();
		}
		
		float dt = handler.getProperties().getDeltaTime();
		float feedback;
		float[] maxIncl;
		
		// position of the point, relative to the drone position
		Vector3f relativePosition =  new Vector3f();
		Vector3f.sub(getPoint(), handler.getProperties().getPosition(), relativePosition);
		
		// HEADING
		float headingTarget = (float) Math.PI/2; //(float) Math.atan2(-relativePosition.x, -relativePosition.z);
		float headingError = headingTarget - handler.getProperties().getHeading();
		
		System.out.println("--------------------");
		System.out.println("heading error: " + headingError + " rad");
		
		// ROLL
		
		
		
		float roll = handler.getProperties().getRoll();
		float rollTarget = maxRoll;
		float rollError = (rollTarget - roll);
		System.out.println("roll: " + roll);
		System.out.println("rollError: " + rollError);
		
		
		float deltaRoll = limitFeedback(rollError, -0.05f, 0.05f);
		deltaRoll = rollPID.getFeedback(rollError, dt);
		System.out.println("deltaRoll: " + deltaRoll + " rad");
		
		
		
		float rightWingInc = deltaRoll;
		float lefttWingInc = -deltaRoll;
		float vertStabInc = -0.6f * deltaRoll;
		
		
		handler.setLeftWingInclination(wingBaseIncl+lefttWingInc);
		handler.setRightWingInclination(wingBaseIncl+rightWingInc);
		handler.setVerStabInclination(vertStabInc);
		
		
		float pitch = handler.getProperties().getPitch();
		float pitchTarget = 0;
		float pitchError = pitchTarget-pitch;
		float heightError = height - handler.getProperties().getY();
		float horIncl = horstabPID.getFeedback(-4f *pitchError - 0.2f*heightError, dt);
		System.out.println("pitch: " + pitch);
		handler.setHorStabInclination(horIncl);
		
		
		
		
		
		// velocity op 50 m/s houden
		float cruiseForce = handler.getProperties().getGravity();
		feedback = thrustPID.getFeedback(50 - handler.getProperties().getVelocity().length(), dt);
		handler.setThrust(Math.max(0, cruiseForce + feedback));
		System.out.println();
		System.out.println("thrust: " + Math.min(Math.max(0, cruiseForce + feedback), handler.getProperties().getMaxThrust()));
		
		
		boolean reached = false;
		if (headingError < 0.2) {
			reached = true;
		}
		// if the point is reached activate next algorithm
		if (reached) {
			handler.setAlgorithm(getNextAlgorithm());
		}
	}
	
	private PID thrustPID = new PID(1000, 400, 50, 2000);
	private PID horstabPID = new PID(1f, 0.5f, 0.3f, 0.2f);
	private PID rollPID = new PID(1f, 0.2f, 0f, 0.5f);
	
	@Override
	public String getName() {
		return "FlyToPoint";
	}
	
	private float limitFeedback(float feedback, float min, float max) {
		if (0.75f*max < feedback) return 0.75f*max;
		else if (feedback < 0.75f*min) return 0.75f*min;
		else return feedback;
	}
	
	/**
	 * Returns the momentum (around the drone frame y-axis) caused by the difference in drag between the left and right wing.
	 */
	private float getMomentumInbalance(AlgorithmHandler handler, float leftInclination, float rightInclination) {
		// both these forces are in drone frame
		Vector3f leftWingForce = handler.getProperties().getLeftWingForce(leftInclination);
		Vector3f rightWingForce = handler.getProperties().getRightWingForce(rightInclination);
		
		return (leftWingForce.z - rightWingForce.z) * handler.getProperties().getWingX();
	}



	private float smooth(float desiredValue, float currentValue, float dt) {
		float delta = desiredValue - currentValue;
		if (delta/dt < -smoothness) delta = -smoothness*dt;
		if (smoothness < delta/dt) delta = smoothness*dt;
		return currentValue + delta;
	}











}
