package autoPilotJar;

public class LandingAutopilot {

	private SimpleAutopilot parent;

	public LandingAutopilot(SimpleAutopilot parent) {
		setParent(parent);
	}

	public DroneProperties timePassed(DroneProperties properties) {
		//LET PLANE GO DOWN
		if(properties.getPosition().getY() < 25 && properties.getPosition().getY() > 10 && properties.getVelocity().length() >= 10) {
			System.out.println("SLOW DOWN");
			properties.setThrust(200);
			properties.setHorStabInclination(0);
			properties.setVerStabInclination(0);
			properties.setLeftWingInclination((float) Math.toRadians(15));
			properties.setRightWingInclination((float) Math.toRadians(15));
		} 
		else if(properties.getPosition().getY() < 10 && properties.getVelocity().length() >= 10) {
			System.out.println("SLOW DOWN 2");
			properties.setThrust(200);
			properties.setHorStabInclination(0);
			properties.setVerStabInclination(0);
			properties.setLeftWingInclination((float) Math.toRadians(5));
			properties.setRightWingInclination((float) Math.toRadians(5));
		} 
		else if(properties.getPosition().getY() > 25 && properties.getVelocity().length() >= 10) {
			System.out.println("REDUCE HEIGHT");
			properties.setThrust(0);
			properties.setHorStabInclination((float) Math.toRadians(5));
			properties.setVerStabInclination(0);
			properties.setLeftWingInclination((float) Math.toRadians(-5));
			properties.setRightWingInclination((float) Math.toRadians(-5));
		}
		//BRAKE
		else {
			properties.setThrust(0);
			properties.setFrontBrakeForce(getParent().getConfig().getRMax());
			properties.setLeftBrakeForce(getParent().getConfig().getRMax());
			properties.setRightBrakeForce(getParent().getConfig().getRMax());
		}
		
		return properties;
	}

	public SimpleAutopilot getParent() {
		return parent;
	}

	public void setParent(SimpleAutopilot parent) {
		this.parent = parent;
	}
}
