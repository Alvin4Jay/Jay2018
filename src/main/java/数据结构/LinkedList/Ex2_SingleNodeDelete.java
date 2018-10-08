package 数据结构.LinkedList;

/**
 *  只能访问单个节点的删除，给定当前待删除的节点引用（采用覆盖的方法进行删除）
 *  给定带删除的节点，请执行删除操作，若该节点为尾节点，返回false，否则返回true
 */
public class Ex2_SingleNodeDelete {

    public boolean removeNode(ListNode pNode) {
        if(pNode == null){
            return false;
        }
        if(pNode.next == null){ //尾节点
            pNode = null;
            return false;
        }

        //当前节点的值更新为当前节点下一个节点的值，
        //当前节点的next更新为下一个节点的下一个节点
        pNode.val = pNode.next.val;
        pNode.next = pNode.next.next;
        return true;
    }

    static class ListNode {
        int val;
        ListNode next = null;

        ListNode(int val) {
            this.val = val;
        }
    }
}
