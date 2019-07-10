package JDKSourceFileAnalysis.completablefuture;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * {@link CompletableFuture} 使用实例
 *
 * @author xuanjian
 */
public class CompletableFutureTest {

    /* ------主动完成计算join/get/getNow/complete/completeExceptionally------ */

    private static void test01() throws Exception {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            int i = 1 / 0;
            return 100;
        });
        // join()/get()抛出不同的异常
        future.join();
//        future.get();
    }

    private static void test02() throws Exception {
        final CompletableFuture<Integer> f = compute(); // 未完成的future
        class Client extends Thread {
            CompletableFuture<Integer> f;

            public Client(String name, CompletableFuture<Integer> f) {
                super(name);
                this.f = f;
            }

            @Override
            public void run() {
                try {
                    System.out.println(this.getName() + ": " + f.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        new Client("Client1", f).start();
        new Client("Client2", f).start();
        System.out.println("waiting");
        TimeUnit.SECONDS.sleep(2);
        // 主动完成future
        f.complete(1000);
//        f.completeExceptionally(new RuntimeException("test exception"));
    }

    // 未完成的future
    private static CompletableFuture<Integer> compute() {
        return new CompletableFuture<>();
    }

    /* ------创建CompletableFuture supplyAsync------ */

    private static void test03() {
        CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> {
            // 长时间的异步计算
            return ".00";
        });
    }

    /* ------计算结果完成时的处理whenComplete------*/

    private static void test04() throws Exception {
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> {
            System.out.println("f1 done...");
        });
        CompletableFuture<Double> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 1.0;
        });

        f2.whenComplete((result, e) -> {
            System.out.println("result: " + result);
        });

        System.in.read();
    }

    private static class CompletableFutureInnerTest {
        private static Random rand = new Random();
        private static long t = System.currentTimeMillis();

        private static int getMoreData() {
            System.out.println("begin to start compute");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("end to start compute. passed " + (System.currentTimeMillis() - t) / 1000 + " seconds");
            return rand.nextInt(1000);
        }

        public static void test05() throws Exception {
            CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(CompletableFutureInnerTest::getMoreData);
            CompletableFuture<Integer> f2 = f1.whenComplete((v, e) -> {
                System.out.println(v);
                System.out.println(e);
            });
            System.out.println(f2.get()); // 返回与f1相同的结果
            System.out.println("end...");
        }
    }

    /* ------转换thenApplyAsync/thenApply------ */

    private static void test06() throws Exception {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> 100)
                .thenApplyAsync(i -> i * 10).thenApply(Object::toString);
        System.out.println(future.get());
    }

    /* ------纯消费(执行Action) thenAccept/thenAcceptBoth/thenRun------*/

    private static void test07() throws Exception {
        CompletableFuture<Void> f = CompletableFuture.supplyAsync(() -> 100).thenAccept(System.out::println);
        System.out.println(f.get());
    }

    private static void test08() throws Exception {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> 100)
                .thenAcceptBoth(CompletableFuture.completedFuture(10), (x, y) -> System.out.println(x * y));
        System.out.println(future.get());
    }

    private static void test09() throws Exception {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> 100)
                .thenRun(() -> System.out.println("finished"));
        System.out.println(future.get());
    }

    /* ------组合thenCompose/thenCombine------*/

    /**
     * {@link CompletableFuture#thenCompose(Function)} future之间有先后依赖顺序
     */
    private static void test10() throws Exception {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> 100)
                .thenCompose(i -> CompletableFuture.supplyAsync(() -> String.valueOf(i * 10)));
        System.out.println(future.get());
    }

    private static void test11() {
        final String original = "message";
        CompletableFuture<String> cf = CompletableFuture
                .completedFuture(original)
                .thenApply(String::toUpperCase)
                .thenCompose(upper -> CompletableFuture
                        .completedFuture(original)
                        .thenApply(String::toLowerCase)
                        .thenApply(s -> upper + s)
                );
        System.out.println("MESSAGEmessage: " + cf.join());
    }

    /**
     * {@link CompletableFuture#thenCombine(CompletionStage, BiFunction)} 并行执行，future之间没有先后依赖顺序
     */
    private static void test12() throws Exception {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> 100);
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> "abc");
        CompletableFuture<String> f = f1.thenCombine(f2, (x, y) -> x + "-" + y);
        System.out.println(f.get());
    }

    /* ------Either applyToEither------*/

    private static void test13() throws Exception {
        Random random = new Random();
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000 + random.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 100;
        });
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000 + random.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 200;
        });
        // 谁先完成，先用谁
        CompletableFuture<String> f = f1.applyToEither(f2, Object::toString);
        System.out.println(f.get());
    }

    /* ------anyOf 与 allOf------ */

    private static void test14() throws Exception {
        Random random = new Random();
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000 + random.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 100;
        });
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000 + random.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 200;
        });
        // 谁先完成，就完成计算，返回该结果
        CompletableFuture<Object> f = CompletableFuture.anyOf(f1, f2);
        System.out.println(f.get());
    }

    public static void main(String[] args) throws Exception {

//         test01();
//         test02();
//         test03();
//         test04();
//        CompletableFutureInnerTest.test05();
//         test06();
//         test07();
//         test08();
//         test09();
//         test10();
//         test11();
//         test12();
//         test13();
        test14();
    }

}
