package com.xxl.job.core.handler.annotation;

import java.lang.annotation.*;

/**
 * annotation for method jobhandler
 *
 * @author xuxueli 2019-12-11 20:50:13
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface XxlJob {

    /**
     * jobhandler name
     */
    String value();

    /**
     * init handler, invoked when JobThread init
     */
    String init() default "";

    /**
     * destroy handler, invoked when JobThread destroy
     */
    String destroy() default "";

    /**
     * cron
     * @return
     */
    String cron();

    /**
     * 报警邮件
     */
    String alarmEmail() default "";

    /**
     * 失败重试次数
     */
    int executorFailRetryCount() default -1;

    /**
     * executorTimeout
     */
    int executorTimeout() default 0;

    /**
     * 负责人
     */
    String author() default "";

    /**
     * 任务描述
     */
    String jobDesc();

}
