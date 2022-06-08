package com.xxl.job.core.web.controller;

import com.xxl.job.core.biz.impl.ExecutorBizImpl;
import com.xxl.job.core.biz.model.IdleBeatParam;
import com.xxl.job.core.biz.model.KillParam;
import com.xxl.job.core.biz.model.LogParam;
import com.xxl.job.core.biz.model.LogResult;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
  * @projectName xxl-job
  * @className XxlJobController
  * @author Barcke
  * @date 2022/6/6 下午3:16
  * @version 1.0
  * @slogan: 源于生活 高于生活
  * @description: 
  **/
@RestController
@RequestMapping
public class XxlJobController {

    @PostMapping("/beat")
    public ReturnT<String> beat() {
        return new ExecutorBizImpl().beat();
    }

    @PostMapping("/idleBeat")
    public ReturnT<String> idleBeat(@RequestBody IdleBeatParam param) {
        return new ExecutorBizImpl().idleBeat(param);
    }

    @PostMapping("/run")
    public ReturnT<String> run(@RequestBody TriggerParam param) {
        return new ExecutorBizImpl().run(param);
    }

    @PostMapping("/kill")
    public ReturnT<String> kill(@RequestBody KillParam param) {
        return new ExecutorBizImpl().kill(param);
    }

    @PostMapping("/log")
    public ReturnT<LogResult> log(@RequestBody LogParam param) {
        return new ExecutorBizImpl().log(param);
    }

}
