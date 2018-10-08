package 数据结构.Sword2Offer;

/**
 * 饿汉式
 */
public class P2_Singleton4 {
    private static P2_Singleton4 instance = new P2_Singleton4(); //饿汉式

    private P2_Singleton4(){}

    public static P2_Singleton4 getInstance(){
        return instance;
    }

}
