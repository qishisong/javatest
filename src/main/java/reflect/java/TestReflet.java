package reflect.java;

import lombok.Data;

import java.lang.reflect.Constructor;

/**
 * Created by qiss on 2017/8/6.
 */

public class TestReflet{

    public static void main(String[] args) throws Exception {
        Class<?> clazz = null;
        clazz=Class.forName("reflect.java.User");
        User user = (User) clazz.newInstance();
        Constructor<?> cs1 = clazz.getConstructor(String.class);
        User user1 = (User)cs1.newInstance("xiaolong");
        user.setAge(10);
        user.setName("xiaoli");
        System.out.println(user);
        System.out.println("---------------------");
        user1.setAge(22);
        System.out.println("user1:"+user1.toString());
        System.out.println("--------------");
        Constructor<?> cs2 = clazz.getDeclaredConstructor(int.class, String.class);
        cs2.setAccessible(true);
        User user2 = (User)cs2.newInstance(25, "lidakang");
        System.out.println("user2:"+user2.toString());
        System.out.println("--------------------");
        Constructor<?>[] cons = clazz.getDeclaredConstructors();
        for(int i=0;i<cons.length;i++){
            Class<?>[] clazzs = cons[i].getParameterTypes();
            System.out.println("构造函数["+i+"]:"+cons[i].toString() );
            System.out.print("参数类型["+i+"]:(");
            for(int j=0;j<clazzs.length;j++){
                if (j == clazzs.length - 1)
                    System.out.print(clazzs[j].getName());
                else
                    System.out.print(clazzs[j].getName() + ",");

            }
            System.out.println(")");
        }

        Integer.bitCount()
    }
}


@Data
class User{
    private int age;
    private String name;

    public User(){
    }

    public User(String name){
        this.name = name;
    }

    private User(int age,String name){
        this.age = age;
        this.name = name;
    }

}
