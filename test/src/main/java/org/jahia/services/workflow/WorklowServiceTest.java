/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow;

import junit.framework.TestCase;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.Before;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 2 févr. 2010
 */
public class WorklowServiceTest extends TestCase {
    private final static String TESTSITE_NAME = "jBPMWorkflowServiceTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private JahiaSite site;
    private JahiaUser johndoe;
    private JahiaUser johnsmoe;
    private JahiaGroup group;

    @Before
    public void setUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME);
        initUsersGroup();
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
    }

    @org.junit.Test
    public void testGetPossibleWorkflow() throws Exception {
        final Map<String, List<Workflow>> possibleWorkflows = WorkflowService.getInstance().getPossibleWorkflows(null);
        assertTrue("There should be some workflows already deployed", possibleWorkflows.size() > 0);
        final List<Workflow> workflowList = possibleWorkflows.get("jBPM");
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
    }

    @org.junit.Test
    public void testGetActiveWorkflows() throws Exception {
        final WorkflowService service = WorkflowService.getInstance();
        final Map<String, List<Workflow>> possibleWorkflows = service.getPossibleWorkflows(null);
        assertTrue("There should be some workflows already deployed", possibleWorkflows.size() > 0);
        final List<Workflow> workflowList = possibleWorkflows.get("jBPM");
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        final Workflow workflow = workflowList.get(0);
        assertNotNull("Worflow should not be null", workflow);
        JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper stageRootNode = sessionWrapper.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper stageNode = stageRootNode.getNode("home");
        final String processId = service.startProcess(stageNode, workflow.getId(), "jBPM",
                                                      new HashMap<String, Object>());
        assertNotNull("The startup of a process should have return an id", processId);
        final Map<String, List<Workflow>> activeWorkflows = service.getActiveWorkflows(stageNode);
        assertTrue("There should be some active workflows providers", activeWorkflows.size() > 0);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.get("jBPM").size() > 0);
        assertTrue("There should be some active activities for the first workflow in jBPM", activeWorkflows.get(
                "jBPM").get(0).getAvailableActions().size() > 0);
    }

    @org.junit.Test
    public void testSignalProcess() throws Exception {
        final WorkflowService service = WorkflowService.getInstance();
        final Map<String, List<Workflow>> possibleWorkflows = service.getPossibleWorkflows(null);
        assertTrue("There should be some workflows already deployed", possibleWorkflows.size() > 0);
        final List<Workflow> workflowList = possibleWorkflows.get("jBPM");
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        final Workflow workflow = workflowList.get(0);
        assertNotNull("Worflow should not be null", workflow);
        JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper stageRootNode = sessionWrapper.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper stageNode = stageRootNode.getNode("home");
        final HashMap<String, Object> emptyMap = new HashMap<String, Object>();
        final String processId = service.startProcess(stageNode, workflow.getId(), "jBPM", emptyMap);
        assertNotNull("The startup of a process should have return an id", processId);
        final Map<String, List<Workflow>> activeWorkflows = service.getActiveWorkflows(stageNode);
        assertTrue("There should be some active workflows providers", activeWorkflows.size() > 0);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.get("jBPM").size() > 0);
        final Set<WorkflowAction> availableActions = activeWorkflows.get("jBPM").get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM",
                   availableActions.size() > 0);
        service.signalProcess(processId, availableActions.iterator().next().getName(), "jBPM", emptyMap);
        final Map<String, List<Workflow>> newActiveWorkflows = service.getActiveWorkflows(stageNode);
        assertTrue("There should be some active workflows providers", newActiveWorkflows.size() > 0);
        assertTrue("There should be some active workflow in jBPM", newActiveWorkflows.get("jBPM").size() > 0);
        final Set<WorkflowAction> newAvailableActions = newActiveWorkflows.get("jBPM").get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM",
                   availableActions.size() > 0);
        assertFalse("Availables actions should not match", availableActions.equals(newAvailableActions));
        assertTrue("Availables action should match between service.getActiveWorkflows and getAvailableActions",
                   newAvailableActions.equals(service.getAvailableActions(processId, "jBPM")));
    }

    @org.junit.Test
    public void testAssignTask() throws Exception {
        final WorkflowService service = WorkflowService.getInstance();
        final Map<String, List<Workflow>> possibleWorkflows = service.getPossibleWorkflows(null);
        assertTrue("There should be some workflows already deployed", possibleWorkflows.size() > 0);
        final List<Workflow> workflowList = possibleWorkflows.get("jBPM");
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        final Workflow workflow = workflowList.get(0);
        assertNotNull("Worflow should not be null", workflow);
        JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper stageRootNode = sessionWrapper.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper stageNode = stageRootNode.getNode("home");
        final String processId = service.startProcess(stageNode, workflow.getId(), "jBPM",
                                                      new HashMap<String, Object>());
        assertNotNull("The startup of a process should have return an id", processId);
        final Map<String, List<Workflow>> activeWorkflows = service.getActiveWorkflows(stageNode);
        assertTrue("There should be some active workflows providers", activeWorkflows.size() > 0);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.get("jBPM").size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get("jBPM").get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;
        JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser("root");
        assertNotNull(user);
        service.assignTask(task.getName(), processId, "jBPM", user);
        List<WorkflowTask> forUser = service.getTasksForUser(user);
        assertTrue(forUser.size() > 0);
        final HashMap<String, Object> emptyMap = new HashMap<String, Object>();
        WorkflowTask workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), "jBPM", workflowTask.getOutcomes().iterator().next(), emptyMap);
        assertTrue(service.getTasksForUser(user).size() < forUser.size());
        assertFalse(service.getActiveWorkflows(stageNode).equals(actionSet));
    }

    @org.junit.Test
    public void testAddParticipatingGroup() throws Exception {
        final WorkflowService service = WorkflowService.getInstance();
        final Map<String, List<Workflow>> possibleWorkflows = service.getPossibleWorkflows(null);
        assertTrue("There should be some workflows already deployed", possibleWorkflows.size() > 0);
        String provider = "jBPM";
        final List<Workflow> workflowList = possibleWorkflows.get(provider);
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        final Workflow workflow = workflowList.get(0);
        assertNotNull("Worflow should not be null", workflow);
        JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper stageRootNode = sessionWrapper.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper stageNode = stageRootNode.getNode("home");
        final String processId = service.startProcess(stageNode, workflow.getId(), provider,
                                                      new HashMap<String, Object>());
        assertNotNull("The startup of a process should have return an id", processId);
        final Map<String, List<Workflow>> activeWorkflows = service.getActiveWorkflows(stageNode);
        assertTrue("There should be some active workflows providers", activeWorkflows.size() > 0);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.get(provider).size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get(provider).get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;

        service.addParticipatingGroup(task.getId(), provider, group, WorkflowService.CANDIDATE);
        List<WorkflowTask> johnDoeList = service.getTasksForUser(johndoe);
        List<WorkflowTask> johnSmoeList = service.getTasksForUser(johnsmoe);
        assertTrue("John Doe and John Smoe should have the same tasks list", johnDoeList.equals(johnSmoeList));
        service.assignTask(johnDoeList.get(0).getName(), processId, provider, johndoe);
        johnSmoeList = service.getTasksForUser(johnsmoe);
        johnDoeList = service.getTasksForUser(johndoe);
        assertFalse("John Doe and John Smoe should not have same tasks list", johnDoeList.equals(johnSmoeList));
        service.completeTask(task.getId(), provider, task.getOutcomes().iterator().next(),
                             new HashMap<String, Object>());
    }

    @After
    public void tearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
        JahiaUserManagerService userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JahiaGroupManagerService groupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        groupManagerService.deleteGroup(groupManagerService.lookupGroup(site.getID(), "taskUsersGroup"));
        userManagerService.deleteUser(userManagerService.lookupUser("johndoe"));
        userManagerService.deleteUser(userManagerService.lookupUser("johnsmoe"));
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
        session.logout();
    }

    private void initUsersGroup() {
        JahiaUserManagerService userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JahiaGroupManagerService groupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        johndoe = userManagerService.lookupUser("johndoe");
        johnsmoe = userManagerService.lookupUser("johnsmoe");
        Properties properties = new Properties();
        if (johndoe == null) {
            properties.setProperty("j:firstName", "John");
            properties.setProperty("j:lastName", "Doe");
            properties.setProperty("j:email", "johndoe@localhost.com");
            johndoe = userManagerService.createUser("johndoe", "johndoe", properties);
        }
        if (johnsmoe == null) {
            properties = new Properties();
            properties.setProperty("j:firstName", "John");
            properties.setProperty("j:lastName", "Smoe");
            properties.setProperty("j:email", "johnsmoe@localhost.com");
            johnsmoe = userManagerService.createUser("johnsmoe", "johnsmoe", properties);
        }
        group = groupManagerService.createGroup(site.getID(), "taskUsersGroup", null, true);
        group.addMember(johndoe);
        group.addMember(johnsmoe);
    }
}
