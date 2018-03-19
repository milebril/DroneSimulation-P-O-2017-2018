package autoPilotJar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import path.MyPath;

public class FlyingAutopilot {

	private SimpleAutopilot parent;

	private float[] pathX = { 0, 0, 0, 0, 0, 0 };
	private float[] pathY = { 20, 20, 20, 20, 20, 20 };
	private float[] pathZ = { -80, -160, -240, -320, -400, -480 };
	// private float[] pathZ = { -480, -560, -640, -720, -800, -880 };

	private MyPath path;
	public float minY = 20;
	public float maxY = 20;

	private PIDController pidHorStab;

	private boolean isFinished = false;
	public boolean failed = false;

	private Vector3f cubePos;
	private float checkpoint = -80;
	public float p, i, d;

	public FlyingAutopilot(SimpleAutopilot parent) {
		this.parent = parent;
		
		// Set the path
		this.path = new MyPath(pathX, pathY, pathZ);
		this.path.setIndex(0);
		this.cubePos = new Vector3f(path.getCurrentX(), path.getCurrentY(), path.getCurrentZ());

		// Randomize the PID values for finetuning
		Random r = new Random();
		p = 1 + r.nextFloat();
		i = Math.abs(r.nextFloat() - 0.5f);
		d = 1 + r.nextFloat();
		// this.pidHorStab = new PIDController(p, i, d, (float) (Math.PI / 180), 0);

		// Best Finetuning until now
		this.pidHorStab = new PIDController(1.1233587f, 0.30645216f, 1.1156111f, (float) (Math.PI / 180), 0);
	}

	public DroneProperties timePassed(DroneProperties properties) {
		// Set the horizontal stabilizer inclination
		properties.setHorStabInclination(properties.getHorStabInclination() + pidHorStab
				.calculateChange(properties.getPitch() + getVerAngle(properties), properties.getDeltaTime()));
		if (properties.getHorStabInclination() > Math.PI / 6) {
			properties.setHorStabInclination((float) (Math.PI / 6));
		} else if (properties.getHorStabInclination() < -Math.PI / 6) {
			properties.setHorStabInclination((float) -(Math.PI / 6));
		}

		// Set the wings to 0 (They slow the plane down)
		properties.setLeftWingInclination(0);
		properties.setRightWingInclination(0);

		// Check whether we reach a cube, or fly by one and fail the path
		if (getEuclidDist(properties.getPosition(), cubePos) <= 3) {
			if (path.getIndex() <= 4) {
				this.path.setIndex(this.path.getIndex() + 1);
				this.cubePos = new Vector3f(path.getCurrentX(), path.getCurrentY(), path.getCurrentZ());
				this.pidHorStab.reset();
				// this.pidVerStab.reset();
				System.out.println("Reached cube at: " + properties.getPosition());
			} else {
				System.out.println("Fininshed");
				isFinished = true;
				System.out.println(maxY + " " + minY);
			}
			checkpoint = -80 * (path.getIndex() + 1);
		}

		if (properties.getPosition().z < (checkpoint)) {
			failed = true;
			System.out.println("Path failed");
		}

		// Set thrustforce to max
		properties.setThrust(properties.getMaxThrust());

		return properties;
	}

	private float getEuclidDist(Vector3f vec1, Vector3f vec2) {
		Vector3f temp = new Vector3f(0, 0, 0);
		Vector3f.sub(vec2, vec1, temp);
		return temp.length();
	}

	private float getVerAngle(DroneProperties properties) {
		float overstaande = cubePos.getY() - properties.getPosition().getY();
		float aanliggende = cubePos.getZ() - properties.getPosition().getZ();
		return (float) Math.atan(overstaande / aanliggende);
	}
}
