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
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.After;
import org.junit.Test;
import org.testng.Assert;

import javax.jcr.RepositoryException;
import java.util.Locale;

/**
 * @author Jerome Blanchard
 */
public class JCRSessionWrapperTest extends AbstractJUnitTest {

    private static final String SITE_NAME = JCRNodeWrapperImplIT.class.getSimpleName();
    private static final Locale LOCALE = Locale.ENGLISH;

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
    public void tearDown() {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testSessionFactoryBuildersCacheEnabled() throws RepositoryException {
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentUserSession());
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, LOCALE));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, JCRSessionCacheStatus.ENABLED));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, JCRSessionCacheStatus.ENABLED));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, false));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, true));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, false, JCRSessionCacheStatus.ENABLED));
        assertSessionCacheEnabled(JCRSessionFactory.getInstance().getCurrentSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, true, JCRSessionCacheStatus.ENABLED));
    }

    @Test
    public void testSessionFactoryBuildersCacheDisabled() throws RepositoryException {
        assertSessionCacheDisabled(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, JCRSessionCacheStatus.DISABLED));
        assertSessionCacheDisabled(JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, JCRSessionCacheStatus.DISABLED));
        assertSessionCacheDisabled(JCRSessionFactory.getInstance().getCurrentSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, false, JCRSessionCacheStatus.DISABLED));
        assertSessionCacheDisabled(JCRSessionFactory.getInstance().getCurrentSession(Constants.EDIT_WORKSPACE, LOCALE, LOCALE, true, JCRSessionCacheStatus.DISABLED));
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

}
