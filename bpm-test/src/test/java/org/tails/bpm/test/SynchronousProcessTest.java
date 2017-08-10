package org.tails.bpm.test;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tails.bpm.test.conf.ApplicationConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfiguration.class })
public class SynchronousProcessTest extends BaseBpmTest {
    public static final Logger logger = LoggerFactory.getLogger(SynchronousProcessTest.class);
    final ExecutorService executor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder()
        .setNameFormat("process-starter-thread-%d").build());

    @Before
    public void setup() {
        bpmTestUtil.deployProcess("/bpm", "synchronous.bpmn20.xml", "synchronous");
    }

    @Test
    public void testSynchronousProcess() throws Exception {
        executor.submit(() -> {
            logger.info("starting process");
            ProcessInstance pi = runtimeService.startProcessInstanceByKey("synchronous");
            Assertions.assertThat(pi).isNotNull();
        });
        //logger.info("waiting 5 seconds");
        //TimeUnit.SECONDS.sleep(5);
        logger.info("asserting process");
        //Assertions.assertThat(runtimeService.createProcessInstanceQuery().list()).isNotEmpty();
        String pid = bpmTestUtil.waitForProcessToExist("synchronous");
        bpmTestUtil.waitForActivityToExist(pid, "Service Task");
        HistoricProcessInstance processHist = historyService.createHistoricProcessInstanceQuery().processInstanceId(pid).singleResult();
        Assertions.assertThat(processHist.getStartTime()).isNotNull();
    }

}
