package com.xxl.job.core.util;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
  * @author Barcke
  * @date 2021/1/19 09:39
  * @version 1.0
  * @slogan: 源于生活 高于生活
  * @description: 
  **/
public class XxlJobBeanUtil {

    private static final Logger logger = LoggerFactory.getLogger(XxlJobBeanUtil.class);

    private static List<Data> mapLists;

    public synchronized static List<Data> getMapLists(ApplicationContext applicationContext) {
        if (Objects.isNull(mapLists)) {
            if (Objects.isNull(applicationContext)) {
                throw new NullPointerException("applicationContext is null");
            }

            String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);

            mapLists = Arrays.stream(beanDefinitionNames)
                    .map(beanDefinitionName -> {
                        Object bean = applicationContext.getBean(beanDefinitionName);

                        Map<Method, XxlJob> annotatedMethods = null;   // referred to ：org.springframework.context.event.EventListenerMethodProcessor.processBean
                        try {
                            annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                                    (MethodIntrospector.MetadataLookup<XxlJob>) method -> AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class));
                        } catch (Throwable ex) {
                            logger.error("xxl-job method-jobhandler resolve error for bean[" + beanDefinitionName + "].", ex);
                        }
                        if (annotatedMethods == null || annotatedMethods.isEmpty()) {
                            return null;
                        }

                        return new Data(annotatedMethods,bean);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return mapLists;
    }

    public static class Data{

        private Map<Method, XxlJob> method;

        private Object bean;

        public Data() {
        }

        public Data(Map<Method, XxlJob> method, Object bean) {
            this.method = method;
            this.bean = bean;
        }

        public Map<Method, XxlJob> getMethod() {
            return method;
        }

        public void setMethod(Map<Method, XxlJob> method) {
            this.method = method;
        }

        public Object getBean() {
            return bean;
        }

        public void setBean(Object bean) {
            this.bean = bean;
        }
    }
}
