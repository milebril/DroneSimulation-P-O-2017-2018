package autopilot;


public class PID {
	
	private final float Kp;
	private final float Ki;
	private final float Kd;
	
	private float totalError = 0; // Integraal (I)
	private float prevError = 0;  // Afgeleide (D)
	
	private float maxFeedback; // Globale gevoeligheid
	private float goal;
	
	public PID(float Kp, float Ki, float Kd, float maxFeedback) {
		this.Kp = Kp;
		this.Ki = Ki;
		this.Kd = Kd;
		this.maxFeedback = maxFeedback;
	}
	
	
	public float getFeedback(float error, float dt) {
		if (dt == 0) return 0;

		float P = error;
		float D = (error - prevError) / dt;
		
		if (Math.abs(Kp * P + Kd * D) < maxFeedback) integrate(error * dt);
		
		float I = totalError;
		
		saveError(error);
		
		//System.out.println("("+Kp*P+" + "+Ki*I+" + "+Kd*D+")");
		
		float feedback = (Kp * P + Ki * I + Kd * D);
		if (maxFeedback < feedback) {
			return maxFeedback;
		} else if (feedback < -maxFeedback) {
			return -maxFeedback;
		} else {
			return feedback;
		}
	}
	
	private void integrate(float error) {
		totalError += error;
	}
	
	private void saveError(float error) {
		this.prevError = error;
	}
	
	public void reset() {
		this.totalError = 0;
		this.prevError = 0;
	}
}

