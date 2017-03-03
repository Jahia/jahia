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
package org.jahia.test.services.workflow;

import static org.jahia.test.TestHelper.triggerScheduledJobsAndWait;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowVariable;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Integration test for the workflow service and related permissions.
 * 
 * @author Sergiy Shyrkov
 */
public class WorkflowPermissionsTest {
    private final static String CONTENTS_NODE;

    private static JahiaUser editor;

    private static final String LIST_NAME = "wf-test-list";

    private static final String LIST_NODE_PATH;

    private static final String PROVIDER = "jBPM";

    private static JCRPublicationService publicationService;

    private static JahiaUser reviewer;

    private static final String SITE_NAME = WorkflowPermissionsTest.class.getSimpleName();

    private static final String TEXT_NAME = "text-node";

    private static final String TEXT_NODE_PATH;

    private static final String TEXT_NODE_WITH_PERMISSIONS_REVOKED_PATH;

    private static final String TEXT_WITH_PERMISSIONS_REVOKED_NAME = "text-node-with-permissions-revoked";

    private static JahiaUserManagerService userManagerService;

    private static WorkflowService workflowService;

    static {
        CONTENTS_NODE = JahiaSitesService.SITES_JCR_PATH + '/' + SITE_NAME + "/contents";
        LIST_NODE_PATH = CONTENTS_NODE + '/' + LIST_NAME;
        TEXT_NODE_PATH = LIST_NODE_PATH + '/' + TEXT_NAME;
        TEXT_NODE_WITH_PERMISSIONS_REVOKED_PATH = LIST_NODE_PATH + '/' + TEXT_WITH_PERMISSIONS_REVOKED_NAME;
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        workflowService = WorkflowService.getInstance();
        publicationService = JCRPublicationService.getInstance();

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);

        JahiaSite site = TestHelper.createSite(SITE_NAME);

        // init users
        editor = userManagerService.createUser("wf-editor", SITE_NAME, "password", new Properties(), session)
                .getJahiaUser();
        reviewer = userManagerService.createUser("wf-reviewer", SITE_NAME, "password", new Properties(), session)
                .getJahiaUser();
        session.save();

        // grant permissions on site node
        JCRNodeWrapper siteNode = session.getNode(site.getJCRLocalPath());
        siteNode.grantRoles("u:" + editor.getName(), ImmutableSet.of("editor"));
        siteNode.grantRoles("u:" + reviewer.getName(), ImmutableSet.of("reviewer"));
        session.save();

        // publish site
        publicationService.publishByMainId(siteNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,
                null, true, null);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(SITE_NAME);

        JCRSessionFactory.getInstance().closeAllSessions();
    }

    protected String processId;

    private void deleteTestContent(JCRSessionWrapper session) throws RepositoryException {
        if (session.nodeExists(LIST_NODE_PATH)) {
            session.getNode(LIST_NODE_PATH).remove();
            session.save();
        }
    }

    private WorkflowDefinition getPossiblePublishWorkflow(JCRNodeWrapper targetNode) throws RepositoryException {
        return workflowService.getPossibleWorkflowForType(targetNode, true, "publish", Locale.ENGLISH);
    }

    @Before
    public void setUp() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);

        // init content
        JCRNodeWrapper listNode = session.getNode(CONTENTS_NODE).addNode(LIST_NAME, "jnt:contentList");
        listNode.addNode(TEXT_NAME, "jnt:text");
        JCRNodeWrapper textNodeWithPermissoinsRevoked = listNode.addNode(TEXT_WITH_PERMISSIONS_REVOKED_NAME,
                "jnt:text");

        // deny permissions on one of the sub-nodes
        textNodeWithPermissoinsRevoked.denyRoles("u:" + editor.getName(), ImmutableSet.of("editor"));
        textNodeWithPermissoinsRevoked.grantRoles("u:" + reviewer.getName(), ImmutableSet.of("reviewer"));

        session.save();
    }

    private String startProcess(JCRNodeWrapper node, String workflowKey) throws RepositoryException {
        List<String> nodeIds = Arrays.asList(node.getIdentifier());

        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("startDate", new WorkflowVariable());
        map.put("endDate", new WorkflowVariable());
        map.put("publicationInfos",
                publicationService.getPublicationInfos(nodeIds, Sets.newHashSet(Locale.ENGLISH.toString()), true, true,
                        true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE));

        return workflowService.startProcess(
                publicationService.getPublicationInfos(nodeIds, Sets.newHashSet(Locale.ENGLISH.toString()), true, true,
                        true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE).get(0).getAllUuids(),
                node.getSession(), workflowKey, PROVIDER, map, null);

    }

    @After
    public void tearDown() throws Exception {
        if (processId != null) {
            workflowService.abortProcess(processId, PROVIDER);
        }
        deleteTestContent(
                JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH));

        deleteTestContent(
                JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH));
    }

    /**
     * The method tests that when trying to start the publication workflow on nodes, the editor permissions are checked correctly.
     * 
     * @throws RepositoryException in case of a JCR error
     */
    @Test
    public void testDirectStartPermissionCheck() throws RepositoryException {
        assertNotNull("There should be some workflows for type 'publish' deployed",
                workflowService.getPossibleWorkflowForType(JCRSessionFactory.getInstance()
                        .getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH).getNode(LIST_NODE_PATH), false,
                        "publish", Locale.ENGLISH));

        JCRTemplate.getInstance().doExecute(editor, Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                new JCRCallback<Object>() {
                    @Override
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        assertNotNull("Editor should have a permission to start publication of the list node",
                                getPossiblePublishWorkflow(session.getNode(LIST_NODE_PATH)));

                        assertNotNull("Editor should have a permission to start publication of the text node",
                                getPossiblePublishWorkflow(session.getNode(TEXT_NODE_PATH)));

                        assertNull(
                                "Editor should NOT have a permission to start publication of the text node with permissions revoked",
                                getPossiblePublishWorkflow(session.getNode(TEXT_NODE_WITH_PERMISSIONS_REVOKED_PATH)));

                        return null;
                    }

                });

    }

    /**
     * Performs the test by starting a publication workflow on the test content list with two text sub-nodes using the editor user. On one
     * of those sub-nodes the permissions are revoked and the editor should not be able to start workflow on it.
     * 
     * @throws RepositoryException in case of a JCR error
     */
    @Test
    public void testStart() throws RepositoryException {
        // with editor start the workflow
        JCRTemplate.getInstance().doExecute(editor, Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                new JCRCallback<Object>() {
                    @Override
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper listNode = session.getNode(LIST_NODE_PATH);
                        WorkflowDefinition possiblePublishoWorkflow = getPossiblePublishWorkflow(listNode);
                        assertNotNull("Editor should have a permission to start publication of the list node",
                                possiblePublishoWorkflow);

                        // start workflow
                        processId = startProcess(listNode, possiblePublishoWorkflow.getKey());
                        assertNotNull("Workflow process should have been started", processId);

                        triggerScheduledJobsAndWait();

                        return null;
                    }
                });

        // verify that the workflow process is started on the list and one of the sub-nodes, but not on the second one (with permissions
        // revoked)
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);
        assertNotNull("The workflow process should have been started on node " + LIST_NODE_PATH,
                session.getNode(LIST_NODE_PATH).getPropertyAsString(Constants.PROCESSID));
        assertNotNull("The workflow process should have been started on node " + TEXT_NODE_PATH,
                session.getNode(TEXT_NODE_PATH).getPropertyAsString(Constants.PROCESSID));
        assertNull(
                "The workflow process should NOT have been started on node " + TEXT_NODE_WITH_PERMISSIONS_REVOKED_PATH,
                session.getNode(TEXT_NODE_WITH_PERMISSIONS_REVOKED_PATH).getPropertyAsString(Constants.PROCESSID));
    }

}
