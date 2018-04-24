package prevAutopilot;

public class PhysicsTestAutopilot {

	public PhysicsTestAutopilot(SimpleAutopilot parent) {
		setParent(parent);
	}
	
	public DroneProperties timePassed(DroneProperties properties) {
		
		properties.setFrontBrakeForce(parent.getConfig().getRMax());
		properties.setLeftBrakeForce(parent.getConfig().getRMax());
		properties.setRightBrakeForce(parent.getConfig().getRMax());
		
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

