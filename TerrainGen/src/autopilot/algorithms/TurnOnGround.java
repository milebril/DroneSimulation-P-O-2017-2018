package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import prevAutopilot.PIDController;

public class TurnOnGround implements Algorithm {
	public float angleDif;

	/**
	 * gebruik voor angle -Math.pi/2 om een hoek van 90 graden te maken naar rechts.
	 * Gebruik -3.14159264 om 180 graden te draaien langs rechts. (langs links
	 * draaien lukt niet (problemen me de pid)
	 * 
	 * @param angle
	 */
	public TurnOnGround(float angle) {
		System.out.println(angle);
		this.angle = angle;
	}

	public float angle;
	private PIDController pidBrake = new PIDController(2f, 0f, 5f, (float) (1 * 2486), 0);
	private AlgorithmHandler handler;

	private float getAngle() {
		return this.angle;
	}

	@Override
	public void cycle(AlgorithmHandler handler) {
		if (this.handler == null) {
			this.handler = handler;
		}
		float dt = handler.getProperties().getDeltaTime();
		angleDif = Math.abs(handler.getProperties().getHeading() - getAngle());

		if (Math.abs(angleDif) > 0.00005) {
			handler.setLeftBrakeForce((handler.getLeftBrakeForce() - pidBrake.calculateChange(angleDif, dt)));
			if (handler.getLeftBrakeForce() >= handler.getProperties().getRMax()) {
				handler.setLeftBrakeForce(handler.getProperties().getRMax());
			} else if (handler.getLeftBrakeForce() <= 0) {
				handler.setLeftBrakeForce(0);
			}

			handler.setRightBrakeForce(handler.getRightBrakeForce() + pidBrake.calculateChange(angleDif, dt));
			if (handler.getRightBrakeForce() >= handler.getProperties().getRMax()) {
				handler.setRightBrakeForce(handler.getProperties().getRMax());
			} else if (handler.getRightBrakeForce() <= 0) {
				handler.setRightBrakeForce(0);
			}

			handler.setThrust(100);
			handler.setFrontBrakeForce(0);

			// System.out.println("left:" + handler.getLeftBrakeForce());
			// System.out.println("right:" + handler.getRightBrakeForce());
			// System.out.println(pidBrake.calculateChange(angleDif, dt));
			// System.out.println("angleDif:" + angleDif);
		} else {
			handler.setRightBrakeForce(0);
			handler.setLeftBrakeForce(0);
			handler.setFrontBrakeForce(0);
			handler.nextAlgorithm();

		}
	}

	// }

	@Override
	public String getName() {

		return "TurnOnGround";
	}

	public void fullBrakeNext() {
		handler.setLeftBrakeForce(handler.getProperties().getRMax());
		handler.setRightBrakeForce(handler.getProperties().getRMax());
		handler.setFrontBrakeForce(handler.getProperties().getRMax());
		handler.nextAlgorithm();
	}

}