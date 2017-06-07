package org.tails.bpm.test;

import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.DeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

public class BpmTestUtil {

    private static final Logger logger = LoggerFactory.getLogger(BpmTestUtil.class);

    private RepositoryService repositoryService;

    private HistoryService historyService;

    private RuntimeService runtimeService;

    public BpmTestUtil(RepositoryService repositoryService, HistoryService historyService, RuntimeService runtimeService) {
        this.repositoryService = repositoryService;
        this.historyService = historyService;
        this.runtimeService = runtimeService;
    }

    void deployProcess(String path, String resourceName, String processName) {
        try (InputStream is = new FileInputStream(new File(getClass().getResource(path + "/" + resourceName).getPath()))) {
            DeploymentBuilder builder = repositoryService.createDeployment();
            builder.addInputStream(resourceName, is).name(processName).deploy();
        } catch (Exception e) {
            logger.info("", e);
        }
    }

    String waitForExecutionElement(final String activitiId) throws Exception {
        return waitFor(() -> {
            Execution execution = runtimeService.createExecutionQuery()
                .activityId(activitiId)
                .singleResult();

            return execution != null ? execution.getId(): null;
        });
    }

    String waitForProcessToExist(final String processDefinitionKey) throws Exception {
        return waitForProcessToExist(processDefinitionKey, 1);
    }

    private String waitForProcessToExist(final String processDefinitionKey, int index) throws Exception {
        return waitFor(() -> {
            List<HistoricProcessInstance> processes = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey).orderByProcessInstanceStartTime().asc().list();

            return processes.size() >= index ? processes.get(index - 1).getId() : null;
        });
    }

    String waitForExecutionElementUnderProcess(final String activitiId,
                                               final String processInstanceId) throws Exception {
        Callable<String> callable = () -> {
            while (true) {
                HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
                    .activityId(activitiId)
                    .processInstanceId(processInstanceId)
                    .singleResult();

                if (historicActivityInstance != null) {
                    return historicActivityInstance.getId();
                }
                TimeUnit.MILLISECONDS.sleep(50);
            }
        };

        return waitFor(callable);
    }

    HistoricActivityInstance waitForActivityToExist(final String activityName) throws Exception {
        return waitFor(() -> historyService.createHistoricActivityInstanceQuery().activityName(activityName).singleResult());
    }

    protected Boolean waitForAllProcessInstancesToComplete() throws Exception {
        return waitFor(() -> runtimeService.createProcessInstanceQuery().list().size() == 0 ? true : null);
    }

    private <T> T waitFor(Callable<T> callable) throws Exception {
        T value = rerunIfNull(callable);
        if (value != null) {
            return value;
        }

        StringBuilder logging = new StringBuilder("Running processes upon timeout:");
        for (ProcessInstance pi : runtimeService.createProcessInstanceQuery().list()) {
            logging.append("\n\t-" + pi.getProcessDefinitionName() + " [" + pi.getActivityId() + "]");
        }
        logging.append("\n");
        logger.info(logging.toString());
        fail();

        return null;
    }
    private static void fail() {
        fail((String)null);
    }
    private static void fail(String message) {
        if(message == null) {
            throw new AssertionError();
        } else {
            throw new AssertionError(message);
        }
    }
    /**
     * Repeatedly executes the <code>callable</code> function till the return value is not null.
     * Between each call, it sleeps for 250 milliseconds. This behaviour is performed for a maximum period of 30 seconds.
     * @param callable the function to be executed
     * @param <T> the type that is returned by the <code>callable</code>
     * @return the value returned by <code>callable</code>
     * @throws Exception
     */
    private <T> T rerunIfNull(Callable<T> callable) throws Exception {
        int maxSecondsToWait = 30;
        Instant before = Instant.now();
        while (Duration.between(before, Instant.now()).getSeconds() < maxSecondsToWait) {
            try {
                T value = callable.call();
                if (value != null) {
                    return value;
                }
                Thread.sleep(250);
            } catch (InterruptedException | ActivitiOptimisticLockingException e) {
                // retry
            }
        }
        return null;
    }
}
