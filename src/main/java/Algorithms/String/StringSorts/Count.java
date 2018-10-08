package Algorithms.String.StringSorts;

import edu.princeton.cs.algs4.Alphabet;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

/**
 * 字符索引数组
 */
public class Count {
    public static void main(String[] args) {
        Alphabet alphabet = new Alphabet(args[0]);  //构造字母表

        int R = alphabet.R();  //字母表字符数

        int[] count = new int[R]; //字符索引数组

        String s = StdIn.readAll(); //字符串

        for (int i = 0; i < s.length(); i++) {
            if(alphabet.contains(s.charAt(i))){ //字符表包含这个字符
                count[alphabet.toIndex(s.charAt(i))] ++; //频率+1
            }
        }

        for (int c = 0; c < R; c++) {
            StdOut.println(alphabet.toChar(c) + " " + count[c]);
        }

    }
}
