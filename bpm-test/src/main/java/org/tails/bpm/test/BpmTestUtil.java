package org.tails.bpm.test;

import org.tails.bpmdsl.BpmException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.DeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.*;

public class BpmTestUtil {

    private static final Logger logger = LoggerFactory.getLogger(BpmTestUtil.class);

    private RepositoryService repositoryService;

    private HistoryService historyService;

    public BpmTestUtil(RepositoryService repositoryService, HistoryService historyService) {
        this.repositoryService = repositoryService;
        this.historyService = historyService;
    }

    public void deployProcess(String path, String resourceName, String processName) {
        try (InputStream is = new FileInputStream(new File(getClass().getResource(path + "/" + resourceName).getPath()))) {
            DeploymentBuilder builder = repositoryService.createDeployment();
            builder.addInputStream(resourceName, is).name(processName).deploy();
        } catch (Exception e) {
            logger.info("", e);
        }
    }

    private  <T> T waitFor(Callable<T> callable) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(callable);

        try {
            return future.get(30, TimeUnit.SECONDS); //throws a timeout if the process is running for over 30 seconds
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BpmException(e);
        } finally {
            executor.shutdownNow();
        }
    }

    public String waitForExecutionElementUnderProcess(final String activitiId,
                                               final String processInstanceId) {
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


}
