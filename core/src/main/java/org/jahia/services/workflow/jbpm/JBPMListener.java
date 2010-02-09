package org.jahia.services.workflow.jbpm;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jbpm.api.Execution;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.pvm.internal.model.ExecutionImpl;

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
        String id = (String) execution.getVariable("nodeId");
        JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(id);

        if (Execution.STATE_ACTIVE_ROOT.equals(execution.getState())) {
            provider.getWorkflowService().addProcessId(node, provider.getKey(), execution.getId());

        } else if (Execution.STATE_ENDED.equals(execution.getState())) {
            provider.getWorkflowService().removeProcessId(node, provider.getKey(), execution.getId());
        }
    }
}