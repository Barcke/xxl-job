package com.xxl.job.core.handler.annotation;

import com.xxl.job.core.executor.impl.SyncJobSpringExecutor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
  * @author Barcke
  * @date 2021/1/18 17:51
  * @version 1.0
  * @slogan: 源于生活 高于生活
  * @description: 开启自动同步任务
  **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SyncJobSpringExecutor.class)
public @interface EnableSyncJob {

    /**
     * 报警邮箱 全局设置
     */
    String alarmEmail() default "";

    /**
     * 失败重试次数 全局设置
     */
    int executorFailRetryCount() default 0;

    /**
     * 负责人 全局设置
     */
    String author() default "";

}
