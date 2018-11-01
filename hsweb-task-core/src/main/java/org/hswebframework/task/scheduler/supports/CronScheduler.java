package org.hswebframework.task.scheduler.supports;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static com.cronutils.model.CronType.QUARTZ;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class CronScheduler extends AbstractScheduler {

    private ExecutionTime executionTime;

    private Cron     cron;
    private CronType cronType;

    public CronScheduler(ScheduledExecutorService executorService) {
        super(executorService);
    }

    public CronScheduler(String cron, ScheduledExecutorService executorService) {
        super(executorService);
        this.cronType = QUARTZ;
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(cronType));
        this.executionTime = ExecutionTime.forCron(this.cron = parser.parse(cron).validate());
    }

    @Override
    protected void initFromConfiguration(Map<String, Object> configuration) {
        String cron = (String) configuration.get("cron");
        String type = (String) configuration.getOrDefault("cronType", "QUARTZ");
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(this.cronType = CronType.valueOf(type.toUpperCase())));
        this.executionTime = ExecutionTime.forCron(this.cron = parser.parse(cron).validate());
    }

    @Override
    protected long getNextFireTimestamp() {
        return executionTime
                .nextExecution(ZonedDateTime.now())
                .map(ChronoZonedDateTime::toInstant)
                .map(Instant::toEpochMilli)
                .orElse(-1L);
    }

    @Override
    public String getType() {
        return CronSchedulerProvider.TYPE;
    }

    @Override
    public List<Long> getNextExecuteTime(int times) {
        List<Long> time = new ArrayList<>(times);
        ZonedDateTime lastTime = ZonedDateTime.now();
        for (int i = 0; i < times; i++) {
            ZonedDateTime nextTime = lastTime = executionTime.nextExecution(lastTime).orElse(null);
            if (null == nextTime) {
                break;
            }
            time.add(nextTime.toInstant().toEpochMilli());
        }
        return time;
    }

    @Override
    public Map<String, Object> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("type", getType());
        config.put("cron", cron);
        config.put("cronType", cronType.name().toUpperCase());
        return config;
    }

    @Override
    public String toString() {
        return "cron scheduler: [" + cronType + ":" + cron + "] : "
                + CronDescriptor.instance(Locale.CHINA).describe(cron);
    }
}
