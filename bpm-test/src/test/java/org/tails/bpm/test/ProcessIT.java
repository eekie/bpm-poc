package org.tails.bpm.test;

import org.tails.bpm.test.conf.ApplicationConfiguration;
import org.tails.bpmdsl.BpmAssertBuilderFactory;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.runtime.Execution;
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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfiguration.class })
public class ProcessIT {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private BpmTestUtil bpmTestUtil;

    private static final Logger logger = LoggerFactory.getLogger(ProcessIT.class);

    @Autowired
    private BpmAssertBuilderFactory bpmAssertFactory;

    @Before
    public void setup() {
        bpmTestUtil.deployProcess("/bpm", "MyProcess.bpmn20.xml", "myProcess");
        bpmTestUtil.deployProcess("/bpm", "parentProcess.bpmn20.xml", "parentProcess");
    }
//  https://community.alfresco.com/thread/216417-queries-from-start-executionlistener-on-first-user-task

    @Test
    public void testProcessIT() throws Exception {

        BiConsumer<String, Callable> waitWithElapsed = (label, callable) -> {
            Instant b = Instant.now();
            try {
                callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Instant e = Instant.now();
            Duration timeElapsed = Duration.between(b, e);
            logger.info("waiting for {} took {} milliseconds", label, timeElapsed.toMillis());
        };


        logger.info("invoke process");

        Instant b = Instant.now();
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("myProcess");
        final String pid = pi.getId();
        Assertions.assertThat(pi).isNotNull();

        waitWithElapsed.accept("exclusivegateway2", () -> bpmTestUtil.waitForExecutionElementUnderProcess("exclusivegateway2", pid));

//        waitWithElapsed.accept("servicetask1", () -> bpmTestUtil.waitForExecutionElementUnderProcess("servicetask1", pid));
//        Assertions.assertThat(historyService.createHistoricActivityInstanceQuery().processInstanceId(pi.getId()).activityId("servicetask1").list()).hasSize(1);

        waitWithElapsed.accept("servicetask2",() -> bpmTestUtil.waitForExecutionElementUnderProcess("servicetask2", pid));
        Assertions.assertThat(historyService.createHistoricActivityInstanceQuery().processInstanceId(pi.getId()).activityId("servicetask2").list()).hasSize(1);

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(pid).singleResult();
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(pid).singleResult();
        logger.info(execution.toString());

//        bpmTestUtil.waitForProcessInstanceToComplete(pid);
//        Instant e = Instant.now();
//        Duration timeElapsed = Duration.between(b, e);
//        logger.info("process elapsed time: " + timeElapsed.toMillis());

    }

    @Test
    public void testParentProcess() {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("parentProcess");
        final String pid = pi.getId();
        logger.info("invoked parent process with pid " + pid);
        Assertions.assertThat(pi).isNotNull();

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pid).singleResult();
        logger.info("");

    }

    @Test
    public void testWithDsl() throws Exception {
        //List<Execution> servicetask1 = runtimeService.createExecutionQuery().processInstanceId(pi.getId()) .activityId("servicetask1").list();
        //LOG.info(servicetask1.toString());
        //servicetask1 = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).activityId("servicetask1").list();
//        ResponseEntity<RSResponse> rsResponseResponseEntity = supplyEndService.startSupplyEnd(createSupplyEndRequest(false));
//        String processInstanceId = rsResponseResponseEntity.getBody().getBpmRequestId();

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("myProcess");
        String pid = pi.getId();

        //bpmTestUtil.waitForProcessInstanceToComplete(pid);

        // 1
        bpmAssertFactory.instance()
            .queryProcess()
            .byProcessInstanceId(pid)
            .result()
                .assertSize(1)
                .assertEnded();

//       // 1.1
        Function<HistoricProcessInstanceQuery, List<HistoricProcessInstance>> pQry = qry -> qry.list();
        Function<HistoricActivityInstanceQuery, List<HistoricActivityInstance>> aQry = qry -> qry.list();

        List<HistoricProcessInstance> apply = pQry.apply(historyService.createHistoricProcessInstanceQuery().processInstanceId(pid));
//        List<HistoricProcessInstance> apply1 = pQry.apply(historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(pid));
//        List<HistoricActivityInstance> apply2 = aQry.apply(historyService.createHistoricActivityInstanceQuery().processInstanceId(apply1.get(0).getId()));
//        System.out.println(apply2.size());

//        bpmAssertFactory.instance().processQrySupplier.get().processInstanceId(pid);
//
//        // 2
        List<HistoricProcessInstance> result = bpmAssertFactory.instance()
            .queryProcess()
            .byProcessInstanceId(pid)
            .listHistoricProceses();

        Assertions.assertThat(result)
            .hasSize(1)
            .extracting(historicProcessInstance -> historicProcessInstance.getEndTime() != null);

//
//        List<HistoricProcessInstance> subprocesses = bpmAssertFactory.instance()
//            .queryProcess()
//            .subprocessesFor(processInstanceId)
//            .listHistoricProceses();
//
//        Assertions.assertThat(subprocesses)
//            .hasSize(1)
//            .filteredOn(historicProcessInstance -> historicProcessInstance.getProcessDefinitionKey().equals(PROCESS_ENERGYCOMM_SUPPLY_END_REQUEST) )
//            .isNotEmpty()
//            .extracting(HistoricProcessInstance::getEndTime).containsNull()
//            // open process
//        ;
    }


}
