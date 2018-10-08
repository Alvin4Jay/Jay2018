package JavaMultiThread.Chapter5.Cache;

/**
 * 计算
 */
public interface Computable <A, V>{
   V compute(A arg) throws InterruptedException;
}
