package com.xxl.job.core.executor.impl;

import cn.hutool.json.JSONUtil;
import com.xxl.job.core.handler.annotation.EnableSyncJob;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.util.XxlJobBeanUtil;
import com.xxl.job.core.util.XxlJobServerHttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
  *                  ,;,,;
  *                ,;;'(    社
  *      __      ,;;' ' \   会
  *   /'  '\'~~'~' \ /'\.)  主
  * ,;(      )    /  |.     义
  *,;' \    /-.,,(   ) \    码
  *     ) /       ) / )|    农
  *     ||        ||  \)     
  *     (_\       (_\
  *
  * @projectName xxl-job
  * @className SyncJobSpringExecutor
  * @author Barcke
  * @date 2021/1/18 17:57
  * @version 1.0
  * @slogan: 源于生活 高于生活
  * @description: 
  **/
@ConditionalOnProperty(name = "xxl.job.enable", havingValue = "true", matchIfMissing = true)
public class SyncJobSpringExecutor implements ApplicationContextAware, SmartInitializingSingleton {

    private static final Logger logger = LoggerFactory.getLogger(SyncJobSpringExecutor.class);

    @Value("${server.servlet.context-path}")
    private String contentPath;

    @Override
    public void afterSingletonsInstantiated() {

        //避免客户端有多个，通过redis做一个分布式的锁避免重复注册
        String redisKey = "xxl-job-sync-info:xxl:y" + contentPath;

        StringRedisTemplate stringRedisTemplate = applicationContext.getBean(StringRedisTemplate.class);

        String syncId = stringRedisTemplate.opsForValue().get(redisKey);

        if (!StringUtils.isEmpty(syncId)) {
            return;
        }

        stringRedisTemplate.opsForValue().set(redisKey,"1",10, TimeUnit.MINUTES);

        logger.info("同步XXL-JOB任务开始");

        Object object = applicationContext
                .getBeansWithAnnotation(EnableSyncJob.class)
                .entrySet()
                .stream()
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Not found EnableSyncJob Class"))
                .getValue();

        EnableSyncJob enableSyncJob = AnnotationUtils.findAnnotation(object.getClass(), EnableSyncJob.class);

        logger.info("获取到注解信息enableSyncJob:{}",JSONUtil.toJsonStr(enableSyncJob));

        String url = applicationContext.getEnvironment().getProperty("xxl.job.admin.addresses");

        if (StringUtils.isEmpty(url)) {
            throw new NullPointerException("Please check the server address ---》 xxl.job.admin.addresses");
        }

        XxlJobServerHttpUtil xxlJobServerHttpUtil = null;

        try {
            xxlJobServerHttpUtil = new XxlJobServerHttpUtil(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * 初始化执行器
         */
        Integer groupId = this.initActuatorData(xxlJobServerHttpUtil);

        /**
         * 同步任务
         */
        this.syncJobInfo(groupId,xxlJobServerHttpUtil,enableSyncJob);

        logger.info("同步XXL-JOB任务结束");

        stringRedisTemplate.delete(redisKey);
    }

    /**
     * 同步任务
     * @param groupId
     * @param xxlJobServerHttpUtil
     * @param enableSyncJob
     */
    private void syncJobInfo(Integer groupId, XxlJobServerHttpUtil xxlJobServerHttpUtil, EnableSyncJob enableSyncJob) {
        List<XxlJobBeanUtil.Data> mapLists = XxlJobBeanUtil.getMapLists(applicationContext);

        for (XxlJobBeanUtil.Data mapList : mapLists) {
            for (Map.Entry<Method, XxlJob> map : mapList.getMethod().entrySet()) {
                XxlJob xxlJob = map.getValue();

                //如果任务不存在则新增
                if (xxlJobServerHttpUtil.getJobInfoIdByHandlerName(xxlJob.value(),groupId) == null) {

                    String alarmEmail = xxlJob.alarmEmail();

                    if (StringUtils.isEmpty(alarmEmail)) {
                        alarmEmail = enableSyncJob.alarmEmail();
                    }

                    int executorFailRetryCount = xxlJob.executorFailRetryCount();

                    if (executorFailRetryCount == -1) {
                        executorFailRetryCount = enableSyncJob.executorFailRetryCount();
                    }

                    String author = xxlJob.author();

                    if (StringUtils.isEmpty(author)) {
                        author = enableSyncJob.author();
                    }

                    String cron = xxlJob.cron();

                    int executorTimeout = xxlJob.executorTimeout();
                    String jobDesc = xxlJob.jobDesc();

                    XxlJobServerHttpUtil.JobInfoData jobInfoData = new XxlJobServerHttpUtil.JobInfoData(
                            groupId, jobDesc, cron, xxlJob.value()
                            , executorTimeout, executorFailRetryCount, author, alarmEmail
                    );

                    if (xxlJobServerHttpUtil.saveJobInfo(
                            jobInfoData
                    )) {
                        logger.info("新增任务成功jobInfoData：{}", JSONUtil.toJsonStr(jobInfoData));
                    }else {
                        logger.info("任务已存在handlerName:{}",xxlJob.value());
                    }
                }

            }
        }
    }

    /**
     * 初始化执行器
     * @param xxlJobServerHttpUtil
     */
    private Integer initActuatorData(XxlJobServerHttpUtil xxlJobServerHttpUtil) {
        XxlJobServerHttpUtil.ActuatorData actuatorData = new XxlJobServerHttpUtil.ActuatorData(applicationContext);

        Integer groupId = xxlJobServerHttpUtil.getGroupIdByAppName(actuatorData.getAppName());

        //判断执行器是否存在
        if (groupId == null) {

            //不存在则新建
            xxlJobServerHttpUtil.saveActuator(actuatorData);

            logger.info("执行器不存在，新建执行器actuatorData：{}",JSONUtil.toJsonStr(actuatorData));

            return xxlJobServerHttpUtil.getGroupIdByAppName(actuatorData.getAppName());
        }

        logger.info("执行器存在返回执行器ID:{}",groupId);

        return groupId;
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
