package org.tails.bpm.test;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RuntimeService;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseBpmTest {

    @Autowired
    BpmTestUtil bpmTestUtil;

    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    HistoryService historyService;

    @Before
    public void start() {
        processEngineConfiguration.getAsyncExecutor().setDefaultTimerJobAcquireWaitTimeInMillis(100);
        processEngineConfiguration.getAsyncExecutor().start();
    }

}
