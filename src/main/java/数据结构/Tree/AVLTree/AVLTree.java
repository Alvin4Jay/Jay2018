package 数据结构.Tree.AVLTree;

/**
 *  AVL树
 */
public class AVLTree<T extends Comparable<? super T>> {
    //AVLNode
    static class AVLNode <T>{
        T key;  //值
        int height; //高度
        AVLNode<T> left; //左子树
        AVLNode<T> right;//右子树

        public AVLNode(T key, AVLNode<T> left, AVLNode<T> right) {
            this.key = key;
            this.left = left;
            this.right = right;
            this.height = 1;
        }
    }

    private AVLNode<T> root; //根节点

    public AVLTree(){
        root = null;
    }

    /**
     * 某个节点的高度
     * @param node 某个节点
     * @return 高度
     */
    public int height(AVLNode<T> node){
        return node==null ? 0 : node.height;
    }

    /**
     * 根节点高度
     * @return
     */
    public int height(){
        return height(root);
    }

    /**
     * 求最大值
     * @param a
     * @param b
     * @return
     */
    public int max(int a, int b){
        return a < b ? b : a;
    }

    /**
     * 前序遍历
     * @param node 节点
     */
    public void preOrder(AVLNode<T> node){
        if(node != null){
            System.out.print(node.key + " ");
            preOrder(node.left);
            preOrder(node.right);
        }
    }

    /**
     * 根节点前序遍历
     */
    public void preOrder(){
        preOrder(root);
    }


    /**
     * 中序遍历
     * @param node 节点
     */
    public void inOrder(AVLNode<T> node){
        if(node != null){
            inOrder(node.left);
            System.out.print(node.key + " ");
            inOrder(node.right);
        }
    }

    /**
     * 根节点中序遍历
     */
    public void inOrder(){
        inOrder(root);
    }

    /**
     * 后序遍历
     * @param node 节点
     */
    public void postOrder(AVLNode<T> node){
        if(node != null){
            postOrder(node.left);
            postOrder(node.right);
            System.out.print(node.key + " ");
        }
    }

    /**
     * 根节点后序遍历
     */
    public void postOrder(){
        postOrder(root);
    }

    /**
     * 查找(递归实现)
     * @param node 被查找的树
     * @param key 键值
     * @return 返回包含该值的AVLNode
     */
    public AVLNode<T> find(AVLNode<T> node, T key){
        if(node == null || key == null) return null; //返回条件1，没找到

        int cmp = key.compareTo(node.key);

        if(cmp < 0){
            return find(node.left, key);
        }else if(cmp > 0){
            return find(node.right, key);
        }else {
            return node; //返回条件2，找到
        }
    }

    /**
     * 查找，非递归实现
     * @param node 要查找的树
     * @param key 键
     * @return 找到的节点
     */
    public AVLNode<T> loopFind(AVLNode<T> node, T key){
        AVLNode<T> current = node;

        while(current != null){
            int cmp = key.compareTo(current.key);

            if(cmp < 0){
                current = current.left;
            }else if(cmp > 0){
                current = current.right;
            }else {
                return current; //找到
            }
        }
        return null; //没找到
    }

    /**
     * 最小值
     * @return min
     */
    public T minimum(){
        if(root == null) return null;

        return minimum(root).key;
    }

    private AVLNode<T> minimum(AVLNode<T> node){
        AVLNode<T> current = node;
        while(current.left != null){
            current = current.left;
        }
        return current;
    }

    /**
     * 最大值
     * @return max
     */
    public T maximum(){
        if(root == null) return null;

        return maximum(root).key;
    }
    private AVLNode<T> maximum(AVLNode<T> node){
        AVLNode<T> current = node;
        while(current.right != null){
            current = current.right;
        }
        return current;
    }

    /**
     * 左左旋转LL（从左往右旋转，顺时针）
     * @param node 被破坏者节点，需要调整
     * @return 返回调整后的根节点
     */
    private AVLNode<T> LLRotation(AVLNode<T> node){
        AVLNode<T> finalRoot = node.left; //左子为父
        node.left = finalRoot.right; //右孙变左孙
        finalRoot.right = node;  //父为右子

        //调整height
        node.height = max(height(node.left), height(node.right)) + 1;
        finalRoot.height = max(height(finalRoot.left), node.height) + 1;

        return finalRoot;
    }

    /**
     * 右右旋转RR（从右往左旋转，逆时针）
     * @param node 被破坏者节点，需要调整
     * @return 返回调整后的根节点
     */
    private AVLNode<T> RRRotation(AVLNode<T> node){
        AVLNode<T> finalRoot = node.right; //右子为父
        node.right = finalRoot.left; //左孙变右孙
        finalRoot.left = node;  //父为左子

        //调整height
        node.height = max(height(node.left), height(node.right)) + 1;
        finalRoot.height = max(height(finalRoot.right), node.height) + 1;

        return finalRoot;
    }

    /**
     * LR旋转，先RR旋转，再LL旋转
     * @param node 被破坏者节点，需要调整
     * @return 返回调整后的根节点
     */
    private AVLNode<T> LRRotation(AVLNode<T> node){
        node.left = RRRotation(node.left); //先右旋RR

        return LLRotation(node); //再左旋LL
    }

    /**
     * RL旋转，先LL旋转，再RR旋转
     * @param node 被破坏者节点，需要调整
     * @return 返回调整后的根节点
     */
    private AVLNode<T> RLRotation(AVLNode<T> node){
        node.right = LLRotation(node.right); //先左旋LL

        return RRRotation(node); //再右旋RR
    }

    /**
     * AVL树插入key
     * @param key 插入的键值
     */
    public void insert(T key){
        root = insert(root, key);
    }

    /**
     * 节点插入AVL树，返回根节点
     * @param tree 根节点
     * @param key 键
     * @return 根节点
     */
    private AVLNode<T> insert(AVLNode<T> tree, T key){
        if(tree == null){
            //新建节点，递归返回条件
            tree = new AVLNode<T>(key, null, null);
        }else {
            int cmp = key.compareTo(tree.key);

            if(cmp < 0){ //插入左子树
                tree.left = insert(tree.left, key);
                //插入节点后，如AVL树失去平衡，则进行相应的调节 二、
                if((height(tree.left)- height(tree.right)) == 2){
                    if(key.compareTo(tree.left.key) < 0){
                        tree = LLRotation(tree); //LL旋转   //每次插入，旋转最多执行一次
                    }else {
                        tree = LRRotation(tree); //LR旋转
                    }
                }
            }else if(cmp > 0){ //插入右子树
                tree.right = insert(tree.right, key);
                if((height(tree.right)-height(tree.left)) == 2){ //二、
                   if(key.compareTo(tree.right.key) < 0){
                       tree = RLRotation(tree);    //每次插入，旋转最多执行一次
                   }else {
                       tree = RRRotation(tree);
                   }
                }
            }else {
                System.out.println("键key已存在!!!"); //递归返回条件
            }
        }

        tree.height = max(height(tree.left), height(tree.right)) + 1; //更新树高 一、
        return tree;
    }

    /**
     * 删除Key节点
     * @param key 待删除节点key
     */
    public void delete(T key){ //先找，再删
        AVLNode<T> toBeDelete = null;
        if((toBeDelete=find(root, key)) != null){
            root = delete(root, toBeDelete);
        }
    }

    /**
     * 删除节点，返回根节点
     * @param tree 根节点
     * @param toBeDelete 要删除的节点
     * @return 返回根节点
     */
    private AVLNode<T> delete(AVLNode<T> tree, AVLNode<T> toBeDelete){
        //根结点为空或者要删除的节点为空
        if(tree == null || toBeDelete == null) return tree;

        int cmp = toBeDelete.key.compareTo(tree.key);
        if(cmp < 0){ //待删除节点在左子树中
            tree.left = delete(tree.left, toBeDelete);
            //删除节点后，AVL树可能失去平衡
            if((height(tree.right) - height(tree.left)) == 2){
                AVLNode<T> right = tree.right;
                if(height(right.left) > height(right.right)){  //每次删除，旋转最多执行一次
                    tree = RLRotation(tree);
                }else {
                    tree = RRRotation(tree);
                }
            }
        }else if(cmp > 0){ //待删除节点在右子树中
            tree.right = delete(tree.right, toBeDelete);
            //AVL树在删除之后不平衡,调整
            if(height(tree.left) - height(tree.right) == 2){ //每次删除，旋转最多执行一次
                AVLNode<T> left = tree.left;
                if(height(left.left) > height(left.right)){
                    tree = LLRotation(tree);
                }else {
                    tree = LRRotation(tree);
                }
            }
        }else {  //找到要删除的节点tree
            //tree左右孩子均非空
            if(tree.left != null && tree.right != null){
                /*
                 * 如果tree左子树比右子树高，则第一步先找出左子树中最大节点
                 * 第二步 将该节点的值赋给tree
                 * 第三步 删除该最大节点
                 * 这类似于用"tree的左子树中最大节点"做"tree"的替身；
                   采用这种方式的好处是：删除"tree的左子树中最大节点"之后，AVL树仍然是平衡的。
                 */
                if(height(tree.left) > height(tree.right)){
                    AVLNode<T> leftMax = maximum(tree.left); //左子树最右边
                    tree.key = leftMax.key;
                    tree.left = delete(tree.left, leftMax);
                }else {
                    // 如果tree的左子树不比右子树高(即它们相等，或右子树比左子树高1)
                    // 则(01)找出tree的右子树中的最小节点
                    //   (02)将该最小节点的值赋值给tree。
                    //   (03)删除该最小节点。
                    // 这类似于用"tree的右子树中最小节点"做"tree"的替身；
                    // 采用这种方式的好处是：删除"tree的右子树中最小节点"之后，AVL树仍然是平衡的。
                    AVLNode<T> rightMin = minimum(tree.right);  //右子树最左边
                    tree.key = rightMin.key;
                    tree.right = delete(tree.right, rightMin);
                }
            }else { //只有一个子节点或者都为空
                tree = tree.left!=null? tree.left : tree.right; //tree可能为null
            }
        }

        //tree可能为null
        if(tree != null) tree.height = max(height(tree.left), height(tree.right)) + 1; //更新树高

        return tree;
    }

    /*
     * 打印"二叉查找树"
     *
     * key        -- 节点的键值
     * direction  --  0，表示该节点是根节点;
     *               -1，表示该节点是它的父结点的左孩子;
     *                1，表示该节点是它的父结点的右孩子。
     */
    private void print(AVLNode<T> tree, T key, int direction) {
        if(tree != null) {
            if(direction==0)    // tree是根节点
                System.out.printf("%2d is root\n", tree.key, key);
            else                // tree是分支节点
                System.out.printf("%2d is %2d's %6s child\n", tree.key, key, direction==1?"right" : "left");

            print(tree.left, tree.key, -1);
            print(tree.right,tree.key,  1);
        }
    }

    public void print() {
        if (root != null)
            print(root, root.key, 0);
    }

}
