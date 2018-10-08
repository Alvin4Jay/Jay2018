package 数据结构.Sword2Offer;

/**
 *  斐波那契数列(动态规划)
 */
public class P9_Fibonaccci {
    public int Fibonacci(int n) {
        if(n == 0) return 0;
        if(n == 1 || n == 2){
            return 1;
        }

        int[] f = new int[n+1];
        f[0] = 0;
        f[1] = 1;
        for(int i=2;i<f.length;i++){
            f[i] = f[i-1]+f[i-2];
        }
        return f[n];
    }
}
