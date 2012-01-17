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

package org.jahia.services.workflow;

import com.google.common.collect.Sets;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.jbpm.JBPMProvider;
import org.jahia.test.TestHelper;
import org.jbpm.api.JbpmException;
import org.junit.*;
import org.slf4j.Logger;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link WorkflowService}.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 2 févr. 2010
 */
public class WorkflowServiceTest {
    private final static String TESTSITE_NAME = "jBPMWorkflowServiceTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME + "/home";
    private static JahiaSite site;
    private static JahiaUser johndoe;
    private static JahiaUser johnsmoe;
    private static JahiaGroup group;
    private static final long MILLIS = 1000l;
    private HashMap<String, Object> emptyMap;
    private static final String PROVIDER = "jBPM";
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(WorkflowServiceTest.class);
    private static int nodeCounter;
    private JCRNodeWrapper stageNode;
    private static WorkflowService service;
    private static JCRPublicationService publicationService;
    private String processId;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME);
        assertNotNull("Unable to create test site", site);
        initUsersGroup();
        JCRSessionFactory.getInstance().getCurrentUserSession().save();
        service = WorkflowService.getInstance();
        publicationService = JCRPublicationService.getInstance();
        JBPMProvider jBPMProvider = (JBPMProvider) SpringContextSingleton.getBean("jBPMProvider");
        jBPMProvider.registerListeners();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JahiaUserManagerService userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JahiaGroupManagerService groupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        groupManagerService.deleteGroup(groupManagerService.lookupGroup(site.getID(), "taskUsersGroup"));
        userManagerService.deleteUser(userManagerService.lookupUser("johndoe"));
        userManagerService.deleteUser(userManagerService.lookupUser("johnsmoe"));
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
        JCRSessionFactory.getInstance().closeAllSessions();
    }
    
    @Before
    public void setUp() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper root = session.getNode(SITECONTENT_ROOT_NODE);
        session.checkout(root);
        stageNode = root.addNode("child-" + ++nodeCounter, "jnt:text");
        session.save();
        emptyMap = new HashMap<String, Object>();
    }

    private void getCleanStageNode() throws Exception {
        JCRSessionFactory.getInstance().closeAllSessions();
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        stageNode = session.getNode(SITECONTENT_ROOT_NODE+"/child-" + nodeCounter);
    }

    @Test
    public void testGetPossibleWorkflow() throws Exception {
        final Collection<WorkflowDefinition> workflowList =  WorkflowService.getInstance().getPossibleWorkflows(stageNode,true,Locale.ENGLISH).values();
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
    }

    @Test
    public void testGetActiveWorkflows() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        List<WorkflowVariable> values = new ArrayList<WorkflowVariable>(1);
        map.put("startDate",values);
        map.put("endDate",values);
        final Collection<WorkflowDefinition> workflowList = service.getPossibleWorkflows(stageNode, true,Locale.ENGLISH).values();
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        final WorkflowDefinition workflow = workflowList.iterator().next();
        assertNotNull("Worflow should not be null", workflow);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live", false));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map, null);
        assertNotNull("The startup of a process should have return an id", processId);
        Thread.sleep(MILLIS);
        getCleanStageNode();
        final List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        assertTrue("There should be some active activities for the first workflow in jBPM", activeWorkflows.get(0)
                .getAvailableActions().size() > 0);
    }

    @Test
    public void testSignalProcess() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        List<WorkflowVariable> values = new ArrayList<WorkflowVariable>(1);
        map.put("startDate",values);
        map.put("endDate",values);
        final List<WorkflowDefinition> workflowList = service.getWorkflowDefinitionsForType("publish", Locale.ENGLISH);
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        WorkflowDefinition workflow = null;
        for (WorkflowDefinition workflowDefinition : workflowList) {
            if ("2 Step Publication Process".equals(workflowDefinition.getName())) {
                workflow = workflowDefinition;
                break;
            }
        }
        assertNotNull("Unable to find workflow process '2 Step Publication Process'", workflow);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live", false));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map,null);
        assertNotNull("The startup of a process should have return an id", processId);
        Thread.sleep(MILLIS);
        getCleanStageNode();
        final List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        final Set<WorkflowAction> availableActions = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM",
                   availableActions.size() > 0);
        WorkflowAction action = availableActions.iterator().next();
        if (action instanceof WorkflowTask) {
            service.signalProcess(processId, action.getName(), ((WorkflowTask) action).getOutcomes().contains(
                    "accept") ? "accept" : "reject", PROVIDER, emptyMap);
        } else {
            service.signalProcess(processId, action.getName(), PROVIDER, emptyMap);
        }
        final List<Workflow> newActiveWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH);
        assertTrue("There should be some active workflow in jBPM", newActiveWorkflows.size() > 0);
        final Set<WorkflowAction> newAvailableActions = newActiveWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM",
                   availableActions.size() > 0);
        assertFalse("Available actions should not match", availableActions.equals(newAvailableActions));
        assertTrue("Available action should match between service.getActiveWorkflows and getAvailableActions",
                   newAvailableActions.equals(service.getAvailableActions(processId, PROVIDER, Locale.ENGLISH)));
    }

    @Test
    public void testAssignTask() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        List<WorkflowVariable> values = new ArrayList<WorkflowVariable>(1);
        map.put("startDate",values);
        map.put("endDate",values);
        final Collection<WorkflowDefinition> workflowList = service.getPossibleWorkflows(stageNode, true,Locale.ENGLISH).values();
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        WorkflowDefinition workflow = null;
        for (WorkflowDefinition currentWorkflow : workflowList) {
            if (!currentWorkflow.getKey().contains("unpublic")) {
                workflow = currentWorkflow;
                break;
            }
        }
        assertNotNull("Workflow should not be null", workflow);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live", false));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map,null);
        assertNotNull("The startup of a process should have return an id", processId);
        Thread.sleep(MILLIS);
        getCleanStageNode();
        final List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;
        JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser("root");
        assertNotNull(user);
        service.assignTask(task.getId(), PROVIDER, user);
        List<WorkflowTask> forUser = service.getTasksForUser(user, Locale.ENGLISH);
        assertTrue(forUser.size() > 0);
        final HashMap<String, Object> emptyMap = new HashMap<String, Object>();
        WorkflowTask workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), PROVIDER, workflowTask.getOutcomes().contains(
                "accept") ? "accept" : "reject", emptyMap, johndoe);
        assertTrue(service.getTasksForUser(user, Locale.ENGLISH).size() < forUser.size());
        assertFalse(service.getActiveWorkflows(stageNode, Locale.ENGLISH).equals(actionSet));
    }

    @Test
    public void testAddParticipatingGroup() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        List<WorkflowVariable> values = new ArrayList<WorkflowVariable>(1);
        map.put("startDate",values);
        map.put("endDate",values);
        final Collection<WorkflowDefinition> workflowList = service.getPossibleWorkflows(stageNode, true,Locale.ENGLISH).values();
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        final WorkflowDefinition workflow = workflowList.iterator().next();
        assertNotNull("Workflow should not be null", workflow);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live", false));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map,null);
        assertNotNull("The startup of a process should have return an id", processId);
        Thread.sleep(MILLIS);
        getCleanStageNode();
        final List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;
        service.addParticipatingGroup(task.getId(), PROVIDER, group, WorkflowService.CANDIDATE);
        List<WorkflowTask> johnDoeList = service.getTasksForUser(johndoe, Locale.ENGLISH);
        List<WorkflowTask> johnSmoeList = service.getTasksForUser(johnsmoe, Locale.ENGLISH);
        assertTrue("John Doe and John Smoe should have the same tasks list", johnDoeList.equals(johnSmoeList));
        service.assignTask(johnDoeList.get(0).getId(), PROVIDER, johndoe);
        johnSmoeList = service.getTasksForUser(johnsmoe, Locale.ENGLISH);
        johnDoeList = service.getTasksForUser(johndoe, Locale.ENGLISH);
        assertFalse("John Doe and John Smoe should not have same tasks list", johnDoeList.equals(johnSmoeList));
        service.completeTask(task.getId(), PROVIDER, task.getOutcomes().iterator().next(),
                             new HashMap<String, Object>(), johndoe);
    }

    @Test
    public void testFullProcess2StepPublication() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        List<WorkflowVariable> values = new ArrayList<WorkflowVariable>(1);
        map.put("startDate",values);
        map.put("endDate",values);
        final List<WorkflowDefinition> workflowList = service.getWorkflowDefinitionsForType("publish", Locale.ENGLISH);
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        WorkflowDefinition workflow = null;
        for (WorkflowDefinition workflowDefinition : workflowList) {
            if ("2 Step Publication Process".equals(workflowDefinition.getName())) {
                workflow = workflowDefinition;
                break;
            }
        }
        assertNotNull("Unable to find workflow process '2 Step Publication Process'", workflow);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live", false));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER,map,null);
        assertNotNull("The startup of a process should have return an id", processId);
        Thread.sleep(MILLIS);
        getCleanStageNode();
        final List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;
        service.assignTask(task.getId(), PROVIDER, johndoe);
        List<WorkflowTask> forUser = service.getTasksForUser(johndoe, Locale.ENGLISH);
        assertTrue(forUser.size() > 0);
        WorkflowTask workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), PROVIDER, "accept", emptyMap, johndoe);
        assertTrue(service.getTasksForUser(johndoe, Locale.ENGLISH).size() < forUser.size());
        assertFalse(service.getActiveWorkflows(stageNode, Locale.ENGLISH).equals(actionSet));
        // Assign john smoe to the next task
        actionSet = service.getAvailableActions(processId, PROVIDER, Locale.ENGLISH);
        service.assignTask(((WorkflowTask)actionSet.iterator().next()).getId(), PROVIDER, johnsmoe);
        // Rollback to previous task
        forUser = service.getTasksForUser(johnsmoe, Locale.ENGLISH);
        assertTrue("John Smoe task list should not be empty", forUser.size() > 0);
        assertTrue("Current task should be final review", forUser.get(0).getName().equals("final review"));
        workflowTask = forUser.get(0);
        assertTrue("Final review should have 3 outcomes", workflowTask.getOutcomes().size() == 3);
        assertTrue("Final review should contains correction needed as an outcome", workflowTask.getOutcomes().contains(
                "correction needed"));
        service.completeTask(workflowTask.getId(), workflowTask.getProvider(), "correction needed", emptyMap,
                             johnsmoe);
        assertTrue("Current Task should be finish correction as we have asked for corrections", service.getAvailableActions(
                processId, PROVIDER, Locale.ENGLISH).iterator().next().getName().equals("finish correction"));
        // Assign john doe to task
        service.assignTask(((WorkflowTask)service.getAvailableActions(processId, PROVIDER, Locale.ENGLISH).iterator().next()).getId(),
                           PROVIDER, johndoe);
        // Complete task
        service.completeTask(service.getTasksForUser(johndoe, Locale.ENGLISH).get(0).getId(), PROVIDER, "finished", emptyMap,
                             johnsmoe);
        // Assign john smoe to the next task
        service.assignTask(((WorkflowTask)service.getAvailableActions(processId, PROVIDER, Locale.ENGLISH).iterator().next()).getId(),
                           PROVIDER, johnsmoe);
        // Complete Task with accept
        service.completeTask(service.getTasksForUser(johnsmoe, Locale.ENGLISH).get(0).getId(), PROVIDER, "accept", emptyMap,
                             johnsmoe);
        // Verify we are at publish state
        assertTrue("Current Task should be final review as we have accepted the correction",
                   service.getAvailableActions(processId, PROVIDER, Locale.ENGLISH).iterator().next().getName().equals("final review"));
    }

    @After
    public void tearDown() throws Exception {
        if(processId!=null){
        	try {
        		service.deleteProcess(processId,PROVIDER);
        	} catch (JbpmException e) {
        		// ignore it
        	}
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    private static void initUsersGroup() {
        JahiaUserManagerService userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JahiaGroupManagerService groupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        johndoe = userManagerService.lookupUser("johndoe");
        johnsmoe = userManagerService.lookupUser("johnsmoe");
        Properties properties = new Properties();
        if (johndoe == null) {
            properties.setProperty("j:firstName", "John");
            properties.setProperty("j:lastName", "Doe");
//            properties.setProperty("j:email", "johndoe@localhost.com");
            johndoe = userManagerService.createUser("johndoe", "johndoe", properties);
        }
        if (johnsmoe == null) {
            properties = new Properties();
            properties.setProperty("j:firstName", "John");
            properties.setProperty("j:lastName", "Smoe");
//            properties.setProperty("j:email", "johnsmoe@localhost.com");
            johnsmoe = userManagerService.createUser("johnsmoe", "johnsmoe", properties);
        }
        group = groupManagerService.createGroup(site.getID(), "taskUsersGroup", new Properties(), true);
        group.addMember(johndoe);
        group.addMember(johnsmoe);

//        RoleBasedAccessControlService roleService = (RoleBasedAccessControlService) SpringContextSingleton.getInstance().getContext().getBean("org.jahia.services.rbac.jcr.RoleBasedAccessControlService");
//        if(roleService!=null){
//            try {
//                roleService.grantRole(group,new RoleIdentity("editor-in-chief"));
//            } catch (RepositoryException e) {
//                logger.error(e.getMessage(), e);
//            }
//        }
    }

    @Test
    public void test2StepPublicationAccept() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        List<WorkflowVariable> values = new ArrayList<WorkflowVariable>(1);
        map.put("startDate",values);
        map.put("endDate",values);
        final List<WorkflowDefinition> workflowList = service.getWorkflowDefinitionsForType("publish", Locale.ENGLISH);
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        WorkflowDefinition workflow = null;
        for (WorkflowDefinition workflowDefinition : workflowList) {
            if ("2 Step Publication Process".equals(workflowDefinition.getName())) {
                workflow = workflowDefinition;
                break;
            }
        }
        assertNotNull("Unable to find workflow process '2 Step Publication Process'", workflow);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live", false));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map,null);
        assertNotNull("The startup of a process should have return an id", processId);
        Thread.sleep(MILLIS);
        getCleanStageNode();
        List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;
        service.assignTask(task.getId(), PROVIDER, johndoe);
        List<WorkflowTask> forUser = service.getTasksForUser(johndoe, Locale.ENGLISH);
        assertTrue(forUser.size() > 0);
        WorkflowTask workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), PROVIDER, "accept", emptyMap, johndoe);
        assertTrue(service.getTasksForUser(johndoe, Locale.ENGLISH).size() < forUser.size());
        
        activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH);
        actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        task = (WorkflowTask) action;
        service.assignTask(task.getId(), PROVIDER, johndoe);
        forUser = service.getTasksForUser(johndoe, Locale.ENGLISH);
        assertTrue(forUser.size() > 0);
        workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), PROVIDER, "publish", emptyMap, johndoe);
        assertTrue(service.getTasksForUser(johndoe, Locale.ENGLISH).size() < forUser.size());

        assertTrue("The workflow process is not completed", service.getActiveWorkflows(stageNode, Locale.ENGLISH).isEmpty());
    }

    @Test
    public void test1StepPublicationAccept() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        List<WorkflowVariable> values = new ArrayList<WorkflowVariable>(1);
        map.put("startDate",values);
        map.put("endDate",values);
        final Collection<WorkflowDefinition> workflowList = service.getPossibleWorkflows(stageNode, true,Locale.ENGLISH).values();
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        WorkflowDefinition workflow = null;
        for (WorkflowDefinition workflowDefinition : workflowList) {
            if ("1 Step Publication Process".equals(workflowDefinition.getName())) {
                workflow = workflowDefinition;
                break;
            }
        }
        assertNotNull("Unable to find workflow process '1 Step Publication Process'", workflow);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live", false));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map,null);
        assertNotNull("The startup of a process should have return an id", processId);
        Thread.sleep(MILLIS);
        getCleanStageNode();
        final List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;
        service.assignTask(task.getId(), PROVIDER, johndoe);
        List<WorkflowTask> forUser = service.getTasksForUser(johndoe, Locale.ENGLISH);
        assertTrue(forUser.size() > 0);
        WorkflowTask workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), PROVIDER, "accept", emptyMap, johndoe);
        assertTrue(service.getTasksForUser(johndoe, Locale.ENGLISH).size() < forUser.size());
        assertTrue("The workflow process is not completed", service.getActiveWorkflows(stageNode, Locale.ENGLISH).isEmpty());
    }

    @Test
    public void test1StepPublicationReject() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        List<WorkflowVariable> values = new ArrayList<WorkflowVariable>(1);
        map.put("startDate",values);
        map.put("endDate",values);
        final Collection<WorkflowDefinition> workflowList = service.getPossibleWorkflows(stageNode, true,Locale.ENGLISH).values();
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        WorkflowDefinition workflow = null;
        for (WorkflowDefinition workflowDefinition : workflowList) {
            if ("1 Step Publication Process".equals(workflowDefinition.getName())) {
                workflow = workflowDefinition;
                break;
            }
        }
        assertNotNull("Unable to find workflow process '1 Step Publication Process'", workflow);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live", false));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map,null);
        assertNotNull("The startup of a process should have return an id", processId);
        Thread.sleep(MILLIS);
        getCleanStageNode();
        final List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;
        service.assignTask(task.getId(), PROVIDER, johndoe);
        List<WorkflowTask> forUser = service.getTasksForUser(johndoe, Locale.ENGLISH);
        assertTrue(forUser.size() > 0);
        WorkflowTask workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), PROVIDER, "reject", emptyMap, johndoe);
        assertTrue(service.getTasksForUser(johndoe, Locale.ENGLISH).size() < forUser.size());
        assertTrue("The workflow process is not completed", service.getActiveWorkflows(stageNode, Locale.ENGLISH).isEmpty());
    }

    @Test
    public void testHistory() throws Exception {
        test1StepPublicationAccept();
        test1StepPublicationReject();
        List<HistoryWorkflow> history = service.getHistoryWorkflows(stageNode, Locale.ENGLISH);
        assertEquals("Node should have two history records", 2, history.size());
    }

    @Test
    public void testHistoryTasks() throws Exception {
        test2StepPublicationAccept();
        List<HistoryWorkflow> history = service.getHistoryWorkflows(stageNode, Locale.ENGLISH);
        assertEquals("Node should have one history record", 1, history.size());
        HistoryWorkflow historyItem = history.get(0);
        List<HistoryWorkflowTask> tasks = service.getHistoryWorkflowTasks(historyItem.getProcessId(), historyItem
                .getProvider(), null);
        assertEquals("The workflow process should have two history task records", 2, tasks.size());
    }
}
