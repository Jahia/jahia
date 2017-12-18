/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.jackrabbit.core.query.lucene.JahiaQueryImpl;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration test that checks ACLs in query results.
 * 
 * @author Sergiy Shyrkov
 */
public class QueryAclCheckTest {
    private final static String GROUP_NAME = "jcrQueryAclCheckTestGroup";

    private static Logger logger = LoggerFactory.getLogger(QueryAclCheckTest.class);

    private final static String SITE_KEY = "jcrQueryAclCheckTest";

    private final static String SITE_PATH = JahiaSitesService.SITES_JCR_PATH + '/' + SITE_KEY;

    private final static String TEST_NODE_NAME = "jcrQueryAclCheckTest";

    private final static String TEST_NODE_PATH = SITE_PATH + "/contents/" + TEST_NODE_NAME;

    private final static String USER_NAME = "jcrQueryAclCheckTestUser";

    private static JCRNodeWrapper createList(JCRNodeWrapper node, String name) throws RepositoryException {
        final JCRNodeWrapper list = node.addNode(name, Constants.JAHIANT_CONTENTLIST);
        list.setProperty("jcr:title", name);
        return list;
    }

    private static JCRNodeWrapper createText(JCRNodeWrapper node, String text) throws RepositoryException {
        JCRNodeWrapper textNode = node.addNode(
                JCRContentUtils.findAvailableNodeName(node, JCRContentUtils.generateNodeName(text)), "jnt:text");
        textNode.setProperty("text", text);

        return textNode;
    }

    private static JCRSessionWrapper getSystemSession() throws RepositoryException {
        return JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                Locale.ENGLISH);
    }

    private static void initContent(JCRSessionWrapper session) throws RepositoryException {
        JahiaUserManagerService userManager = JahiaUserManagerService.getInstance();
        JCRUserNode user = userManager.createUser(USER_NAME, SITE_KEY, "password", new Properties(), session);

        JahiaGroupManagerService groupManager = JahiaGroupManagerService.getInstance();
        JCRGroupNode group = groupManager.createGroup(SITE_KEY, GROUP_NAME, new Properties(), false, session);
        group.addMember(user);

        session.getNode(SITE_PATH).grantRoles("g:" + group.getName(), Collections.singleton("editor"));

        JCRNodeWrapper topNode = createList(
                session.getNode(JahiaSitesService.SITES_JCR_PATH + '/' + SITE_KEY + "/contents"), TEST_NODE_NAME);

        createText(topNode, "text-1");
        createText(topNode, "text-2");
        createText(topNode, "text-3");

        JCRNodeWrapper subNode = createList(topNode, "sub-node-1");

        createText(subNode, "sub-text-1-1");
        createText(subNode, "sub-text-1-2");
        createText(subNode, "sub-text-1-3");

        subNode = createList(topNode, "sub-node-2");
        subNode.setAclInheritanceBreak(true);
        subNode.grantRoles("u:" + user.getName(), Collections.singleton("editor"));

        createText(subNode, "sub-text-2-1");
        createText(subNode, "sub-text-2-2");
        createText(subNode, "sub-text-2-3");

        subNode = createList(topNode, "sub-node-3");
        subNode.setAclInheritanceBreak(true);
        subNode.grantRoles("g:" + group.getName(), Collections.singleton("editor"));

        createText(subNode, "sub-text-3-1");
        createText(subNode, "sub-text-3-2");
        createText(subNode, "sub-text-3-3");

        subNode = createList(topNode, "sub-node-4");
        subNode.setAclInheritanceBreak(true);
        subNode.grantRoles("u:root", Collections.singleton("editor"));

        logger.info("UUID: {}", createText(subNode, "sub-text-4-1").getIdentifier());
        createText(subNode, "sub-text-4-2");
        createText(subNode, "sub-text-4-3");

        session.save();
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(SITE_KEY);
        assertNotNull(site);

        initContent(getSystemSession());
    }

    @AfterClass
    public static void oneTimeTearUp() throws Exception {
        TestHelper.deleteSite(SITE_KEY);
    }

    private void checkResultSizeNodes(QueryResult res, final int expected) throws RepositoryException {
        // check total results
        NodeIterator ni = res.getNodes();
        int count = 0;
        while (ni.hasNext()) {
            ni.next();
            count++;
        }
        assertEquals("The result size is not the expected one", expected, count);
    }

    private void checkResultSizeRows(QueryResult res, final int expected) throws RepositoryException {
        // check total results
        RowIterator ri = res.getRows();
        int count = 0;
        while (ri.hasNext()) {
            ri.next();
            count++;
        }
        assertEquals("The result size is not the expected one", expected, count);
    }

    private QueryResult doQuery(JCRSessionWrapper session, final String statement, String language)
            throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug("Query: " + statement);
        }
        return session.getWorkspace().getQueryManager().createQuery(statement, language).execute();
    }

    private void performSql2Test(final boolean countRows) throws Exception {
        JCRTemplate.getInstance().doExecute(USER_NAME, SITE_KEY, Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                new JCRCallback<Boolean>() {
                    @Override
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        QueryResult res = doQuery(session,
                                "SELECT * FROM [jnt:text] WHERE ISDESCENDANTNODE('" + SITE_PATH + "')", Query.JCR_SQL2);
                        if (countRows) {
                            checkResultSizeRows(res, 12);
                        } else {
                            checkResultSizeNodes(res, 12);
                        }

                        return Boolean.TRUE;
                    }
                });
    }

    private void performXpathTest(final boolean countRows) throws RepositoryException {
        JCRTemplate.getInstance().doExecute(USER_NAME, SITE_KEY, Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                new JCRCallback<Boolean>() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        QueryResult res = doQuery(session, "/jcr:root" + SITE_PATH + "//element(*,jnt:text)",
                                Query.XPATH);
                        // when using XPath the number of rows is doubled due to translation nodes
                        if (countRows) {
                            checkResultSizeRows(res, 2 * 12);
                        } else {
                            checkResultSizeNodes(res, 2 * 12);
                        }

                        return Boolean.TRUE;
                    }
                });
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testAclSetup() throws Exception {
        JCRTemplate.getInstance().doExecute(USER_NAME, SITE_KEY, Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                new JCRCallback<Boolean>() {
                    @Override
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        assertTrue("Test node does not exist", session.nodeExists(TEST_NODE_PATH));

                        assertTrue("User should be able to read the node",
                                session.nodeExists(TEST_NODE_PATH + "/text-1"));
                        assertTrue("User should be able to read the node",
                                session.nodeExists(TEST_NODE_PATH + "/sub-node-1"));
                        assertTrue("User should be able to read the node",
                                session.nodeExists(TEST_NODE_PATH + "/sub-node-1/sub-text-1-3"));
                        assertTrue("User should be able to read the node",
                                session.nodeExists(TEST_NODE_PATH + "/sub-node-2"));
                        assertTrue("User should be able to read the node",
                                session.nodeExists(TEST_NODE_PATH + "/sub-node-2/sub-text-2-3"));
                        assertTrue("User should be able to read the node",
                                session.nodeExists(TEST_NODE_PATH + "/sub-node-3"));
                        assertTrue("User should be able to read the node",
                                session.nodeExists(TEST_NODE_PATH + "/sub-node-3/sub-text-3-3"));

                        assertFalse("User should NOT be able to read the node",
                                session.nodeExists(TEST_NODE_PATH + "/sub-node-4"));
                        assertFalse("User should NOT be able to read the node",
                                session.nodeExists(TEST_NODE_PATH + "/sub-node-4/sub-text-4-3"));
                        return Boolean.TRUE;
                    }
                });
    }

    @Test
    public void testSql2Nodes() throws Exception {
        performSql2Test(false);
    }

    @Test
    public void testSql2Rows() throws Exception {
        performSql2Test(false);
    }

    @Test
    public void testXpathNodes() throws Exception {
        boolean oldValue = JahiaQueryImpl.checkAclUuidInIndex;
        try {
            JahiaQueryImpl.checkAclUuidInIndex = false;
            performXpathTest(false);
        } finally {
            JahiaQueryImpl.checkAclUuidInIndex = oldValue;
        }
    }

    @Test
    public void testXpathNodesCheckAclUuidInIndex() throws Exception {
        boolean oldValue = JahiaQueryImpl.checkAclUuidInIndex;
        try {
            JahiaQueryImpl.checkAclUuidInIndex = true;
            performXpathTest(false);
        } finally {
            JahiaQueryImpl.checkAclUuidInIndex = oldValue;
        }
    }

    @Test
    public void testXpathRows() throws Exception {
        boolean oldValue = JahiaQueryImpl.checkAclUuidInIndex;
        try {
            JahiaQueryImpl.checkAclUuidInIndex = false;
            performXpathTest(true);
        } finally {
            JahiaQueryImpl.checkAclUuidInIndex = oldValue;
        }
    }

    @Test
    public void testXpathRowsCheckAclUuidInIndex() throws Exception {
        boolean oldValue = JahiaQueryImpl.checkAclUuidInIndex;
        try {
            JahiaQueryImpl.checkAclUuidInIndex = true;
            performXpathTest(true);
        } finally {
            JahiaQueryImpl.checkAclUuidInIndex = oldValue;
        }
    }

}
