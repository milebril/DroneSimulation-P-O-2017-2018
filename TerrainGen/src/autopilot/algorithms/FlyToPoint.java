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
		System.out.println();
		
		// if heading error is small, use vert stab, else use rolll
		if (Math.abs(headingError) < 0.0) {
			System.out.println("- - vert stab - -");
			feedback = vertrollPID.getFeedback(- handler.getProperties().getRoll(), dt);// limit inclination
			float altitudeFeedback = altitudePID.getFeedback(-handler.getProperties().getVelocity().y, dt);

			System.out.println("rollPID feedback: " + Math.round(feedback * 10000.0) / 10000.0);
			System.out.println("altitude feedback: " + Math.round(altitudeFeedback * 10000.0) / 10000.0);
			
			maxIncl = handler.getProperties().getMaxInclinationLeftWing();
			feedback = limitFeedback(altitudeFeedback-feedback, maxIncl[0],  maxIncl[1]);
			handler.setLeftWingInclination(feedback);
			maxIncl = handler.getProperties().getMaxInclinationRightWing();
			feedback = limitFeedback(altitudeFeedback+feedback, maxIncl[0],  maxIncl[1]);
			handler.setRightWingInclination(feedback);
			
			
			feedback = vertStabPID.getFeedback(-headingError, dt);
			maxIncl = handler.getProperties().getMaxInclinationVertStab();
			feedback = limitFeedback(feedback, maxIncl[0],  maxIncl[1]);
			handler.setVerStabInclination(feedback);
			
		} else {
			System.out.println("- - roll - -");
			System.out.println("heading error: " + Math.round(headingError * 10000.0) / 10000.0);
			
			// to get the desired heading vector, the plane has to roll
			float rollTarget = headingPID.getFeedback(headingError, dt);
			float rollError = rollTarget - handler.getProperties().getRoll();
			
			System.out.println("rollTarget: " + rollTarget + " (error: " + rollError + ")");
			
			// to get the required roll, use pid for left and right wing inclinations
			feedback = rollPID.getFeedback(rollError, dt);
			
			System.out.println("rollPID feedback: " + Math.round(feedback * 10000.0) / 10000.0);
			
			// alt PID
			float altError = handler.getProperties().getY() - 40;
			float altPID = altitudePID.getFeedback(altError, dt);
			
			System.out.println("altFeedback: " + altPID);
			System.out.println();
						
			
			
			// limit inclination
			maxIncl = handler.getProperties().getMaxInclinationLeftWing();
			float left = limitFeedback(-feedback + altPID, maxIncl[0],  maxIncl[1]);
			left = smooth(left, handler.getLeftWingInclination(), dt);
			maxIncl = handler.getProperties().getMaxInclinationRightWing();
			float right = limitFeedback(feedback + altPID, maxIncl[0],  maxIncl[1]);
			right = smooth(right, handler.getRightWingInclination(), dt);
			
			handler.setLeftWingInclination(left);
			handler.setRightWingInclination(right);
			
			
			// get resulting momentum inbalance (to counter adverse yaw)
			float momentum = getMomentumInbalance(handler, left, right);
			// force the vert stab has to excert in the negative x direction (of drone frame)
			float requiredForce = momentum / handler.getProperties().getTailSize();
			// calculate vert stab inclination required for this force
			
			
			System.out.println("requiredForce: " + requiredForce);
			maxIncl = handler.getProperties().getMaxInclinationVertStab();
			feedback = limitFeedback(-0.003f * requiredForce, maxIncl[0],  maxIncl[1]);
			handler.setVerStabInclination(feedback);
			
			
			
			// horstab
			float pitchError = handler.getProperties().getPitch();
			float pitchFeedback = pitchPID.getFeedback(pitchError, dt);
			
			maxIncl = handler.getProperties().getMaxInclinationRightWing();
			pitchFeedback = limitFeedback(-10*pitchFeedback, maxIncl[0],  maxIncl[1]);
			
			System.out.println("\npitchError:" + pitchError);
			System.out.println("pitchFeedback:" + pitchFeedback);
			handler.setHorStabInclination(0.75f*pitchFeedback);
			
			
			
		}
		
		// HORIZONTAL STABILIZER -> pitch op 0 deg houden
		float pitch = 0.0f;
		maxIncl = handler.getProperties().getMaxInclinationHorStab();
		feedback = horStabPID.getFeedback(handler.getProperties().getPitch() - pitch, dt);
		feedback = limitFeedback(feedback, maxIncl[0],  maxIncl[1]);
		handler.setHorStabInclination(feedback);
		
		
		
		
		// velocity op 50 m/s houden
		float cruiseForce = handler.getProperties().getGravity();
		feedback = thrustPID.getFeedback(50 - handler.getProperties().getVelocity().length(), dt);
		handler.setThrust(Math.max(0, cruiseForce + feedback));
		System.out.println();
		System.out.println("thrust: " + Math.min(Math.max(0, cruiseForce + feedback), handler.getProperties().getMaxThrust()));
		
		boolean reached = false;
		// if the point is reached activate next algorithm
		if (reached) {
			handler.setAlgorithm(getNextAlgorithm());
		}
	}
	
	private PID thrustPID = new PID(1000, 400, 50, 2000);
	private PID rollPID = new PID(1f, 0.05f, 0.0f, (float) (30 * Math.PI / 180));
	private PID headingPID = new PID(2f, 0.1f, 0.1f, maxRoll);
	private PID horStabPID = new PID(4, 1, 1, 2);
	private PID altitudePID = new PID(0.1f, 1f, 0.0f, (float) (10 * Math.PI / 180));
	private PID vertStabPID = new PID(1.5f, 0.7f, 0.2f,(float) (15 * Math.PI / 180));
	private PID vertrollPID = new PID(1.5f, 0.2f, 0.1f, (float) (30 * Math.PI / 180));

	private PID pitchPID = new PID(1f, 0.1f, 0.01f, (float) (10 * Math.PI / 180));
	
	
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
