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
			float maxIncl[];

			// HORIZONTAL STABILIZER -> pitch op 0 deg houden
			float pitch = 0.0f;
			
			
			//System.out.println("- - Horizontal stabilizer - -");
			
			maxIncl = handler.getProperties().getMaxInclinationHorStab();
			
			feedback = horStabPID.getFeedback(handler.getProperties().getPitch() - pitch, dt);
			feedback = limitFeedback(feedback, maxIncl[0],  maxIncl[1]);
			
			//System.out.println("pitch: " + Math.round((handler.getProperties().getPitch()) * 10.0) / 10.0 + " (error: " +  Math.round((handler.getProperties().getPitch() - pitch) * 10.0) / 10.0 + ")");
			//System.out.println("PID feedback: " + feedback);
			//System.out.println("-> new angle: " + Math.round((180 / Math.PI * feedback) * 10.0) / 10.0 + "�");
			//System.out.println();
			handler.setHorStabInclination(feedback);
			
			
			
			// LEFT AND RIGHT WING -> ensure the plane is moving as its forward vector
			float roll = 0.0f;
			
			Vector3f velocityD = handler.getProperties().transformToDroneFrame(handler.getProperties().getVelocity());
			
			//System.out.println("- - Left and right wing - -");
			//System.out.println("error: " + -velocityD.y);
			
			// left wing
			
			feedback = upwardsForcePID.getFeedback(-velocityD.y, dt);
			
			maxIncl = handler.getProperties().getMaxInclinationLeftWing();
			feedback = limitFeedback(feedback, maxIncl[0],  maxIncl[1]);
			
			maxIncl = handler.getProperties().getMaxInclinationRightWing();
			feedback = limitFeedback(feedback, maxIncl[0],  maxIncl[1]);
			
			handler.setLeftWingInclination(feedback);
			handler.setRightWingInclination(feedback);
			//System.out.println("feedback: " + feedback);
			
			//System.out.println();
			
			
			
			
			// velocity op 50 m/s houden
			float cruiseForce = handler.getProperties().getGravity();
			feedback = thrustPID.getFeedback(50 - handler.getProperties().getVelocity().length(), dt);
			handler.setThrust(Math.max(0, cruiseForce + feedback));
			//System.out.println("- - Engine - -");
			//System.out.println("thrust: " + handler.getThrust());
			//System.out.println();
			//System.out.println();
		}
	}
	
	private float limitFeedback(float feedback, float min, float max) {
		if (0.95f*max < feedback) return 0.95f*max;
		else if (feedback < 0.95f*min) return 0.95f*min;
		else return feedback;
	}
	
	
	private PID horStabPID = new PID(4, 1, 1, 2);
	private PID thrustPID = new PID(1000, 400, 50, 2000);
	private PID upwardsForcePID = new PID(0.1f, 1f, 0.0f, 2);
	private PID rollPID = new PID(0.7f, 0.5f, 0.16f, 2);
	private PID vertStabPID = new PID(0, 0, 0.05f, 2);

	@Override
	public String getName() {
		return "PathFinder";
	}

}
