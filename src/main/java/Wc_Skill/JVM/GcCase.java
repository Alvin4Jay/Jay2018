package Wc_Skill.JVM;

/**
 * YGC测试
 * @param -XX:+PrintGCDetails -Xmn500m
 * @author xuanjian.xuwj
 */
public class GcCase {
    public static void main(String[] args){

        for (int i = 0; i < 1200; i++) {
            allocate_1M();
        }

    }

    public static void allocate_1M(){
        byte[] _1M = new byte[1024*1024];
    }

}
