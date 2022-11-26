package com.xxl.job.core.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xxl.job.core.biz.model.ServerInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
  * @className XxlJobServerHttpUtil
  * @author Barcke
  * @date 2021/1/19 11:03
  * @version 1.0
  * @slogan: 源于生活 高于生活
  * @description: 
  **/
public class XxlJobServerHttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(XxlJobServerHttpUtil.class);

    /**
     * 获取执行器列表地址
     */
    private final String GET_JOB_GROUP_URL = "/jobgroup/pageList";

    /**
     * 保存执行器地址
     */
    private final String SAVE_ACTUATOR_URL = "/jobgroup/save";

    /**
     * 登陆地址
     */
    private final String LOGIN_URL = "/login";

    /**
     * 获取任务列表
     */
    private final String GET_JOB_INFO_URL = "/jobinfo/pageList";

    /**
     * 编辑任务
     * TODO 因一个handler可以存在多个时间点执行不好判断是否要编辑如果发现已存在则不管了
     */
    private final String UPDATE_JOB_INFO_URL = "/jobinfo/update";

    /**
     * 保存任务
     */
    private final String SAVE_JOB_INFO_URL = "/jobinfo/add";

    /**
     * 获取服务信息url
     */
    private final String SERVER_INFO_URL = "/server-info/getServerInfo";

    /**
     * 服务器地址记得带上项目名～
     */
    private String url;

    /**
     * 登陆态
     */
    private HttpCookie[] httpCookies;

    public XxlJobServerHttpUtil(String url) throws Exception {
        this.url = url;

        ServerInfoVO serverInfoVO = this.getServerInfo(url);

        //初始化登陆态
        HttpResponse execute = HttpRequest
                .post(url + LOGIN_URL)
                .form(new HashMap<String, Object>() {{
                    this.put("userName", serverInfoVO.getUserName());
                    this.put("password", serverInfoVO.getPassWord());
                }})
                .execute();

        if ("500".equals(new JSONObject(execute.body()).getStr("code"))) {
            throw new Exception("Xxl-Job登陆用户名或密码错误");
        }

        List<HttpCookie> cookies = execute.getCookies();

        httpCookies = new HttpCookie[cookies.size()];

        cookies.toArray(httpCookies);
    }

    /**
     * 获取服务信息，主要是登陆xxl-job用户名密码
     * 主要是用于获取登陆server的用户名和密码
     * @param url 定时任务服务端url
     * @return 服务信息
     */
    private ServerInfoVO getServerInfo(String url) {
        String body = HttpUtil.createGet(url + SERVER_INFO_URL).execute().body();

        return JSONUtil.parseObj(body).get("data", ServerInfoVO.class);
    }

    /**
     * 获取执行器id
     * @return
     */
    public Integer getGroupIdByAppName(String appName){

        String body = this.getRequest()
                .method(Method.POST)
                .setUrl(url + GET_JOB_GROUP_URL)
                .form(new HashMap<String, Object>() {{
                    this.put("start", "0");
                    this.put("length", "10");
                    this.put("appName", appName);
                }})
                .execute()
                .body();

        JSONObject jsonObject = new JSONObject(body);

        JSONArray data = jsonObject.getJSONArray("data");

        for (int i = 0; i < data.size(); i++) {
            JSONObject dataJSONObject = data.getJSONObject(i);

            if (appName.equals(dataJSONObject.getStr("appName"))) {
                return dataJSONObject.getInt("id");
            }
        }

        return null;
    }

    /**
     * 保存执行器
     */
    public boolean saveActuator(ActuatorData actuatorData){
        String body = this.getRequest()
                .method(Method.POST)
                .setUrl(url + SAVE_ACTUATOR_URL)
                .form(new HashMap<String, Object>() {{
                    this.put("addressList", actuatorData.getAddress());
                    this.put("addressType", "1");
                    this.put("title", actuatorData.getName());
                    this.put("appName", actuatorData.getAppName());
                }})
                .execute()
                .body();

        JSONObject jsonObject = new JSONObject(body);

        boolean code = "200".equals(jsonObject.getStr("code"));

        if (!code) {
            logger.info("保存执行器失败body:{}",body);
        }

        return code;
    }

    /**
     * 获取任务Id
     */
    public Integer getJobInfoIdByHandlerName(String executorHandler,Integer jobGroupId){
        String body = this.getRequest()
                .method(Method.POST)
                .setUrl(url + GET_JOB_INFO_URL)
                .form(new HashMap<String, Object>() {{
                    this.put("executorHandler", executorHandler);
                    this.put("jobGroup", jobGroupId);
                    this.put("start", "0");
                    this.put("length", "10");
                    this.put("triggerStatus", "-1");
                }})
                .execute()
                .body();

        JSONObject jsonObject = new JSONObject(body);


        JSONArray data = jsonObject.getJSONArray("data");

        for (int i = 0; i < data.size(); i++) {
            JSONObject dataJSONObject = data.getJSONObject(i);

            if (executorHandler.equals(dataJSONObject.getStr("executorHandler"))) {
                return dataJSONObject.getInt("id");
            }
        }

        return null;
    }

    /**
     * 保存任务
     */
    public boolean saveJobInfo(JobInfoData jobInfoData){

        if (
                StringUtils.isEmpty(jobInfoData.getJobCron())
                        || StringUtils.isEmpty(jobInfoData.getExecutorHandler())
                        || Objects.isNull(jobInfoData.getJobGroup())
                        || StringUtils.isEmpty(jobInfoData.getAuthor())
                        || StringUtils.isEmpty(jobInfoData.getJobDesc())
        ){
            throw new NullPointerException("Please ensure that the parameters are correct");
        }

        String body = this.getRequest()
                .method(Method.POST)
                .setUrl(url + SAVE_JOB_INFO_URL)
                .form(
                        BeanUtil.beanToMap(jobInfoData)
                )
                .execute()
                .body();

        JSONObject jsonObject = new JSONObject(body);

        boolean code = "200".equals(jsonObject.getStr("code"));

        if (!code){
            logger.info("保存任务失败body:{}",body);
        }

        return code;
    }

    private HttpRequest getRequest(){
        HttpRequest request = new HttpRequest(url);

        request.cookie(httpCookies);
        request.header("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");

        return request;
    }

    /**
     * 执行器信息
     */
    public static class ActuatorData{

        private String appName;

        private String name;

        private String address;

        public ActuatorData() {
        }

        public ActuatorData(ApplicationContext applicationContext) {
            this.appName = applicationContext.getEnvironment().getProperty("xxl.job.executor.appname");
            this.name = appName;
            this.address = applicationContext.getEnvironment().getProperty("xxl.job.executor.address");

            if (StringUtils.isEmpty(address)){
                throw new NullPointerException("Please specify the actuator address ---》 xxl.job.executor.address");
            }
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    /**
     * 任务信息
     */
    public static class JobInfoData{
        private Integer jobGroup;
        private String jobDesc;
        private String executorRouteStrategy;
        private String cronGen_display;
        private String jobCron;
        private String glueType;
        private String executorHandler;
        private String executorBlockStrategy;
        private Integer executorTimeout;
        private Integer executorFailRetryCount;
        private String author;
        private String alarmEmail;
        private String glueRemark;

        public JobInfoData() {
        }

        public JobInfoData(Integer jobGroup, String jobDesc, String jobCron, String executorHandler, Integer executorTimeout, Integer executorFailRetryCount, String author, String alarmEmail) {
            this.jobGroup = jobGroup;
            this.jobDesc = jobDesc;
            this.executorRouteStrategy = "FIRST";
            this.cronGen_display = jobCron;
            this.jobCron = jobCron;
            this.glueType = "BEAN";
            this.executorHandler = executorHandler;
            this.executorBlockStrategy = "SERIAL_EXECUTION";
            this.executorTimeout = executorTimeout;
            this.executorFailRetryCount = executorFailRetryCount;
            this.author = author;
            this.alarmEmail = alarmEmail;
            this.glueRemark = "GLUE代码初始化";
        }

        public Integer getJobGroup() {
            return jobGroup;
        }

        public void setJobGroup(Integer jobGroup) {
            this.jobGroup = jobGroup;
        }

        public String getJobDesc() {
            return jobDesc;
        }

        public void setJobDesc(String jobDesc) {
            this.jobDesc = jobDesc;
        }

        public String getExecutorRouteStrategy() {
            return executorRouteStrategy;
        }

        public void setExecutorRouteStrategy(String executorRouteStrategy) {
            this.executorRouteStrategy = executorRouteStrategy;
        }

        public String getCronGen_display() {
            return cronGen_display;
        }

        public void setCronGen_display(String cronGen_display) {
            this.cronGen_display = cronGen_display;
        }

        public String getJobCron() {
            return jobCron;
        }

        public void setJobCron(String jobCron) {
            this.jobCron = jobCron;
        }

        public String getGlueType() {
            return glueType;
        }

        public void setGlueType(String glueType) {
            this.glueType = glueType;
        }

        public String getExecutorHandler() {
            return executorHandler;
        }

        public void setExecutorHandler(String executorHandler) {
            this.executorHandler = executorHandler;
        }

        public String getExecutorBlockStrategy() {
            return executorBlockStrategy;
        }

        public void setExecutorBlockStrategy(String executorBlockStrategy) {
            this.executorBlockStrategy = executorBlockStrategy;
        }

        public Integer getExecutorTimeout() {
            return executorTimeout;
        }

        public void setExecutorTimeout(Integer executorTimeout) {
            this.executorTimeout = executorTimeout;
        }

        public Integer getExecutorFailRetryCount() {
            return executorFailRetryCount;
        }

        public void setExecutorFailRetryCount(Integer executorFailRetryCount) {
            this.executorFailRetryCount = executorFailRetryCount;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getAlarmEmail() {
            return alarmEmail;
        }

        public void setAlarmEmail(String alarmEmail) {
            this.alarmEmail = alarmEmail;
        }

        public String getGlueRemark() {
            return glueRemark;
        }

        public void setGlueRemark(String glueRemark) {
            this.glueRemark = glueRemark;
        }
    }
}
