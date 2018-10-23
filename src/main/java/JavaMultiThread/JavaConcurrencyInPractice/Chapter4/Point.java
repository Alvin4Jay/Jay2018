package JavaMultiThread.JavaConcurrencyInPractice.Chapter4;

import net.jcip.annotations.Immutable;

/**
 * 不可变Point类
 */
@Immutable
public class Point {
    public final int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
