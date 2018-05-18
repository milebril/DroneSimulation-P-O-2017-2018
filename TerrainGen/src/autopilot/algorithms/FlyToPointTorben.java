package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import prevAutopilot.PIDController;

public class FlyToPointTorben implements Algorithm {

		private PID heightPID = new PID(0.5f, 0.5f, 0.1f, 0.2f);
		private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);

		private final PIDController pidRoll;
		private PIDController pidGetRoll = new PIDController(1000.0f,0,0,(float) Math.toRadians(1),0);

		public FlyToPointTorben(Algorithm nextAlgorithm, Vector3f point) {
			this.point = point;
			this.nextAlgorithm = nextAlgorithm;
			this.pidRoll = new PIDController(5.0f, 0.0f, 3.0f, (float) Math.toRadians(1), (float) Math.toRadians(0));
		}

		private final Vector3f point;

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

			// properties.setHorStabInclination((float)Math.toRadians(-4));
			handler.setVerStabInclination((float) Math.toRadians(0));
			
			float headingError = (float) Math.tan((point.x - handler.getProperties().getPosition().x) / (point.z - handler.getProperties().getPosition().z));
			float roll = - pidGetRoll.calculateChange(headingError, dt);
			System.out.println("Wanted roll: " + Math.toDegrees(roll));
			roll = (float) Math.toRadians( - 5 );
			//float feedback = rollPID.getFeedback(headingError, dt);
			
			// ROLL AT 15 DEGREES
			//TODO: check +- changeWingRoll
			float changeWingRoll = this.pidRoll.calculateChange(roll - handler.getProperties().getRoll(),dt);
			handler.setLeftWingInclination(handler.getLeftWingInclination() + changeWingRoll);
			if (handler.getLeftWingInclination() > Math.toRadians(15))
				handler.setLeftWingInclination((float) Math.toRadians(15));
			if (handler.getLeftWingInclination() < Math.toRadians(-15))
				handler.setLeftWingInclination((float) Math.toRadians(-15));
			handler.setRightWingInclination(handler.getLeftWingInclination() - changeWingRoll);
			if (handler.getRightWingInclination() > Math.toRadians(15))
				handler.setRightWingInclination((float) Math.toRadians(15));
			if (handler.getRightWingInclination() < Math.toRadians(-15))
				handler.setRightWingInclination((float) Math.toRadians(-15));

			
			// STIJGEN/DALEN ~ ZIJVLEUGELS
			float heightError = point.y - handler.getProperties().getY();
			float feedback = heightPID.getFeedback(heightError, dt);

			handler.setLeftWingInclination(handler.getLeftWingInclination() + feedback);
			handler.setRightWingInclination(handler.getRightWingInclination() + feedback);

			// PITCH ~ HOR STAB
			feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), dt);
			handler.setHorStabInclination(feedback);

			boolean reached = false;
			if (heightError < 0.2 && handler.getProperties().getZ() < point.z) { // TODO
				reached = true;
			}

			// if the point is reached activate next algorithm
			if (reached) {
				handler.setAlgorithm(getNextAlgorithm());
			}
		}


		@Override
		public String getName() {
			return "FlyToPoint";
		}
}
