package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.HistoryWorkflow;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.jbpm.process.audit.VariableInstanceLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
* Get all history process for a node id
*/
public class GetHistoryWorkflowsForNodeCommand extends BaseCommand<List<HistoryWorkflow>> {
    private final String nodeId;
    private final Locale uiLocale;

    public GetHistoryWorkflowsForNodeCommand(String nodeId, Locale uiLocale) {
        this.nodeId = nodeId;
        this.uiLocale = uiLocale;
    }

    @Override
    public List<HistoryWorkflow> execute() {
        @SuppressWarnings("unchecked")
        List<VariableInstanceLog> result = getEm()
                .createQuery("FROM VariableInstanceLog v WHERE v.variableId = :variableId AND v.value = :variableValue")
                .setParameter("variableId", "nodeId")
                .setParameter("variableValue", nodeId).getResultList();

        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> l = new ArrayList<String>();
        for (VariableInstanceLog log : result) {
            l.add(Long.toString(log.getProcessInstanceId()));
        }

        return getHistoryWorkflows(l, uiLocale);
    }
}
