package org.tails.bpm.test;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tails.bpm.test.conf.ApplicationConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfiguration.class })
public class BoundaryTimerTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private BpmTestUtil bpmTestUtil;

    private static final Logger logger = LoggerFactory.getLogger(BoundaryTimerTest.class);

    @Before
    public void setup() {
        bpmTestUtil.deployProcess("/bpm", "boundarytimer.bpmn20.xml", "boundarytimerProcess");
        bpmTestUtil.deployProcess("/bpm", "boundarytimerParent.bpmn20.xml", "boundarytimerParentProcess");
    }

    @Test
    public void testBoundaryTimerParentProcess() throws Exception {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("boundarytimerParentProcess");
        final String pid = pi.getId();
        logger.info("invoked parent process with pid " + pid);
        Assertions.assertThat(pi).isNotNull();
        bpmTestUtil.waitForProcessToExist("boundarytimerProcess");
        bpmTestUtil.waitForExecutionElement("usertask1");
        bpmTestUtil.waitForActivityToExist("expireServicetask1");
    }

}
