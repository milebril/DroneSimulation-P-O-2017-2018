package autoPilotJar;

import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

public class TurningAutopilot {
	
	private SimpleAutopilot parent;
	
	private PIDController pidRoll;
	private PIDController pidHorStab;
	
	
	private float height;
	
	public float p, i, d;
	public boolean failed = false;
	
	public TurningAutopilot(SimpleAutopilot pa) {
		this.parent = pa;
		this.height = 32; // p.getProperties().getPosition().getY();
		System.out.println("HEIGHT: " + this.height);
		
		Random r = new Random();
		p = 0.0f; //r.nextFloat();
		i = 0; //= Math.abs(r.nextFloat() - 0.5f);
		d = 0.5f; // + r.nextFloat();
		this.pidRoll = new PIDController(5.0f,0.0f,3.0f,(float) Math.toRadians(1),(float) Math.toRadians(30));
		this.pidHorStab = new PIDController(p, i, d,(float) (Math.PI / 360), 0);
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
	//	properties.setHorStabInclination((float)Math.toRadians(-4));
		properties.setVerStabInclination((float)Math.toRadians(0));

//		System.out.println("DIFFERENCE: " + (properties.getPosition().getY() - this.height));
//		System.out.println("INCLINATION: " + properties.getHorStabInclination());
		
//		if(properties.getPosition().getY() < height - 5) {
//			System.out.println("FAILED");
//			this.failed = true;
//		}


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
		System.out.println("HEADING: " +  Math.toDegrees(properties.getHeading()));
		
		properties.setThrust(properties.getMaxThrust());
		
//		if(properties.getHeading() >= Math.toRadians(85)) {
//			this.pidRoll = new PIDController(5.0f,0.0f,3.0f,(float) Math.toRadians(1),(float) Math.toRadians(0));
//		}
		
		
		return properties;
	}
	
	private float getVerAngle(DroneProperties properties) {
		float overstaande = this.height - properties.getPosition().getY();
		float aanliggende;
		if(properties.getHeading() < Math.toRadians(90)) aanliggende = -10;
		else aanliggende = 10;
		return (float) Math.atan(overstaande / aanliggende);
	}

}
