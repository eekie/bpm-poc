package org.tails.bpm.test;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tails.bpm.test.conf.ApplicationConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfiguration.class })
@TestPropertySource(properties = {"datasource.url=jdbc:h2://tmp/bpmtest:activiti"})
public class AsyncGatewayTest extends BaseBpmTest {
    public static final Logger logger = LoggerFactory.getLogger(AsyncGatewayTest.class);
    final ExecutorService executor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder()
        .setNameFormat("process-starter-thread-%d").build());

    @Before
    public void setup() {
        bpmTestUtil.deployProcess("/bpm", "AsyncGateway.bpmn20.xml", "async-gateway");
    }

    @Test
    public void testSynchronousProcess() throws Exception {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("async-gateway");
        Assertions.assertThat(pi).isNotNull();

        logger.info("asserting process");
        String pid = bpmTestUtil.waitForProcessToExist("async-gateway");
        HistoricProcessInstance processHist = historyService.createHistoricProcessInstanceQuery().processInstanceId(pid).singleResult();
        Assertions.assertThat(processHist.getStartTime()).isNotNull();
        Thread.sleep(5000);
    }

}
