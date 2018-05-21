package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import prevAutopilot.DroneProperties;
import prevAutopilot.PIDController;

public class Turn180 implements Algorithm {

		private PID heightPID = new PID(0.5f, 0.5f, 0.1f, 0.2f);
		private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);

		private final PIDController pidRoll = new PIDController(5.0f, 0.0f, 3.0f, (float) Math.toRadians(1), (float) Math.toRadians(-35));

		public Turn180() {
			
		}

		@Override
		public void cycle(AlgorithmHandler handler) {
			
			// PITCH OP 0
			float feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), handler.getProperties().getDeltaTime());
			handler.setHorStabInclination(feedback);
			
			handler.setVerStabInclination((float) Math.toRadians(0));

			// ROLL AT 30 DEGREES
			float changeWingRoll = this.pidRoll.calculateChange(handler.getProperties().getRoll(),
					handler.getProperties().getDeltaTime());

			handler.setLeftWingInclination(handler.getLeftWingInclination() + changeWingRoll);
			handler.setRightWingInclination(handler.getRightWingInclination() - changeWingRoll);

			// STIJGEN/DALEN ~ ZIJVLEUGELS
			float heightError = 20.0f - handler.getProperties().getY();
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
						
			if(handler.getProperties().getHeading() > Math.toRadians(179)) {
				handler.nextAlgorithm();
			}
		}
		


		private float getVerAngle(AlgorithmHandler handler) {
			float overstaande = 20.0f - handler.getProperties().getPosition().getY();
			float aanliggende;
			if (handler.getProperties().getHeading() < Math.toRadians(90))
				aanliggende = -10;
			else
				aanliggende = 10;

			//System.out.println("getVerAngle()" + Math.atan(overstaande / aanliggende));

			return (float) Math.atan(overstaande / aanliggende);
		}

		@Override
		public String getName() {
			return "FlyToPointRight";
		}
}
