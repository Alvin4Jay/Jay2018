package JDKSourceFileAnalysis.JUCLocks.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Unsafe.allocateInstance(java.lang.Class)
 * @author xuanjian.xuwj
 */
public class UnsafeAllocateInstanceTest {
	public static void main(String[] args) throws Exception {
		// 反射创建Unsafe实例
		Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Unsafe unsafe = (Unsafe) theUnsafe.get(Unsafe.class);

		// 实例化构造器私有的类
		User user = (User) unsafe.allocateInstance(User.class);
		user.setAge(1);
		user.setName("Jay");
		System.out.println(user);
		System.out.println(unsafe.objectFieldOffset(User.class.getDeclaredField("age")));
	}

	private static class User {
		private String name;
		private int age;
		private static int count = 1;

		/** 构造器私有化 */
		private User() {
			System.out.println("initializing...");
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		@Override
		public String toString() {
			return "User{" + "name='" + name + '\'' + ", age=" + age + '}';
		}
	}
}
