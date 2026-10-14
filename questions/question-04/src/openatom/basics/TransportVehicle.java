package openatom.basics;
public abstract class TransportVehicle {
    protected String vehicleId;
    public TransportVehicle(String vehicleId) {
        if (vehicleId == null || vehicleId.isBlank()) throw new IllegalArgumentException("车辆编号不能为空");
        this.vehicleId = vehicleId;
    }
    public String getVehicleId() { return vehicleId; }
    public abstract double calculateCost(double weight);
    protected void validateWeight(double weight) {
        if (!Double.isFinite(weight) || weight < 0) throw new IllegalArgumentException("重量必须为有限非负数");
    }
}
