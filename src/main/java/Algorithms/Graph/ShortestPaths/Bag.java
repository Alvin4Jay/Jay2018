package Algorithms.Graph.ShortestPaths;

import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *  背包---链表数据结构的实现
 */
public class Bag<Item> implements Iterable<Item>{
    private Node<Item> first;   //第一个元素
    private int N;   //背包内一共有几个元素

    private static class Node<Item>{
        Item item;
        Node<Item> next;
    }

    public Bag(){
        first = null;
        N = 0;
    }

    public boolean isEmpty(){
        return first == null;  // return N == 0;
    }

    public int size(){
        return N;
    }

    public void add(Item item){
        Node<Item> oldFirst = first;
        first = new Node<Item>();
        first.item = item;
        first.next = oldFirst;
        N ++;
    }

    @Override
   public Iterator<Item> iterator() {
        return new ListIterator<Item>(first);
    }

    private class ListIterator<Item> implements Iterator<Item>{
        private Node<Item> current ;

        public ListIterator(Node<Item> first){
            current = first;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public Item next() {
            if(isEmpty()) throw new NoSuchElementException();
            Item item = current.item;
            current = current.next;
            return item;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove");
        }
    }

    public static void main(String[] args) {
        Bag<String> bag = new Bag<String>();
        while(!StdIn.isEmpty()){
            String item = StdIn.readString();
            if(!item.equals("-")){
                bag.add(item);
            }
        }
        StdOut.println("size of bag = " + bag.size());
        for (String s : bag) {
            StdOut.println(s);
        }
    }

}
