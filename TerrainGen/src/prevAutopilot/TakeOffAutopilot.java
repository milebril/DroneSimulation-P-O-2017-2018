package prevAutopilot;

public class TakeOffAutopilot {
	
	private SimpleAutopilot parent;

	public TakeOffAutopilot(SimpleAutopilot parent) {
		setParent(parent);
	}
	
	public DroneProperties timePassed(DroneProperties properties) {
		properties.setThrust(parent.getConfig().getMaxThrust());
		
		//Als de drone sneller gaat dan 60 m/s, begin met opstijgen.
		if (properties.getVelocity().length() > 40) {
			properties.setLeftWingInclination((float) Math.toRadians(20));
			properties.setRightWingInclination((float) Math.toRadians(20));
		} else {
			properties.setLeftWingInclination(0);
			properties.setRightWingInclination(0);
		}

		//Als de drone een hoogte van 10 meter bereikt heeft, mag de flyingAP overnemen
//		if (properties.getPosition().getY() > 32) {
//			getParent().setStage(AutopilotStages.TURNING);
//		}
		if (properties.getPosition().getY() > 10) {
			getParent().setStage(AutopilotStages.FLYING);
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