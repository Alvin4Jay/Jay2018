package 数据结构.NowcoderZuo.day731;

/**
 * KMP查找子串
 */
public class Problem_02_KMPAlgorithm {

	public static int getIndexOf(String s, String m) {
		if (s == null || m == null || m.length() < 1 || s.length() < m.length()) {
			return -1;
		}
		char[] ss = s.toCharArray();
		char[] ms = m.toCharArray();
		int si = 0;
		int mi = 0;
		int[] next = getNextArray(ms); //next数组，长度m.length()
		while (si < ss.length && mi < ms.length) { //匹配过程
			if (ss[si] == ms[mi]) {
				si++;
				mi++;
			} else if (next[mi] == -1) { //m匹配到最左边，还未匹配到
				si++;
			} else {
				mi = next[mi];
			}
		}
		return mi == ms.length ? si - mi : -1;
	}

	//获取每个位置之前的子串前后缀最大匹配长度
	public static int[] getNextArray(char[] ms) {
		if (ms.length == 1) {
			return new int[] { -1 };
		}
		int[] next = new int[ms.length];
		next[0] = -1; //0位置规定长度为-1
		next[1] = 0;  //1位置规定长度为0
		int pos = 2;
		int cn = 0; //跳到的位置=next[1]
		while (pos < next.length) { //O(M)
			if (ms[pos - 1] == ms[cn]) { //找到匹配，长度为cn+1，cn移动到cn+1位置，pos移动到pos+1位置
				next[pos++] = ++cn; //继续计算
			} else if (cn > 0) { //未找到且未跳到最左边，继续往左跳
				cn = next[cn];
			} else {
				next[pos++] = 0; //跳转到最左边，没找到
			}
		}
		return next;
	}

	public static void main(String[] args) {
		String str = "abcabcababaccc";
		String match = "ababa";
		System.out.println(getIndexOf(str, match));

	}

}
