package interfaces;

import autoPilotJar.SimpleAutopilot;

public class AutopilotFactory {
	
	public static Autopilot createAutopilot() {
		return new SimpleAutopilot();
	}
	
}
