package JDKSourceFileAnalysis.completablefuture;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link CompletableFuture} Util
 *
 * @author xuanjian
 */
public class CompletableFutureUtil {

    /**
     * 将多个CompletableFuture组合成一个CompletableFuture，这个组合后的CompletableFuture的
     * 计算结果是个List,它包含前面所有的CompletableFuture的计算结果
     *
     * @param futures {@link CompletableFuture<T>}
     * @param <T>     Type Parameter
     * @return {@link CompletableFuture<List<T>>}
     */
    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return allDoneFutures.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }

    /**
     * 将多个CompletableFuture组合成一个CompletableFuture，这个组合后的CompletableFuture的
     * 计算结果是个List,它包含前面所有的CompletableFuture的计算结果
     *
     * @param futures {@link CompletableFuture<T>}
     * @param <T>     Type Parameter
     * @return {@link CompletableFuture<List<T>>}
     */
    public static <T> CompletableFuture<List<T>> sequence(Stream<CompletableFuture<T>> futures) {
        List<CompletableFuture<T>> futureList = futures.filter(Objects::nonNull).collect(Collectors.toList());
        return sequence(futureList);
    }

    /**
     * 实现JDK {@code Future<T>} 与 {@code CompletableFuture<T>}的转换
     *
     * @param future   {@link Future}
     * @param executor {@link Executor}
     * @param <T>      Type Parameter
     * @return {@link CompletableFuture<T>}
     */
    public static <T> CompletableFuture<T> toCompletable(Future<T> future, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

}
