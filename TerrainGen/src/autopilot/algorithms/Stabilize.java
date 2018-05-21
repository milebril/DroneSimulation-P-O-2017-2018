package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;
import prevAutopilot.PIDController;

public class Stabilize implements Algorithm {
	
	private float heightTarget = -100;
	private float headingTarget = -100;
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		if (heightTarget == -100) heightTarget = handler.getProperties().getY();
		if (headingTarget == -100) headingTarget = handler.getProperties().getHeading();
		
		float dt = handler.getProperties().getDeltaTime();
		
		// HEADING advh VERT STAB
		float headingError = handler.getProperties().getHeading() - headingTarget;
		handler.setVerStabInclination(headingError);
		
		// PITCH OP 0
		float feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), dt);
		handler.setHorStabInclination(feedback);
		
		// ROLL op 0
		feedback = rollPID.getFeedback(-handler.getProperties().getRoll(), dt);
		handler.setLeftWingInclination(-feedback+0.15f);
		handler.setRightWingInclination(feedback+0.15f);
		
		// STIJGEN/DALEN ~ ZIJVLEUGELS
		float heightError = handler.getProperties().getY() - heightTarget;
		feedback = heightPID.getFeedback(-heightError, dt);
		
		handler.setLeftWingInclination(handler.getLeftWingInclination()+feedback);
		handler.setRightWingInclination(handler.getRightWingInclination()+feedback);
		
		// cruisesnelheid houden
		float cruiseForce = handler.getProperties().getGravity();
		feedback = thrustPID.getFeedback(AutopilotAlain.CRUISESPEED - handler.getProperties().getVelocity().length(), dt);
		handler.setThrust(Math.max(0, cruiseForce + feedback));
		
		System.out.println("headingerror: " + headingError);
	}

	private PIDController verStab = new PIDController(1.0f,0,1.0f, (float) Math.toRadians(1),0);
	private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);
	private PID rollPID = new PID(1f, 0f, 0f, 0.3f);
	private PID thrustPID = new PID(1000, 400, 50, 2000);
	
	private Vector3f point;
	
	@Override
	public String getName() {
		return "Stabilize";
	}
	
	private float getHorAngle(AlgorithmHandler handler) {
		float overstaande = point.getX() - handler.getProperties().getPosition().getX();
		float aanliggende = point.getZ() - handler.getProperties().getPosition().getZ();
		return (float) Math.atan(overstaande / aanliggende);
	}
	
	public void setPoint(Vector3f v){
		this.point = v;
	}
	
	private PID heightPID = new PID(0.5f, 0.5f, 0.1f, 0.2f);

}
