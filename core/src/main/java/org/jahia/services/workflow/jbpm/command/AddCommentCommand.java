package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.WorkflowComment;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* Add a comment to a process
*/
public class AddCommentCommand extends BaseCommand {
    private final String processId;
    private final String comment;
    private final String user;

    public AddCommentCommand(String processId, String comment, String user) {
        this.processId = processId;
        this.comment = comment;
        this.user = user;
    }

    @Override
    public Object execute() {
        KieSession ksession = getKieSession();
        ProcessInstance processInstance = ksession.getProcessInstance(Long.parseLong(processId));
        WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) processInstance;
        List<WorkflowComment> comments = (List<WorkflowComment>) workflowProcessInstance.getVariable("comments");
        if ( comments == null) {
            comments = new ArrayList<WorkflowComment>();
        }
        final WorkflowComment wfComment = new WorkflowComment();
        wfComment.setComment(comment);
        wfComment.setUser(user);
        wfComment.setTime(new Date());
        comments.add(wfComment);
        workflowProcessInstance.setVariable("comments",comments);
        return null;
    }
}
