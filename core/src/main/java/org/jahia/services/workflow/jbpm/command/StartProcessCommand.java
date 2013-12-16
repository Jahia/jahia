package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.jbpm.BaseCommand;
import org.jahia.services.workflow.jbpm.JBPM6WorkflowProvider;
import org.kie.api.runtime.process.ProcessInstance;

import java.util.Map;

/**
* Start a new process
*/
public class StartProcessCommand extends BaseCommand<String> {
    private final String processKey;
    private final Map<String, Object> args;

    public StartProcessCommand(String processKey, Map<String, Object> args) {
        this.processKey = processKey;
        this.args = args;
    }

    @Override
    public String execute() {
        ProcessInstance processInstance = getKieSession().startProcess(JBPM6WorkflowProvider.getEncodedProcessKey(processKey), args);
        return Long.toString(processInstance.getId());
    }
}
