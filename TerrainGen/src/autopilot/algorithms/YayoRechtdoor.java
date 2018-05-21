package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;

public class YayoRechtdoor implements Algorithm {
	
	public YayoRechtdoor(float z, Vector3f targetW) {
		this.Z = z;
		this.targetW = targetW;
	}
	
	private float Z;
	private Vector3f targetW;
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		
		// RELATIEVE POS vh TARGET
		Vector3f relTarget = new Vector3f();
		relTarget.x = targetW.x - handler.getProperties().getX();
		relTarget.y = targetW.y - handler.getProperties().getY();
		relTarget.z = targetW.z - handler.getProperties().getZ();
		relTarget = handler.getProperties().transformToDroneFrame(relTarget);
		
		
		// RECHTDOOR VLIEGEN
		handler.setHorStabInclination(0);
		handler.setVerStabInclination(0);
		
		// fly in a straight line at 40m/s, maintaining altitude
		float feedback;
		float dt = handler.getProperties().getDeltaTime();

		// HORIZONTAL STABILIZER -> pitch op 0 deg houden
		float pitch = 0.0f;
		
		feedback = horStabPID.getFeedback(handler.getProperties().getPitch() - pitch, dt);
		handler.setHorStabInclination(feedback);
		
		
		
		// LEFT AND RIGHT WING -> ensure the plane is moving as its forward vector
		float roll = 0.0f;
		
		Vector3f velocityD = handler.getProperties().transformToDroneFrame(handler.getProperties().getVelocity());
		
		
		feedback = upwardsForcePID.getFeedback(-velocityD.y, dt);
		
		handler.setLeftWingInclination(feedback+0.15f);
		handler.setRightWingInclination(feedback+0.15f);
		
		// velocity op 50 m/s houden
		float cruiseForce = handler.getProperties().getGravity();
		feedback = thrustPID.getFeedback(AutopilotAlain.CRUISESPEED - handler.getProperties().getVelocity().length(), dt);
		handler.setThrust(Math.max(0, cruiseForce + feedback));
		
		System.out.println("rel target: " + relTarget);
		if (relTarget.z > this.Z) {
			System.out.println("drone Z");
			handler.nextAlgorithm();
		}
	}
	

	private PID thrustPID = new PID(1000, 400, 50, 2000);
	private PID upwardsForcePID = new PID(0.1f, 1f, 0.0f, 2);
	private PID horStabPID = new PID(4, 1, 1, 2);

	@Override
	public String getName() {
		return "yayo";
	}

}
