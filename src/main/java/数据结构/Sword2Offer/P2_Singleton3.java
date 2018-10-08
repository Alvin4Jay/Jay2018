package 数据结构.Sword2Offer;

/**
 * 比较适用多线程
 */
public class P2_Singleton3 {
    private volatile static P2_Singleton3 instance = null;

    private P2_Singleton3(){}

    public static P2_Singleton3 getInstance(){
        if(instance == null){  //双重检查
            synchronized (P2_Singleton3.class){  //同步锁，多线程环境
                if(instance == null){
                    instance = new P2_Singleton3();
                }
            }
        }

        return instance;
    }

}
