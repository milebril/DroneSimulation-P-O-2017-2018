package autoPilotJar;

public class PIDController {
	private float Kp;
	private float Ki;
	private float Kd;
	
	private float totalError = 0; //Integraal
	private float prevError = 0; //(currentError - prevError)/dt = Afgeleide
	
	private float HorizontalStabinclinationChange = (float) -(Math.PI / 180);
	private float thrustChange = -10;
	
	//Horizontaal: Wing.y-force == Gravity; -> Dan vliegen we recht
	//E(t) = yForce - Gravity;
	
	public PIDController(float Kp, float Ki, float Kd) {
		this.Kp = Kp;
		this.Ki = Ki;
		this.Kd = Kd;
	}
	
	public float calculateThrustChange(float currentSpeed, float goalSpeed, float dt){
		float currentError = currentSpeed - goalSpeed;
		totalError += currentError*dt;
		float P = Kp * currentError;
		float I = Ki * totalError;
		float D = 0;
		if(dt > 0.00001){
			D = Kd * (currentError - this.prevError)/dt;
		}
		this.prevError = currentError;
		
		float factor = P + I + D;
		
		return factor * this.thrustChange;
	}
	
	/**
	 * Horizontal WING PI.
	 * 
	 * If roll > 0 -> Roll PID 
	 * else fly straight PID
	 * 
	 * Returns factor 
	 */
	public float[] calculateHorizontalFactor(float goalY, float currentY, float dt) {
		float currentError = currentY - goalY;
		totalError += currentError*dt;
		float P = Kp * currentError;
		float I = Ki * totalError;
		float D = 0;
		if(dt > 0.00001){
			D = Kd * (currentError - this.prevError)/dt;
		}
		//System.out.println("P :" + P);
		//System.out.println("I :" + I);
		//System.out.println("D :" + D);

		float factor = P + I + D;
		
		float InclinationChange = factor * HorizontalStabinclinationChange;
		//System.out.println("InclinationChange: (graden)" + InclinationChange*57.2957795);
		this.prevError = currentError;
		
		return new float[] { InclinationChange, InclinationChange };
	}
	
	
	
//	public float[] calculateHorizontalFactor(float goalY, float currentY, float dt) {
//		float currentError = currentY - goalY;
//		totalError += currentError*dt;
//		float P = Kp * currentError;
//		float I = Ki * totalError;
//		float D = 0;
//		if(dt > 0.00001){
//			D = Kd * (currentError - this.prevError)/dt;
//		}
//		System.out.println("P :" + P);
//		System.out.println("I :" + I);
//		System.out.println("D :" + D);
//
//		float factor = P + I + D;
//		
//		float newInclination = factor * HorizontalStabinclinationChange;
//		if(newInclination >= Math.PI/4) {
//			newInclination = (float) (Math.PI/4);
//		}
//		if(newInclination <= -Math.PI/4) {
//			newInclination = (float) -(Math.PI/4);
//		}
//		this.prevError = currentError;
//		
//		return new float[] { newInclination, newInclination };
//	}
//	public float[] calculateHorizontalFactor(float goalY, float currentY) {
//		float currentError = currentY - goalY;
//		totalError += currentError;
//		System.out.println(currentError);
//		float P = Kp * currentError;
//		float I = Ki * totalError;
//
//		float factor = P + I;
//		
//		float newInclination = factor * inclinationChange;
//		if (newInclination > Math.PI/4) newInclination = (float)Math.PI/4;
//		//System.out.println(totalError);
//		
//		return new float[] { newInclination, newInclination };
//	}
}
