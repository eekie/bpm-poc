package org.tails.bpm.test.conf;

import org.activiti.engine.RuntimeService;
import org.tails.bpm.test.BpmTestUtil;
import org.tails.bpmdsl.BpmAssertBuilderFactory;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestBeanFactoryConfig {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Bean
    public BpmAssertBuilderFactory getFactory() {
        return new BpmAssertBuilderFactory(historyService);
    }

    @Bean
    public BpmTestUtil getBpmTestUtil() {
        return new BpmTestUtil(repositoryService, historyService, runtimeService);
    }

}
