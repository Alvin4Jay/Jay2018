package 数据结构.Sword2Offer;

/**
 * 复杂链表的复制 O(n) O(1)
 */
public class P26_CopyComplexList {
    public RandomListNode Clone(RandomListNode pHead){
        if(pHead == null) return null;

        cloneNodes(pHead);
        connectSiblingNodes(pHead);
        return reconnectNodes(pHead);
    }

    //原链表插入节点
    public void cloneNodes(RandomListNode pHead){
        RandomListNode temp;
        RandomListNode current = pHead; //原链表目前的节点
        while(current != null){
            temp = new RandomListNode(current.label); //新插入节点
            RandomListNode next = current.next;
            current.next = temp;
            temp.next = next;
            current = next; //原链表下一个节点
        }

    }

    //设置随机指针random
    public void connectSiblingNodes(RandomListNode pHead){
        RandomListNode current = pHead; //原链表目前的节点
        while(current != null){
            RandomListNode copiedNode = current.next; //指向新插入的节点
            if(current.random == null){ //如果原链表随机指针为null
                copiedNode.random = null;
            }else {
                copiedNode.random = current.random.next;
            }
            current = copiedNode.next; //原链表下一个节点
        }
    }

    //拆散有上面两步得到的链表,分为两个链表
    public RandomListNode reconnectNodes(RandomListNode pHead){
        RandomListNode current = pHead;   //原链表头结点
        RandomListNode copiedCurrent = pHead.next; //新链表当前指针
        RandomListNode copiedPHead = pHead.next; //新链表头结点

        current.next = copiedCurrent.next; //初始化
        current = current.next;

        while(current != null){
            copiedCurrent.next = current.next;
            copiedCurrent  = copiedCurrent.next;
            current.next = copiedCurrent.next;
            current = copiedCurrent.next;
        }

        return copiedPHead;
    }

    public class RandomListNode {
        int label;
        RandomListNode next = null;
        RandomListNode random = null;

        RandomListNode(int label) {
            this.label = label;
        }
    }

    /*也可以使用HashMap将原始节点与新节点作为key value映射存在map中，
    方便设置random指针，空间换时间O(1)*/
}
