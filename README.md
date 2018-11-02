# 任务模块
[![Maven Central](https://img.shields.io/maven-central/v/org.hswebframework/hsweb-task.svg)](http://search.maven.org/#search%7Cga%7C1%7Corg.hswebframework/hsweb-task)
[![Build Status](https://travis-ci.org/hs-web/hsweb-task.svg?branch=master)](https://travis-ci.org/hs-web/hsweb-task)
[![codecov](https://codecov.io/gh/hs-web/hsweb-task/branch/master/graph/badge.svg)](https://codecov.io/gh/hs-web/hsweb-task)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)



提供任务配置,调度,监控. 支持单节点,集群,分布式部署.支持多种任务配置方式: 
注解, jar包, 脚本(js,groovy)

# 单点模式

引入依赖
```xml
<dependency>
    <groupId>org.hswebframework</groupId>
    <artifactId>hsweb-task-spring-boot-starter</artifactId>
    <version>${hsweb.task.version}</version>
</dependency>
```

```java
 @SpringBootApplication
 @EnableTaskWorker
 @EnableTaskScheduler
 public class Application{
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
         
    //使用注解方式定义一个任务,并指定调度规则
    @Job(id="test.job",name="测试任务")
    @Scheduled(cron="0 0/1 * * * ?") //支持spring的Scheduled注解
    public void myJob(){
        System.out.println("执行任务");
    }
 }
```
 
# 分布式模式
 
在worker和scheduler都引入依赖

```xml
<dependency>
    <groupId>org.hswebframework</groupId>
    <artifactId>hsweb-task-spring-boot-starter</artifactId>
    <version>${hsweb.task.version}</version>
</dependency>
<dependency>
    <groupId>org.hswebframework</groupId>
    <artifactId>hsweb-task-cluster</artifactId>
    <version>${hsweb.task.version}</version>
</dependency>
```
 
application.yml

```yaml
hsweb: 
    task:
      cluster: #集群管理配置,默认使用redis进行集群管理
        redis:
          hosts: redis://127.0.0.1:8761
          database: 1
      worker:
       # id: ${spring.application.name}-${HOSTNAME} #默认使用此ID,不同的节点id必须不同.
        name: worker-node-1
        groups: ${spring.application.name} #worker将执行这些分组的任务
        client-group: ${spring.application.name} #worker作为client自动提交任务时,将使用此分组,必须为groups属性的子集
```

 ## 定义worker
 
 ```java
 @SpringBootApplication
 @EnableTaskWorker
 public class Application{
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
         
    //使用注解方式定义一个任务,并指定调度规则
    @Job(id="test.job",name="测试任务")
    @Scheduled(cron="0 0/1 * * * ?") //支持spring的Scheduled注解
    public void myJob(){
        System.out.println("执行任务");
    }
 }
```

## 定义scheduler

```java
 @SpringBootApplication
 @EnableTaskScheduler
 public class Application{
     public static void main(String[] args) {
         SpringApplication.run(Application.class);
     }
 }

```

application.yml
```yaml
hsweb: 
    task:
      cluster:
        redis:
          address: redis://127.0.0.1:8761
          database: 1
      scheduler:
#        id: ${spring.application.name}-${HOSTNAME} #默认使用此ID
```