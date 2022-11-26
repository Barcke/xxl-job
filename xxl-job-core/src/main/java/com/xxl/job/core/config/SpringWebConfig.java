package com.xxl.job.core.config;

import com.xxl.job.core.web.controller.XxlJobController;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
  * @projectName xxl-job
  * @className SpringWebConfig
  * @author Barcke
  * @date 2022/6/6 下午3:32
  * @version 1.0
  * @slogan: 源于生活 高于生活
  * @description: 
  **/
@Configuration
@ComponentScan(basePackageClasses = XxlJobController.class)
public class SpringWebConfig {
}
