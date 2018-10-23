package JavaMultiThread.JavaConcurrencyInPractice.Chapter5.Cache;

import java.util.Map;
import java.util.concurrent.*;

/**
 *   缓存最终实现
 */
public class Memoizer<A, V> implements Computable<A, V> {
    private final Map<A, Future<V>> cache = new ConcurrentHashMap<A, Future<V>>();
    private final Computable<A, V> c;

    public Memoizer(Computable<A, V> c) {
        this.c = c;
    }

    @Override
    public V compute(A arg) throws InterruptedException {
        while(true){
            Future<V> f = cache.get(arg);
            if(f == null){
                Callable<V> eval = new Callable<V>() {
                    @Override
                    public V call() throws Exception {
                        return c.compute(arg);
                    }
                };
                FutureTask<V> ft = new FutureTask<V>(eval);
                f = cache.putIfAbsent(arg, ft); //插入成功，返回null
                if(f == null) { //第一次缓存
                    f = ft;
                    ft.run();  //执行计算
                }
            }
            try{
                return f.get();
            }catch (CancellationException e) {
                cache.remove(arg, f);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
    }
}
