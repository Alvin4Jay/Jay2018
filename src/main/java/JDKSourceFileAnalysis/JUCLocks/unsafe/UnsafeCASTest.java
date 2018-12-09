package JDKSourceFileAnalysis.JUCLocks.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Unsafe CAS
 *
 * https://mp.weixin.qq.com/s?__biz=MzU0MzQ5MDA0Mw==&mid=2247484510&idx=2&sn=621f1c67603c8ada3dec2ab0467c65c5&chksm=fb0beecacc7c67dcd7466a246722da66792060ed079dcceb83dcf8e9c9ad515919cef1dc0641&mpshare=1&scene=1&srcid=#rd
 *
 * @author xuanjian.xuwj
 */
public class UnsafeCASTest {
	public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		// 反射获取Unsafe实例
		Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Unsafe unsafe = (Unsafe) theUnsafe.get(Unsafe.class);

		// 实例化构造器私有的类 allocateInstance
		Player player = (Player) unsafe.allocateInstance(Player.class);
		player.setName("Jack");
		player.setAge(10);
		for (Field fd : Player.class.getDeclaredFields()) {
			// 对象Field偏移
			System.out.println("[" +fd.getName() + "]对应的内存偏移地址: " + unsafe.objectFieldOffset(fd));
		}

		Field name = Player.class.getDeclaredField("name");
		long nameOffset = unsafe.objectFieldOffset(name);
		Field age = Player.class.getDeclaredField("age");
		long ageOffset = unsafe.objectFieldOffset(age);
		// CAS compareAndSwapInt
		System.out.println(unsafe.compareAndSwapInt(player, ageOffset, 10, 20));
		System.out.println("age修改后的值:" + player.getAge());

		unsafe.putOrderedInt(player, ageOffset, player.getAge() + 2);
		System.out.println("age修改后的值:" + player.getAge());

		unsafe.putObjectVolatile(player, nameOffset, "tom");
		System.out.println("name:" + player.getName());
		System.out.println("name:" + unsafe.getObjectVolatile(player, nameOffset));
	}

	private static class Player {
		private String name;
		private int age;

		private Player() {
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
	}

}
