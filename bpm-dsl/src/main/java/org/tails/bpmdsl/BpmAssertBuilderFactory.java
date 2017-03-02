package org.tails.bpmdsl;

import org.activiti.engine.HistoryService;

public class BpmAssertBuilderFactory {

    private  HistoryService historyService;

    public BpmAssertBuilderFactory(HistoryService historyService) {
        this.historyService = historyService;
    }

    public BpmAssertBuilder instance() {
        return new BpmAssertBuilder(historyService);
    }

}
