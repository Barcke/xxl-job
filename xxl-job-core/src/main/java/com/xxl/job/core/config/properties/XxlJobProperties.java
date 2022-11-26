package com.xxl.job.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Barcke
 * @version 1.0
 * @projectName xxl-job
 * @className XxlJobProperties
 * @date 2022/11/26 14:13
 * @slogan: 源于生活 高于生活
 * @description:
 **/
@ConfigurationProperties(prefix = "xxl.job.executor")
public class XxlJobProperties {

    private String appname;

    private String address;

    private Integer port;

    private String logpath;

    private Integer logretentiondays;

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getLogpath() {
        return logpath;
    }

    public void setLogpath(String logpath) {
        this.logpath = logpath;
    }

    public Integer getLogretentiondays() {
        return logretentiondays;
    }

    public void setLogretentiondays(Integer logretentiondays) {
        this.logretentiondays = logretentiondays;
    }
}
