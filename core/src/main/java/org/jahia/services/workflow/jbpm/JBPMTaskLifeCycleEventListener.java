/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.workflow.jbpm;

import org.drools.core.command.impl.KnowledgeCommandContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowTask;
import org.jahia.services.workflow.WorkflowVariable;
import org.jahia.services.workflow.jbpm.custom.AbstractTaskLifeCycleEventListener;
import org.jahia.utils.Patterns;
import org.jbpm.runtime.manager.impl.task.SynchronizedTaskService;
import org.jbpm.services.task.events.AfterTaskAddedEvent;
import org.kie.api.task.model.Task;

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

    private static final long serialVersionUID = 4434614988996316632L;

    @Override
    public void afterTaskActivatedEvent(Task ti) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void afterTaskClaimedEvent(Task ti) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void afterTaskSkippedEvent(Task ti) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void afterTaskStartedEvent(Task ti) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void afterTaskStoppedEvent(Task ti) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void afterTaskCompletedEvent(Task ti) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void afterTaskFailedEvent(Task ti) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void afterTaskAddedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskAddedEvent Task task) {
        Map<String, Object> taskInputParameters = getTaskInputParameters(task);
        Map<String, Object> taskOutputParameters = getTaskOutputParameters(task, taskInputParameters);
        String nodeId = (String) taskInputParameters.get("nodeId");
        ;
        Locale locale = (Locale) taskInputParameters.get("locale");
        WorkflowDefinition def = workflowProvider.getWorkflowDefinitionById(task.getTaskData().getProcessId(), locale);
        JCRNodeWrapper node = null;
        try {
            node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(nodeId);
            String name = JBPM6WorkflowProvider.getI18NText(task.getNames(), locale);

            final List<JahiaPrincipal> principals = WorkflowService.getInstance().getAssignedRole(node, def, name, Long.toString(task.getTaskData().getProcessInstanceId()));
            createTask(task, taskInputParameters, taskOutputParameters, principals);
            ((SynchronizedTaskService) taskService).addContent(task.getId(), taskOutputParameters);

            observationManager.notifyNewTask("jBPM", Long.toString(task.getId()));
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
        final String username = (String) taskInputParameters.get("user");
        final JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(username);

        if (user != null) {
            final Locale locale = (Locale) taskInputParameters.get("locale");
            JCRTemplate.getInstance().doExecuteWithSystemSession(user.getUsername(), (String) taskInputParameters.get("workspace"), null, new JCRCallback<Object>() {
                @SuppressWarnings("unchecked")
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRUser jcrUser;
                    if (user instanceof JCRUser) {
                        jcrUser = (JCRUser) user;
                    } else {
                        jcrUser = ((JCRUserManagerProvider) SpringContextSingleton.getBean("JCRUserManagerProvider")).lookupExternalUser(user);
                    }
                    JCRNodeWrapper n = jcrUser.getNode(session);
                    JCRNodeWrapper tasks;

                    if (!n.hasNode("workflowTasks")) {
                        tasks = n.addNode("workflowTasks", "jnt:tasks");
                    } else {
                        tasks = n.getNode("workflowTasks");
                    }
                    JCRNodeWrapper jcrTask = tasks.addNode(JCRContentUtils.findAvailableNodeName(tasks, JBPM6WorkflowProvider.getI18NText(task.getNames(), locale)), "jnt:workflowTask");
                    String definitionKey = JBPM6WorkflowProvider.getDecodedProcessKey(task.getTaskData().getProcessId());
                    jcrTask.setProperty("taskName", JBPM6WorkflowProvider.getI18NText(task.getNames(), locale));
                    String bundle = WorkflowService.class.getPackage().getName() + "." + Patterns.SPACE.matcher(definitionKey).replaceAll("");
                    jcrTask.setProperty("taskBundle", bundle);
                    jcrTask.setProperty("taskId", task.getId());
                    jcrTask.setProperty("provider", "jBPM");

                    String uuid = (String) taskInputParameters.get("nodeId");
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
                            candidatesArray.add(valueFactory.createValue("g:" + principal.getName()));
                        } else if (principal instanceof JahiaUser) {
                            candidatesArray.add(valueFactory.createValue("u:" + principal.getName()));
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
                            Patterns.SPACE.matcher(JBPM6WorkflowProvider.getI18NText(task.getNames(), locale)).replaceAll(".").trim().toLowerCase() +
                            "," +
                            bundle +
                            ")## : " +
                            session.getNodeByIdentifier(uuid).getDisplayableName());

                    if (taskInputParameters.get("jcr:title") instanceof List && ((List<WorkflowVariable>) taskInputParameters.get("jcr:title")).size() > 0) {
                        jcrTask.setProperty("description", ((List<WorkflowVariable>) taskInputParameters.get("jcr:title")).get(0).getValue());
                    }

                    String form = (String) taskInputParameters.get("formName");
                    if (form != null && NodeTypeRegistry.getInstance().hasNodeType(form)) {
                        JCRNodeWrapper data = jcrTask.addNode("taskData", form);
                        ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(form);
                        Map<String, ExtendedPropertyDefinition> m = type.getPropertyDefinitionsAsMap();
                        for (String s : m.keySet()) {
                            Object variable = taskInputParameters.get(s);
                            if (variable instanceof List) {
                                List<WorkflowVariable> list = (List<WorkflowVariable>) variable;
                                if (m.get(s).isMultiple()) {
//                                    data.setProperty(s, list.get(0).getValue());
                                } else if (!list.isEmpty()) {
                                    data.setProperty(s, list.get(0).getValue());
                                }
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
