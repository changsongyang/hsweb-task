package org.hswebframework.task;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
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
    public <T> Future<?> doTryAsync(Callable<T> callable,
                                    long time,
                                    TimeUnit timeUnit,
                                    Function<Throwable, T> onError,
                                    BiConsumer<T, Boolean> consumer) {
        AtomicBoolean isCancel = new AtomicBoolean();

        AtomicReference<Future<T>> realFuture = new AtomicReference<>();

        Future<?> target = executorService.submit(() -> {
            if (isCancel.get()) {
                return;
            }
            Future<T> future = executorService.submit(callable);
            realFuture.set(future);
            T val;
            boolean timeout;
            try {
                val = future.get(time, timeUnit);
                timeout = false;
            } catch (Throwable e) {
                future.cancel(true);
                val = onError.apply(e);
                timeout = e instanceof TimeoutException;
            }
            consumer.accept(val, timeout);
        });
        return new Future<Object>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                isCancel.set(true);

                if (realFuture.get() != null) {
                    return realFuture.get().cancel(mayInterruptIfRunning);
                }
                // consumer.accept(onError.apply(new CancellationException("job canceled")), false);
                return target.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return realFuture.get().isCancelled();
            }

            @Override
            public boolean isDone() {
                return realFuture.get().isDone();
            }

            @Override
            public Object get() throws InterruptedException, ExecutionException {
                return realFuture.get().get();
            }

            @Override
            public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return realFuture.get().get(timeout, unit);
            }
        };

    }
}
