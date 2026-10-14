package openatom.basics;
public class OopDemo {
    public static void main(String[] args) {
        String task = args.length == 0 ? "all" : args[0];
        if (task.equals("all") || task.equals("4.1")) productDemo();
        if (task.equals("all") || task.equals("4.2")) vehicleDemo();
        if (task.equals("all") || task.equals("4.3")) storageDemo();
        if (task.equals("all") || task.equals("4.4")) arrayDemo();
        if (task.equals("all") || task.equals("4.5")) patternsDemo();
    }
    private static void productDemo() {
        System.out.println("=== 4.1 封装 ===");
        Product first = new Product("P001", "机械键盘", 450);
        Product second = new Product();
        second.setId("P002"); second.setName("蓝牙耳机"); second.setPrice(299);
        first.printInfo(); second.printInfo();
        System.out.println("系统当前共创建了 " + Product.getTotalCount() + " 个商品实例。");
    }
    private static void vehicleDemo() {
        System.out.println("=== 4.2 多态 ===");
        TransportVehicle[] vehicles = {new Truck("T-998"), new Drone("D-007")};
        for (TransportVehicle vehicle : vehicles) {
            GPSLocatable gps = (GPSLocatable) vehicle;
            System.out.println(vehicle + " 当前位置：" + gps.getLocation() + " | 配送10kg货物费用：" + vehicle.calculateCost(10) + "元");
        }
    }
    private static void storageDemo() {
        System.out.println("=== 4.3 泛型储物箱 ===");
        StorageBox<Product> products = new StorageBox<>();
        products.storeItem(new Product("P001", "机械键盘", 450));
        Product product = products.retrieveItem();
        System.out.println("商品储物箱存取测试：成功取出商品 -> " + product.getName());
        StorageBox<TransportVehicle> vehicles = new StorageBox<>();
        vehicles.storeItem(new Drone("D-007"));
        TransportVehicle vehicle = vehicles.retrieveItem();
        System.out.println("设备储物箱存取测试：成功取出设备 -> " + vehicle);
    }
    private static void arrayDemo() {
        System.out.println("=== 4.4 动态数组 ===");
        MyArrayList<String> packages = new MyArrayList<>();
        for (String id : new String[]{"单号A", "单号B", "单号C", "单号D", "单号E"}) packages.add(id);
        System.out.println("获取索引[2]的元素：" + packages.get(2));
        packages.remove(1);
        System.out.println("验证移位，此时索引[1]的元素变为了：" + packages.get(1));
    }
    private static void patternsDemo() {
        System.out.println("=== 4.5 设计模式 ===");
        System.out.println("单例测试：配置类实例A与实例B是否相同？ -> " + (LogisticsConfig.getInstance() == LogisticsConfig.getInstance()));
        TransportVehicle vehicle = VehicleFactory.createVehicle("Drone");
        System.out.println("工厂调度：工厂成功分配 -> " + vehicle + "，配送费用：" + vehicle.calculateCost(10));
        LogisticsOrder order = new LogisticsOrder.Builder("OD-999").sender("Alice").recipient("Bob").fragile(true).insuredAmount(1000).build();
        System.out.println("运单生成：" + order);
    }
}
