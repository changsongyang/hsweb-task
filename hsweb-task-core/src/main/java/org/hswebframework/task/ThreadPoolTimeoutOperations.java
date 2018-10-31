package org.hswebframework.task;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class ThreadPoolTimeoutOperations implements TimeoutOperations {

    private ExecutorService executorService;

    public ThreadPoolTimeoutOperations(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public <T> T doTrySync(Callable<T> callable, long time, TimeUnit timeUnit, BiFunction<Throwable, Boolean, T> onError) {
        Future<T> future = executorService.submit(callable);
        try {
            return future.get(time, timeUnit);
        } catch (Throwable e) {
            future.cancel(true);
            return onError.apply(e, e instanceof TimeoutException);
        }
    }

    @Override
    public <T> void doTryAsync(Callable<T> callable,
                               long time,
                               TimeUnit timeUnit,
                               Function<Throwable, T> onError,
                               BiConsumer<T, Boolean> consumer) {
        executorService.execute(() -> {
            Future<T> future = executorService.submit(callable);
            try {
                consumer.accept(future.get(time, timeUnit), false);
            } catch (Throwable e) {
                future.cancel(true);
                consumer.accept(onError.apply(e), e instanceof TimeoutException);
            }
        });
    }
}
