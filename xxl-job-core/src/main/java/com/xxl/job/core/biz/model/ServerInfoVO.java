package com.xxl.job.core.biz.model;

/**
  * @projectName xxl-job
  * @className ServerInfoVO
  * @author Barcke
  * @date 2022/6/6 下午2:48
  * @version 1.0
  * @slogan: 源于生活 高于生活
  * @description: 
  **/
public class ServerInfoVO {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String passWord;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
