package 数据结构.Nowcoder.day0823;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Problem_01 {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String s = sc.nextLine();
		Set<Character> set = new HashSet<Character>(); //不重复
		int count = 0;
		for (char c : s.toCharArray()) {
			if (!set.contains(c)) {
				set.add(c);
				count++;
			}
		}
		if (count > 2)
			System.out.println(0);
		else if (count == 2)
			System.out.println(2);
		else
			System.out.println(count);
		sc.close();
	}
}
