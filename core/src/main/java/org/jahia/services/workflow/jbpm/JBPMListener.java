package org.jahia.services.workflow.jbpm;

import org.jahia.services.content.*;
import org.jbpm.api.Execution;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.pvm.internal.model.ExecutionImpl;

import javax.jcr.RepositoryException;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 8:04:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class JBPMListener implements EventListener {
    private JBPMProvider provider;

    public JBPMListener(JBPMProvider provider) {
        this.provider = provider;
    }

    public void notify(EventListenerExecution execution) throws Exception {
        final String id = (String) execution.getVariable("nodeId");
        String workspace = (String) execution.getVariable("workspace");
        Locale locale = (Locale) execution.getVariable("locale");

        final String executionState = execution.getState();
        final String executionId = execution.getId();

        JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, locale, new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                final JCRNodeWrapper node = session.getNodeByUUID(id);
                if (Execution.STATE_ACTIVE_ROOT.equals(executionState)) {
                    provider.getWorkflowService().addProcessId(node, provider.getKey(), executionId);
        
                } else if (Execution.STATE_ENDED.equals(executionState)) {
                    provider.getWorkflowService().removeProcessId(node, provider.getKey(), executionId);
                }
                return true;
            }
        });
    }
}