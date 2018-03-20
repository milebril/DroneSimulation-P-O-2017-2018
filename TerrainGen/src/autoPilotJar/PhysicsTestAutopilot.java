package autoPilotJar;

public class PhysicsTestAutopilot {

	public PhysicsTestAutopilot(SimpleAutopilot parent) {
		setParent(parent);
	}
	
	public DroneProperties timePassed(DroneProperties properties) {
		
		// set thrust 50% of max thrust
		properties.setThrust((float) (parent.getConfig().getMaxThrust()*0.5));
		return properties;
	}
	
	private SimpleAutopilot parent;
	
	public SimpleAutopilot getParent() {
		return parent;
	}

	public void setParent(SimpleAutopilot parent) {
		this.parent = parent;
	}
	

}

