package 数据结构.LeetCode.greedy;

/**
 Imagine you have a special keyboard with the following keys:

 Key 1: (Demo.A): Prints one 'Demo.A' on screen.

 Key 2: (Ctrl-Demo.A): Select the whole screen.

 Key 3: (Ctrl-C): Copy selection to buffer.

 Key 4: (Ctrl-V): Print buffer on screen appending it after what has already been printed.

 Now, you can only press the keyboard for N times (with the above four keys), find out the
 maximum numbers of 'Demo.A' you can print on screen.

 Example 1:

 Input: N = 3
 Output: 3
 Explanation:
 We can at most get 3 Demo.A's on screen by pressing following key sequence:
 Demo.A, Demo.A, Demo.A
 Example 2:

 Input: N = 7
 Output: 9
 Explanation:
 We can at most get 9 Demo.A's on screen by pressing following key sequence:
 Demo.A, Demo.A, Demo.A, Ctrl Demo.A, Ctrl C, Ctrl V, Ctrl V
 Note:

 1 <= N <= 50
 Answers will be in the range of 32-bit signed integer.
 */
public class Ex651_4_Keys_Keyboard {
    public int maxA(int N) {
        int[] dp = new int[N+1]; //从1开始，dp[i]表示到i位置为止得到的最大A数量

        if(N <= 3){
            return N;
        }

        for (int i = 1; i <= N; i++) {
            dp[i] = i; // 只按A键
            for(int j = 3; j< i; j++){
                //从dp[i-j]开始，Ctrl-A,Ctrl-C,Ctrl-V,Ctrl-V...复制策略
                dp[i] = Math.max(dp[i], dp[i-j]*(j-1));
            }
        }
        return dp[N];
    }

    public static void main(String[] args) {
        Ex651_4_Keys_Keyboard ex = new Ex651_4_Keys_Keyboard();
        for (int i = 1; i <= 50; i++) {
            System.out.println("N: " + i + ", Sum: " + ex.maxA(i));
        }
    }
}
