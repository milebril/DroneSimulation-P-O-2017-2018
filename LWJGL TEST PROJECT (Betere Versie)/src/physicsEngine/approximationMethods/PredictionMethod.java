package physicsEngine.approximationMethods;

import org.lwjgl.util.vector.Vector3f;

public abstract class PredictionMethod {
	
	/**
	 * A prediction method can be used to get an approximation of the linear and angular velocities,
	 * a given amount of seconds into the future
	 * @param stepSize The default step size to be used by the prediction method.
	 */
	public PredictionMethod(float stepSize) {
		this.stepSize = stepSize;
	}
	
	
	/**
	 * The step size used by this approximation method.
	 */
	private final float stepSize;
	
	/**
	 * Returns the step size used by this approximation method.
	 */
	public float getStepSize() {
		return this.stepSize;
	};
	
	
	/**
	 * Returns an array of the predicted linear and angular velocity, one h step into the future.
	 * @param linVelocity The current linear velocity
	 * @param angVelocity The current angular velocity
	 * @param linAcceleration The current linear acceleration
	 * @param angAcceleration The current angular acceleration
	 */
	public abstract Vector3f[] predictVelocity(Vector3f linVelocity, Vector3f angVelocity, 
			Vector3f linAcceleration, Vector3f angAcceleration);
	
	/**
	 * Returns an array of the predicted linear and angular velocity, dt seconds into the future.
	 * @param linVelocity The current linear velocity
	 * @param angVelocity The current angular velocity
	 * @param linAcceleration The current linear acceleration
	 * @param angAcceleration The current angular acceleration
	 * @param dt For how many seconds into the future the velocity has to be predicted
	 */
	public abstract Vector3f[] predictVelocity(Vector3f linVelocity, Vector3f angVelocity, 
			Vector3f linAcceleration, Vector3f angAcceleration, float stepSize);

	
}
