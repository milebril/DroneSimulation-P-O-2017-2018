package autopilot.interfaces;

public interface AutopilotModule {
    void defineAirportParams(float length, float width);
    void defineAirport(float centerX, float centerZ, float centerToRunway0X, float centerToRunway0Z); 
        // (centerToRunway0X, centerToRunway0Z) constitutes a unit vector pointing from the center of the airport towards runway 0
    void defineDrone(int airport, int gate, int pointingToRunway, AutopilotConfig config); 
        // airport and gate define the drone's initial location, pointingToRunway its initial orientation. The first drone that is defined is drone 0, etc.
    void startTimeHasPassed(int drone, AutopilotInputs inputs); 
        // Allows the autopilots for all drones to run in parallel if desired. Called with drone = 0 through N - 1, in that order, if N drones have been defined.
    AutopilotOutputs completeTimeHasPassed(int drone); 
        // Called with drone = 0 through N - 1, in that order, if N drones have been defined.
    void deliverPackage(int fromAirport, int fromGate, int toAirport, int toGate); 
        // Informs the autopilot module of a new package delivery request generated by the testbed.
    void simulationEnded();
}
