package 数据结构.Sword2Offer;

/**
 * 二进制中1的个数（位运算）
 */
public class P10_NumberOf1InBinary {
    //n二进制整数中有多少个1就只需循环几次
    public static int NumberOf1_3(int n){
        int count = 0;
        while(n != 0){ //二进制中还有位为1
            count ++;
            n = n & (n-1);  //二进制靠近右边第一个为1的位从1变为0，再与原数与，将原数n减去了一个1位
        }
        return count;
    }

    //位运算，n二进制有几位flag就移动几次
    public static int NumberOf1_2(int n){
        int count = 0;
        int flag = 1;
        while(flag != 0){  //从最左边高位移除，变为0
            if((n & flag) != 0){ //与运算结果不为0，说明该位为1
                count ++;
            }
            flag = flag << 1; //flag一直右移，与n进行与运算
        }
        return count;
    }

    //O(n) O(n)时间、空间复杂度
    public  static int NumberOf1(int n) {
        String s = Integer.toBinaryString( n );

        char[] c = s.toCharArray();

        int cnt = 0;
        for(int i=0;i<c.length;i++){
            if(c[i] == '1'){
                cnt ++;
            }
        }
        return cnt;
    }

    public static void main(String[] args) {
        boolean flag = false;
        for (int i = 0; i < 1000000; i++) {
            int n = (int)(Math.random()*1000000);
            if(NumberOf1(n) != NumberOf1_2(n) || NumberOf1(n) != NumberOf1_3(n)){
                flag = true;
                break;
            }
        }
        if(flag){
            System.out.println("2333333");
        }else {
            System.out.println("6666666");
        }
    }

}
