/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.render.filter.cache.base;

import org.jahia.api.Constants;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.render.filter.AggregateFilter;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.services.render.filter.cache.CacheFilter;
import org.jahia.services.render.filter.cache.CacheKeyGenerator;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * base unit tests for both old and new implementation of CacheFilter
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 12 janv. 2010
 */
public class CacheFilterTest extends JahiaTestCase {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(CacheFilterTest.class);
    public final static String TESTSITE_NAME = "test";
    private static boolean isJsessionIdActive;

    private static boolean cacheFilterDisabled;
    private static boolean aggregateFilterDisabled;
    private static boolean aggregateCacheFilterDisabled;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            isJsessionIdActive = SettingsBean.getInstance().isDisableJsessionIdParameter();

            SettingsBean.getInstance().setDisableJsessionIdParameter(true);

            JahiaSite site = TestHelper.createSite(TESTSITE_NAME);
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
            JCRNodeWrapper siteNode = (JCRSiteNode) session.getNode("/sites/"+site.getSiteKey());

            JCRPublicationService.getInstance().publishByMainId(siteNode.getNode("search-results").getIdentifier(), Constants.EDIT_WORKSPACE,
                    Constants.LIVE_WORKSPACE, new LinkedHashSet<String>(Arrays.asList(Locale.ENGLISH.toString())),
                    true, Collections.<String> emptyList());

            JCRNodeWrapper shared = siteNode.getNode("home");
            if (shared.hasNode("testContent")) {
                shared.getNode("testContent").remove();
            }
            if(shared.isVersioned()) session.checkout(shared);
            JCRNodeWrapper node = shared.addNode("testContent", "jnt:page");
            node.setProperty("jcr:title", "English test page");
            node.setProperty("j:templateName", "simple");
            node.addNode("testType2", "jnt:mainContent");

            JCRNodeWrapper notCacheable1 = node.addNode("testNotCacheable1", "jnt:text");
            notCacheable1.addMixin("jmix:cache");
            notCacheable1.setProperty("j:expiration", 0L);
            notCacheable1.setProperty("text", "test not cacheable 1");

            JCRNodeWrapper notCacheable2 = node.addNode("testNotCacheable2", "jnt:text");
            notCacheable2.addMixin("jmix:cache");
            notCacheable2.setProperty("j:expiration", 0L);
            notCacheable2.setProperty("text", "test not cacheable 2");
            session.save();

            JCRPublicationService.getInstance().publishByMainId(shared.getIdentifier(), Constants.EDIT_WORKSPACE,
                    Constants.LIVE_WORKSPACE, new LinkedHashSet<String>(Arrays.asList(Locale.ENGLISH.toString())),
                    true, Collections.<String> emptyList());

            session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);
            node = session.getNode("/sites/"+site.getSiteKey()+"/home/testContent");

            // store filters config
            cacheFilterDisabled = ((CacheFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).isDisabled();
            aggregateFilterDisabled = ((AggregateFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).isDisabled();
            aggregateCacheFilterDisabled = ((AggregateCacheFilter) SpringContextSingleton.getBean("cacheFilter")).isDisabled();
        } catch (Exception e) {
            logger.warn("Exception during test setUp", e);
            Assert.fail();
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite("test");
        } catch (Exception e) {
            logger.warn("Exception during test tearDown", e);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
        SettingsBean.getInstance().setDisableJsessionIdParameter(isJsessionIdActive);

        // restore filters config
        ((CacheFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).setDisabled(cacheFilterDisabled);
        ((AggregateFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).setDisabled(aggregateFilterDisabled);
        ((AggregateCacheFilter) SpringContextSingleton.getBean("cacheFilter")).setDisabled(aggregateCacheFilterDisabled);
    }

    @Test
    public void testFixForEmptyCacheBug() throws Exception {
        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);
        final JCRNodeWrapper node = liveSession.getNode("/sites/"+TESTSITE_NAME+"/home/testContent");

        String relativeUrl = "/cms/render/live/en" + node.getPath() + ".html";
        String firstResponse = getAsText(relativeUrl);

        JCRTemplate.getInstance().doExecuteWithSystemSession(
                JahiaUserManagerService.GUEST_USERNAME,
                Constants.LIVE_WORKSPACE, Locale.ENGLISH, new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        RenderContext context = new RenderContext(getRequest(), getResponse(), session.getUser());
                        context.setSite(node.getResolveSite());
                        ChannelService channelService = (ChannelService) SpringContextSingleton.getInstance().getContext().getBean("ChannelService");
                        context.setChannel(channelService.getChannel(Channel.GENERIC_CHANNEL));
                        JCRNodeWrapper pageContentNode = session
                                .getNode("/modules/"
                                        + node.getResolveSite().getTemplatePackage().getIdWithVersion()
                                        + "/templates/base/simple/pagecontent");
                        Resource resource = new Resource(pageContentNode,
                                "html", null,
                                Resource.CONFIGURATION_WRAPPEDCONTENT);
                        context.setMainResource(resource);
                        try {
                            context.getRequest().setAttribute(
                                    "script",
                                    RenderService.getInstance().resolveScript(
                                            resource, context));
                        } catch (TemplateNotFoundException e) {
                            logger.debug("Template not found during unit test execution");
                        }
                        String referencePath = resource.getNode().getPath();
                        String referenceId = resource.getNode().getIdentifier();

                        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton
                                .getInstance().getContext().getBean("ModuleCacheProvider");
                        CacheKeyGenerator generator = moduleCacheProvider.getKeyGenerator();
                        Map<String, String> parsed = null;

                        for (Object existingKey : moduleCacheProvider.getCache().getKeys()) {
                            String existingKeyAsString = (String) existingKey;
                            parsed = generator.parse(existingKeyAsString);
                            if (parsed.get("path").equals(referencePath)
                                    && parsed.get("resourceID").equals(referenceId)) {
                                moduleCacheProvider.getCache().remove(existingKey);
                            }
                        }

                        return true;
                    }
                });

        String responseBody = getAsText(relativeUrl);
        logger.debug("Response body=[{}]", responseBody);
        if (firstResponse != null) {
            assertTrue(
                    "First and second response are not equal",
                    responseBody
                            .replaceAll("(?m)^[ \t]*\r?\n", "")
                            .replaceAll("(?m)^[ \t]+", "")
                            .replaceAll("\r\n", "\n")
                            .equals(firstResponse.replaceAll("(?m)^[ \t]*\r?\n", "").replaceAll("(?m)^[ \t]+", "")
                                    .replaceAll("\r\n", "\n")));
        }
    }

    /*
      Here is the scenario:

      create 3 users:
     - userAB
     - userAC
     - userBC

     create 3 groups
     - groupA -> userAB, userAC
     - groupB -> userAB, userBC
     - groupC -> userAC, userBC

     create 3 contents:
     - contentA -> only visible for groupA
     - contentB -> only visible for groupB
     - contentC -> only visible for groupC

     I log with userAB -> I see contentA and contentB
     I log with userAC -> I see contentA and contentB (this is the bug; I should only see contentA and contentC)
     If I flush the cache and reload the page with userAC, see contentA and contentC.
     */
    @Test
    public void testAclsCasesWithinLiveMode() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRSiteNode site = (JCRSiteNode) session.getNode("/sites/"+TESTSITE_NAME);
        // Create three users
        final JahiaUserManagerService userManagerProvider = JahiaUserManagerService.getInstance();
        final JCRUserNode userAB = userManagerProvider.createUser("userAB", "password", new Properties(), session);
        final JCRUserNode userAC = userManagerProvider.createUser("userAC", "password", new Properties(), session);
        final JCRUserNode userBC = userManagerProvider.createUser("userBC", "password", new Properties(), session);
        // Create three groups
        final JahiaGroupManagerService groupManagerProvider = JahiaGroupManagerService.getInstance();
        final JCRGroupNode groupA = groupManagerProvider.createGroup(site.getSiteKey(), "groupA", new Properties(), false, session);
        final JCRGroupNode groupB = groupManagerProvider.createGroup(site.getSiteKey(), "groupB", new Properties(), false, session);
        final JCRGroupNode groupC = groupManagerProvider.createGroup(site.getSiteKey(), "groupC", new Properties(), false, session);
        // Associate each user to two group
        groupA.addMember(userAB);
        groupA.addMember(userAC);
        groupB.addMember(userAB);
        groupB.addMember(userBC);
        groupC.addMember(userAC);
        groupC.addMember(userBC);
        // Create three content
        JCRNodeWrapper shared = site.getNode("home");
        if (shared.hasNode("testAclContent")) {
            shared.getNode("testAclContent").remove();
        }
        if(shared.isVersioned()) session.checkout(shared);
        JCRNodeWrapper node = shared.addNode("testAclContent", "jnt:page");
        node.setProperty("jcr:title", "English test page");
        node.setProperty("j:templateName", "simple");
        final JCRNodeWrapper list = node.addNode("listA", "jnt:contentList");
        final JCRNodeWrapper contentA = list.addNode("contentA", "jnt:mainContent");
        contentA.setProperty("body","Content__A__");
        final JCRNodeWrapper contentB = list.addNode("contentB", "jnt:mainContent");
        contentB.setProperty("body","Content__B__");
        final JCRNodeWrapper contentC = list.addNode("contentC", "jnt:mainContent");
        contentC.setProperty("body","Content__C__");
        // Set acls for each content
        contentA.setAclInheritanceBreak(true);
        contentA.grantRoles("g:" + groupA.getName(), new LinkedHashSet<String>(Arrays.asList("reader")));
        contentB.setAclInheritanceBreak(true);
        contentB.grantRoles("g:" + groupB.getName(), new LinkedHashSet<String>(Arrays.asList("reader")));
        contentC.setAclInheritanceBreak(true);
        contentC.grantRoles("g:" + groupC.getName(), new LinkedHashSet<String>(Arrays.asList("reader")));
        session.save();

        // Publish all
        JCRPublicationService.getInstance().publishByMainId(shared.getIdentifier(), Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, new LinkedHashSet<String>(Arrays.asList(Locale.ENGLISH.toString())), true,
                Collections.<String> emptyList());

        // Login as userAB using httpclient
        checkContentForUser(node, "userAB", "Content__A__", "Content__B__", "Content__C__");
        // Login as userAC using httpclient
        checkContentForUser(node, "userAC", "Content__A__", "Content__C__", "Content__B__");
        // Login as userBC using httpclient
        checkContentForUser(node, "userBC", "Content__B__", "Content__C__", "Content__A__");
    }

    private void checkContentForUser(JCRNodeWrapper node, String username, CharSequence firstContent, CharSequence secondContent,
                                     String missingContent) throws IOException, RepositoryException {
        login(username, "password");

        String firstResponse = getAsText("/cms/render/live/en" + node.getPath() + ".html");
        // Assert existence of content
        assertTrue("Page for " + username + " should contains " + firstContent, firstResponse.contains(firstContent));
        assertTrue("Page for " + username + " should contains " + secondContent, firstResponse.contains(secondContent));
        assertFalse("Page for " + username + " should not contains " + missingContent,
                firstResponse.contains(missingContent));

        logout();
    }
}
