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
		handler.getProperties().getImage();

		//HEY URBAN TODO FIX
		
		
		
		handler.setVerStabInclination(0);
		
		// PITCH OP 0
		float feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), dt);
		handler.setHorStabInclination(feedback);
		
		float changeWingRoll;
		
		//ROLL LINKS
		if(- handler.getProperties().getHeading() > getHorAngle(handler) + 0.05 ) {
			changeWingRoll = this.leftRoll.calculateChange(handler.getProperties().getRoll(),
					handler.getProperties().getDeltaTime());
			handler.setLeftWingInclination(0.15f + changeWingRoll);
			handler.setRightWingInclination(0.15f - changeWingRoll);
			
		//ROLL RECHTS
		} else if(- handler.getProperties().getHeading() < getHorAngle(handler) - 0.05 ) {
			changeWingRoll = this.rightRoll.calculateChange(handler.getProperties().getRoll(),
					handler.getProperties().getDeltaTime());

			handler.setLeftWingInclination(0.15f + changeWingRoll);
			handler.setRightWingInclination(0.15f - changeWingRoll);
			
		} else {
		
		// ROLL op 0
		feedback = rollPID.getFeedback(-handler.getProperties().getRoll(), dt);
		handler.setLeftWingInclination(-feedback+0.15f);
		handler.setRightWingInclination(feedback+0.15f);
		}
		
		// STIJGEN/DALEN ~ ZIJVLEUGELS
		float heightError = point.getY() - handler.getProperties().getY();
		feedback = heightPID.getFeedback(heightError, dt);
		
		handler.setLeftWingInclination(handler.getLeftWingInclination()+feedback);
		handler.setRightWingInclination(handler.getRightWingInclination()+feedback);
		
		// cruisesnelheid houden
		if(handler.getProperties().getVelocity().length() > 40) handler.setThrust(0);
		else  handler.setThrust(handler.getProperties().getMaxThrust());
		
		if(getEuclidDist(handler.getProperties().getPosition(),point) < 5 ) {
			handler.nextAlgorithm();
		}
			
	}
	
	private float getEuclidDist(Vector3f vec1, Vector3f vec2) {
		Vector3f temp = new Vector3f(0, 0, 0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
	}

	//P = 0.5, D = 0.75 -> lichte oscillatie, haalt 2de kubus niet
	//P = 0.75, D = 0.75 -> sterkere oscillatie, haalt 2de kubus niet
	//P = 0.5, D = 1.0 -> lichte oscillatie, haalt 2de kubus niet
	//P = 0.5, I = 0.1, D = 0.75 -> lichte oscillatie, haalt 2de kubus niet
	//P = 0.5, I = 0.3, D = 0.75 -> lichte oscillatie, haalt 2de kubus niet
	private PIDController verStab = new PIDController(0.1f,0.0f,0.2f, (float) Math.toRadians(1),0);
	private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);
	private PID rollPID = new PID(1f, 0f, 0f, 0.3f);
	private PID thrustPID = new PID(1000, 400, 50, 2000);
	private final PIDController leftRoll = new PIDController(5.0f, 0.0f, 3.0f, (float) Math.toRadians(2), (float) Math.toRadians(10));
	private final PIDController rightRoll = new PIDController(5.0f, 0.0f, 3.0f, (float) Math.toRadians(2), (float) Math.toRadians(-10));
	
	
	private Vector3f point;
	private Vector3f editedPoint;
	
	@Override
	public String getName() {
		return "Stabilize";
	}
	
	private float getHorAngle(AlgorithmHandler handler) {
		float overstaande;
		if(point.getX() > handler.getProperties().getPosition().getX())
			overstaande = Math.abs(point.getX() - handler.getProperties().getPosition().getX());
		else
			overstaande = - Math.abs(handler.getProperties().getPosition().getX() - point.getX());

		float aanliggende = Math.abs(point.getZ() - handler.getProperties().getPosition().getZ());
		return (float) Math.atan(overstaande / aanliggende);
	}
	
	public void setPoint(Vector3f v){
		this.point = v;
	}
	
	private PID heightPID = new PID(0.5f, 0.5f, 0.1f, 0.2f);

}
