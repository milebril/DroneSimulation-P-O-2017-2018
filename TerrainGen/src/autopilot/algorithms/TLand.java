package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import prevAutopilot.PIDController;

public class TLand implements Algorithm {

	@Override
	public void cycle(AlgorithmHandler handler) {
		float dt = handler.getProperties().getDeltaTime();

		if (handler.getProperties().getPosition().getZ() > -300) {
			handler.setThrust(0);

			// pitch op 0 houden
			float feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), dt);
			handler.setHorStabInclination(feedback);

			// roll op 0
			feedback = rollPID.getFeedback(-handler.getProperties().getRoll(), dt);
			handler.setLeftWingInclination(-feedback + 0.15f);
			handler.setRightWingInclination(feedback + 0.15f);

			// remmen
			handler.setFrontBrakeForce(handler.getProperties().getRMax());
			handler.setLeftBrakeForce(handler.getProperties().getRMax());
			handler.setRightBrakeForce(handler.getProperties().getRMax());
		} else {
			handler.setVerStabInclination(0);

			// PITCH OP 0
			float feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), dt);
			handler.setHorStabInclination(feedback);

			float changeWingRoll;
			double heading;
			System.out.println("Real Heading: " + handler.getProperties().getHeading());

			if (handler.getProperties().getHeading() < 0) {
				heading = handler.getProperties().getHeading() + Math.PI;
			} else {
				heading = handler.getProperties().getHeading() - Math.PI;
			}

			System.out.println("Heading " + heading);
			System.out.println("Horangle " + getHorAngle(handler));
			// ROLL LINKS
			if (-heading > getHorAngle(handler) + 0.05) {
				changeWingRoll = this.leftRoll.calculateChange(handler.getProperties().getRoll(),
						handler.getProperties().getDeltaTime());
				handler.setLeftWingInclination(0.15f + changeWingRoll);
				handler.setRightWingInclination(0.15f - changeWingRoll);

				System.out.println("ROLL LINKS");
				// ROLL RECHTS
			} else if (-heading < getHorAngle(handler) - 0.05) {
				changeWingRoll = this.rightRoll.calculateChange(handler.getProperties().getRoll(),
						handler.getProperties().getDeltaTime());

				handler.setLeftWingInclination(0.05f + changeWingRoll);
				handler.setRightWingInclination(0.05f - changeWingRoll);

				System.out.println("ROLL RECHTS");
			} else {

				// ROLL op 0
				feedback = rollPID.getFeedback(-handler.getProperties().getRoll(), dt);
				handler.setLeftWingInclination(-feedback + 0.15f);
				handler.setRightWingInclination(feedback + 0.15f);

				System.out.println("NO ROLL");
			}

			// STIJGEN/DALEN ~ ZIJVLEUGELS
			float heightError = point.getY() - handler.getProperties().getY();
			feedback = heightPID.getFeedback(heightError, dt);

			handler.setLeftWingInclination(handler.getLeftWingInclination() + feedback);
			handler.setRightWingInclination(handler.getRightWingInclination() + feedback);

			// cruisesnelheid houden
			// float cruiseForce = handler.getProperties().getGravity();
			// feedback = thrustPID.getFeedback(AutopilotAlain.CRUISESPEED -
			// handler.getProperties().getVelocity().length(), dt);
			// handler.setThrust(Math.max(0, cruiseForce + feedback));
			if (handler.getProperties().getVelocity().length() > 40)
				handler.setThrust(0);
			else
				handler.setThrust(handler.getProperties().getMaxThrust());
		}

		// wanneer het vliegtuig stilstaat is het geland
		if (handler.getProperties().getVelocity().length() <= 0.1) {
			handler.nextAlgorithm();
		}
	}

	private float getHorAngle(AlgorithmHandler handler) {
		float overstaande;
		if (point.getX() > handler.getProperties().getPosition().getX())
			overstaande = -Math.abs(point.getX() - handler.getProperties().getPosition().getX());
		else
			overstaande = Math.abs(handler.getProperties().getPosition().getX() - point.getX());
		System.out.println("overstaande " + overstaande);
		float aanliggende = Math.abs(point.getZ() - handler.getProperties().getPosition().getZ());
		System.out.println("aanliggend " + aanliggende);
		return (float) Math.atan(overstaande / aanliggende);
	}

	private PID rollPID = new PID(0.7f, 0.01f, 0.1f, 0.8f);
	private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);
	private Vector3f point = new Vector3f(0.0f, 20.0f, 0.0f);
	private PID heightPID = new PID(0.5f, 0.5f, 0.1f, 0.2f);
	private final PIDController leftRoll = new PIDController(5.0f, 0.0f, 3.0f, (float) Math.toRadians(2),
			(float) Math.toRadians(15));
	private final PIDController rightRoll = new PIDController(5.0f, 0.0f, 3.0f, (float) Math.toRadians(2),
			(float) Math.toRadians(-15));

	@Override
	public String getName() {
		return "TLanden";
	}

}
