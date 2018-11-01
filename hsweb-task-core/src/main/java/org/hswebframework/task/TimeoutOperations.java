package org.hswebframework.task;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface TimeoutOperations {


    <T> Future<?> doTryAsync(Callable<T> callable,
                          long time,
                          TimeUnit timeUnit,
                          Function<Throwable, T> onError,
                          BiConsumer<T, Boolean> consumer);

    <T> T doTrySync(Callable<T> callable,
                    long time,
                    TimeUnit timeUnit,
                    BiFunction<Throwable, Boolean, T> onError);
}
