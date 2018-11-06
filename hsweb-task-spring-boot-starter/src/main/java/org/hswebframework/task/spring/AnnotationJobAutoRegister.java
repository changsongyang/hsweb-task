package org.hswebframework.task.spring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.TaskClient;
import org.hswebframework.task.job.JobDetail;
import org.hswebframework.task.spring.annotation.Job;
import org.hswebframework.task.spring.annotation.Scheduler;
import org.hswebframework.task.spring.configuration.TaskProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class AnnotationJobAutoRegister implements BeanPostProcessor, CommandLineRunner {

    @Autowired
    private TaskProperties taskProperties;

    @Autowired
    private TaskClient taskClient;

    private List<Runnable> allScheduler = new ArrayList<>();

    @Autowired
    private Executor executor;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class clazz = ClassUtils.getUserClass(bean);
        TaskProperties.WorkerProperties workerProperties = taskProperties.getWorker().validate();
        ReflectionUtils.doWithMethods(clazz, method -> {
            Job job = method.getAnnotation(Job.class);
            if (job != null) {
                String jobId = job.id();
                String jobName = job.name();
                JobDetail jobDetail = new JobDetail();
                jobDetail.setId(jobId);
                jobDetail.setName(jobName);
                jobDetail.setParallel(job.parallel());
                jobDetail.setJobType("java-method");
                jobDetail.setContent(clazz.getName() + "." + method.getName());
                jobDetail.setEnabled(true);
                jobDetail.setGroup(workerProperties.getClientGroup());

                jobDetail.setClientId(workerProperties.getId());
                jobDetail.setDescription("java-method-annotation-job");
                jobDetail.setExecuteTimeOut(TimeUnit.SECONDS.toMillis(job.timeoutSeconds()));
                jobDetail.setRetryWithout(Stream.of(job.retryWithout()).map(Class::getName).collect(Collectors.toList()));
                jobDetail.setVersion(job.version());
                jobDetail.setRetryTimes(job.errorRetryTimes());
                jobDetail.setRetryInterval(job.retryInterval());
                //适配spring调度注解
                Scheduled scheduled = method.getAnnotation(Scheduled.class);
                Map<String, Object> config = null;
                if (scheduled != null) {
                    config = createSpringScheduledAnnConfig(scheduled);
                } else {
                    //适配自定义注解
                    for (Annotation annotation : method.getAnnotations()) {
                        Scheduler scheduler = annotation.annotationType().getAnnotation(Scheduler.class);
                        if (null != scheduler) {
                            config = convertSchedulerConfiguration(annotation);
                            config.put("type", scheduler.type());
                            break;
                        }
                    }
                }
                taskClient.submitJob(jobDetail);

                if (config != null) {
                    Map<String, Object> finalConfig = config;
                    allScheduler.add(() -> taskClient.schedule(jobId, jobId, finalConfig));
                }
            }
        });
        return bean;
    }

    public Map<String, Object> createSpringScheduledAnnConfig(Scheduled scheduled) {
        Map<String, Object> map = new HashMap<>();
        if (!scheduled.cron().equals("")) {
            map.put("type", "cron");
            map.put("cron", scheduled.cron());
            map.put("cronType", "SPRING");
            return map;
        }
        if (scheduled.fixedRate() != -1) {
            map.put("type", "period");
            map.put("initialDelay", scheduled.initialDelay() == -1 ? 0 : scheduled.initialDelay());
            map.put("period", scheduled.fixedRate());
            map.put("timeUnit", TimeUnit.MILLISECONDS);
            return map;
        }
        throw new UnsupportedOperationException("just support cron and fixedRate in this version");

    }

    @SneakyThrows
    public Map<String, Object> convertSchedulerConfiguration(Annotation annotation) {
        return AnnotationUtils.getAnnotationAttributes(annotation);
    }

    @Override
    public void run(String... args) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
            log.debug("submit schedule request size:{}", allScheduler.size());
            allScheduler.forEach(Runnable::run);
        }, executor);
    }
}
