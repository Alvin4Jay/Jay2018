package 数据结构.NowcoderZuo.day0814;

/**
 * 折纸算法
 */
public class Problem_06_PaperFolding {

	public static void printAllFolds(int N) {
		printProcess(1, N, true);
	}

	/**
	 * 中序遍历打印
	 * @param i 当前在i层
	 * @param N 共有n层
	 * @param down true为下折痕
	 */
	public static void printProcess(int i, int N, boolean down) {
		if (i > N) {
			return;
		}
		printProcess(i + 1, N, true); //下折痕
		System.out.println(down ? "down " : "up ");
		printProcess(i + 1, N, false); //上折痕
	}

	public static void main(String[] args) {
		int N = 4;
		printAllFolds(N);

	}
}
