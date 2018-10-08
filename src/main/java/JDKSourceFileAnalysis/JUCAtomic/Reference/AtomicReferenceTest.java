package JDKSourceFileAnalysis.JUCAtomic.Reference;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Jay on 2017/6/15.
 */
public class AtomicReferenceTest {

    public static void main(String[] args) {
        Person p1 = new Person(101);
        Person p2 = new Person(102);

        AtomicReference<Person> reference = new AtomicReference<>(p1); //p1引用

        System.out.println("p1: " + reference.get());

        reference.compareAndSet(p1, p2); //原子操作设置为p2引用

        Person p3 = reference.get(); //获取引用
        System.out.println("p3: " + p3);


        System.out.println("p3==p1: " + (p3==p1));
        System.out.println("p3==p2: " + (p3==p2));


    }


    static class Person {
        volatile long id;

        public Person(long id) {
            this.id = id;
        }

        public String toString() {
            return "id:" + id;
        }
    }
}


