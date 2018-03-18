package autoPilotJar;

import javax.vecmath.Vector3f;

public class LandingAutopilot {

	private SimpleAutopilot parent;

	private Vector3f studCube;
	private PIDController pidHorStab;

	public LandingAutopilot(SimpleAutopilot parent) {
		setParent(parent);

		this.pidHorStab = new PIDController(1.1233587f, 0.30645216f, 1.1156111f, (float) (Math.PI / 180), 0);
	}

	public DroneProperties timePassed(DroneProperties properties) {
		// LET PLANE GO DOWN
		if (properties.getPosition().getY() < 25 && properties.getPosition().getY() > 15
				&& properties.getVelocity().length() >= 10) {
			System.out.println("SLOW DOWN");
			properties.setThrust(200);
			properties.setHorStabInclination(0);
			properties.setVerStabInclination(0);
			properties.setLeftWingInclination((float) Math.toRadians(15));
			properties.setRightWingInclination((float) Math.toRadians(15));
		} else if (properties.getPosition().getY() < 15 && properties.getVelocity().length() >= 10) {
			System.out.println("SLOW DOWN 2");
			studCube = new Vector3f(0, 5, properties.getPosition().z);
			studCube.z -= 20;
			System.out.println(studCube);
			properties.setHorStabInclination(properties.getHorStabInclination() + pidHorStab
					.calculateChange(properties.getPitch() + getVerAngle(properties), properties.getDeltaTime()));
			properties.setThrust(0);
			properties.setLeftWingInclination((float) Math.toRadians(15));
			properties.setRightWingInclination((float) Math.toRadians(15));
			properties.setFrontBrakeForce(getParent().getConfig().getRMax());
			properties.setLeftBrakeForce(getParent().getConfig().getRMax());
			properties.setRightBrakeForce(getParent().getConfig().getRMax());
		} else if (properties.getPosition().getY() > 25 && properties.getVelocity().length() >= 10) {
			System.out.println("REDUCE HEIGHT");
			properties.setThrust(0);
			properties.setHorStabInclination((float) Math.toRadians(5));
			properties.setVerStabInclination(0);
			properties.setLeftWingInclination((float) Math.toRadians(-5));
			properties.setRightWingInclination((float) Math.toRadians(-5));
		} else { // BRAKE
			properties.setThrust(0);
			properties.setFrontBrakeForce(getParent().getConfig().getRMax());
			properties.setLeftBrakeForce(getParent().getConfig().getRMax());
			properties.setRightBrakeForce(getParent().getConfig().getRMax());
		}

		return properties;
	}

	private float getVerAngle(DroneProperties properties) {
		float overstaande = studCube.getY() - properties.getPosition().getY();
		float aanliggende = studCube.getZ() - properties.getPosition().getZ();
		return (float) Math.atan(overstaande / aanliggende);
	}

	public SimpleAutopilot getParent() {
		return parent;
	}

	public void setParent(SimpleAutopilot parent) {
		this.parent = parent;
	}
}
