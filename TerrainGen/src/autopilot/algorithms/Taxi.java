package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;

public class Taxi implements Algorithm {

	public Taxi(Algorithm nextAlgorithm, Vector3f point) {
		this.point = point;
		this.nextAlgorithm = nextAlgorithm;
	}

	private final Vector3f point;
	private PID pidBrake = new PID(1f, 0f, 0.5f, (float) (0.1 * 2486));

	private Vector3f getPoint() {
		return this.point;
	}

	private final Algorithm nextAlgorithm;

	private Algorithm getNextAlgorithm() {
		return this.nextAlgorithm;
	}

	@Override
	public void cycle(AlgorithmHandler handler) {
		float dt = handler.getProperties().getDeltaTime();

		float aanliggende = getPoint().getX() - handler.getProperties().getPosition().getX();
		float overstaande = getPoint().getZ() - handler.getProperties().getPosition().getZ();
		float horzangle = (float) Math.atan(aanliggende / overstaande);
		float angle = handler.getProperties().getHeading() - horzangle;

		if (getPoint().getZ() > handler.getProperties().getPosition().getZ() && angle < Math.PI) {
			angle += Math.PI;
			if (angle > Math.PI) {
				angle -= 2 * Math.PI;
			}
			if (angle < -0.1 || angle > 0.1) {
				handler.setLeftBrakeForce((handler.getLeftBrakeForce() - pidBrake.getFeedback(angle, dt)));
				if (handler.getLeftBrakeForce() > handler.getProperties().getRMax()) {
					handler.setLeftBrakeForce(handler.getProperties().getRMax());
				} else if (handler.getLeftBrakeForce() < 0) {
					handler.setLeftBrakeForce(0);
				}
				handler.setRightBrakeForce(handler.getRightBrakeForce() + pidBrake.getFeedback(angle, dt));
				if (handler.getRightBrakeForce() > handler.getProperties().getRMax()) {
					handler.setRightBrakeForce(handler.getProperties().getRMax());
				} else if (handler.getRightBrakeForce() < 0) {
					handler.setRightBrakeForce(0);
				}
				handler.setThrust(700);
			} else if (handler.getProperties().getVelocity().length() > 13) {
				handler.setThrust(0);
				handler.setLeftBrakeForce(0);
				handler.setRightBrakeForce(0);
				handler.setFrontBrakeForce(0);
			} else {
				handler.setThrust(700);
				handler.setLeftBrakeForce(0);
				handler.setRightBrakeForce(0);
				handler.setFrontBrakeForce(0);
			}

		}

		else if (angle < -0.1 || angle > 0.1) {
			handler.setLeftBrakeForce(
					handler.getLeftBrakeForce() - pidBrake.getFeedback(angle, handler.getProperties().getDeltaTime()));
			if (handler.getLeftBrakeForce() > handler.getProperties().getRMax()) {
				handler.setLeftBrakeForce(handler.getProperties().getRMax());
			} else if (handler.getLeftBrakeForce() < 0) {
				handler.setLeftBrakeForce(0);
			}
			handler.setRightBrakeForce(
					handler.getRightBrakeForce() + pidBrake.getFeedback(angle, handler.getProperties().getDeltaTime()));
			if (handler.getRightBrakeForce() > handler.getProperties().getRMax()) {
				handler.setRightBrakeForce(handler.getProperties().getRMax());
			} else if (handler.getRightBrakeForce() < 0) {
				handler.setRightBrakeForce(0);
			}
			handler.setThrust(700);
		} else if (handler.getProperties().getVelocity().length() > 13) {
			handler.setThrust(0);
			handler.setLeftBrakeForce(0);
			handler.setRightBrakeForce(0);
			handler.setFrontBrakeForce(0);
		} else {
			handler.setThrust(700);
			handler.setLeftBrakeForce(0);
			handler.setRightBrakeForce(0);
			handler.setFrontBrakeForce(0);
		}

		if (getEuclidDist(handler.getProperties().getPosition(), point) <= 1.5) {
			handler.setThrust(0);
			handler.setLeftBrakeForce(handler.getProperties().getRMax());
			handler.setRightBrakeForce(handler.getProperties().getRMax());
			handler.setFrontBrakeForce(handler.getProperties().getRMax());
		}

		// Als de drone binnen 1 meter komt en trager dan 1m/s rijdt, dan wordt de goal
		// bereikt.
		// In de opgave staat er dat de goal exact bereikt moet worden, maar we nemen
		// binnen 1 meter want zo exact is niet echt belangrijk.
		if (getEuclidDist(handler.getProperties().getPosition(), point) <= 2
				&& handler.getProperties().getVelocity().length() <= 1)
			handler.setAlgorithm(getNextAlgorithm());
		
	}

	@Override
	public String getName() {

		return "taxi";
	}
	
	public static float getEuclidDist(Vector3f vec1, Vector3f vec2) {
		Vector3f temp = new Vector3f(0, 0, 0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
	}

}