package 数据结构.LinkedList;

/**
 *  复杂链表复制
 思路
 1.根据原始链表的每个结点N 创建对应的 N’。把 N’链接在N的后面
 2.设置复制出来的结点的 random。假设原始链表上的 N 的 random
 指向结点 S，那么其对应复制出来的 N’是 N的 next 指向的结点，同样 S’也是 S 的 next 指向的结点。
 3.把这个长链表拆分成两个链表。把奇数位置的结点用 next 链接起来就是原始链表，把偶数位置的结点用
 next 链接起来就是复制 出来的链表。
 */
public class Ex8_ComplexListCopy {
    public static ComplexListNode Clone(ComplexListNode head){
        if(head == null) return null;
        CloneNodes(head); //复制
        ConnectRandomNodes(head); //设置random指针
        return ReconnectNodes(head); //链表分离
    }
    //第一步 复制
    public static void CloneNodes(ComplexListNode head){
        ComplexListNode node = head;
        while (node != null){
            ComplexListNode temp = new ComplexListNode(node.val);
            temp.next = node.next;
            node.next = temp;
            node = temp.next;
        }
    }

    //第二步，random指针修改
    public static void ConnectRandomNodes(ComplexListNode head){
        ComplexListNode node = head;

        while(node != null){
            ComplexListNode cloneNode = node.next;
            if(node.random == null){
                cloneNode.random = null;
            }else {
                cloneNode.random = node.random.next;  //重要
            }
            node = cloneNode.next;
        }
    }

    //第三步 分离两条链表
    public static ComplexListNode ReconnectNodes(ComplexListNode head){
        ComplexListNode node = head;  //遍历过程中原链表的节点
        ComplexListNode cloneHead = head.next; //新链表头节点
        ComplexListNode cloneNode = cloneHead; //遍历过程中新链表的节点
        node.next = cloneNode.next; //第一次断开
        node = cloneNode.next;

        while(node != null){
            cloneNode.next = node.next;
            cloneNode = node.next;
            node.next = cloneNode.next;
            node = cloneNode.next;
        }
        return cloneHead; //返回复制后的链表首节点
    }

    static class ComplexListNode{
        int val;
        ComplexListNode next; //下一个
        ComplexListNode random; //随即指向，可能是null

        ComplexListNode(int val) {
            this.val = val;
        }
    }
}
