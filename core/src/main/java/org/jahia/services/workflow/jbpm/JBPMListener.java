package org.jahia.services.workflow.jbpm;

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
    public void notify(EventListenerExecution eventListenerExecution) throws Exception {
        System.out.println(eventListenerExecution.getState());
        if (Execution.STATE_ACTIVE_ROOT.equals(eventListenerExecution.getState())) {

        } else if (Execution.STATE_ENDED.equals(eventListenerExecution.getState())) {

        }
    }
}