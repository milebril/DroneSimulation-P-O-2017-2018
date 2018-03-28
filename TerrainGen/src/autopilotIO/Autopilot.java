package autopilotIO;

import autopilotIO.config.AutopilotConfig;
import autopilotIO.input.AutopilotInputs;
import autopilotIO.output.AutopilotOutputs;

public interface Autopilot {
    AutopilotOutputs simulationStarted(AutopilotConfig config, AutopilotInputs inputs);
    AutopilotOutputs timePassed(AutopilotInputs inputs);
    void simulationEnded();
}