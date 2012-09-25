/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.services.workflow.WorkflowVariable;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.model.Transition;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;
import org.jbpm.pvm.internal.task.TaskImpl;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import java.util.*;


/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 4 févr. 2010
 */
public class JBPMTaskAssignmentListener implements AssignmentHandler {

    private static final long serialVersionUID = 4434614988996316632L;

    /**
     * sets the actorId and candidates for the given task.
     */
    public void assign(final Assignable assignable, final OpenExecution execution) throws Exception {

        WorkflowDefinition def = (WorkflowDefinition) execution.getVariable("workflow");
        String id = (String) execution.getVariable("nodeId");
        JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(id);
        String name = null;
        if (assignable instanceof TaskImpl) {
            name = ((TaskImpl)assignable).getActivityName();
        }
        final List<JahiaPrincipal> principals = WorkflowService.getInstance().getAssignedRole(node, def, name, execution.getProcessInstance().getId());
        for (JahiaPrincipal principal : principals) {
            if (principal instanceof JahiaGroup) {
                assignable.addCandidateGroup(((JahiaGroup)principal).getGroupKey());
            } else if (principal instanceof JahiaUser) {
                assignable.addCandidateUser(((JahiaUser)principal).getUserKey());
            }
        }
        assignable.addCandidateGroup(ServicesRegistry.getInstance().getJahiaGroupManagerService().getAdministratorGroup(0).getGroupKey());

        createTask(assignable, execution, principals);

        if (assignable instanceof TaskImpl) {
            WorkflowService.getInstance().notifyNewTask("jBPM", ((TaskImpl)assignable).getId());
        }
    }

    protected void createTask(final Assignable assignable, final OpenExecution execution, final List<JahiaPrincipal> candidates) throws RepositoryException {
        final String username = (String) execution.getVariable("user");
        final JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(username);

        if (assignable instanceof TaskImpl && user != null) {
            final Locale locale = (Locale) execution.getVariable("locale");
            JCRTemplate.getInstance().doExecuteWithSystemSession(user.getUsername(), (String) execution.getVariable("workspace"), null, new JCRCallback<Object>() {
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
                    TaskImpl wfTask = (TaskImpl) assignable;
                    JCRNodeWrapper task = tasks.addNode(JCRContentUtils.findAvailableNodeName(tasks, wfTask.getName()), "jnt:workflowTask");
                    String definitionKey = wfTask.getProcessInstance().getProcessDefinition().getKey();
                    task.setProperty("taskName", wfTask.getName());
                    String bundle = WorkflowService.class.getPackage().getName() + "." + Patterns.SPACE.matcher(definitionKey).replaceAll("");
                    task.setProperty("taskBundle", bundle);
                    task.setProperty("taskId", wfTask.getId());
                    task.setProperty("provider","jBPM");

                    String uuid = (String) execution.getVariable("nodeId");
                    if (uuid != null) {
                        task.setProperty("targetNode", uuid);
                    }

                    if (wfTask.getDuedate() != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(wfTask.getDuedate());
                        task.setProperty("dueDate", calendar);
                    }
                    List<Value> candidatesArray = new ArrayList<Value>();
                    ValueFactory valueFactory = session.getValueFactory();
                    for (JahiaPrincipal principal : candidates) {
                        if (principal instanceof JahiaGroup) {
                            candidatesArray.add(valueFactory.createValue("g:"+principal.getName()));
                        } else if (principal instanceof JahiaUser) {
                            candidatesArray.add(valueFactory.createValue("u:"+principal.getName()));
                        }
                    }
                    task.setProperty("candidates",candidatesArray.toArray(new Value[candidatesArray.size()]));
                    List<Value> outcomes = new ArrayList<Value>();
                    for (Transition transition : execution.getActivity().getOutgoingTransitions()) {
                        outcomes.add(valueFactory.createValue(transition.getName()));
                    }
                    task.setProperty("possibleOutcomes", outcomes.toArray(new Value[outcomes.size()]));
                    task.setProperty("state", "active");
                    task.setProperty("type", "workflow");
                    /*//todo : get titles for all locales
                    List<Locale> locales = LanguageCodeConverters.getAvailableBundleLocales(bundle, locale);
                    for (Locale aLocale : locales) {
                        try {
                            String taskname = JahiaResourceBundle.lookupBundle(bundle, aLocale).getString(Patterns.SPACE.matcher(wfTask.getName()).replaceAll(".").trim().toLowerCase());*/
                            task.setProperty("jcr:title", "##resourceBundle("+Patterns.SPACE.matcher(wfTask.getName()).replaceAll(".").trim().toLowerCase() + ","+ bundle + ")## : " + session.getNodeByIdentifier(uuid).getDisplayableName());
                        /*} catch (MissingResourceException e) {
                            task.setProperty("jcr:title", wfTask.getName() + " : " + session.getNodeByIdentifier(uuid).getDisplayableName());
                        }
                    }*/

                    if (execution.getVariable("jcr:title") instanceof List && ((List<WorkflowVariable>)execution.getVariable("jcr:title")).size() > 0) {
                        task.setProperty("description", ((List<WorkflowVariable>)execution.getVariable("jcr:title")).get(0).getValue());
                    }

                    String form = wfTask.getTaskDefinition().getFormResourceName();
                    if (form != null && NodeTypeRegistry.getInstance().hasNodeType(form)) {
                        JCRNodeWrapper data = task.addNode("taskData", form);
                        ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(form);
                        Map<String, ExtendedPropertyDefinition> m = type.getPropertyDefinitionsAsMap();
                        for (String s : m.keySet()) {
                            Object variable = execution.getVariable(s);
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

                    execution.setVariable("task-"+wfTask.getId(), task.getIdentifier());
                    return null;
                }
            });
        }
    }
}
