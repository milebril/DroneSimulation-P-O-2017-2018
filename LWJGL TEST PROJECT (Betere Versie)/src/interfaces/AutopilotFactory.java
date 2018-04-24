package interfaces;

import autoPilotJar.SimpleAutopilot;
import autoPilotJar.TakeOffAutopilot;
import autoPilotJar.TaxiAutopilot;

public class AutopilotFactory {
	
	public static Autopilot createAutopilot() {
		return new SimpleAutopilot();
	}
	
}
