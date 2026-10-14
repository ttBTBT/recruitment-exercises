package openatom.basics;
public final class VehicleFactory {
    private VehicleFactory() { }
    public static TransportVehicle createVehicle(String type) {
        if ("Truck".equals(type)) return new Truck("T-998");
        if ("Drone".equals(type)) return new Drone("D-007");
        return null;
    }
}
