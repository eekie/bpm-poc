package org.tails.bpm.test;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tails.bpm.test.model.Customer;
import org.tails.bpm.test.conf.ApplicationConfiguration;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfiguration.class })
@TestPropertySource(properties = {"datasource.url=jdbc:h2://tmp/bpmtest:activiti"})
public class VariablesPersistenceIT {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private BpmTestUtil bpmTestUtil;

    private static final Logger logger = LoggerFactory.getLogger(VariablesPersistenceIT.class);

    @Before
    public void setup() {
        bpmTestUtil.deployProcess("/bpm", "MyProcess.bpmn20.xml", "myProcess");
    }

    @Test
    public void testVariablePersistence() throws Exception {
        Map<String, Object> variables = new HashMap<>();
        variables.put("customer", new Customer("Meneertje Koekepeertje", "1234567", "meneertje.koekepeertje@example.com"));
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("myProcess", variables);
        logger.info(pi.getId());

        bpmTestUtil.waitForExecutionElementUnderProcess("receivetask1", pi.getId());
    }

    @Test
    @Ignore
    public void testRetrieveVar(){
        Execution execution = runtimeService.createExecutionQuery().processInstanceId("52505").singleResult();
        Map<String, Object> variables = runtimeService.getVariables(execution.getId());

        Assertions.assertThat(variables.values()).isNotEmpty();
        Customer customer = (Customer)variables.get("customer");
        System.out.print("contact info: " + customer.getContactInfo().getEmail());
    }

}
