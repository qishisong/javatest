package jmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by qiss on 2017/8/25.
 */
public class Agent {
    public static void main(String[] args) throws Exception{
        //首先建立一个MBeanServer,用来管理我们的MBean,通常MBeanServer来获取我们MBean的信息
        //间接调用MBean的方法，然后生产我们的资源的一个对象。
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        //MBeanServer server = MBeanServerFactory.createMBeanServer(); 该方式不能再JConsole中使用
        String domainName = "MyMBean";

        //为MBean（下面的new Hello）创建ObjectName实例
        ObjectName helloName = new ObjectName(domainName + ":name=HelloWorld");
        //将new Hello()这个对象注册到MBeanServer上去
        mbs.registerMBean(new Hello(),helloName);

        //Distribute Layer提供了一个HtmlAdapter。支持Http访问协议，并且有一个不错的HTML界面，这里
        //的Hello就是用这个作为远端管理的界面，事实上HtmlAdapter是一个简单的HttpServer，它将http请求
        //转换为JMX Agent的请求
        ObjectName adapterName = new ObjectName(domainName + ":name=htmladapter,port=8082");
        HtmlAdaptorServer adapter = new HtmlAdaptorServer();
        adapter.start();
        mbs.registerMBean(adapter,adapterName);

        int rmiPort=1099;
        Registry registry = LocateRegistry.createRegistry(rmiPort);
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi:/localhost:" + rmiPort + "/" + domainName);
        JMXConnectorServer jmxConnector = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
        jmxConnector.start();


    }
}
