package autopilotIO;

import autopilot.AutopilotAlain;
import prevAutopilot.SimpleAutopilot;
import prevAutopilot.TaxiAutopilot;

public class AutopilotFactory {
	
	public static Autopilot createAutopilot() {
		return new AutopilotAlain();
	}
	
}
