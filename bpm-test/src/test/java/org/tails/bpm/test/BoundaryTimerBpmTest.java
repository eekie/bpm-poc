package org.tails.bpm.test;

import org.activiti.engine.runtime.ProcessInstance;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tails.bpm.test.conf.ApplicationConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfiguration.class })
public class BoundaryTimerBpmTest extends BaseBpmTest {

    @Before
    public void setup() {
        bpmTestUtil.deployProcess("/bpm", "boundarytimer.bpmn20.xml", "boundarytimerProcess");
        bpmTestUtil.deployProcess("/bpm", "boundarytimerParent.bpmn20.xml", "boundarytimerParentProcess");
    }

    @Test
    public void testBoundaryTimerParentProcess() throws Exception {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("boundarytimerParentProcess");
        Assertions.assertThat(pi).isNotNull();
        bpmTestUtil.waitForProcessToExist("boundarytimerProcess");
        bpmTestUtil.waitForActivityToExist("expireServicetask1");
    }

}
