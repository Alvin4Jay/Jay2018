package JDKSourceFileAnalysis.Collection.WeakHashMap;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * WeakReference Test
 *
 * @author xuanjian
 */
public class WeakReferenceTest {
	public static void main(String[] args) throws InterruptedException {
		// 引用队列
		ReferenceQueue<Object> queue = new ReferenceQueue<>();
		WeakReference<Object> reference = new WeakReference<>(new Object(), queue);
		System.out.println(reference);

		System.gc();

		// 从引用队列中取出回收的虚引用对象WeakReference
		Reference<?> gcReference = queue.remove();
		System.out.println(gcReference == reference); // true
	}
}
