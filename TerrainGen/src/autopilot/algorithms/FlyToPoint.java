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

	private float height = 40;
	
	private float maxRoll = (float) Math.toRadians(30);
	
	private float smoothness = 9.4f; // max inclination change per dt
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		float dt = handler.getProperties().getDeltaTime();
		float feedback;
		float[] maxIncl;
		
		// position of the point, relative to the drone position
		Vector3f relativePosition =  new Vector3f();
		Vector3f.sub(getPoint(), handler.getProperties().getPosition(), relativePosition);
		
		// calculate heading error
		float headingTarget = 0.5f; //(float) Math.atan2(-relativePosition.x, -relativePosition.z);
		float headingError = headingTarget - handler.getProperties().getHeading();
		float headingFeedback = headingPID.getFeedback(headingError, dt);
		
		
		
		System.out.println();
		System.out.println("heading error: " + headingError);
		
		// pitch
		float pitchTarget = 0;
		float pitchError = pitchTarget - handler.getProperties().getPitch();
		feedback = pitchPID.getFeedback(pitchError, dt);
		maxIncl = handler.getProperties().getMaxInclinationHorStab();
		feedback = limitFeedback(feedback, maxIncl[0], maxIncl[1]);
		handler.setHorStabInclination(-feedback);
		
		// altitude
		float altitudeTarget = 22;
		float altitudeError = altitudeTarget - handler.getProperties().getY();
		float altitudeFeedback = altitudePID.getFeedback(altitudeError, dt);
		
		maxIncl = handler.getProperties().getMaxInclinationLeftWing();
		altitudeFeedback = limitFeedback(altitudeFeedback, maxIncl[0], maxIncl[1]);
		handler.setLeftWingInclination(altitudeFeedback);
		handler.setRightWingInclination(altitudeFeedback);
		
		
		
		// velocity op 50 m/s houden
		float cruiseForce = handler.getProperties().getGravity();
		feedback = thrustPID.getFeedback(50 - handler.getProperties().getVelocity().length(), dt);
		handler.setThrust(Math.max(0, cruiseForce + feedback));
		System.out.println();
		System.out.println("thrust: " + Math.min(Math.max(0, cruiseForce + feedback), handler.getProperties().getMaxThrust()));
		
		boolean reached = true;
		// if the point is reached activate next algorithm
		if (reached) {
			handler.setAlgorithm(getNextAlgorithm());
		}
	}
	
	private PID thrustPID = new PID(1000, 400, 50, 2000);
	private PID altitudePID = new PID(0.1f, 1f, 0.0f, (float) (10 * Math.PI / 180));
	private PID pitchPID = new PID(1f, 0.5f, 0.01f, (float) (10 * Math.PI / 180));
	private PID rollPID = new PID(1f, 0.2f, 0.05f,  (float) (45 * Math.PI / 180));
	private PID headingPID = new PID(1f, 0.2f, 0.05f,  (float) (45 * Math.PI / 180));
	
	
	@Override
	public String getName() {
		return "FlyToPoint";
	}
	
	private float limitFeedback(float feedback, float min, float max) {
		if (0.95f*max < feedback) return 0.95f*max;
		else if (feedback < 0.95f*min) return 0.95f*min;
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
