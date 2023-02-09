/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.test.services.workflow;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.*;
import org.jahia.settings.readonlymode.ReadOnlyModeException;
import org.jahia.test.TestHelper;
import org.junit.*;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.util.*;

import static org.jahia.test.TestHelper.triggerScheduledJobsAndWait;
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
    private static final String WORKFLOW_TYPE_1_STEP_PUBLICATION = "1-step-publication";
    private static final String WORKFLOW_TYPE_2_STEP_PUBLICATION = "2-step-publication";
    private static JahiaSite site;
    private static JahiaUser johndoe;
    private static JahiaUser johnsmoe;
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
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JahiaUserManagerService userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        userManagerService.deleteUser(userManagerService.lookupUser("johndoe").getPath(), session);
        userManagerService.deleteUser(userManagerService.lookupUser("johnsmoe").getPath(), session);
        session.save();
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Before
    public void setUp() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper root = session.getNode(SITECONTENT_ROOT_NODE);
        session.checkout(root);
        stageNode = root.addNode("child-" + (++nodeCounter), "jnt:text");
        session.save();
        emptyMap = new HashMap<String, Object>();
    }

    private void getCleanStageNode() throws Exception {
        JCRSessionFactory.getInstance().closeAllSessions();
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        stageNode = session.getNode(SITECONTENT_ROOT_NODE + "/child-" + nodeCounter);
    }

    @Test
    public void testGetPossibleWorkflow() throws Exception {
        // that call also asserts that possible workflows are present
        getPossibleWorkflows();
    }

    @Test
    public void testGetActiveWorkflows() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("startDate", new WorkflowVariable());
        map.put("endDate", new WorkflowVariable());
        getPossibleWorkflowEnsureExists(null);
        final WorkflowDefinition workflow = getPossibleWorkflowEnsureExists(WORKFLOW_TYPE_1_STEP_PUBLICATION);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live"));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map, null);
        assertNotNull("The startup of a process should have return an id", processId);
        triggerScheduledJobsAndWait();
        getCleanStageNode();
        final List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH, null);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        assertTrue("There should be some active activities for the first workflow in jBPM", activeWorkflows.get(0)
                .getAvailableActions().size() > 0);
    }

    @Test
    public void testAssignTask() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("startDate", new WorkflowVariable());
        map.put("endDate", new WorkflowVariable());
        WorkflowDefinition workflow = getPossibleWorkflowEnsureExists(WORKFLOW_TYPE_1_STEP_PUBLICATION);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live"));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map, null);
        assertNotNull("The startup of a process should have return an id", processId);
        triggerScheduledJobsAndWait();
        getCleanStageNode();
        final List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH, null);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;
        JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser("root").getJahiaUser();
        assertNotNull(user);
        service.assignTask(task.getId(), PROVIDER, user);
        triggerScheduledJobsAndWait();
        List<WorkflowTask> forUser = service.getTasksForUser(user, Locale.ENGLISH);
        assertTrue(forUser.size() > 0);
        final HashMap<String, Object> emptyMap = new HashMap<String, Object>();
        WorkflowTask workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), user, PROVIDER,
                "reject", emptyMap);
        triggerScheduledJobsAndWait();
        assertTrue(service.getTasksForUser(user, Locale.ENGLISH).size() < forUser.size());
        getCleanStageNode();
        assertFalse(service.getActiveWorkflows(stageNode, Locale.ENGLISH, null).equals(actionSet));
    }

    @Test
    public void testFullProcess2StepPublication() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("startDate", new WorkflowVariable());
        map.put("endDate", new WorkflowVariable());
        WorkflowDefinition workflow = getPublishWorkflowEnsureExists(WORKFLOW_TYPE_2_STEP_PUBLICATION);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live"));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map, null);
        assertNotNull("The startup of a process should have return an id", processId);
        triggerScheduledJobsAndWait();
        getCleanStageNode();
        final List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH, null);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;
        service.assignTask(task.getId(), PROVIDER, johndoe);
        triggerScheduledJobsAndWait();
        List<WorkflowTask> forUser = service.getTasksForUser(johndoe, Locale.ENGLISH);
        assertTrue(forUser.size() > 0);
        WorkflowTask workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), johndoe, PROVIDER, "accept", emptyMap);
        triggerScheduledJobsAndWait();
        //assertTrue(service.getTasksForUser(johndoe, Locale.ENGLISH).size() < forUser.size());
        getCleanStageNode();
        assertFalse(service.getActiveWorkflows(stageNode, Locale.ENGLISH, null).equals(actionSet));
        // Assign john smoe to the next task
        actionSet = service.getAvailableActions(processId, PROVIDER, Locale.ENGLISH);
        service.assignTask(((WorkflowTask) actionSet.iterator().next()).getId(), PROVIDER, johnsmoe);
        triggerScheduledJobsAndWait();
        // Rollback to previous task
        forUser = service.getTasksForUser(johnsmoe, Locale.ENGLISH);
        assertTrue("John Smoe task list should not be empty", forUser.size() > 0);
        assertTrue("Current task should be final review", forUser.get(0).getName().equals("final review"));
        workflowTask = forUser.get(0);
        assertTrue("Final review should have 3 outcomes", workflowTask.getOutcomes().size() == 3);
        assertTrue("Final review should contains correction needed as an outcome", workflowTask.getOutcomes().contains(
                "correction needed"));
        service.completeTask(workflowTask.getId(), johnsmoe, workflowTask.getProvider(), "correction needed", emptyMap
        );
        triggerScheduledJobsAndWait();
        assertTrue("Current Task should be finish correction as we have asked for corrections", service.getAvailableActions(
                processId, PROVIDER, Locale.ENGLISH).iterator().next().getName().equals("finish correction"));
        // Assign john doe to task
        service.assignTask(((WorkflowTask) service.getAvailableActions(processId, PROVIDER, Locale.ENGLISH).iterator().next()).getId(),
                PROVIDER, johndoe);
        triggerScheduledJobsAndWait();
        // Complete task
        service.completeTask(service.getTasksForUser(johndoe, Locale.ENGLISH).get(0).getId(), johndoe, PROVIDER, "finished", emptyMap
        );
        triggerScheduledJobsAndWait();
        // Assign john smoe to the next task
        service.assignTask(((WorkflowTask) service.getAvailableActions(processId, PROVIDER, Locale.ENGLISH).iterator().next()).getId(),
                PROVIDER, johnsmoe);
        triggerScheduledJobsAndWait();
        // Complete Task with accept
        service.completeTask(service.getTasksForUser(johnsmoe, Locale.ENGLISH).get(0).getId(), johnsmoe, PROVIDER, "accept", emptyMap
        );
        triggerScheduledJobsAndWait();
        // Verify we are at publish state
        assertTrue("Current Task should be final review as we have accepted the correction",
                service.getAvailableActions(processId, PROVIDER, Locale.ENGLISH).iterator().next().getName().equals("final review"));
    }

    @After
    public void tearDown() throws Exception {
        if (processId != null) {
            service.deleteProcess(processId, PROVIDER);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    private static void initUsersGroup() throws RepositoryException {
        JahiaUserManagerService userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JahiaGroupManagerService groupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        johndoe = userManagerService.lookupUser("johndoe") != null ? userManagerService.lookupUser("johndoe").getJahiaUser() : null;
        johnsmoe = userManagerService.lookupUser("johnsmoe")!= null ? userManagerService.lookupUser("johndoe").getJahiaUser() : null;
        Properties properties = new Properties();

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        if (johndoe == null) {
            properties.setProperty("j:firstName", "John");
            properties.setProperty("j:lastName", "Doe");
            johndoe = userManagerService.createUser("johndoe", "johndoe", properties, session).getJahiaUser();
        }
        if (johnsmoe == null) {
            properties = new Properties();
            properties.setProperty("j:firstName", "John");
            properties.setProperty("j:lastName", "Smoe");
            johnsmoe = userManagerService.createUser("johnsmoe", "johnsmoe", properties, session).getJahiaUser();
        }
        JCRGroupNode group = groupManagerService.createGroup(site.getSiteKey(), "taskUsersGroup", new Properties(), true, session);
        group.addMember(johndoe);
        group.addMember(johnsmoe);

        session.getNode("/sites/" + site.getSiteKey()).grantRoles("g:" + group.getName(), ImmutableSet.of("editor-in-chief"));
        session.save();
    }

    @Test
    public void test2StepPublicationAccept() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("startDate", new WorkflowVariable());
        map.put("endDate", new WorkflowVariable());
        WorkflowDefinition workflow = getPublishWorkflowEnsureExists(WORKFLOW_TYPE_2_STEP_PUBLICATION);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live"));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map, null);
        assertNotNull("The startup of a process should have return an id", processId);
        triggerScheduledJobsAndWait();
        getCleanStageNode();
        List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH, null);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;
        service.assignTask(task.getId(), PROVIDER, johndoe);
        triggerScheduledJobsAndWait();
        List<WorkflowTask> forUser = service.getTasksForUser(johndoe, Locale.ENGLISH);
        assertTrue(forUser.size() > 0);
        WorkflowTask workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), johndoe, PROVIDER, "accept", emptyMap);
        triggerScheduledJobsAndWait();
        //assertTrue(service.getTasksForUser(johndoe, Locale.ENGLISH).size() < forUser.size());

        getCleanStageNode();
        activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH, null);
        actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        task = (WorkflowTask) action;
        service.assignTask(task.getId(), PROVIDER, johndoe);
        triggerScheduledJobsAndWait();
        forUser = service.getTasksForUser(johndoe, Locale.ENGLISH);
        assertTrue(forUser.size() > 0);
        workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), johndoe, PROVIDER, "reject", emptyMap);
        triggerScheduledJobsAndWait();
        assertTrue(service.getTasksForUser(johndoe, Locale.ENGLISH).size() < forUser.size());

        getCleanStageNode();
        assertTrue("The workflow process is not completed", service.getActiveWorkflows(stageNode, Locale.ENGLISH, null).isEmpty());
    }

    @Test
    public void test1StepPublicationAccept() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("startDate", new WorkflowVariable());
        map.put("endDate", new WorkflowVariable());
        WorkflowDefinition workflow = getPossibleWorkflowEnsureExists(WORKFLOW_TYPE_1_STEP_PUBLICATION);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live"));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map, null);
        assertNotNull("The startup of a process should have return an id", processId);
        triggerScheduledJobsAndWait();
        getCleanStageNode();
        final List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH, null);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;
        service.assignTask(task.getId(), PROVIDER, johndoe);
        triggerScheduledJobsAndWait();
        List<WorkflowTask> forUser = service.getTasksForUser(johndoe, Locale.ENGLISH);
        assertTrue(forUser.size() > 0);
        WorkflowTask workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), johndoe, PROVIDER, "reject", emptyMap);
        triggerScheduledJobsAndWait();
        getCleanStageNode();
        assertTrue(service.getTasksForUser(johndoe, Locale.ENGLISH).size() < forUser.size());
        getCleanStageNode();
        assertTrue("The workflow process is not completed", service.getActiveWorkflows(stageNode, Locale.ENGLISH, null).isEmpty());
    }

    @Test
    public void test1StepPublicationReject() throws Exception {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("startDate", new WorkflowVariable());
        map.put("endDate", new WorkflowVariable());
        WorkflowDefinition workflow = getPossibleWorkflowEnsureExists(WORKFLOW_TYPE_1_STEP_PUBLICATION);
        map.put("publicationInfos", publicationService.getPublicationInfos(
                Arrays.asList(stageNode.getIdentifier()),
                Sets.newHashSet(Locale.ENGLISH.toString()), true, true, false, "default", "live"));
        processId = service.startProcess(Arrays.asList(stageNode.getIdentifier()), stageNode.getSession(), workflow.getKey(), PROVIDER, map, null);
        assertNotNull("The startup of a process should have return an id", processId);
        triggerScheduledJobsAndWait();
        getCleanStageNode();
        final List<Workflow> activeWorkflows = service.getActiveWorkflows(stageNode, Locale.ENGLISH, null);
        assertTrue("There should be some active workflow in jBPM", activeWorkflows.size() > 0);
        Set<WorkflowAction> actionSet = activeWorkflows.get(0).getAvailableActions();
        assertTrue("There should be some active activities for the first workflow in jBPM", actionSet.size() > 0);
        WorkflowAction action = actionSet.iterator().next();
        assertTrue(action instanceof WorkflowTask);
        WorkflowTask task = (WorkflowTask) action;
        service.assignTask(task.getId(), PROVIDER, johndoe);
        triggerScheduledJobsAndWait();
        List<WorkflowTask> forUser = service.getTasksForUser(johndoe, Locale.ENGLISH);
        assertTrue(forUser.size() > 0);
        WorkflowTask workflowTask = forUser.get(0);
        service.completeTask(workflowTask.getId(), johndoe, PROVIDER, "reject", emptyMap);
        triggerScheduledJobsAndWait();
        assertTrue(service.getTasksForUser(johndoe, Locale.ENGLISH).size() < forUser.size());
        getCleanStageNode();
        assertTrue("The workflow process is not completed", service.getActiveWorkflows(stageNode, Locale.ENGLISH, null).isEmpty());
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

    @Test
    public void abortProcessShouldFailInReadOnlyMode() {

        verifyFailureInReadOnlyMode(new Runnable() {

            @Override
            public void run() {
                service.abortProcess(processId, PROVIDER);
            }
        });
    }

    @Test
    public void startProcessShouldFailInReadOnlyMode() {

        verifyFailureInReadOnlyMode(new Runnable() {

            @Override
            public void run() {
                try {
                    service.startProcess(null, null, processId, PROVIDER, null, null);
                } catch (RepositoryException e) {
                    throw new JahiaRuntimeException(e);
                }
            }
        });
    }

    @Test
    public void assignTaskShouldFailInReadOnlyMode() {

        verifyFailureInReadOnlyMode(new Runnable() {

            @Override
            public void run() {
                service.assignTask(null, PROVIDER, johndoe);
            }
        });
    }

    @Test
    public void completeTaskShouldFailInReadOnlyMode() {

        verifyFailureInReadOnlyMode(new Runnable() {

            @Override
            public void run() {
                service.completeTask(null, johndoe, PROVIDER, null, null);
            }
        });
    }

    @Test
    public void addCommentShouldFailInReadOnlyMode() {

        verifyFailureInReadOnlyMode(new Runnable() {

            @Override
            public void run() {
                service.addComment(processId, PROVIDER, "comment", "johndoe");
            }
        });
    }

    @Test
    public void deleteProcessShouldFailInReadOnlyMode() {

        verifyFailureInReadOnlyMode(new Runnable() {

            @Override
            public void run() {
                service.deleteProcess(processId, PROVIDER);
            }
        });
    }

    protected Collection<WorkflowDefinition> getPossibleWorkflows() throws RepositoryException {
        final Collection<WorkflowDefinition> workflowList = service.getPossibleWorkflows(stageNode, true, Locale.ENGLISH).values();
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        return workflowList;
    }

    protected WorkflowDefinition getPublishWorkflowEnsureExists(String type) throws RepositoryException {
        final List<WorkflowDefinition> workflowList = service.getWorkflowDefinitionsForType("publish", null, Locale.ENGLISH);
        assertTrue("There should be some workflows already deployed", workflowList.size() > 0);
        WorkflowDefinition workflow = null;
        for (WorkflowDefinition workflowDefinition : workflowList) {
            if (type.equals(workflowDefinition.getName())) {
                workflow = workflowDefinition;
                break;
            }
        }

        assertNotNull("Unable to find workflow process of type: " + type, workflow);

        return workflow;
    }

    protected WorkflowDefinition getPossibleWorkflowEnsureExists(String type) throws RepositoryException {
        final Collection<WorkflowDefinition> workflowList = getPossibleWorkflows();
        if (type == null) {
            return workflowList.iterator().next();
        }
        WorkflowDefinition workflow = null;
        for (WorkflowDefinition workflowDefinition : workflowList) {
            if (type.equals(workflowDefinition.getName())) {
                workflow = workflowDefinition;
                break;
            }
        }

        assertNotNull("Unable to find workflow process of type: " + type, workflow);

        return workflow;
    }

    private void verifyFailureInReadOnlyMode(Runnable action) {
        service.switchReadOnlyMode(true);
        try {
            action.run();
            Assert.fail("The action should have failed due to read only mode");
        } catch (ReadOnlyModeException e) {
            // Expected.
        } finally {
            service.switchReadOnlyMode(false);
        }
    }
}
