package org.jahia.services.workflow.jbpm.custom.email;

import org.jahia.services.workflow.jbpm.custom.AbstractWorkItemHandler;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import javax.mail.Message;
import java.util.Collection;

/**
 * A custom jBPM mailing work item handler that uses a mail template registry to send localized emails
 */
public class JBPMMailWorkItemHandler extends AbstractWorkItemHandler implements WorkItemHandler {

    private JBPMMailProducer mailProducer;
    private JBPMMailSession mailSession;

    public void setMailProducer(JBPMMailProducer mailProducer) {
        this.mailProducer = mailProducer;
    }

    public void setMailSession(JBPMMailSession mailSession) {
        this.mailSession = mailSession;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        Collection<Message> messages = mailProducer.produce(workItem);
        mailSession.send(messages);
        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        manager.abortWorkItem(workItem.getId());
    }
}
