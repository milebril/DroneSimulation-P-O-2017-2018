package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.interfaces.Path;
import autopilot.interfaces.path.VectorPath;

public class PathFinder implements Algorithm {
	
	public PathFinder(Path path) {
		this.path = new VectorPath(path);
	}
	
	private VectorPath path;
	private VectorPath getPath() {
		return this.path;
	}
	
	private float height = -1;
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		
		Vector3f nextPos = getPath().poll();
		
		
		
		if (nextPos != null) // if there is a position to reach
		{
			// fly to the next position
			System.out.println("[PathFinder] next position: (" + nextPos.x + ", " + nextPos.y + ", " + nextPos.z + ")");
			Algorithm flyToPoint = new FlyToPoint(this, nextPos);
			handler.setAlgorithm(flyToPoint);
			flyToPoint.cycle(handler);
		}
		else // if all positions have been reached
		{
			if (this.height == -1) 
				this.height = handler.getProperties().getY();
			
			handler.setHorStabInclination(0);
			handler.setVerStabInclination(0);
			
			// fly in a straight line at 40m/s, maintaining altitude
			float feedback;
			float dt = handler.getProperties().getDeltaTime();
			float maxAoA = (float) Math.toRadians(handler.getProperties().getMaxAOA());
			float maxIncl[];

			// pitch op 0 deg houden
			float pitch = 0.0f;
			maxIncl = handler.getProperties().getMaxInclinationHorStab();

			System.out.println("min: " + maxIncl[0] + " max: " + maxIncl[1]);
			feedback = limitFeedback(horStabPID.getFeedback(handler.getProperties().getPitch() - pitch, dt), maxIncl[0], maxIncl[1]);
			handler.setHorStabInclination(feedback);
			System.out.println("pitch: " + handler.getProperties().getPitch() + " feedback: " + 180 / Math.PI * feedback);
			
			/*
			// hoogte behouden en roll op 0 deg houden
			float roll = 0.0f;
			maxIncl = handler.getProperties().getMaxInclinationHorStab();
			System.out.println("min: " + maxIncl[0] + " max: " + maxIncl[1]);
			feedback = upwardsForcePID.getFeedback(this.height - handler.getProperties().getY(), dt);
			float rollFeedback = rollPID.getFeedback(roll - handler.getProperties().getRoll(), dt);

			System.out.println("roll: " + handler.getProperties().getRoll() + " feedback: " + rollFeedback);
			feedback = limitFeedback(feedback, 0.9f*maxIncl[0], 0.9f*maxIncl[1]);
			System.out.println("hover feedback; " + feedback);
			rollFeedback = limitFeedback(rollFeedback, maxIncl[0]-Math.abs(feedback), maxIncl[1]+Math.abs(feedback));
			
			handler.setLeftWingInclination(feedback - rollFeedback);
			handler.setRightWingInclination(feedback + rollFeedback);
			System.out.println("roll: " + handler.getProperties().getRoll() + " feedback: " + rollFeedback);
			
			feedback = maxAoA*0.75f*vertStabPID.getFeedback(roll - handler.getProperties().getRoll(), dt);
			handler.setVerStabInclination(feedback);
			
			*/
			
			// velocity op 50 m/s houden
			float cruiseForce = 10f; // TODO
			feedback = thrustPID.getFeedback(50 - handler.getProperties().getVelocity().length(), dt);
			handler.setThrust(Math.max(0, cruiseForce + feedback));
			System.out.println("thrust: " + handler.getThrust());
		}
	}
	
	private float limitFeedback(float feedback, float min, float max) {
		if (0.95f*max < feedback) return 0.95f*max;
		else if (feedback < 0.95f*min) return 0.95f*min;
		else return feedback;
	}
	
	
	private PID horStabPID = new PID(1, 1, 1, 2);
	private PID thrustPID = new PID(1000, 200, 50, 2000);
	private PID upwardsForcePID = new PID(0.7f, 0.5f, 0.16f, 2);
	private PID rollPID = new PID(0.7f, 0.5f, 0.16f, 2);
	private PID vertStabPID = new PID(0, 0, 0.05f, 2);

	@Override
	public String getName() {
		return "PathFinder";
	}

}
