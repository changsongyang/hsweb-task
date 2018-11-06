package org.hswebframework.task.job;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hswebframework.task.TaskStatus;
import org.hswebframework.task.TaskFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 任务详情,用于配置任务信息以及执行策略等
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class JobDetail implements Serializable {

    /**
     * 任务id,全局唯一
     */
    private String id;

    /**
     * 任务标识,如: my-job
     */
    private String key;

    /**
     * 任务分组
     */
    private String group;

    /**
     * 任务版本,任务支持多个版本
     */
    private int version;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务类型,该字段用于content组合用于不同类型任务的支持,如: java-method,script,jar 等
     */
    private String jobType;

    /**
     * 任务内容,{@link this#jobType}字段不同,配置格式也不同,具体格式由{@link TaskFactory}进行处理
     *
     * @see TaskFactory
     */
    private String content;

    /**
     * 参数,执行任务的时候可指定参数
     */
    private Map<String, Object> parameters;

    /**
     * 任务执行的超时时间
     *
     * @see TaskStatus#timeout
     */
    private long executeTimeOut;

    /**
     * 重试时间,任务失败后重试次数,-1时为一直重试
     */
    private long retryTimes;

    /**
     * 不重试条件,为异常的类名
     */
    private List<String> retryWithout;

    /**
     * 每次重试的间隔
     */
    private long retryInterval;

    /**
     * 是否支持并行执行,如果不支持,同一个任务将顺序执行
     */
    private boolean parallel;

    /**
     * 是否启用
     */
    private boolean enabled;

}
