package JDKSourceFileAnalysis.JUCAtomic.Reference;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 *  AtomicLongFieldUpdaterTest
 */
public class AtomicLongFieldUpdaterTest {
    public static void main(String[] args) {
        //获取Class对象
        Class<Person> clazz = Person.class;

        //创建updater对象，传递参数是“class对象”和“long类型在类中对应的名称”
        AtomicLongFieldUpdater<Person> updater = AtomicLongFieldUpdater
                .newUpdater(clazz, "id");

        Person p = new Person(123456789L);

        //更新id字段的值
        updater.compareAndSet(p, 123456789L, 1000L);

        System.out.println(p.getId());

    }

    static class Person {
        volatile long id;
        public Person(long id) {
            this.id = id;
        }
        public void setId(long id) {
            this.id = id;
        }
        public long getId() {
            return id;
        }
    }
}
