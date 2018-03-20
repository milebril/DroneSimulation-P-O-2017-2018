package autoPilotJar;

public class TakeOffAutopilot {
	
	private SimpleAutopilot parent;

	public TakeOffAutopilot(SimpleAutopilot parent) {
		setParent(parent);
	}
	
	public DroneProperties timePassed(DroneProperties properties) {
		properties.setThrust(parent.getConfig().getMaxThrust());
		
		//Als de drone sneller gaat dan 60 m/s, begin met opstijgen.
		if (properties.getVelocity().length() > 60) {
			properties.setLeftWingInclination((float) Math.toRadians(20));
			properties.setRightWingInclination((float) Math.toRadians(20));
		} else {
			properties.setLeftWingInclination(0);
			properties.setRightWingInclination(0);
		}

		//Als de drone een hoogte van 10 meter bereikt heeft, mag de flyingAP overnemen
		if (properties.getPosition().getY() > 20) {
			getParent().setStage(AutopilotStages.LANDING);
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
