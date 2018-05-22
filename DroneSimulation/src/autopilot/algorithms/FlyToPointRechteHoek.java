package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;
import prevAutopilot.PIDController;

public class FlyToPointRechteHoek implements Algorithm {
	
	public FlyToPointRechteHoek(Vector3f target) {
		this.target = target;
	}
	
	private Vector3f target;
	
	private enum Orientatie {NOORD, OOST, ZUID, WEST};
	
	private enum Doel {LINKS, RECHTS, RECHTDOOR};
	
	private enum State {UNO, DOS, TRES};
	
	private Orientatie vliegrichting = null;
	private Doel doelrichting = null;
	private float error = 0.1f;
	
	private State yayo = State.UNO;
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		float dt = handler.getProperties().getDeltaTime();
		
		// relatieve target
		Vector3f relativeTarget = new Vector3f();
		relativeTarget.x = target.x - handler.getProperties().getX();
		relativeTarget.y = target.y - handler.getProperties().getY();
		relativeTarget.z = target.z - handler.getProperties().getZ();
		relativeTarget = handler.getProperties().transformToDroneFrame(relativeTarget);
		Doel doelrichting = null;
		if (Math.abs(relativeTarget.x + 750) < 20) doelrichting = Doel.LINKS;
		else if (Math.abs(relativeTarget.x - 750) < 20) doelrichting = Doel.RECHTS;
		else doelrichting = Doel.RECHTDOOR;
		
		
		
		// rechtdoor vliegen tot -260 > relativeTarget.z
		if (yayo == State.UNO && -260 > relativeTarget.z) {yayo = State.DOS;
		
		
		
		
		if (yayo == State.UNO) {
			
		}
		
			System.out.println("yayo uno");
			// PITCH OP 0
			float feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), dt);
			handler.setHorStabInclination(feedback);
			
			// ROLL op 0
			feedback = rollPID.getFeedback(-handler.getProperties().getRoll(), dt);
			handler.setLeftWingInclination(-feedback+0.15f);
			handler.setRightWingInclination(feedback+0.15f);
			
			// STIJGEN/DALEN ~ ZIJVLEUGELS
			float heightError = handler.getProperties().getY() - handler.getProperties().getCruiseheight();
			feedback = heightPID.getFeedback(-heightError, dt);
			
			handler.setLeftWingInclination(handler.getLeftWingInclination()+feedback);
			handler.setRightWingInclination(handler.getRightWingInclination()+feedback);
			
			// cruisesnelheid houden
			float cruiseForce = handler.getProperties().getGravity();
			feedback = thrustPID.getFeedback(AutopilotAlain.CRUISESPEED - handler.getProperties().getVelocity().length(), dt);
			handler.setThrust(Math.max(0, cruiseForce + feedback));
			
			
			
		} else if (yayo == State.DOS) {

			System.out.println("yayo dos");
			switch (doelrichting) {
			case LINKS:
				// vlieg rechtdoor op cruisehoogte tot de drone een bocht moet beginnen nemen
				if (-260 > relativeTarget.z) {
					
				} else {
				// anders draaien
				}
				break;
			case RECHTS:
				break;
			case RECHTDOOR:
				break;
			}
		}
		System.out.println("doelrichting: " + doelrichting);
		
	}
	
	

	private PIDController verStab = new PIDController(1.0f,0,1.0f, (float) Math.toRadians(1),0);
	private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);
	private PID rollPID = new PID(1f, 0f, 0f, 0.3f);
	private PID heightPID = new PID(0.5f, 0.5f, 0.1f, 0.2f);
	private PID thrustPID = new PID(1000, 400, 50, 2000);

	@Override
	public String getName() {
		return "FlyToPointv2";
	}

}
