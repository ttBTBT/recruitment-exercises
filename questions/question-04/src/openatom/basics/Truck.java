package openatom.basics;
public class Truck extends TransportVehicle implements GPSLocatable {
    public Truck(String id) { super(id); }
    @Override public double calculateCost(double weight) { validateWeight(weight); return weight * 5.0; }
    @Override public String getLocation() { return "仓库A区"; }
    @Override public String toString() { return "卡车 [" + vehicleId + "]"; }
}
