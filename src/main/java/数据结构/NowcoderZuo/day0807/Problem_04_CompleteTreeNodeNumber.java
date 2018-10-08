package 数据结构.NowcoderZuo.day0807;

/**
 * 统计完全二叉树的节点个数
 */
public class Problem_04_CompleteTreeNodeNumber {

	public static class Node {
		public int value;
		public Node left;
		public Node right;

		public Node(int data) {
			this.value = data;
		}
	}

	/**
	 * 求完全二叉树的节点个数
	 */
	public static int nodeNum(Node head) {
		if (head == null) {
			return 0;
		}
		return bs(head, 1, mostLeftLevel(head, 1));
	}

	/**
	 * 以node为头结点的完全二叉胡共有多少节点
	 * @param node 头结点
	 * @param l node在l层
	 * @param h 整个二叉树总共h层
	 * @return
	 */
	public static int bs(Node node, int l, int h) {
		if (l == h) {
			return 1; //叶结点
		}
		if (mostLeftLevel(node.right, l + 1) == h) {
			return (1 << (h - l)) + bs(node.right, l + 1, h); //左子树和根节点清算，递归求右子树
		} else {
			return (1 << (h - l - 1)) + bs(node.left, l + 1, h);//右子树和根节点清算，递归求左子树
		}
	}

	/**
	 * 以node为头节点的完全二叉树在level层，一共有几层
	 * @param node
	 * @param level
	 * @return
	 */
	public static int mostLeftLevel(Node node, int level) {
		while (node != null) {
			level++;
			node = node.left;
		}
		return level - 1;
	}

	public static void main(String[] args) {
		Node head = new Node(1);
		head.left = new Node(2);
		head.right = new Node(3);
		head.left.left = new Node(4);
		head.left.right = new Node(5);
		head.right.left = new Node(6);
		//head.right.right = new Node(7);
		System.out.println(nodeNum(head));

	}

}
