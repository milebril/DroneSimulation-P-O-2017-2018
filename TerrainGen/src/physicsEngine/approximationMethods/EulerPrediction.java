package physicsEngine.approximationMethods;

import org.lwjgl.util.vector.Vector3f;

public class EulerPrediction extends PredictionMethod {
	
	/**
	 * The Euler method for predicting velocity
	 * @param stepSize The default step size to be used.
	 */
	public EulerPrediction(float stepSize) {
		super(stepSize);
	}
	
	@Override
	public Vector3f[] predictVelocity(Vector3f linVelocity, Vector3f angVelocity, Vector3f linAcceleration,
			Vector3f angAcceleration) {
		
		// linear prediction
		Vector3f linPrediction = new Vector3f();
		Vector3f.add(linVelocity, (Vector3f) linAcceleration.scale(getStepSize()), linPrediction);
		
		// angular prediction
		Vector3f angPrediction = new Vector3f();
		Vector3f.add(angVelocity, (Vector3f) angAcceleration.scale(getStepSize()), angPrediction);
		
		return new Vector3f[]{linPrediction, angPrediction};
	}
	
	@Override
	public Vector3f[] predictVelocity(Vector3f linVelocity, Vector3f angVelocity, Vector3f linAcceleration,
			Vector3f angAcceleration, float stepSize) {

		// linear prediction
		Vector3f linPrediction = new Vector3f();
		Vector3f.add(linVelocity, (Vector3f) linAcceleration.scale(stepSize), linPrediction);
		
		// angular prediction
		Vector3f angPrediction = new Vector3f();
		Vector3f.add(angVelocity, (Vector3f) angAcceleration.scale(stepSize), angPrediction);

		System.out.println("heufd");
		
		return new Vector3f[]{linPrediction, angPrediction};
	}
	
}
