package autoPilotJar;

public class PIDController {
	private float Kp;
	private float Ki;
	private float Kd;
	
	private float totalError = 0; //Integraal
	private float prevError = 0; //(currentError - prevError)/dt = Afgeleide
	
	private float changeFactor;
	private float goal;
	
	public PIDController(float Kp, float Ki, float Kd, float changeFactor, float goal) {
		this.Kp = Kp;
		this.Ki = Ki;
		this.Kd = Kd;
		
		this.changeFactor = changeFactor;
		this.goal = goal;
	}
	
	public float calculateChange(float current, float dt){
		float currentError = current - this.goal;
		this.totalError += currentError*dt;
	
		float P = Kp * currentError;
		float I = Ki * totalError;
		float D = 0;

	if(dt > 0.00001){
		D = Kd * (currentError - this.prevError)/dt;
		this.prevError = currentError;
	}
	
		float factor = P + I + D;
		return factor * this.changeFactor;
	}

	public void reset(){
		this.totalError = 0;
		this.prevError = 0;
	}
}
