package autoPilotJar;

public class TurningAutopilot {
	
	private SimpleAutopilot parent;
	private PIDController pidRoll;
	
	public TurningAutopilot(SimpleAutopilot p) {
		this.parent = p;
		
		 this.pidRoll = new PIDController(0.0f,0.5f,1.0f,(float) Math.toRadians(1),0);
	}
	
	public DroneProperties timePassed(DroneProperties properties) {

		properties.setLeftWingInclination(properties.getLeftWingInclination() + pidRoll.calculateChange(properties.getRoll(), properties.getDeltaTime()));
		if(properties.getLeftWingInclination() > Math.PI/6) properties.setLeftWingInclination((float) (Math.PI/6));
		else if(properties.getLeftWingInclination() < - Math.PI/6) properties.setLeftWingInclination((float) -(Math.PI/6));
		
		
		return properties;
	}

}
