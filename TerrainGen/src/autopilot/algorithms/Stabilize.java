package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;
import prevAutopilot.PIDController;

public class Stabilize implements Algorithm {
	
	public Stabilize(Vector3f p) {
		setPoint(p);
	}

	@Override
	public void cycle(AlgorithmHandler handler) {
		
		float dt = handler.getProperties().getDeltaTime();
		// VLIEG OP VERT STABILISER
		System.out.println("handler.getProperties().getHeading() - getHorAngle(handler)" + (handler.getProperties().getHeading() - getHorAngle(handler)));
		float verStabChange = verStab.calculateChange(handler.getProperties().getHeading() - getHorAngle(handler), dt);
		handler.setVerStabInclination(handler.getVerStabInclination() + verStabChange);
		if (handler.getVerStabInclination() > Math.toRadians(8))
			handler.setVerStabInclination((float) Math.toRadians(8));
		if (handler.getVerStabInclination() < Math.toRadians(-8))
			handler.setVerStabInclination((float) Math.toRadians(-8));
		
		// PITCH OP 0
		float feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), dt);
		handler.setHorStabInclination(feedback);
		
		// ROLL op 0
		feedback = rollPID.getFeedback(-handler.getProperties().getRoll(), dt);
		handler.setLeftWingInclination(-feedback+0.15f);
		handler.setRightWingInclination(feedback+0.15f);
		
		// STIJGEN/DALEN ~ ZIJVLEUGELS
		float heightError = point.getY() - handler.getProperties().getY();
		feedback = heightPID.getFeedback(heightError, dt);
		
		handler.setLeftWingInclination(handler.getLeftWingInclination()+feedback);
		handler.setRightWingInclination(handler.getRightWingInclination()+feedback);
		
		// cruisesnelheid houden
//		float cruiseForce = handler.getProperties().getGravity();
//		feedback = thrustPID.getFeedback(AutopilotAlain.CRUISESPEED - handler.getProperties().getVelocity().length(), dt);
//		handler.setThrust(Math.max(0, cruiseForce + feedback));
		if(handler.getProperties().getVelocity().length() > 40) handler.setThrust(0);
		else  handler.setThrust(handler.getProperties().getMaxThrust());
		
		System.out.println(getEuclidDist(handler.getProperties().getPosition(),point));
		if(getEuclidDist(handler.getProperties().getPosition(),point) < 5 ) {
			handler.nextAlgorithm();
		}
			
	}
	
	private float getEuclidDist(Vector3f vec1, Vector3f vec2) {
		Vector3f temp = new Vector3f(0, 0, 0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
	}

	private PIDController verStab = new PIDController(1.0f,0,0.5f, (float) Math.toRadians(1),0);
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
