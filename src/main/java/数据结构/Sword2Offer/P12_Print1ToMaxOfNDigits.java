package 数据结构.Sword2Offer;

/**
 * 打印1到最大的n位数(模拟整数加法，大数思想)
 */
public class P12_Print1ToMaxOfNDigits {

    //打印1至n位的最大数
    public void print1ToMaxOfNDigits(int n){
        if(n <= 0){
            return;
        }
        char[] number = new char[n]; //存放n位整数
        for (int i = 0; i < number.length; i++) { //初始化
            number[i]= '0';
        }

        while(!increment(number)){ //加1，退出条件是到达n位9999...
            printNumber(number); //打印
        }
    }

    //模拟整数加法
    private boolean increment(char[] number){
        boolean isOverflow = false; //溢出标志
        int takeOver = 0;  //进位标志
        int length = number.length;
        for (int i = length-1; i >= 0; i++) {
            int sum = number[i] - '0' + takeOver;
            if(i == length-1){
                sum ++;
            }
            if(sum >= 10){ //进位，循环
                if(i == 0){
                    isOverflow = true;
                }else {
                    sum -= 10;
                    takeOver = 1;
                    number[i] = (char) ('0' + sum);
                }
            }else { //没进位，退出循环
                number[i] = (char) ('0' + sum);
                break;
            }
        }
        return isOverflow;
    }

    //打印数字(不打印开始的0位)
    private void printNumber(char[] number){
        boolean isBeginning0 = true;
        int length = number.length;
        for (int i = 0; i < length; i++) {
            if(isBeginning0 && number[i] != '0'){
                isBeginning0 = false;
            }
            if(!isBeginning0){
                System.out.printf("%c", number[i]);
            }
        }
        System.out.print("\t");
    }
}
