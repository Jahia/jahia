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
package org.jahia.test.services.render.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.render.filter.URLFilter;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.api.Constants;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Sets;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link URLFilter} User: toto Date: Nov 26, 2009 Time: 12:57:51 PM
 */
public class URLFilterTest extends JahiaTestCase {
    private final static String TESTSITE_NAME = "test";
    private final static String SITECONTENT_ROOT_NODE = "/sites/"
            + TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        TestHelper.createSite(TESTSITE_NAME, Sets.newHashSet("en", "fr"), null, false);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
    }

    @Before
    public void setUp() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper siteNode = session.getNode(SITECONTENT_ROOT_NODE);

        if (!siteNode.isCheckedOut()) {
            session.checkout(siteNode);
        }

        if (siteNode.hasNode("testPage")) {
            siteNode.getNode("testPage").remove();
        }

        JCRNodeWrapper pageNode = siteNode.addNode("testPage", Constants.JAHIANT_PAGE);
        pageNode.setProperty("jcr:title", "English test page");
        pageNode.setProperty("j:templateName", "simple");
        pageNode.addNode("testContent", "jnt:mainContent");

        session.save();

        JCRSessionWrapper frenchSession = JCRSessionFactory.getInstance().getCurrentUserSession(
                null, Locale.FRENCH);
        pageNode = frenchSession.getNode(SITECONTENT_ROOT_NODE + "/testPage");
        pageNode.setProperty("jcr:title", "French test page");
        frenchSession.save();
    }

    @After
    public void tearDown() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper siteNode = session.getNode(SITECONTENT_ROOT_NODE);

        if (!siteNode.isCheckedOut()) {
            session.checkout(siteNode);
        }

        if (siteNode.hasNode("testPage")) {
            siteNode.getNode("testPage").remove();
        }

        session.save();

        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testAssigningUrlMappings() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper pageNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/testPage");
        JCRNodeWrapper contentNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/testPage/testContent");

        VanityUrl vanityUrl = new VanityUrl("/testpage", TESTSITE_NAME, "en");
        vanityUrl.setDefaultMapping(true);
        vanityUrl.setActive(true);
        assertTrue("URL mapping should not exist yet", getVanityUrlService()
                .findExistingVanityUrls(vanityUrl.getUrl(),
                        vanityUrl.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        getVanityUrlService().saveVanityUrlMapping(pageNode, vanityUrl);
        assertFalse("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(vanityUrl.getUrl(),
                        vanityUrl.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        try {
            getVanityUrlService().saveVanityUrlMapping(contentNode, vanityUrl);
            assertTrue("Exception should have been thrown", false);
        } catch (ConstraintViolationException ex) {
            // expected
        }

        VanityUrl newVanityUrl = new VanityUrl("/testcontent", TESTSITE_NAME,
                "en");
        newVanityUrl.setDefaultMapping(true);
        newVanityUrl.setActive(true);
        getVanityUrlService().saveVanityUrlMapping(contentNode, newVanityUrl);
        assertFalse("New URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(vanityUrl.getUrl(),
                        vanityUrl.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());

        VanityUrl savedVanityUrl = getVanityUrlService()
                .getVanityUrlForWorkspaceAndLocale(pageNode,
                        session.getWorkspace().getName(), session.getLocale(), TESTSITE_NAME);
        assertTrue("Wrong page vanity URL returned", vanityUrl.getUrl().equals(
                savedVanityUrl.getUrl()));

        VanityUrl savedNewVanityUrl = getVanityUrlService()
                .getVanityUrlsForCurrentLocale(contentNode, session).get(0);
        assertTrue("Wrong container vanity URL returned", newVanityUrl.getUrl()
                .equals(savedNewVanityUrl.getUrl()));

        getVanityUrlService().removeVanityUrlMapping(pageNode, vanityUrl);
        getVanityUrlService().removeVanityUrlMapping(contentNode, newVanityUrl);
        assertTrue("URL mapping should no longer exist", getVanityUrlService()
                .findExistingVanityUrls(vanityUrl.getUrl(),
                        vanityUrl.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        assertNull("No page vanity URL should exist", getVanityUrlService()
                .getVanityUrlForWorkspaceAndLocale(pageNode,
                        session.getWorkspace().getName(), session.getLocale(), TESTSITE_NAME));
        assertTrue("No container vanity URL should exist",
                getVanityUrlService().getVanityUrlsForCurrentLocale(
                        contentNode, session).isEmpty());
    }

    @Test
    public void testBulkDiffAssigningUrlMappings() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper pageNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/testPage");

        VanityUrl englishVanityUrl = new VanityUrl("/test2page", TESTSITE_NAME,
                "en");
        englishVanityUrl.setActive(true);
        VanityUrl englishVanityUrl2 = new VanityUrl("/test2page2",
                TESTSITE_NAME, "en");
        englishVanityUrl2.setActive(false);
        VanityUrl englishVanityUrl3 = new VanityUrl("/test2page/page3",
                TESTSITE_NAME, "en");
        englishVanityUrl3.setActive(true);

        VanityUrl frenchVanityUrl = new VanityUrl("/test2page/french",
                TESTSITE_NAME, "fr");
        frenchVanityUrl.setActive(true);
        VanityUrl frenchVanityUrl2 = new VanityUrl("/test2page/french2",
                TESTSITE_NAME, "fr");
        frenchVanityUrl2.setDefaultMapping(true);
        frenchVanityUrl2.setActive(true);

        List<VanityUrl> vanityUrls = new ArrayList<VanityUrl>();
        vanityUrls.add(englishVanityUrl);
        vanityUrls.add(englishVanityUrl2);
        vanityUrls.add(englishVanityUrl3);
        vanityUrls.add(frenchVanityUrl);
        vanityUrls.add(frenchVanityUrl2);
        Set<String> languages = new HashSet<String>();
        languages.add("en");
        languages.add("fr");

        getVanityUrlService().saveVanityUrlMappings(pageNode, vanityUrls,
                languages);

        assertFalse("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(englishVanityUrl.getUrl(),
                        englishVanityUrl.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        assertFalse("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(englishVanityUrl2.getUrl(),
                        englishVanityUrl2.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        assertFalse("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(englishVanityUrl3.getUrl(),
                        englishVanityUrl3.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        assertFalse("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(frenchVanityUrl.getUrl(),
                        frenchVanityUrl.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());
        assertFalse("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrls(frenchVanityUrl2.getUrl(),
                        frenchVanityUrl2.getSite(), Constants.EDIT_WORKSPACE)
                .isEmpty());

        englishVanityUrl.setDefaultMapping(true);
        englishVanityUrl2.setDefaultMapping(true);

        try {
            getVanityUrlService().saveVanityUrlMappings(pageNode, vanityUrls,
                    languages);
            assertTrue("Exception should have been thrown", false);
        } catch (ConstraintViolationException ex) {
            // expected as two default mappings on same language - nothing should be saved
        }

        VanityUrl frenchVanityUrl3 = new VanityUrl("/test2page", TESTSITE_NAME,
                "fr");
        frenchVanityUrl3.setDefaultMapping(true);
        frenchVanityUrl3.setActive(true);

        try {
            getVanityUrlService().saveVanityUrlMappings(pageNode, vanityUrls,
                    languages);
            assertTrue("Exception should have been thrown", false);
        } catch (ConstraintViolationException ex) {
            // expected as same mapping is used in English - nothing should be saved
        }

        List<VanityUrl> vanityUrls_en = getVanityUrlService()
                .getVanityUrlsForCurrentLocale(pageNode, session);

        String[] expectedUrls_en = new String[] { "/test2page", "/test2page2",
                "/test2page/page3" };
        assertTrue("Number of Urls is not expected " + expectedUrls_en.length
                + " vs. " + vanityUrls_en.size(),
                expectedUrls_en.length == vanityUrls_en.size());
        for (VanityUrl vanityUrl_en : vanityUrls_en) {
            boolean found = false;
            for (String expectedUrl_en : expectedUrls_en) {
                if (vanityUrl_en.getUrl().equals(expectedUrl_en)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Expected vanity url not found: " + vanityUrl_en, found);
        }

        JCRSessionWrapper frenchSession = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.FRENCH);

        List<VanityUrl> vanityUrls_fr = getVanityUrlService()
                .getVanityUrlsForCurrentLocale(pageNode, frenchSession);

        String[] expectedUrls_fr = new String[] { "/test2page/french",
                "/test2page/french2" };
        assertTrue("Number of Urls is not expected " + expectedUrls_fr.length
                + " vs. " + vanityUrls_fr.size(),
                expectedUrls_fr.length == vanityUrls_fr.size());
        for (VanityUrl vanityUrl_fr : vanityUrls_fr) {
            boolean found = false;
            for (String expectedUrl_fr : expectedUrls_fr) {
                if (vanityUrl_fr.getUrl().equals(expectedUrl_fr)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Expected vanity url not found: " + vanityUrl_fr, found);
        }

        getVanityUrlService().removeVanityUrlMappings(pageNode, "en");
        getVanityUrlService().removeVanityUrlMappings(pageNode, "fr");
        assertNull("No page vanity URL should exist", getVanityUrlService()
                .getVanityUrlForWorkspaceAndLocale(pageNode,
                        session.getWorkspace().getName(), session.getLocale(), TESTSITE_NAME));
    }

    @Test
    public void testURLFilterResolving() throws Exception {
        Object[][] testPathes = new Object[][] {
                { "/", "", Locale.ENGLISH, "/", NoSuchWorkspaceException.class, "" },
                { "/render", "", Locale.ENGLISH, "/", NoSuchWorkspaceException.class, "" },
                { "/render/default", Constants.EDIT_WORKSPACE, Locale.ENGLISH, "/", JCRNodeWrapper.class, "/" },
                { "/render/live", Constants.LIVE_WORKSPACE, Locale.ENGLISH, "/", JCRNodeWrapper.class, "/" },
                { "/render/live/fr", Constants.LIVE_WORKSPACE, Locale.FRENCH, "/", JCRNodeWrapper.class, "/" },
                { "/render/default/en/sites/test/testPage.html", Constants.EDIT_WORKSPACE, Locale.ENGLISH, "/sites/test/testPage.html", JCRNodeWrapper.class, "/sites/test/testPage" },
                { "/render/default/en/sites/test/testPage/testContent.detail.html", Constants.EDIT_WORKSPACE, Locale.ENGLISH, "/sites/test/testPage/testContent.detail.html", JCRNodeWrapper.class, "/sites/test/testPage/testContent" },
                { "/edit/default", Constants.EDIT_WORKSPACE, Locale.ENGLISH, "/", JCRNodeWrapper.class, "/" },
                { "/edit/default/en", Constants.EDIT_WORKSPACE, Locale.ENGLISH, "/", JCRNodeWrapper.class, "/" },
                { "/edit/default/fr", Constants.EDIT_WORKSPACE, Locale.FRENCH, "/", JCRNodeWrapper.class, "/" },
                { "/edit/default/fr/sites/test/home.html", Constants.EDIT_WORKSPACE, Locale.FRENCH, "/sites/test/home.html", JCRNodeWrapper.class, "/sites/test/home" }, };
        for (Object[] testPath : testPathes) {
            URLResolver urlResolver = getUrlResolverFactory().createURLResolver((String) testPath[0], "", (HttpServletRequest) new MockHttpServletRequest("GET",(String) testPath[0]));
            urlResolver.setSiteKey(TESTSITE_NAME);

            assertTrue("Path " + testPath[0] + " not resolved correctly",
                    testPath[1].equals(urlResolver.getWorkspace())
                            && testPath[2].equals(urlResolver.getLocale())
                            && testPath[3].equals(urlResolver.getPath()));
            Object nodeObject = null;
            String path = null;
            try {
                nodeObject = urlResolver.getNode();
                path = ((JCRNodeWrapper) nodeObject).getPath();
            } catch (Exception e) {
                nodeObject = e;
                path = "";
            }
            assertTrue("Path " + testPath[0] + " not resolved correctly",
                    ((Class<?>)testPath[4]).isAssignableFrom(nodeObject.getClass())
                            && testPath[5].equals(path));
        }
    }

    @Test
    public void testVanityURLResolving() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);

        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(TESTSITE_NAME);

        JCRNodeWrapper pageNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/testPage");

        VanityUrl englishVanityUrl = new VanityUrl("/test4page", TESTSITE_NAME,
                "en");
        englishVanityUrl.setActive(true);
        VanityUrl englishVanityUrl2 = new VanityUrl("/test4page2",
                TESTSITE_NAME, "en");
        englishVanityUrl2.setActive(false);
        VanityUrl englishVanityUrl3 = new VanityUrl("/test4page/page3",
                TESTSITE_NAME, "en");
        englishVanityUrl3.setActive(true);

        VanityUrl frenchVanityUrl = new VanityUrl("/test4page/french",
                TESTSITE_NAME, "fr");
        frenchVanityUrl.setActive(true);
        VanityUrl frenchVanityUrl2 = new VanityUrl("/test4page/french2",
                TESTSITE_NAME, "fr");
        frenchVanityUrl2.setDefaultMapping(true);
        frenchVanityUrl2.setActive(true);

        List<VanityUrl> vanityUrls = new ArrayList<VanityUrl>();
        vanityUrls.add(englishVanityUrl);
        vanityUrls.add(englishVanityUrl2);
        vanityUrls.add(englishVanityUrl3);
        vanityUrls.add(frenchVanityUrl);
        vanityUrls.add(frenchVanityUrl2);
        Set<String> languages = new HashSet<String>();
        languages.add("en");
        languages.add("fr");

        getVanityUrlService().saveVanityUrlMappings(pageNode, vanityUrls,
                languages);

        assertTrue("Wrong URL returned", getVanityUrlService()
                .getVanityUrlForWorkspaceAndLocale(pageNode,
                        Constants.EDIT_WORKSPACE, Locale.ENGLISH, TESTSITE_NAME).getUrl()
                .equals("/test4page"));
        assertTrue("Wrong URL returned", getVanityUrlService()
                .getVanityUrlForWorkspaceAndLocale(pageNode,
                        Constants.EDIT_WORKSPACE, Locale.FRENCH, TESTSITE_NAME).getUrl()
                .equals("/test4page/french2"));
        URLResolver urlResolver = null;

        JCRNodeWrapper resolvedNode;

        try {
            urlResolver = getUrlResolverFactory().createURLResolver("/edit/default/test4page", site.getServerName(), (HttpServletRequest) new MockHttpServletRequest("GET","/edit/default/testpage"));
            urlResolver.getNode();
            fail("Node should not be returned as edit servlet does not resolve vanity URLs");
        } catch (PathNotFoundException e) {
        }

        try {
            urlResolver = getUrlResolverFactory().createURLResolver("/render/live/test4page", site.getServerName(), (HttpServletRequest) new MockHttpServletRequest("GET","/render/live/testpage"));
            urlResolver.getNode();
            fail("Node should not be returned as it is not published yet");
        } catch (PathNotFoundException e) {
        }

        languages.clear();
        languages.add("en");
        ServicesRegistry.getInstance().getJCRPublicationService().publishByMainId(
                pageNode.getIdentifier(), Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, true, null);

        urlResolver = getUrlResolverFactory().createURLResolver("/render/live/test4page", site.getServerName(), (HttpServletRequest) new MockHttpServletRequest("GET","/render/live/testpage"));
        resolvedNode = urlResolver.getNode();
        assertTrue("Wrong node or language returned", pageNode
                .equals(resolvedNode)
                && "en".equals(resolvedNode.getLanguage()));

        try {
            urlResolver = getUrlResolverFactory().createURLResolver("/render/live/test4page2", site.getServerName(), (HttpServletRequest) new MockHttpServletRequest("GET","/render/live/testpage2"));
            urlResolver.getNode();
            fail("Node should not be returned as mapping is not active");
        } catch (PathNotFoundException e) {
        }

        urlResolver = getUrlResolverFactory().createURLResolver("/render/live/test4page/page3", site.getServerName(), (HttpServletRequest) new MockHttpServletRequest("GET","/render/live/testpage/page3"));
        resolvedNode = urlResolver.getNode();
        assertTrue("Wrong node or language returned", pageNode
                .equals(resolvedNode)
                && "en".equals(resolvedNode.getLanguage()));


        urlResolver = getUrlResolverFactory().createURLResolver("/render/live/test4page/french2", site.getServerName(), (HttpServletRequest) new MockHttpServletRequest("GET","/render/live/testpage/french2"));
        try {
            resolvedNode = urlResolver.getNode();
            assertTrue("Wrong node or language returned - all vanity urls are published, not just the one of the published language", pageNode
                    .equals(resolvedNode)
                    && "fr".equals(resolvedNode.getLanguage()));
        } catch (PathNotFoundException e) {
        }

        languages.clear();
        languages.add("fr");
        ServicesRegistry.getInstance().getJCRPublicationService().publishByMainId(
                pageNode.getIdentifier(), Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, true, null);

        urlResolver = getUrlResolverFactory().createURLResolver("/render/live/test4page/french2", site.getServerName(), (HttpServletRequest) new MockHttpServletRequest("GET","/render/live/testpage/french2"));
        resolvedNode = urlResolver.getNode();
        assertTrue("Wrong node or language returned", pageNode
                .equals(resolvedNode)
                && "fr".equals(resolvedNode.getLanguage()));

        urlResolver = getUrlResolverFactory().createURLResolver("/render/live/test4page/french", site.getServerName(), (HttpServletRequest) new MockHttpServletRequest("GET","/render/live/testpage/french"));
        resolvedNode = urlResolver.getNode();
        assertTrue("Wrong node or language returned", pageNode
                .equals(resolvedNode)
                && "fr".equals(resolvedNode.getLanguage()));
    }

    private VanityUrlService getVanityUrlService() {
        return (VanityUrlService) SpringContextSingleton
                .getBean(VanityUrlService.class.getName());
    }

    private URLResolverFactory getUrlResolverFactory() {
        return (URLResolverFactory) SpringContextSingleton.getBean("urlResolverFactory");
    }

}
