package org.jahia.services.workflow.jbpm.custom;

import org.jahia.pipelines.Pipeline;
import org.jahia.pipelines.PipelineException;
import org.jbpm.services.task.impl.model.I18NTextImpl;
import org.jbpm.services.task.impl.model.TaskDataImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.services.task.wih.LocalHTWorkItemHandler;
import org.jbpm.services.task.wih.util.HumanTaskHandlerHelper;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.InternalTaskData;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom implementation of the LocalHTWorkItemHandler so that we can use pluggeable
 * people assignments
 * <p/>
 * This implementation copies the implementation of the createTaskBasedOnWorkItemParams
 * method instead of calling it because it has the following differences:
 * - "en" is used as a language instead of "en-UK"
 */
public class JahiaLocalHTWorkItemHandler extends LocalHTWorkItemHandler {

    private Pipeline peopleAssignmentPipeline = null;

    public void setPeopleAssignmentPipeline(Pipeline peopleAssignmentPipeline) {
        this.peopleAssignmentPipeline = peopleAssignmentPipeline;
    }

    public class PeopleAssignmentContext {
        private Task task;
        private WorkItem workItem;
        private KieSession kieSession;

        public PeopleAssignmentContext(Task task, WorkItem workItem, KieSession kieSession) {
            this.task = task;
            this.workItem = workItem;
            this.kieSession = kieSession;
        }

        public Task getTask() {
            return task;
        }

        public WorkItem getWorkItem() {
            return workItem;
        }

        public KieSession getKieSession() {
            return kieSession;
        }
    }

    @Override
    protected Task createTaskBasedOnWorkItemParams(KieSession session, WorkItem workItem) {
        InternalTask task = new TaskImpl();
        String taskName = (String) workItem.getParameter("NodeName");
        if (taskName != null) {
            List<I18NText> names = new ArrayList<I18NText>();
            names.add(new I18NTextImpl("en", taskName));
            task.setNames(names);
        }
        String comment = (String) workItem.getParameter("Comment");
        if (comment == null) {
            comment = "";
        }
        List<I18NText> descriptions = new ArrayList<I18NText>();
        descriptions.add(new I18NTextImpl("en", comment));
        task.setDescriptions(descriptions);
        List<I18NText> subjects = new ArrayList<I18NText>();
        subjects.add(new I18NTextImpl("en", comment));
        task.setSubjects(subjects);
        String priorityString = (String) workItem.getParameter("Priority");
        int priority = 0;
        if (priorityString != null) {
            try {
                priority = new Integer(priorityString);
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
        task.setPriority(priority);
        InternalTaskData taskData = new TaskDataImpl();
        taskData.setWorkItemId(workItem.getId());
        taskData.setProcessInstanceId(workItem.getProcessInstanceId());
        if (session != null && session.getProcessInstance(workItem.getProcessInstanceId()) != null) {
            taskData.setProcessId(session.getProcessInstance(workItem.getProcessInstanceId()).getProcess().getId());
            String deploymentId = (String) session.getEnvironment().get("deploymentId");
            taskData.setDeploymentId(deploymentId);
        }
        if (session != null && (session instanceof KieSession)) {
            taskData.setProcessSessionId(((KieSession) session).getId());
        }
        taskData.setSkipable(!"false".equals(workItem.getParameter("Skippable")));
        //Sub Task Data
        Long parentId = (Long) workItem.getParameter("ParentId");
        if (parentId != null) {
            taskData.setParentId(parentId);
        }
        String createdBy = (String) workItem.getParameter("CreatedBy");
        if (createdBy != null && createdBy.trim().length() > 0) {
            taskData.setCreatedBy(new UserImpl(createdBy));
        }
        PeopleAssignmentHelper peopleAssignmentHelper = new PeopleAssignmentHelper();
        peopleAssignmentHelper.handlePeopleAssignments(workItem, task, taskData);

        PeopleAssignments peopleAssignments = task.getPeopleAssignments();
        List<OrganizationalEntity> businessAdministrators = peopleAssignments.getBusinessAdministrators();

        task.setTaskData(taskData);
        task.setDeadlines(HumanTaskHandlerHelper.setDeadlines(workItem, businessAdministrators, session.getEnvironment()));

        PeopleAssignmentContext peopleAssignmentContext = new PeopleAssignmentContext(task, workItem, session);
        try {
            peopleAssignmentPipeline.invoke(peopleAssignmentContext);
        } catch (PipelineException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return task;
    }
}
