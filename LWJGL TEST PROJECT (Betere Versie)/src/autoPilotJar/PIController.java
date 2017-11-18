package autoPilotJar;

public class PIController {
	//old values
	//	private float Kp = 0.1f; //Proportional -> Trial Error
	//	private float Ki = 0.051f; //Integraal constante
	private float Kp = 10f;
	private float Ki = 0.1f;
	private float Kd = 0.1f;
	
	private float totalError = 0; //Integraal
	private float prevError = 0; //(currentError - prevError)/dt = Afgeleide
	
	private float inclinationChange = (float) -(Math.PI / 180);
	
	//Horizontaal: Wing.y-force == Gravity; -> Dan vliegen we recht
	//E(t) = yForce - Gravity;
	
	public PIController() {
		
	}
	
	/**
	 * Horizontal WING PI.
	 * 
	 * If roll > 0 -> Roll PID 
	 * else fly straight PID
	 * 
	 * Returns factor 
	 */
	public float[] calculateHorizontalFactor(float yForceLeft, float yForceRight, float goalY, float currentY, float gravity, float dt) {
		//float currentError1 = yForceLeft + yForceRight - gravity;
		float currentError = currentY - goalY;
		//float currentError = currentError1 + 10*currentError2;
		totalError += currentError;
		System.out.println(currentError);
		float P = Kp * currentError;
		float I = Ki * totalError;
		float D = 0;
		if(dt > 0.00001){
			D = Kd * (this.prevError - currentError)/dt;
		}
		System.out.println("P :" + P);
		System.out.println("I :" + I);
		System.out.println("D :" + D);

		float factor = P + I + D;
		
		float newInclination = factor * inclinationChange;
		if(newInclination >= Math.PI/4) {
			newInclination = (float) (Math.PI/4);
		}
		if(newInclination <= -Math.PI/4) {
			newInclination = (float) -(Math.PI/4);
		}
		this.prevError = currentError;
		System.out.println("newInc : " + newInclination);
		
		return new float[] { newInclination, newInclination };
	}
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
