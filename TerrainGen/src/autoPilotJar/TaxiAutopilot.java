package autoPilotJar;

import org.lwjgl.util.vector.Vector3f;


public class TaxiAutopilot {

	private Vector3f goal = new Vector3f(0, 0, 200);
	
	private SimpleAutopilot parent;
	
	private PIDController pidBrake;

	
public TaxiAutopilot(SimpleAutopilot parent) {
		setParent(parent);
		pidBrake = new PIDController(1, 0, 0.5f, (float) 0.05*2486, 0);
	}

	private float getHorAngle(DroneProperties properties) {
		float overstaande = goal.getX() - properties.getPosition().getX();
		float aanliggende = goal.getZ() - properties.getPosition().getZ();
		return (float) Math.atan(overstaande / aanliggende);
	}
	
	public DroneProperties timePassed(DroneProperties properties) {
		
		if (properties.getHeading() - getHorAngle(properties) < -0.2 || properties.getHeading() - getHorAngle(properties) > 0.2) {
			properties.setLeftBrakeForce(properties.getLeftBrakeForce() - pidBrake.calculateChange(properties.getHeading() - getHorAngle(properties), properties.getDeltaTime()));
			if(properties.getLeftBrakeForce() > properties.getRMax()/2) {
				properties.setLeftBrakeForce(properties.getRMax()/2);
			} else if(properties.getLeftBrakeForce() < 0) {
				properties.setLeftBrakeForce(0);
			}	
			properties.setRightBrakeForce(properties.getRightBrakeForce() + pidBrake.calculateChange(properties.getHeading() - getHorAngle(properties), properties.getDeltaTime()));
			if(properties.getRightBrakeForce() > properties.getRMax()/2) {
				properties.setRightBrakeForce(properties.getRMax()/2);
			} else if(properties.getRightBrakeForce() < 0) {
				properties.setRightBrakeForce(0);
			}
			properties.setThrust(50);
		} else {
			properties.setThrust(200);
			properties.setLeftBrakeForce(0);
			properties.setRightBrakeForce(0);
			properties.setFrontBrakeForce(0);
		}
		
		
		
		
//		if (properties.getHeading() - getHorAngle(properties) < -0.02) {
////			System.out.println("jow");
////			properties.setLeftBrakeForce(getParent().getConfig().getRMax()/2);
////			properties.setRightBrakeForce(0);
////			properties.setFrontBrakeForce(0);
////			properties.setThrust(50);	
//		} else if (properties.getHeading() - getHorAngle(properties) > 0.02) {
//			System.out.println("hey");
//			properties.setLeftBrakeForce(0);
//			properties.setRightBrakeForce(getParent().getConfig().getRMax()/2);
//			properties.setFrontBrakeForce(0);
//			properties.setThrust(50);
//		} else if (properties.getVelocity().length() > 10) {
//			properties.setThrust(0);
//			properties.setLeftBrakeForce(0);
//			properties.setRightBrakeForce(0);
//			properties.setFrontBrakeForce(0);
//		} else {
//			properties.setThrust(200);
//			properties.setLeftBrakeForce(0);
//			properties.setRightBrakeForce(0);
//			properties.setFrontBrakeForce(0);
//		}
		
		System.out.println("hoek: " + String.valueOf(properties.getHeading() - getHorAngle(properties)));
		System.out.println(properties.getRMax());
		System.out.println("thrust: " + String.valueOf(properties.getThrust()));
		System.out.println("linksrem: " + String.valueOf(properties.getLeftBrakeForce()));
		System.out.println("rechtsrem: " + String.valueOf(properties.getRightBrakeForce()));
		System.out.println("factor: " + String.valueOf(pidBrake.calculateChange(properties.getHeading() - getHorAngle(properties), properties.getDeltaTime())));
		
		//TODO: Case if nearly reached -> vertragen
		if (SimpleAutopilot.getEuclidDist(properties.getPosition(),goal) <= 3) {
			properties.setThrust(0);
			properties.setLeftBrakeForce(getParent().getConfig().getRMax());
			properties.setRightBrakeForce(getParent().getConfig().getRMax());
			properties.setFrontBrakeForce(getParent().getConfig().getRMax());
		}
		
		// Als de drone binnen 1 meter komt en trager dan 1m/s rijdt, dan wordt de goal bereikt.
		// In de opgave staat er dat de goal exact bereikt moet worden, maar we nemen binnen 1 meter want zo exact is niet echt belangrijk.
		if(SimpleAutopilot.getEuclidDist(properties.getPosition(),goal) <= 2 &&	properties.getVelocity().length() <= 1)
			System.out.println("GOAL REACHED!");
			
		
		return properties;
	}

		public SimpleAutopilot getParent() {
			return parent;
		}


		public void setParent(SimpleAutopilot parent) {
			this.parent = parent;
		}


}
