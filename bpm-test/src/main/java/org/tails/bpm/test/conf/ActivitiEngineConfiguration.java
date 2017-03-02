package org.tails.bpm.test.conf;

import org.tails.bpmdsl.BpmException;
import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class ActivitiEngineConfiguration {

    private static final Logger LOG =
        LoggerFactory.getLogger(ActivitiEngineConfiguration.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private Environment environment;

    @Bean(name = "processEngine")
    public ProcessEngineFactoryBean processEngineFactoryBean() {
        ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
        factoryBean.setProcessEngineConfiguration(processEngineConfiguration());

        return factoryBean;
    }

    public ProcessEngine processEngine() {
        // Safe to call the getObject() on the @Bean annotated
        // processEngineFactoryBean(), will be
        // the fully initialized object instanced from the factory and will NOT be
        // created more than once
        try {
            return processEngineFactoryBean().getObject();
        } catch (Exception e) {
            LOG.error("Error creating process engine", e);
            throw new BpmException(e);
        }
    }

    @Bean(name = "processEngineConfiguration")
    public ProcessEngineConfigurationImpl processEngineConfiguration() {
        SpringProcessEngineConfiguration processEngineConfiguration =
            new SpringProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(dataSource);
        processEngineConfiguration.setDatabaseSchemaUpdate(
            ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        processEngineConfiguration.setTransactionManager(transactionManager);
        processEngineConfiguration.setJobExecutorActivate(true);

        // Enable safe XML. See
        // http://activiti.org/userguide/index.html#advanced.safe.bpmn.xml
        processEngineConfiguration.setEnableSafeBpmnXml(true);
        processEngineConfiguration.setAsyncExecutorActivate(Boolean.TRUE);
        processEngineConfiguration.setAsyncExecutorEnabled(Boolean.TRUE);

        processEngineConfiguration.setAsyncExecutorDefaultAsyncJobAcquireWaitTime(Integer.parseInt(environment.getProperty("asyncjob.acquire.waittime.millis")));
        processEngineConfiguration.setAsyncFailedJobWaitTime(Integer.parseInt(environment.getProperty("asyncjob.failed.waittime.seconds")));

        LOG.info("AsyncExecutorDefaultAsyncJobAcquireWaitTime: " + processEngineConfiguration.getAsyncExecutorDefaultAsyncJobAcquireWaitTime() + "");
        LOG.info("AsyncFailedJobWaitTime: " + processEngineConfiguration.getAsyncFailedJobWaitTime() + "");

        return processEngineConfiguration;
    }

    @Bean
    public RepositoryService repositoryService() {
        return processEngine().getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService() {
        return processEngine().getRuntimeService();
    }

    @Bean
    public TaskService taskService() {
        return processEngine().getTaskService();
    }

    @Bean
    public HistoryService historyService() {
        return processEngine().getHistoryService();
    }

    @Bean
    public ManagementService managementService() {
        return processEngine().getManagementService();
    }
}
