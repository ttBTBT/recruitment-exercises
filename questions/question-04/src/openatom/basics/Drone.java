package openatom.basics;
public class Drone extends TransportVehicle implements GPSLocatable {
    public Drone(String id) { super(id); }
    @Override public double calculateCost(double weight) { validateWeight(weight); return weight * 15.0; }
    @Override public String getLocation() { return "高空坐标(39.9, 116.4)"; }
    @Override public String toString() { return "无人机 [" + vehicleId + "]"; }
}
