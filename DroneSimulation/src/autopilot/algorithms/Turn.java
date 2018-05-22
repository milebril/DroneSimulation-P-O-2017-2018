package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;
import autopilot.algorithmHandler.Properties;
import prevAutopilot.DroneProperties;
import prevAutopilot.PIDController;

/**
 * An algorithm aligning the drone with the given heading orientation.
 */
public class Turn implements Algorithm {

	public Turn(float height) {
		// this.headingDest = headingDest;
		this.pidRoll = new PIDController(1.0f, 0.0f, 2.0f, (float) Math.toRadians(1), (float) Math.toRadians(-20));
		this.pidHorStab = new PIDController(0, 0, 0.5f, (float) (Math.PI / 360), 0);
		this.height = 20;
	}

	// private final float headingDest;
	private int height = 20;
	private PIDController pidRoll;
	private final PIDController pidHorStab;
	private PID heightPID = new PID(0.5f, 0.5f, 0.1f, 0.2f);

	@Override
	public void cycle(AlgorithmHandler handler) {
		// BLIJF OP JUISTE HOOGTE
		handler.setHorStabInclination(handler.getHorStabInclination() + pidHorStab.calculateChange(
				handler.getProperties().getPitch() + getVerAngle(handler), handler.getProperties().getDeltaTime()));
		if (handler.getHorStabInclination() > Math.PI / 6) {
			handler.setHorStabInclination((float) (Math.PI / 6));
		} else if (handler.getHorStabInclination() < -Math.PI / 6) {
			handler.setHorStabInclination((float) -(Math.PI / 6));
		}
		// properties.setHorStabInclination((float)Math.toRadians(-4));
		handler.setVerStabInclination((float) Math.toRadians(0));

		// ROLL AT 15 DEGREES
		float changeWingRoll = this.pidRoll.calculateChange(handler.getProperties().getRoll(),
				handler.getProperties().getDeltaTime());

		handler.setLeftWingInclination(handler.getLeftWingInclination() + changeWingRoll);
		handler.setRightWingInclination(handler.getRightWingInclination() - changeWingRoll);

		// STIJGEN/DALEN ~ ZIJVLEUGELS
		float heightError = this.height - handler.getProperties().getY();
		float feedback = heightPID.getFeedback(heightError, handler.getProperties().getDeltaTime());

		handler.setLeftWingInclination(handler.getLeftWingInclination() + feedback);
		handler.setRightWingInclination(handler.getRightWingInclination() + feedback);
		
//		System.out.println("--------------------");
//		System.out.println("heading error: " + headingError + " rad");
//		System.out.println("rollPID: " + rollFeedback);
//		System.out.println("roll: " + roll + " -> error: " + rollError);
//		System.out.println("wingPID: " + wingFeedback);
//		System.out.println("HEightERROR " + heightError);
		
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
		if(handler.getProperties().getZ() < - 450) {
			handler.nextAlgorithm();
			//this.pidRoll = new PIDController(1.0f, 0.0f, 2.0f, (float) Math.toRadians(1), (float) Math.toRadians(0));
		}
//		System.out.println("Left Wing INC: " + handler.getLeftWingInclination());
//		System.out.println("Right Wing INC: " + handler.getRightWingInclination());
//		if(handler.getProperties().getZ() < - 450) {
//			this.pidRoll = new PIDController(5.0f, 0.0f, 3.0f, (float) Math.toRadians(1), (float) Math.toRadians(20));
//		}
	}

	private float getVerAngle(AlgorithmHandler handler) {
		float overstaande = this.height - handler.getProperties().getPosition().getY();
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
		return "Turn";
	}

}