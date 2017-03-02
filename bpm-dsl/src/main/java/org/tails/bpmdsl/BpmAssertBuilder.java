package org.tails.bpmdsl;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.assertj.core.api.Assertions;

import java.util.List;

public class BpmAssertBuilder {

    private  HistoryService historyService;
    private ProcessQueryBuilder  processQueryBuilder;

    private HistoricProcessInstanceQuery historicProcessInstanceQuery;

    BpmAssertBuilder(HistoryService historyService) {
        this.historyService = historyService;
        processQueryBuilder = new ProcessQueryBuilder();
    }

    public ProcessQueryBuilder queryProcess() {
        return processQueryBuilder;
    }

    public class ProcessQueryBuilder {

        ProcessQueryBuilder() {
            historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery();
        }

        public ProcessQueryBuilder forAnyProcess() {
            return this;
        }

        public ProcessQueryBuilder byProcessInstanceId(String pid) {
            historicProcessInstanceQuery.processInstanceId(pid);
            return this;
        }

        public ProcessQueryBuilder subprocessesFor(String pid) {
            historicProcessInstanceQuery.superProcessInstanceId(pid);
            return this;
        }

        public ProcessQueryResult result() {
            List<HistoricProcessInstance> list = historicProcessInstanceQuery.list();
            Assertions.assertThat(list).isNotEmpty();
            return new ProcessQueryResult(list);
        }

        public List<HistoricProcessInstance> listHistoricProceses() {
            return historicProcessInstanceQuery.list();
        }



    }

    public class ProcessQueryResult {

        private List<HistoricProcessInstance> list;
        private ProcessesHistoricActivities subProcessesHistoricActivities;

        private ProcessQueryResult(List<HistoricProcessInstance> list) {
             this.list = list;
        }

        public ProcessQueryResult assertEnded() {
            Assertions.assertThat(list.get(0).getEndTime() != null);
            return this;
        }
        public ProcessQueryResult assertSize(int size) {
            Assertions.assertThat(list).hasSize(size);
            return new ProcessQueryResult(list);
        }
        public ProcessQueryResult querySubProcesses() {
            List<HistoricProcessInstance> list2 = historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(this.list.get(0).getId()).list();
            return new ProcessQueryResult(list2);
        }

        public ProcessesHistoricActivities assertProcessNameExists(String name) {
            for (HistoricProcessInstance inst: list) {
                if (name.equalsIgnoreCase(inst.getProcessDefinitionKey())) {
                    subProcessesHistoricActivities = new ProcessesHistoricActivities(inst);
                    return subProcessesHistoricActivities;
                }
            }
            Assertions.fail("expected to find a process with name " + name);
            return null;
        }

        public HistoricActivityInstanceQuery expQryBuilder() {
            return historyService.createHistoricActivityInstanceQuery();
        }

        public void expApplyQry(HistoricActivityInstanceQuery qry) {
            qry.list();

        }

        public ProcessesHistoricActivities forProcessName(String processName) {
            for (HistoricProcessInstance inst: list) {
                if (processName.equalsIgnoreCase(inst.getProcessDefinitionKey())) {
                    subProcessesHistoricActivities = new ProcessesHistoricActivities(inst);
                    return subProcessesHistoricActivities;
                }
            }
            Assertions.fail("expected to find a process with name " + processName);
            return null;
        }

//        public void notifyListeners (Consumer<? super L> algorithm) {
//            Assertions.assertThat(list).isEqualTo()
//
//        }

    }

    public class ProcessesHistoricActivities {
        private List<HistoricActivityInstance> activities;
        public ProcessesHistoricActivities(HistoricProcessInstance historicProcessInstance) {
            activities = historyService.createHistoricActivityInstanceQuery().processInstanceId(historicProcessInstance.getId()).list();
        }

        public void assertActivityNameExists(String name) {
            Assertions.assertThat(activities).filteredOn("activityName", name).isNotEmpty();
        }

        public void assertActivityNameNotExists(String name) {
            Assertions.assertThat(activities).filteredOn("activityName", name).isEmpty();
        }

        public List<HistoricActivityInstance> listHistoricActivities() {
            return activities;
        }
    }

    @FunctionalInterface
    public interface ActivitiQueryBuilder {
        List<ProcessQueryResult> process(HistoricProcessInstanceQuery query);
    }

//    public static List<ProcessQueryResult> processQuery(ActivitiQueryBuilder q) throws ActivitiException {
//        return q.process();
//    }


//    private static final Function<HistoricProcessInstanceQuery, List<HistoricProcessInstance>> pQry = Query::list;
//    private static final Function<HistoricActivityInstanceQuery, List<HistoricActivityInstance>> aQry = Query::list;

//    private final Supplier<HistoricProcessInstanceQuery> processQrySupplier = () -> historyService.createHistoricProcessInstanceQuery();
    //new Function<HistoricProcessInstanceQuery,List<HistoricProcessInstance>>() processQuery = historicProcessInstanceQuery.list();

}
