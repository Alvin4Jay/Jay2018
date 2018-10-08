package 数据结构.Nowcoder.day0823;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * 操作序列(直接模拟会超时)
 */
public class Problem_04 {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		while (in.hasNextInt()) {
			int n = in.nextInt();
			/*Deque<Integer> deque = new LinkedList<Integer>(); //双端队列
			boolean convert = false;
			for (int i = 0; i < n; i++) {
				if (convert) {
					deque.addLast(in.nextInt());
				} else {
					deque.addFirst(in.nextInt());
				}
				convert = !convert;
			}
			if (convert) {
				while (deque.size() != 1) {
					System.out.print(deque.pollFirst() + " ");
				}
				System.out.println(deque.pollFirst());
			} else {
				while (deque.size() != 1) {
					System.out.print(deque.pollLast() + " ");
				}
				System.out.println(deque.pollLast());
			}*/

			LinkedList<Integer> res = new LinkedList<Integer>(); //双端队列
			boolean flag = false; //奇数，左，不逆序
			for (int i = 0; i < n; i++) {
				if(!flag){
					res.addFirst(in.nextInt());
				}else {
					res.addLast(in.nextInt());
				}
				flag = !flag;
			}
			if(!flag){
				while(res.size() != 1){
					System.out.print(res.pollLast()+ " "); //逆序
				}
				System.out.print(res.pollLast());
			}else {
				while(res.size() != 1){
					System.out.print(res.pollFirst()+ " "); //不逆序
				}
				System.out.print(res.pollFirst());
			}
		}
		in.close();
	}

}
