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
public class TurnStijn implements Algorithm {
	
	/**
	 * Draaiklasse
	 * @param headingDest: de hoek die de drone moet draaien
	 */
	public TurnStijn(float turn) {
		this.turn = turn;
	}

	private final float turn;
	private float headingDest = -100;

	private float maxRoll = (float) Math.toRadians(35);
	private float wingBaseIncl = (float) Math.toRadians(2);
	private float smoothness = 9.4f; // max inclination change per dt
	private float height = -10;

	@Override
	public void cycle(AlgorithmHandler handler) {
		if (height == -10) height = handler.getProperties().getY();
		if (headingDest == -100) headingDest = (handler.getProperties().getHeading() + turn) % (float) (2*Math.PI);
		
		   
		float dt = handler.getProperties().getDeltaTime();

		float roll = handler.getProperties().getRoll();
	    float heading = handler.getProperties().getHeading();
	    float pitch = handler.getProperties().getPitch();
	    
	    float feedback;
	    
	    // HEADING
	    System.out.println("headingDest: " + headingDest);
	    float headingError = (headingDest - handler.getProperties().getHeading()) % (float)(2*Math.PI);
	    System.out.println(headingError);
	    if (headingError < -Math.PI) headingError += 2*Math.PI;
	    if (Math.PI < headingError) headingError -= 2*Math.PI;
	    
	    // ROLL
	    float rollFeedback = rollPID.getFeedback(headingError, dt);
	    float rollError = (rollFeedback - roll);
	    
		 // REAR WINGS
		float heightError = handler.getProperties().getY() - height;
	    float heightFeedback = heightPID.getFeedback(-heightError, dt);
	    float wingFeedback = rearWingPID.getFeedback(rollError, dt);
	    handler.setLeftWingInclination(-wingFeedback+0.15f+heightFeedback);
	    handler.setRightWingInclination(wingFeedback+0.15f+heightFeedback);
	    handler.setVerStabInclination(-0.7f*wingFeedback);
	    
	    // HOR STAB
	    feedback = horstabPID.getFeedback(handler.getProperties().getPitch(), dt);
	    handler.setHorStabInclination(feedback);
	    
	    
	    System.out.println("--------------------");
	    System.out.println("heading error: " + headingError + " rad");
	    System.out.println("rollPID: " + rollFeedback);
	    System.out.println("roll: " + roll + " -> error: " + rollError);
	    System.out.println("wingPID: " + wingFeedback);
	    System.out.println("HeightERROR " + heightError);
	    System.out.println("height feedback: " + heightFeedback);
	    
	    
	    // THRUST
	    float cruiseForce = handler.getProperties().getGravity();
	    feedback = thrustPID.getFeedback(AutopilotAlain.CRUISESPEED - handler.getProperties().getVelocity().length(), dt);
	    handler.setThrust(Math.max(0, cruiseForce + feedback));
	    
	    
	    if (Math.abs(headingError) < 0.05) {
	    	System.out.println("Math.abs(headingError) < 0.05");
	    	handler.nextAlgorithm();
	    }

	}

	private PID rollPID = new PID(2f, 0.0f, 0.05f, maxRoll);
	private PID rearWingPID = new PID(0.7f, 0.05f, 0f, 0.1f);
	private PID heightPID = new PID(0.5f, 0.05f, 0f, 0.1f);
	  
	private PID thrustPID = new PID(1000, 400, 50, 2000);
		  
	private PID horstabPID = new PID(1f, 0.1f, 0.1f, 1f);


	@Override
	public String getName() {
		return "TurnStijn";
	}

}
