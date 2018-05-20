package autopilot.algorithms;

import org.lwjgl.util.vector.Vector3f;

import autopilot.PID;
import autopilot.Pep;
import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;
import imageRecognition.openCV.ImageProcessor;

public class CubeZoeker implements Algorithm {
	
	@Override
	public void cycle(AlgorithmHandler handler) {
		float dt = handler.getProperties().getDeltaTime();
		
		// SNELHEID ~ THRUST
		float feedback = thrustPID.getFeedback(AutopilotAlain.CRUISESPEED - handler.getProperties().getVelocity().length(), dt);
		handler.setThrust(Math.max(0, feedback));
		
		// PITCH ~ HOR STAB
		feedback = pitchPID.getFeedback(handler.getProperties().getPitch(), dt);
		handler.setHorStabInclination(feedback);
		
		// STIJGEN/DALEN ~ ZIJVLEUGELS
		float dY = handler.getProperties().transformToDroneFrame(handler.getProperties().getVelocity()).y;
		
		feedback = spaghetti.getFeedback(-dY, dt);
		handler.setLeftWingInclination(0.15f+feedback);
		handler.setRightWingInclination(0.15f+feedback);

		double[] temp = GetRotation(handler.getProperties().getImage());
		double hoek = temp[0];
		double r = temp[1];
//		System.out.println();
//		System.out.println("x:" + temp[0] +" y:"+ temp[1]);
//		System.out.println("hoek:" + hoek + " r:" + r);
	}
	
	private PID thrustPID = new PID(1000, 400, 50, 2000);
	private PID pitchPID = new PID(1f, 0.1f, 0.1f, 1f);
	
	private PID spaghetti = new PID(0.2f, 0.1f, 0.0f, 0.5f);
	
	/**
	 * Returnt hoe de drone moet draaien om naar de cube te wijzen
	 * @returns (vereiste roll, vereiste "pitch" -> wat de hor stab moet doen)
	 */
	private double[] GetRotation(byte[] image) {
		double[] xy = Pep.getCoordinatesOfCube(image);
		double x = xy[0]; double y = xy[1];
		
		double r = Math.hypot(x, y)*Math.signum(y);
		double hoek = Math.toRadians(90) - Math.acos(x/r);
		
		return new double[]{hoek ,r};
	}
	
	
	

	@Override
	public String getName() {
		return "CubeZoeker";
	}
	
	
	
	

}
