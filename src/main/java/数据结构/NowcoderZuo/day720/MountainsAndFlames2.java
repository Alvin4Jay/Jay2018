package 数据结构.NowcoderZuo.day720;

import java.util.Scanner;
import java.util.Stack;

public class MountainsAndFlames2 {

	public static int nextIndex(int size, int i) {
		return i < (size - 1) ? (i + 1) : 0;
	}

	public static int getInternalSum(int n) {
		return n == 1 ? 0 : n * (n - 1) / 2;
	}

	public static long communications(Node max) {
		Stack<Node> stack = new Stack<>();
		stack.push(max);
		Node cur = max.next;
		max.next = null;
		long res = 0L;
		while(cur != max) {			
			while (!stack.isEmpty() && stack.peek().value < cur.value) {
				int times = stack.pop().times;
				res += getInternalSum(times) + times;
				res += stack.isEmpty() ? 0 : times;
			}
			if (!stack.isEmpty() && stack.peek().value == cur.value) {
				stack.peek().times += cur.times;
			} else {
				stack.push(cur);
			}
			Node tmp = cur.next;
			cur.next = null;
			cur = tmp;
		}
		while (!stack.isEmpty()) {
			int times = stack.pop().times;
			res += getInternalSum(times);
			if (!stack.isEmpty()) {
				res += times;
				res += stack.lastElement().times > 1 ? times : 0;
			}
		}
		return res;
	}

	public static class Node {
		public int value;
		public int times;
		public Node next;

		public Node(int value) {
			this.value = value;
			this.times = 1;
		}
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		while (in.hasNextInt()) {
			int size = in.nextInt();
			if (size == 0) {
				System.out.println(0);
				continue;
			}
			if (size == 1) {
				in.nextInt();
				System.out.println(0);
				continue;
			}
			if (size == 2) {
				in.nextInt();
				in.nextInt();
				System.out.println(1);
				continue;
			}
			Node head = new Node(in.nextInt());
			Node pre = head;
			Node cur = head;
			Node max = head;
			for (int i = 1; i < size; i++) {
				cur = new Node(in.nextInt());
				if(cur.value == pre.value) {
					pre.times++;
				}else {
					pre.next = cur;
					pre = cur;
				}
				max = cur.value > max.value ? cur : max;
			}
			if(pre.value == head.value &&  pre != head) {
				pre.times += head.times;
				pre.next = head.next;
				max = max == head ? pre : max;
			}else {
				pre.next = head;
			}
			
			System.out.println(communications(max));

		}
	}

}
