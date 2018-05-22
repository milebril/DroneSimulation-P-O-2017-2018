package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import prevAutopilot.DroneProperties;
import prevAutopilot.PIDController;

public class FlyToPointRight implements Algorithm {

		private PID heightPID = new PID(0.5f, 0.5f, 0.1f, 0.2f);
		private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);
		private PIDController pidHorStab = new PIDController(0.0f, 0, 0.5f, (float) (Math.PI / 360), 0);

		private final PIDController pidRoll = new PIDController(5.0f, 0.0f, 3.0f, (float) Math.toRadians(1), (float) Math.toRadians(-15));
		private PIDController pidGetRoll = new PIDController(1000.0f,0,0,(float) Math.toRadians(1),0);

		public FlyToPointRight(Algorithm nextAlgorithm, Vector3f point) {
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

		@Override
		public void cycle(AlgorithmHandler handler) {
			
			// PITCH OP 0
			float feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), handler.getProperties().getDeltaTime());
			handler.setHorStabInclination(feedback);
			
			handler.setVerStabInclination((float) Math.toRadians(0));

			// ROLL AT 15 DEGREES
			float changeWingRoll = this.pidRoll.calculateChange(handler.getProperties().getRoll(),
					handler.getProperties().getDeltaTime());

			handler.setLeftWingInclination(handler.getLeftWingInclination() + changeWingRoll);
			handler.setRightWingInclination(handler.getRightWingInclination() - changeWingRoll);

			// STIJGEN/DALEN ~ ZIJVLEUGELS
			float heightError = point.getY() - handler.getProperties().getY();
			feedback = heightPID.getFeedback(heightError, handler.getProperties().getDeltaTime());

			handler.setLeftWingInclination(handler.getLeftWingInclination() + feedback);
			handler.setRightWingInclination(handler.getRightWingInclination() + feedback);
			
			// MAX ANGLE OF 15
			if (handler.getLeftWingInclination() > Math.toRadians(15))
				handler.setLeftWingInclination((float) Math.toRadians(15));
			if (handler.getLeftWingInclination() < Math.toRadians(-15))
				handler.setLeftWingInclination((float) Math.toRadians(-15));
			
			if (handler.getRightWingInclination() > Math.toRadians(15))
				handler.setRightWingInclination((float) Math.toRadians(15));
			if (handler.getRightWingInclination() < Math.toRadians(-15))
				handler.setRightWingInclination((float) Math.toRadians(-15));
			
			//THRUST
			if (handler.getProperties().getVelocity().length() > 40) // als de drone sneller vliegt dan 40m/s zet de thrust dan uit
				handler.setThrust(0);
			else
				handler.setThrust(handler.getProperties().getMaxThrust());
			
			//ROLL naar de andere kant vanaf ??? meter in Z
			if((handler.getProperties().getHeading() - getHorAngle(handler)) < 0.01) {
				handler.nextAlgorithm();
			}
		}
		
		private float getHorAngle(AlgorithmHandler handler) {
			float overstaande = point.getX() - handler.getProperties().getPosition().getX();
			float aanliggende = point.getZ() - handler.getProperties().getPosition().getZ();
			return (float) Math.atan(overstaande / aanliggende);
		}

		@Override
		public String getName() {
			return "FlyToPointRight";
		}
}
