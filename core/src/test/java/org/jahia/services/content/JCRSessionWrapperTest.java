/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.services.content;

import org.jahia.api.Constants;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.SitesSettings;
import org.jahia.settings.SettingsBean;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.After;
import org.junit.Test;
import org.testng.Assert;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import java.util.Locale;

/**
 * @author Jerome Blanchard
 */
public class JCRSessionWrapperTest extends AbstractJUnitTest {
    private static final Locale LOCALE = Locale.ENGLISH;

    private static final String SITE_NAME = JCRSessionWrapperTest.class.getSimpleName();

    private static String siteKey;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        siteKey = TestHelper.createSite(SITE_NAME).getSiteKey();
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        TestHelper.deleteSite(SITE_NAME);
    }

    @After
    public void tearDown() throws RepositoryException {
        JCRSessionWrapper cleanupSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        if (cleanupSession.nodeExists("/sites/" + siteKey + "/home/a")) {
            cleanupSession.getNode("/sites/" + siteKey + "/home/a").remove();
            cleanupSession.save();
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testSessionFactoryBuildersCacheEnabled() throws RepositoryException {
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentUserSession());
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, LOCALE));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, true));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, true));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, false));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, true));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, false, true));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, true, true));
    }

    @Test
    public void testSessionFactoryBuildersCacheDisabled() throws RepositoryException {
        assertSessionCacheDisabled(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, false));
        assertSessionCacheDisabled(JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, false));
        assertSessionCacheDisabled(JCRSessionFactory.getInstance().getCurrentSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, false, false));
        assertSessionCacheDisabled(JCRSessionFactory.getInstance().getCurrentSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, true, false));
    }

    private void assertSessionCacheEnabled(JCRSessionWrapper session) throws RepositoryException {
        Assert.assertTrue(session.isSessionCacheEnabled());
        Assert.assertEquals(0, session.getCacheSize());
        session.getNode("/sites/" + siteKey);
        Assert.assertTrue(session.getCacheSize()>0);
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    private void assertSessionCacheDisabled(JCRSessionWrapper session) throws RepositoryException {
        Assert.assertFalse(session.isSessionCacheEnabled());
        Assert.assertEquals(0, session.getCacheSize());
        session.getNode("/sites/" + siteKey);
        Assert.assertEquals(0, session.getCacheSize());
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testDisablingCacheOnCurrentSession() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        Assert.assertTrue(session.isSessionCacheEnabled());
        Assert.assertEquals(0, session.getCacheSize());
        session.getNode("/sites/" + siteKey);
        Assert.assertTrue(session.getCacheSize()>0);

        session.disableSessionCache();
        Assert.assertFalse(session.isSessionCacheEnabled());
        Assert.assertEquals(0, session.getCacheSize());
        session.getNode("/sites/" + siteKey);
        Assert.assertEquals(0, session.getCacheSize());

        session.enableSessionCache();
        Assert.assertTrue(session.isSessionCacheEnabled());
        Assert.assertEquals(0, session.getCacheSize());
        session.getNode("/sites/" + siteKey);
        Assert.assertTrue(session.getCacheSize()>0);

        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testSessionNodeCacheIsLimited() throws RepositoryException {
        JCRSessionFactory.getInstance().closeAllSessions();
        int originalCacheMaxSize = SettingsBean.getInstance().getNodesCachePerSessionMaxSize();
        int newCacheMaxSize = 2;
        try {
            // Set Max node in cache to 2 for testing purpose
            SettingsBean.getInstance().setNodesCachePerSessionMaxSize(newCacheMaxSize);
            // Create a new session
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
            // get 3 nodes;
            session.getNode("/sites");
            session.getNode("/sites/" + siteKey);
            session.getNode("/sites/" + siteKey + "/home");
            Assert.assertEquals(session.getCacheSize(), newCacheMaxSize, "Cache size not correct for nodes cached in session");
        } finally {
            SettingsBean.getInstance().setNodesCachePerSessionMaxSize(originalCacheMaxSize);
            JCRSessionFactory.getInstance().closeAllSessions();
        }
    }

    // Original test was here to reproduce inconsistency in cache between path and uuid caches, since then the internal
    // cache has been refactored to use a single cache for both path and uuid.
    // Test case is still relevant, as it tests the consistency of the cache after a move operation.
    @Test
    public void testMovedNode() throws RepositoryException {
        JCRSessionWrapper setupSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper home = setupSession.getNode("/sites/" + siteKey + "/home");
        JCRNodeWrapper c = home.addNode("a", "jnt:contentList")
                .addNode("b", "jnt:contentList")
                .addNode("c", "jnt:contentList");
        String c_id = c.getIdentifier();
        c.addNode("d", "jnt:contentList");
        setupSession.save();

        JCRSessionFactory.getInstance().closeAllSessions();
        int originalCacheMaxSize = SettingsBean.getInstance().getNodesCachePerSessionMaxSize();
        int newCacheMaxSize = 3;
        try {
            SettingsBean.getInstance().setNodesCachePerSessionMaxSize(newCacheMaxSize);
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
            // Cache size is limited to 3, populate them with 3 nodes;
            session.getNode("/sites/" + siteKey + "/home/a/b/c/d");
            session.getNode("/sites/" + siteKey + "/home/a/b/c");
            session.getNode("/sites/" + siteKey + "/home/a/b");
            // get again the /a/b/c/d
            session.getNode("/sites/" + siteKey + "/home/a/b/c/d");
            // get a 4th node, in order to evict the first node from cache, /a/b/c is expected to be evicted
            session.getNode("/sites/" + siteKey + "/home/a");
            // Do a move operation, to check if the cache is still consistent
            session.move("/sites/" + siteKey + "/home/a/b", "/sites/" + siteKey + "/home/a/z");
            Assert.assertEquals(session.getNodeByIdentifier(c_id).getPath(), "/sites/" + siteKey + "/home/a/z/c",
                    "Internal cache node of session may be corrupted, the node path is not correct after move");
        } finally {
            SettingsBean.getInstance().setNodesCachePerSessionMaxSize(originalCacheMaxSize);
        }
    }

    @Test
    public void testModifiedNode() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        // get a node, assert cache is 1 (home)
        JCRNodeWrapper home = session.getNode("/sites/" + siteKey + "/home");
        Assert.assertEquals(session.getCacheSize(), 1);
        Assert.assertTrue(session.getChangedNodes().isEmpty());

        // Update home node, assert cache is 0 (home being modified is not counting in cached nodes)
        home.addMixin("jmix:keywords");
        home.setProperty("j:keywords", new String[]{"keyword1", "keyword2"});
        Assert.assertEquals(session.getCacheSize(), 0);
        Assert.assertEquals(session.getChangedNodes().size(), 1);
        Assert.assertEquals(session.getChangedNodes().iterator().next().getPath(), "/sites/" + siteKey + "/home");

        // Save invalid node
        try {
            session.save();
        } catch (ConstraintViolationException e) {
            // this is expected, missing jcr:title, ensure cache is still correct
            Assert.assertEquals(session.getCacheSize(), 0);
            Assert.assertEquals(session.getChangedNodes().size(), 1);
            Assert.assertEquals(session.getChangedNodes().iterator().next().getPath(), "/sites/" + siteKey + "/home");
            home.setProperty("jcr:title", "new title");
        }

        // Save valid node with _en translation
        session.save();
        Assert.assertEquals(session.getCacheSize(), 1); // /home/translation_en
        Assert.assertTrue(session.getChangedNodes().isEmpty()); // no more changes after successful save
        Assert.assertEquals(session.getNode("/sites/" + siteKey + "/home").getProperty("jcr:title").getString(), "new title");
    }

    @Test
    public void testEvictedCachedNodeUpdate() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        // get a node, assert cache is 1 (home)
        JCRNodeWrapper home = session.getNode("/sites/" + siteKey + "/home");
        Assert.assertEquals(session.getCacheSize(), 1);
        Assert.assertTrue(session.getChangedNodes().isEmpty());

        // Create node a
        home.addNode("a", "jnt:contentList");
        Assert.assertEquals(session.getCacheSize(), 1);  // /home
        Assert.assertEquals(session.getChangedNodes().size(), 1); // /home/a
        Assert.assertTrue(session.getChangedNodes().stream().anyMatch(node -> node.getPath().equals("/sites/" + siteKey + "/home/a")));
        session.save();
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertEquals(session.getChangedNodes().size(), 0); // /home/a evicted

        // Update node a
        JCRNodeWrapper a = session.getNode("/sites/" + siteKey + "/home/a");
        a.setProperty("j:nodename", "Node A");
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertEquals(session.getChangedNodes().size(), 1); // /home/a
        session.save();
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertEquals(session.getChangedNodes().size(), 0); // /home/a evicted

        // Check that modification are reflected on the already loaded node (reference still tracked by cache)
        Assert.assertEquals(a.getProperty("j:nodename").getString(), "Node A");

        a.setProperty("j:nodename", "The Node A");
        session.save();
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertEquals(session.getChangedNodes().size(), 0); // /home/a evicted

        // Check that modification are reflected on the already loaded a node (reference still tracked by cache)
        Assert.assertEquals(a.getProperty("j:nodename").getString(), "The Node A");

        // Delete node a
        a.remove();
        session.save();
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertEquals(session.getChangedNodes().size(), 0);
    }

    @Test
    public void testEvictedCachedSiteNodeUpdate() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);

        //Load the site
        JCRSiteNode siteNode = (JCRSiteNode) session.getNode("/sites/" + siteKey);
        Assert.assertEquals(session.getCacheSize(), 1);
        Assert.assertTrue(session.getChangedNodes().isEmpty());

        //Simulate a modification on the site done by any Helper class (like ModuleInstallationHelper)
        String initialServerName = siteNode.getServerName();
        ((JCRSiteNode) session.getNode("/sites/" + siteKey)).setServerName("The New ServerName");
        Assert.assertEquals(session.getCacheSize(), 0);
        Assert.assertEquals(session.getChangedNodes().size(), 1);
        Assert.assertNotEquals(initialServerName, siteNode.getServerName());
        Assert.assertEquals("The New ServerName", siteNode.getServerName()); //The modification is reflected on the loaded node
        session.save();
        Assert.assertEquals("The New ServerName", siteNode.getServerName()); //The modification is reflected on the loaded node
        Assert.assertEquals(session.getCacheSize(), 0); //Node has been evicted from the cache after session save
        Assert.assertEquals(session.getChangedNodes().size(), 0);

        // Make another modification
        ((JCRSiteNode) session.getNode("/sites/" + siteKey)).setServerName("The Newest ServerName");
        Assert.assertEquals(session.getCacheSize(), 0);
        Assert.assertEquals(session.getChangedNodes().size(), 1);
        Assert.assertEquals("The New ServerName", siteNode.getServerName()); //The modification is not yet reflected on the loaded node
        session.save();
        Assert.assertEquals("The Newest ServerName", siteNode.getServerName()); //The modification is reflected on the loaded node
        Assert.assertEquals(session.getCacheSize(), 0);
        Assert.assertEquals(session.getChangedNodes().size(), 0);

        //Restore modification
        ((JCRSiteNode) session.getNode("/sites/" + siteKey)).setServerName(initialServerName);
        session.save();
    }

    @Test
    public void testMaxCacheSizeReachedNodeUpdate() throws RepositoryException {
        String nodesuffix = "." + System.currentTimeMillis();
        // Set Max node in cache to 5 for testing purpose
        SettingsBean.getInstance().setNodesCachePerSessionMaxSize(5);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);

        JCRSiteNode site = (JCRSiteNode) session.getNode("/sites/" + siteKey);
        JCRNodeWrapper home = session.getNode("/sites/" + siteKey + "/home");
        JCRNodeWrapper a = home.addNode("a" + nodesuffix, "jnt:contentList");
        JCRNodeWrapper b = home.addNode("b" + nodesuffix, "jnt:contentList");
        JCRNodeWrapper c = home.addNode("c" + nodesuffix, "jnt:contentList");
        JCRNodeWrapper d = home.addNode("d" + nodesuffix, "jnt:contentList");
        JCRNodeWrapper e = home.addNode("e" + nodesuffix, "jnt:contentList");
        Assert.assertEquals(session.getCacheSize(), 2);
        Assert.assertEquals(session.getChangedNodes().size(), 5);
        String initialServerName = site.getServerName();
        session.save();
        Assert.assertEquals(session.getCacheSize(), 2);
        Assert.assertEquals(session.getChangedNodes().size(), 0);

        // Load nodes to reach max cache size
        a = session.getNode("/sites/" + siteKey + "/home/a" + nodesuffix);
        b = session.getNode("/sites/" + siteKey + "/home/b" + nodesuffix);
        c = session.getNode("/sites/" + siteKey + "/home/c" + nodesuffix);
        d = session.getNode("/sites/" + siteKey + "/home/d" + nodesuffix);
        e = session.getNode("/sites/" + siteKey + "/home/e" + nodesuffix);
        Assert.assertEquals(session.getCacheSize(), 5);
        Assert.assertEquals(session.getChangedNodes().size(), 0);

        // Simulate site server name modification
        ((JCRSiteNode) session.getNode("/sites/" + siteKey)).setServerName("ServerName5");
        Assert.assertEquals(session.getCacheSize(), 4); // the getnode replace an entry but just switch to changedNode after
        Assert.assertEquals(session.getChangedNodes().size(), 1);
        session.save();
        Assert.assertEquals(session.getCacheSize(), 4);
        Assert.assertEquals(session.getChangedNodes().size(), 0);

        // Check that modification are reflected on the already loaded nodes (reference still tracked by cache)
        Assert.assertEquals(site.getServerName(), "ServerName5");

        // Simulate site server name modification (second time)
        ((JCRSiteNode) session.getNode("/sites/" + siteKey)).setServerName("ServerName51");
        session.save();
        Assert.assertEquals(site.getServerName(), "ServerName51");

        //Restore servername
        ((JCRSiteNode) session.getNode("/sites/" + siteKey)).setServerName(initialServerName);
        a.remove();
        b.remove();
        c.remove();
        d.remove();
        e.remove();
        session.save();
    }

    @Test
    public void testDecoratedNodeUpdateWithoutCache() throws RepositoryException {
        String nodesuffix = "." + System.currentTimeMillis();
        // Set Max node in cache to 5 for testing purpose
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        session.disableSessionCache();

        JCRSiteNode site = (JCRSiteNode) session.getNode("/sites/" + siteKey);
        session.getNode("/sites/" + siteKey + "/home");
        Assert.assertEquals(session.getCacheSize(), 0);
        Assert.assertEquals(session.getChangedNodes().size(), 0);
        String initialServerName = site.getServerName();
        session.save();
        Assert.assertEquals(session.getCacheSize(), 0);
        Assert.assertEquals(session.getChangedNodes().size(), 0);
        Assert.assertFalse(site.isNew());
        Assert.assertFalse(site.isModified());

        // Simulate site serverName modification
        JCRSiteNode loadedSite = ((JCRSiteNode) session.getNode("/sites/" + siteKey));
        loadedSite.setServerName("ServerName5");
        Assert.assertEquals(session.getCacheSize(), 0);
        Assert.assertEquals(session.getChangedNodes().size(), 1);
        session.save();
        Assert.assertEquals(session.getCacheSize(), 0);
        Assert.assertEquals(session.getChangedNodes().size(), 0);

        // Check that modification are reflected on the already loaded nodes (reference still tracked by cache)
        Assert.assertEquals(site.getServerName(), "ServerName5");

        //Restore servername
        ((JCRSiteNode) session.getNode("/sites/" + siteKey)).setServerName(initialServerName);
        session.save();
    }

    @Test
    public void testAddedNode() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        // get a node, assert cache is 1 (home)
        JCRNodeWrapper home = session.getNode("/sites/" + siteKey + "/home");
        Assert.assertEquals(session.getCacheSize(), 1);
        Assert.assertTrue(session.getChangedNodes().isEmpty());

        // Add a node
        JCRNodeWrapper a = home.addNode("a", "jnt:contentList");
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertEquals(session.getChangedNodes().size(), 1); // /home/a
        Assert.assertEquals(session.getChangedNodes().iterator().next().getPath(), "/sites/" + siteKey + "/home/a");
        Assert.assertTrue(a.isNew());

        // Save valid node with _en translation
        session.save();
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertTrue(session.getChangedNodes().isEmpty()); // no more changes after successful save
        Assert.assertFalse(a.isNew());
        Assert.assertFalse(home.isModified());
        Assert.assertFalse(a.isModified());
    }

    @Test
    public void testRemoveAddedNode() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper home = session.getNode("/sites/" + siteKey + "/home");
        home.addNode("a", "jnt:contentList")
                .addNode("b", "jnt:contentList")
                .addNode("c", "jnt:contentList")
                .addNode("d", "jnt:contentList");

        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertEquals(session.getChangedNodes().size(), 4); // /home/a, /home/a/b, /home/a/b/c, /home/a/b/c/d
        home.getNode("a").remove();
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a"));
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a/b"));
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a/b/c"));
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a/b/c/d"));
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertTrue(session.getChangedNodes().isEmpty()); // no more nodes.
    }

    @Test
    public void testRemoveChildAddedNode() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        session.getNode("/sites/" + siteKey + "/home")
                .addNode("a", "jnt:contentList");
        session.save();
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertTrue(session.getChangedNodes().isEmpty()); // no more nodes.

        JCRNodeWrapper a = session.getNode("/sites/" + siteKey + "/home/a");
        a.addNode("b", "jnt:contentList")
                .addNode("c", "jnt:contentList")
                .addNode("d", "jnt:contentList");
        Assert.assertEquals(session.getCacheSize(), 2); // /home, /home/a
        Assert.assertEquals(session.getChangedNodes().size(), 3); // /home/a/b, /home/a/b/c, /home/a/b/c/d

        a.remove();
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a"));
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a/b"));
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a/b/c"));
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a/b/c/d"));
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertTrue(session.getChangedNodes().isEmpty()); // no more nodes.

        session.save();
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a"));
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a/b"));
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a/b/c"));
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a/b/c/d"));
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertTrue(session.getChangedNodes().isEmpty()); // no more nodes.
    }

    @Test
    public void testValidateNewNode() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper page = session.getNode("/sites/" + siteKey + "/home")
                .addNode("a", "jnt:page");
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertEquals(session.getChangedNodes().size(), 1); // /home/a

        // Save invalid node
        try {
            session.save();
        } catch (ConstraintViolationException e) {
            // this is expected, missing jcr:title, ensure cache is still correct
            Assert.assertEquals(session.getCacheSize(), 0); // there is a refresh in case of validation error, so cache will be empty at this stage
            Assert.assertEquals(session.getChangedNodes().size(), 1);
            Assert.assertEquals(session.getChangedNodes().iterator().next().getPath(), "/sites/" + siteKey + "/home/a");
            page.setProperty("jcr:title", "new title");
            page.setProperty("j:templateName", "home");
        }

        session.save();
        Assert.assertEquals(session.getCacheSize(), 1);
        Assert.assertTrue(session.getChangedNodes().isEmpty()); // no more changes after successful save
    }

    @Test
    public void testDisablingCacheKeepChangedNodes() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper home = session.getNode("/sites/" + siteKey + "/home");
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertEquals(session.getChangedNodes().size(), 0);

        home.addNode("a", "jnt:contentList");
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertEquals(session.getChangedNodes().size(), 1);
        Assert.assertEquals(session.getChangedNodes().iterator().next().getPath(), "/sites/" + siteKey + "/home/a");

        session.disableSessionCache();
        Assert.assertEquals(session.getCacheSize(), 0); // no more cache
        Assert.assertEquals(session.getChangedNodes().size(), 1); // keep changes
        Assert.assertEquals(session.getChangedNodes().iterator().next().getPath(), "/sites/" + siteKey + "/home/a");

        session.getNode("/sites/" + siteKey + "/home");
        Assert.assertEquals(session.getCacheSize(), 0); // no more cache
        Assert.assertEquals(session.getChangedNodes().size(), 1); // keep changes
        Assert.assertEquals(session.getChangedNodes().iterator().next().getPath(), "/sites/" + siteKey + "/home/a");

        session.enableSessionCache();
        session.getNode("/sites/" + siteKey + "/home");
        Assert.assertEquals(session.getCacheSize(), 1); // home back in cache
        Assert.assertEquals(session.getChangedNodes().size(), 1); // keep changes
        Assert.assertEquals(session.getChangedNodes().iterator().next().getPath(), "/sites/" + siteKey + "/home/a");
    }

    @Test
    public void testRefresh() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper home = session.getNode("/sites/" + siteKey + "/home");
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertEquals(session.getChangedNodes().size(), 0);

        home.addNode("a", "jnt:contentList");
        Assert.assertEquals(session.getCacheSize(), 1); // /home
        Assert.assertEquals(session.getChangedNodes().size(), 1);
        Assert.assertEquals(session.getChangedNodes().iterator().next().getPath(), "/sites/" + siteKey + "/home/a");

        session.refresh(false);
        Assert.assertEquals(session.getCacheSize(), 0);
        Assert.assertEquals(session.getChangedNodes().size(), 0);
        Assert.assertTrue(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a"));

        home = session.getNode("/sites/" + siteKey + "/home");
        home.addNode("a", "jnt:contentList");
        Assert.assertEquals(session.getCacheSize(), 1);
        Assert.assertEquals(session.getChangedNodes().size(), 1);
        Assert.assertEquals(session.getChangedNodes().iterator().next().getPath(), "/sites/" + siteKey + "/home/a");

        session.refresh(true);
        Assert.assertEquals(session.getCacheSize(), 0);
        Assert.assertEquals(session.getChangedNodes().size(), 1);
        Assert.assertEquals(session.getChangedNodes().iterator().next().getPath(), "/sites/" + siteKey + "/home/a");
        Assert.assertFalse(assertPathNotFoundException(session, "/sites/" + siteKey + "/home/a"));
    }

    private boolean assertPathNotFoundException(JCRSessionWrapper session, String path) throws RepositoryException {
        try {
            session.getNode("/sites/" + siteKey + "/home/a");
            return false;
        } catch (PathNotFoundException e) {
            return true;
        }
    }
}
