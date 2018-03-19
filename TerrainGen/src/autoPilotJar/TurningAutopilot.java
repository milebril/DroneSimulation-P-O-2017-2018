package autoPilotJar;

import org.lwjgl.util.vector.Vector3f;

public class TurningAutopilot {
	
	private SimpleAutopilot parent;
	
	private PIDController pidRoll;
	private PIDController pidHorStab;
	
	private float height;
	
	public TurningAutopilot(SimpleAutopilot p) {
		this.parent = p;
		this.height = 32; // p.getProperties().getPosition().getY();
		System.out.println("HEIGHT: " + this.height);
		
		this.pidRoll = new PIDController(5.0f,0.0f,3.0f,(float) Math.toRadians(1),(float) Math.toRadians(10));
		this.pidHorStab = new PIDController(1.1233587f, 0.30645216f, 1.1156111f, (float) (Math.PI / 180), 0);
	}
	
	public DroneProperties timePassed(DroneProperties properties) {
		
		//BLIJF OP JUISTE HOOGTE
		properties.setHorStabInclination(properties.getHorStabInclination() + pidHorStab
				.calculateChange(properties.getPitch() + getVerAngle(properties), properties.getDeltaTime()));
		if (properties.getHorStabInclination() > Math.PI / 6) {
			properties.setHorStabInclination((float) (Math.PI / 6));
		} else if (properties.getHorStabInclination() < -Math.PI / 6) {
			properties.setHorStabInclination((float) -(Math.PI / 6));
		}


		//ROLL AT 10 DEGREES
		float changeWingRoll = this.pidRoll.calculateChange(properties.getRoll(),properties.getDeltaTime());
		//System.out.println("Roll | ChangeWingRoll : " + this.inputAP.getRoll() + " | " + changeWingRoll);
		
		properties.setLeftWingInclination(properties.getLeftWingInclination() + changeWingRoll);
		if(properties.getLeftWingInclination() > Math.toRadians(15)) properties.setLeftWingInclination((float) Math.toRadians(15));
		if(properties.getLeftWingInclination() < Math.toRadians(-15)) properties.setLeftWingInclination((float) Math.toRadians(-15));
		
		properties.setRightWingInclination(properties.getRightWingInclination() - changeWingRoll);
		if(properties.getRightWingInclination() > Math.toRadians(15)) properties.setRightWingInclination((float) Math.toRadians(15));
		if(properties.getRightWingInclination() < Math.toRadians(-15)) properties.setRightWingInclination((float) Math.toRadians(-15));
		
		System.out.println("+++++++++++");
		System.out.println("Wing roll change : " + changeWingRoll);
		System.out.println("ROLL: " + Math.toDegrees(properties.getRoll()));
		System.out.println("LeftWingInc: " + properties.getLeftWingInclination());
		System.out.println("RightWingInc: " + properties.getRightWingInclination());
		
		properties.setThrust(properties.getMaxThrust());
		properties.setVerStabInclination(0);
		
		return properties;
	}
	
	private float getVerAngle(DroneProperties properties) {
		float overstaande = this.height - properties.getPosition().getY();
		float aanliggende = - 10;
		return (float) Math.atan(overstaande / aanliggende);
	}

}
