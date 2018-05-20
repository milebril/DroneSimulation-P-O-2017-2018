package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.Properties;
import prevAutopilot.PIDController;

/**
 * An algorithm for flying to a given point.
 */
public class FlyToPoint implements Algorithm {

	private PID heightPID = new PID(0.5f, 0.5f, 0.1f, 0.2f);
	private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);

	private final PIDController pidRoll;

	private float prevP = -100f;
	private float prevH = -100f;

	public FlyToPoint(Algorithm nextAlgorithm, Vector3f point) {
		this.point = point;
		this.nextAlgorithm = nextAlgorithm;
		this.pidRoll = new PIDController(5.0f, 0.0f, 3.0f, (float) Math.toRadians(1), (float) Math.toRadians(-30));
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
		float[] maxIncl;

		// BLIJF OP JUISTE HOOGTE

		// handler.setHorStabInclination(handler.getHorStabInclination() +
		// pidHorStab.calculateChange(
		// handler.getProperties().getPitch() + getVerAngle(handler),
		// handler.getProperties().getDeltaTime()));
		// if (handler.getHorStabInclination() > Math.PI / 6) {
		// handler.setHorStabInclination((float) (Math.PI / 6));
		// } else if (handler.getHorStabInclination() < -Math.PI / 6) {
		// handler.setHorStabInclination((float) -(Math.PI / 6));
		// }

		// properties.setHorStabInclination((float)Math.toRadians(-4));
		handler.setVerStabInclination((float) Math.toRadians(0));
		
		float headingError = (float) Math.tan((point.x - handler.getProperties().getPosition().x) / (point.z - handler.getProperties().getPosition().z));
		float feedback = rollPID.getFeedback(headingError, dt);
		
		// ROLL AT 15 DEGREES
		//float changeWingRoll = this.pidRoll.calculateChange(handler.getProperties().getRoll(),
				//handler.getProperties().getDeltaTime());
		handler.setLeftWingInclination(-feedback * 0.1f);
//		if (handler.getLeftWingInclination() > Math.toRadians(15))
//			handler.setLeftWingInclination((float) Math.toRadians(15));
//		if (handler.getLeftWingInclination() < Math.toRadians(-15))
//			handler.setLeftWingInclination((float) Math.toRadians(-15));
		handler.setRightWingInclination(feedback * 0.1f);
//		if (handler.getRightWingInclination() > Math.toRadians(15))
//			handler.setRightWingInclination((float) Math.toRadians(15));
//		if (handler.getRightWingInclination() < Math.toRadians(-15))
//			handler.setRightWingInclination((float) Math.toRadians(-15));

		
		// STIJGEN/DALEN ~ ZIJVLEUGELS
		float heightError = point.y - handler.getProperties().getY();
		feedback = heightPID.getFeedback(heightError, dt);

		handler.setLeftWingInclination(handler.getLeftWingInclination() + feedback);
		handler.setRightWingInclination(handler.getRightWingInclination() + feedback);

		// PITCH ~ HOR STAB
		feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), dt);
		handler.setHorStabInclination(feedback);

		// CONTROLEREN OF DOEL (stabiliteit op gevraagde hoogte) BEREIKT IS
		if (prevP == -100f)
			prevP = handler.getProperties().getPitch();
		if (prevH == -100f)
			prevH = handler.getProperties().getY();
		float deltaP = (handler.getProperties().getPitch() - prevP) / dt;
		prevP = handler.getProperties().getPitch();
		float deltaH = (handler.getProperties().getY() - prevH) / dt;
		prevH = handler.getProperties().getY();
		System.out.println(heightError + " E");

		// velocity op 50 m/s houden
		float cruiseForce = handler.getProperties().getGravity();
		feedback = thrustPID.getFeedback(50 - handler.getProperties().getVelocity().length(), dt);
		handler.setThrust(Math.max(0, cruiseForce + feedback));
		// System.out.println();
		// System.out.println(
		// "thrust: " + Math.min(Math.max(0, cruiseForce + feedback),
		// handler.getProperties().getMaxThrust()));

		boolean reached = false;
		if (heightError < 0.2 && handler.getProperties().getZ() < point.z) { // TODO
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
		if (0.75f * max < feedback)
			return 0.75f * max;
		else if (feedback < 0.75f * min)
			return 0.75f * min;
		else
			return feedback;
	}

	/**
	 * Returns the momentum (around the drone frame y-axis) caused by the difference
	 * in drag between the left and right wing.
	 */
	private float getMomentumInbalance(AlgorithmHandler handler, float leftInclination, float rightInclination) {
		// both these forces are in drone frame
		Vector3f leftWingForce = handler.getProperties().getLeftWingForce(leftInclination);
		Vector3f rightWingForce = handler.getProperties().getRightWingForce(rightInclination);

		return (leftWingForce.z - rightWingForce.z) * handler.getProperties().getWingX();
	}

	private float smooth(float desiredValue, float currentValue, float dt) {
		float delta = desiredValue - currentValue;
		if (delta / dt < -smoothness)
			delta = -smoothness * dt;
		if (smoothness < delta / dt)
			delta = smoothness * dt;
		return currentValue + delta;
	}

}
