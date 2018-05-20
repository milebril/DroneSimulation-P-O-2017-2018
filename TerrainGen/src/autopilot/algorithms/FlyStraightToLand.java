package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;

public class FlyStraightToLand implements Algorithm{
	private float height = -1;
	
	private Vector3f whereToLand;
	
	public FlyStraightToLand(Vector3f position) {
		this.whereToLand = position;
	}

	@Override
	public void cycle(AlgorithmHandler handler) {
		if (this.height == -1)
			this.height = handler.getProperties().getY();

		handler.setHorStabInclination(0);
		handler.setVerStabInclination(0);

		// fly in a straight line at 40m/s, maintaining altitude
		float feedback;
		float dt = handler.getProperties().getDeltaTime();
		float maxIncl[];

		// HORIZONTAL STABILIZER -> pitch op 0 deg houden
		float pitch = 0.0f;
		maxIncl = handler.getProperties().getMaxInclinationHorStab();

		feedback = horStabPID.getFeedback(handler.getProperties().getPitch() - pitch, dt);
		feedback = limitFeedback(feedback, maxIncl[0], maxIncl[1]);
		handler.setHorStabInclination(feedback);

		// LEFT AND RIGHT WING -> ensure the plane is moving as its forward vector
		float roll = 0.0f;

		Vector3f velocityD = handler.getProperties().transformToDroneFrame(handler.getProperties().getVelocity());

		feedback = upwardsForcePID.getFeedback(-velocityD.y, dt);

		maxIncl = handler.getProperties().getMaxInclinationLeftWing();
		feedback = limitFeedback(feedback, maxIncl[0], maxIncl[1]);

		maxIncl = handler.getProperties().getMaxInclinationRightWing();
		feedback = limitFeedback(feedback, maxIncl[0], maxIncl[1]);

		handler.setLeftWingInclination(feedback);
		handler.setRightWingInclination(feedback);

		// velocity op 50 m/s houden
		float cruiseForce = handler.getProperties().getGravity();
		feedback = thrustPID.getFeedback(AutopilotAlain.CRUISESPEED - handler.getProperties().getVelocity().length(),
				dt);
		handler.setThrust(Math.max(0, cruiseForce + feedback));
		
		float landingLength = 49.359f * (float) handler.getProperties().getCruiseheight() / 5f + 3.4363f;
		System.out.println("L: " + landingLength);
		float startZ = whereToLand.z + landingLength;
		System.out.println(handler.getProperties().getHeading());
		if (handler.getProperties().getHeading() <= 0.0001 && handler.getProperties().getHeading() >= -0.0001) { 
			if (handler.getProperties().getPosition().z <= startZ) {
				handler.nextAlgorithm();
			}
		} else {
			if (handler.getProperties().getPosition().z >= startZ) {
				handler.nextAlgorithm();
			}
		}
	}

	private PID horStabPID = new PID(4, 1, 1, 2);
	private PID thrustPID = new PID(1000, 400, 50, 2000);
	private PID upwardsForcePID = new PID(0.1f, 1f, 0.0f, 2);
	private PID rollPID = new PID(0.7f, 0.5f, 0.16f, 2);
	private PID vertStabPID = new PID(0, 0, 0.05f, 2);

	@Override
	public String getName() {
		return "VliegRechtdoor";
	}

	private float limitFeedback(float feedback, float min, float max) {
		if (0.75f * max < feedback)
			return 0.75f * max;
		else if (feedback < 0.75f * min)
			return 0.75f * min;
		else
			return feedback;
	}
}
