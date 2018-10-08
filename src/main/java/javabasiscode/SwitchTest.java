package javabasiscode; /**
 * Created by Jay on 2017/5/4.
 */
public class SwitchTest {

    public static void main(String[] args) {
        switchEnum();
        switchString();
    }

    public static void switchEnum(){
        EnumDemo e = EnumDemo.LEFT;

        switch (e){
            case LEFT:
                System.out.println("left...");
                break;
            default:
                System.out.println("right...");
        }


    }

    public static void switchString(){
        String s  ="abc";

        switch (s){
            case "abc":
                System.out.println("abc");
                break;
            default:
                System.out.println("other..");
        }
    }

    enum EnumDemo{
        LEFT,
        RIGHT;
    }
}



