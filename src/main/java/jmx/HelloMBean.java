package jmx;

/**
 * Created by qiss on 2017/8/25.
 * Standrad MBean是JMX管理构架中最简单的一种，只需要开发一个MBean接口（为了实现standard MBean，必须遵循
 * 一套集成规范，必须每一个MBean定义一个接口，而且这个接口的名字必须是其被管理的资源的对象类的名称后面加上“MBean”）
 * 一个实现MBean接口的类，并且把他们注册到MBeanServer中就可以了
 */
public interface HelloMBean {
    public String getName();
    public void setName(String name);
    public void printHello();
    public void printHello(String whoName);
}
