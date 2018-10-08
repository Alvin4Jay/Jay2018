package 数据结构.Tree.BinarySearchTree;

/**
 * 二叉搜索树BST结点
 */
public class BinarySearchTreeNode<T>{
	private T data;
	private BinarySearchTreeNode<T> parent;  //父节点
	private BinarySearchTreeNode<T> left;    //左子结点
	private BinarySearchTreeNode<T> right;   //右子节点
	private int state;  //递归状态(非递归遍历时表示一个节点运行到的状态)

	public BinarySearchTreeNode(T data) {
		this.data = data;
		this.left = null;
		this.right = null;
		this.parent = null;
		this.state = 0;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public BinarySearchTreeNode<T> getLeft() {
		return left;
	}

	public void setLeft(BinarySearchTreeNode<T> left) {
		this.left = left;
	}

	public BinarySearchTreeNode<T> getRight() {
		return right;
	}

	public void setRight(BinarySearchTreeNode<T> right) {
		this.right = right;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	public BinarySearchTreeNode<T> getParent() {
		return parent;
	}

	public void setParent(BinarySearchTreeNode<T> parent) {
		this.parent = parent;
	}
}
