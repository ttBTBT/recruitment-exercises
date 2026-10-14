package openatom.basics;

public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello world");
        new GreetingService().sayHello("开放原子");
    }
}
