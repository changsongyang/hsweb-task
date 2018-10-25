package redisson.bug.test;

import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class RedissonSubErrorTest {

    static RedissonClient redissonClient = Redisson.create();
    static ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);


    public static RTopic<String> getRequestTopic() {
        return redissonClient.getTopic("request-topic");
    }

    public static RTopic<String> getResponseTopic(String msgId) {
        return redissonClient.getTopic("response-topic-" + msgId);
    }

    public static void consume(String msgId, Consumer<String> consumer) {
        RTopic<String> topic = getResponseTopic(msgId);
        topic.addListener((channel, msg) -> {
            consumer.accept(msg);

            //*********[删除本行可解决此问题]**********
            topic.removeAllListeners();
        });

    }

    public static void main(String[] args) {
        AtomicLong responseCounter = new AtomicLong();
        AtomicLong requestCounter = new AtomicLong();

        executorService.submit(() -> {
            try {
                getRequestTopic()
                        .addListener((channel, msg) -> {
                            System.out.print("accept [" + msg + "] publish result: ");
                            getResponseTopic(msg).publish("response-" + msg + "(" + responseCounter.incrementAndGet() + ")");
                            System.out.println("ok");
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        executorService.scheduleAtFixedRate(() -> {
            String msgId = UUID.randomUUID().toString();
            try {
                System.out.print("consume [" + msgId+"] times "+requestCounter.incrementAndGet());
                consume(msgId, msg->{

                });
                System.out.println(" ok");
                System.out.print("publish request [" + msgId+"]");
                getRequestTopic().publish(msgId);
                System.out.println(" ok");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 100, 100, TimeUnit.MILLISECONDS);
    }
}
