package org.jahia.services.workflow.jbpm.custom.email;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import javax.mail.Message;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: loom
 * Date: 28.06.13
 * Time: 22:05
 * To change this template use File | Settings | File Templates.
 */
public class JBPMMailWorkItemHandler implements WorkItemHandler {

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
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    }
}
