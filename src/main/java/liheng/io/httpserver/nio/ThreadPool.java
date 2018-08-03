package liheng.io.httpserver.nio;

import java.util.concurrent.*;

public class ThreadPool {
    private static ExecutorService executorService;

    static {
        executorService = new ThreadPoolExecutor(50,
                200,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.DiscardPolicy());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                executorService.shutdown();
            }
        });
    }

    public static void execute(Runnable task) {
        executorService.execute(task);
    }
}
