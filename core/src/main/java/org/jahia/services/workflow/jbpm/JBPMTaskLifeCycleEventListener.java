/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow.jbpm;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.usermanager.*;
import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowTask;
import org.jahia.services.workflow.WorkflowVariable;
import org.jahia.services.workflow.jbpm.custom.AbstractTaskLifeCycleEventListener;
import org.jahia.utils.Patterns;
import org.jbpm.runtime.manager.impl.task.SynchronizedTaskService;
import org.jbpm.services.task.events.AfterTaskAddedEvent;
import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import java.util.*;


/**
 * JBPM Task lifecycle event listener
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 4 févr. 2010
 */
public class JBPMTaskLifeCycleEventListener extends AbstractTaskLifeCycleEventListener {

    @Override
    public void afterTaskReleasedEvent(Task ti) {

    }

    @Override
    public void afterTaskResumedEvent(Task ti) {

    }

    @Override
    public void afterTaskSuspendedEvent(Task ti) {

    }

    @Override
    public void afterTaskForwardedEvent(Task ti) {

    }

    @Override
    public void afterTaskDelegatedEvent(Task ti) {

    }

    @Override
    public void afterTaskActivatedEvent(Task ti) {

    }

    @Override
    public void afterTaskClaimedEvent(Task ti) {

    }

    @Override
    public void afterTaskSkippedEvent(Task ti) {

    }

    @Override
    public void afterTaskStartedEvent(Task ti) {

    }

    @Override
    public void afterTaskStoppedEvent(Task ti) {

    }

    @Override
    public void afterTaskCompletedEvent(Task ti) {

    }

    @Override
    public void afterTaskFailedEvent(Task ti) {

    }

    @Override
    public void afterTaskAddedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskAddedEvent Task task) {
        Map<String, Object> taskInputParameters = getTaskInputParameters(task);
        Map<String, Object> taskOutputParameters = getTaskOutputParameters(task, taskInputParameters);
        try {
            final List<JahiaPrincipal> principals = new ArrayList<JahiaPrincipal>();
            for (OrganizationalEntity entity : task.getPeopleAssignments().getPotentialOwners()) {
                if (entity instanceof UserImpl) {
                    principals.add(JahiaUserManagerService.getInstance().lookupUserByPath(entity.getId()).getJahiaUser());
                } else if (entity instanceof GroupImpl) {
                    principals.add(JahiaGroupManagerService.getInstance().lookupGroupByPath(entity.getId()).getJahiaGroup());
                }
            }
            createTask(task, taskInputParameters, taskOutputParameters, principals);
            ((SynchronizedTaskService) taskService).addContent(task.getId(), taskOutputParameters);
            JobDetail jobDetail = BackgroundJob.createJahiaJob("notifyNewTask", NotifyNewTaskJob.class);
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put(NotifyNewTaskJob.TASK_ID,Long.toString(task.getId()));
            try {
                ServicesRegistry.getInstance().getSchedulerService().scheduleJobAtEndOfRequest(jobDetail,true);
            } catch (SchedulerException e) {
                throw new RuntimeException("error while notifying the task_id " + task.getId(),e);
            }
        } catch (RepositoryException e) {
            throw new RuntimeException("Error while setting up task assignees and creating a JCR task", e);
        }
    }


    @Override
    public void afterTaskExitedEvent(Task ti) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void createTask(final Task task,
                              final Map<String, Object> taskInputParameters,
                              final Map<String, Object> taskOutputParameters,
                              final List<JahiaPrincipal> candidates) throws RepositoryException {
        final Workflow workflow = workflowProvider.getWorkflow(Long.toString(task.getTaskData().getProcessInstanceId()), null);

        String userPath = (String) taskInputParameters.get("user");
        if (userPath == null) {
            userPath = workflow.getStartUser();
        }
        final JCRUserNode user = JahiaUserManagerService.getInstance().lookupUserByPath(userPath);

        if (user != null) {
            String workspace = (String) taskInputParameters.get("workspace");
            if (workspace == null) {
                workspace = (String) workflow.getVariables().get("workspace");
            }
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user.getJahiaUser(), workspace, null, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                    JCRNodeWrapper n = session.getNode(user.getPath());
                    JCRNodeWrapper tasks;

                    if (!n.hasNode("workflowTasks")) {
                        tasks = n.addNode("workflowTasks", "jnt:tasks");
                    } else {
                        tasks = n.getNode("workflowTasks");
                    }
                    final String taskName = task.getNames().get(0).getText();
                    JCRNodeWrapper jcrTask = tasks.addNode(JCRContentUtils.findAvailableNodeName(tasks, taskName), "jnt:workflowTask");
                    String definitionKey = JBPM6WorkflowProvider.getDecodedProcessKey(task.getTaskData().getProcessId());
                    jcrTask.setProperty("taskName", taskName);
                    String bundle = workflow.getWorkflowDefinition().getPackageName() + "." + Patterns.SPACE.matcher(definitionKey).replaceAll("");
                    jcrTask.setProperty("taskBundle", bundle);
                    jcrTask.setProperty("taskId", task.getId());
                    jcrTask.setProperty("provider", "jBPM");

                    String uuid = (String) taskInputParameters.get("nodeId");
                    if (uuid == null) {
                        uuid = (String) workflow.getVariables().get("nodeId");
                    }
                    if (uuid != null) {
                        jcrTask.setProperty("targetNode", uuid);
                    }

                    if (task.getTaskData().getExpirationTime() != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(task.getTaskData().getExpirationTime());
                        jcrTask.setProperty("dueDate", calendar);
                    }
                    List<Value> candidatesArray = new ArrayList<Value>();
                    ValueFactory valueFactory = session.getValueFactory();
                    for (JahiaPrincipal principal : candidates) {
                        if (principal instanceof JahiaGroup) {
                            candidatesArray.add(valueFactory.createValue(((JahiaGroup) principal).getGroupKey()));
                        } else if (principal instanceof JahiaUser) {
                            candidatesArray.add(valueFactory.createValue(((JahiaUser) principal).getUserKey()));
                        }
                    }
                    jcrTask.setProperty("candidates", candidatesArray.toArray(new Value[candidatesArray.size()]));
                    WorkflowTask wfTask = workflowProvider.getWorkflowTask(Long.toString(task.getId()), null);
                    Set<String> outcomes = wfTask.getOutcomes();
                    List<Value> outcomesArray = new ArrayList<Value>();
                    for (String outcome : outcomes) {
                        outcomesArray.add(valueFactory.createValue(outcome));
                    }
                    jcrTask.setProperty("possibleOutcomes", outcomesArray.toArray(new Value[outcomes.size()]));
                    jcrTask.setProperty("state", "active");
                    jcrTask.setProperty("type", "workflow");
                    jcrTask.setProperty("jcr:title", "##resourceBundle(" +
                            Patterns.SPACE.matcher(taskName).replaceAll(".").trim().toLowerCase() +
                            "," +
                            bundle +
                            ")## : " +
                            session.getNodeByIdentifier(uuid).getDisplayableName());

                    if (taskInputParameters.containsKey("jcr:title") && taskInputParameters.get("jcr:title") instanceof WorkflowVariable) {
                        jcrTask.setProperty("description", ((WorkflowVariable) taskInputParameters.get("jcr:title")).getValue());
                    }
                    String form = WorkflowService.getInstance().getFormForAction(definitionKey, taskName);
                    if (form != null && NodeTypeRegistry.getInstance().hasNodeType(form)) {
                        JCRNodeWrapper data = jcrTask.addNode("taskData", form);
                        ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(form);
                        Map<String, ExtendedPropertyDefinition> m = type.getPropertyDefinitionsAsMap();
                        for (String s : m.keySet()) {
                            Object variable = taskInputParameters.get(s);
                            if (variable instanceof WorkflowVariable) {
                                WorkflowVariable workflowVariable = (WorkflowVariable) variable;
                                data.setProperty(s, workflowVariable.getValue(), workflowVariable.getType());
                            } else if (variable instanceof List) {
                                List list = (List) variable;
                                List<Value> v = new ArrayList<Value>();
                                for (Object o : list) {
                                    if (o instanceof WorkflowVariable) {
                                        WorkflowVariable workflowVariable = (WorkflowVariable) o;
                                        v.add(session.getValueFactory().createValue(workflowVariable.getValue(), workflowVariable.getType()));
                                    }
                                }
                                data.setProperty(s, v.toArray(new Value[v.size()]));
                            }
                        }
                    }

                    session.save();

                    taskOutputParameters.put("task-" + task.getId(), jcrTask.getIdentifier());

                    return null;
                }
            });
        }
    }
}
