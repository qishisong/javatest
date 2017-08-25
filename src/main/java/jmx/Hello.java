package jmx;

/**
 * Created by qiss on 2017/8/25.
 */
public class Hello implements HelloMBean{
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void printHello() {
        System.out.println("Hello world, "+ name);
    }

    public void printHello(String whoName) {
        System.out.println("Hello, "+whoName);
    }
}
