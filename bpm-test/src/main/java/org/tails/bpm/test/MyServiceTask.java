package org.tails.bpm.test;

import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyServiceTask {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;

    private static final Logger logger = LoggerFactory.getLogger(MyServiceTask.class);

    public void execute(DelegateExecution execution) {
        logger.info("start invocation " + execution.getCurrentActivityId() + " pid=" + execution.getProcessInstanceId());
        logger.info("end invocation " + execution.getCurrentActivityId());
        managementService.createJobQuery().timers().list().forEach(x ->
            logger.info("timer: " + x.getId() + " " + x.getProcessInstanceId()));
    }

}
