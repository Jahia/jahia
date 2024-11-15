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
package org.jahia.test.services.vanity;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlMapper;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.junit.*;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class VanityUrlMapperTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(VanityUrlMapperTest.class);

    private static final String SERVERNAME_LOCALHOST = "localhost";
    private static final String SERVERNAME_NOT_MAPPED = "notmapped";
    private static final String SERVERNAME_TEST1 = "test1";
    private static final String SERVERNAME_TEST2 = "test2";
    private static final String SERVERNAME_TEST3 = "test3";

    private static final String SITEA = "siteA";
    private static final String SITEB = "siteB";
    private static final String SITEC = "siteC";
    private static final String SITED = "siteD";

    private static final String SITEE = "siteE";
    private static final String SITEF = "siteF";

    private static final String AMBIGUOUS_VANITY = "ambiguous_vanity";
    private static final String AMBIGUOUS_I18N_VANITY = "ambiguous_i18n_vanity";
    private static final String UNIQUE_VANITY = "unique_vanity";

    private static VanityUrlService vanityUrlService;
    private static VanityUrlMapper vanityUrlMapper = new VanityUrlMapper();

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            vanityUrlService = (VanityUrlService) SpringContextSingleton.getBean("org.jahia.services.seo.jcr.VanityUrlService");

            initSite(SITEA, SERVERNAME_LOCALHOST, true, false,true, false,"en");
            initSite(SITEB, SERVERNAME_LOCALHOST, true, false,true, false,"en");
            initSite(SITEC, SERVERNAME_TEST1, true, false,true, false,"en");
            initSite(SITED, SERVERNAME_TEST2, false, true,false, false, "en");

            initSite(SITEE, SERVERNAME_LOCALHOST, false, false,false, true, "en");
            initSite(SITEF, SERVERNAME_TEST3, false, false,false, true, "fr");
        } catch (Exception e) {
            logger.error("Error setting up ValidationTest environment", e);
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(SITEA);
            TestHelper.deleteSite(SITEB);
            TestHelper.deleteSite(SITEC);
            TestHelper.deleteSite(SITED);
            TestHelper.deleteSite(SITEE);
            TestHelper.deleteSite(SITEF);
        } catch (Exception e) {
            logger.error("Error tearing down ValidationTest environment", e);
        }
    }

    /**
     * When:
     * - vanity are uniques on sites A, B and C page.
     * - site D does not have vanity for the page.
     * - servername is localhost
     *
     * Then:
     * - only site A, B, C are allow to use there unique vanity
     */
    @Test
    public void simpleVanityTestUsingLocalhost() {
        simpleTestMapper(SERVERNAME_LOCALHOST, SITEA, false);
        simpleTestMapper(SERVERNAME_LOCALHOST, SITEB, false);
        simpleTestMapper(SERVERNAME_LOCALHOST, SITEC, false);
        simpleTestMapper(SERVERNAME_LOCALHOST, SITED, false);
    }

    /**
     * When:
     * - vanity are uniques on sites A, B and C page.
     * - site D does not have vanity for the page.
     * - servername is mapped to a site C.
     *
     * Then:
     * - only site C is allow to use his vanity because the servername is directly resolving "site C"
     */
    @Test
    public void simpleVanityTestUsingServernameMappedOnSiteUsingTheVanity() {
        simpleTestMapper(SERVERNAME_TEST1, SITEA, false);
        simpleTestMapper(SERVERNAME_TEST1, SITEB, false);
        simpleTestMapper(SERVERNAME_TEST1, SITEC, true);
        simpleTestMapper(SERVERNAME_TEST1, SITED, false);
    }

    /**
     * When:
     * - vanity are uniques on sites A, B and C page.
     * - site D does not have vanity for the page.
     * - servername is mapped to a site D.
     *
     * Then:
     * - nobody can use the vanity, because servername will resolve "site D"
     */
    @Test
    public void simpleVanityTestUsingServernameMappedOnSiteNotUsingTheVanity() {
        simpleTestMapper(SERVERNAME_TEST2, SITEA, false);
        simpleTestMapper(SERVERNAME_TEST2, SITEB, false);
        simpleTestMapper(SERVERNAME_TEST2, SITEC, false);
        simpleTestMapper(SERVERNAME_TEST2, SITED, false);
    }

    /**
     * When:
     * - vanity are uniques on sites A, B and C page.
     * - site D does not have vanity for the page.
     * - servername is not mapped.
     *
     * Then:
     * - everybody is allowed to use there unique vanities, because servername does not alow site resolution.
     */
    @Test
    public void simpleVanityTestUsingServernameNotMapped() {
        simpleTestMapper(SERVERNAME_NOT_MAPPED, SITEA, true);
        simpleTestMapper(SERVERNAME_NOT_MAPPED, SITEB, true);
        simpleTestMapper(SERVERNAME_NOT_MAPPED, SITEC, true);
        simpleTestMapper(SERVERNAME_NOT_MAPPED, SITED, false);
    }

    /**
     * When:
     * - ambiguous vanity is used by site A, B, C page
     * - unique vanity is used by site D page
     * - servername is localhost
     *
     * Then:
     * - nobody can use the vanity url
     */
    @Test
    public void ambiguousVanityTestUsingLocalhost() {
        ambiguousTestMapper(SERVERNAME_LOCALHOST, SITEA, null);
        ambiguousTestMapper(SERVERNAME_LOCALHOST, SITEB, null);
        ambiguousTestMapper(SERVERNAME_LOCALHOST, SITEC, null);
        ambiguousTestMapper(SERVERNAME_LOCALHOST, SITED, null);
    }

    /**
     * When:
     * - ambiguous vanity is used by site A, B, C for the page
     * - unique vanity is used by site D for the page
     * - servername is mapped to a site C
     *
     * Then:
     * - site C is allow to use the ambiguous vanity, because servername allow to resolve it
     * - site D is not allow to use his unique vanity, because the servername will resolve "site C"
     */
    @Test
    public void ambiguousVanityTestUsingServernameMappedOnSiteUsingTheVanity() {
        ambiguousTestMapper(SERVERNAME_TEST1, SITEA, null);
        ambiguousTestMapper(SERVERNAME_TEST1, SITEB, null);
        ambiguousTestMapper(SERVERNAME_TEST1, SITEC, AMBIGUOUS_VANITY);
        ambiguousTestMapper(SERVERNAME_TEST1, SITED, null);
    }

    /**
     * When:
     * - ambiguous vanity is used by site A, B, C
     * - unique vanity is used by site D
     * - servername is mapped to a site D
     *
     * Then:
     * - site C is not allow to use the ambiguous vanity, because the servername will resolve "site D"
     * - site D is allow to use his unique vanity, because the servername allow to resolve it
     */
    @Test
    public void ambiguousVanityTestUsingServernameMappedOnSiteNotUsingTheVanity(){
        ambiguousTestMapper(SERVERNAME_TEST2, SITEA, null);
        ambiguousTestMapper(SERVERNAME_TEST2, SITEB, null);
        ambiguousTestMapper(SERVERNAME_TEST2, SITEC, null);
        ambiguousTestMapper(SERVERNAME_TEST2, SITED, UNIQUE_VANITY);
    }

    /**
     * When:
     * - ambiguous vanity is used by site A, B, C
     * - unique vanity is used by site D
     * - servername is not mapped to a site
     *
     * Then:
     * - nobody can use the ambiguous vanity because servername does not allow site resolution
     * - site D is allow to use his unique vanity, because his vanity is unique
     */
    @Test
    public void ambiguousVanityTestUsingServernameNotMapped(){
        ambiguousTestMapper(SERVERNAME_NOT_MAPPED, SITEA, null);
        ambiguousTestMapper(SERVERNAME_NOT_MAPPED, SITEB, null);
        ambiguousTestMapper(SERVERNAME_NOT_MAPPED, SITEC, null);
        ambiguousTestMapper(SERVERNAME_NOT_MAPPED, SITED, UNIQUE_VANITY);
    }

    /**
     * When:
     * - ambiguous vanity is used by site E, F
     * - each site use the same vanity in different languages
     * - servername is not mapped to a site
     *
     * Then:
     * - nobody can use the ambiguous vanity because servername does not allow site resolution
     */
    @Test
    public void ambiguousi18NVanityTestUsingServernameNotMapped(){
        ambiguousi18nTestMapper(SERVERNAME_NOT_MAPPED, SITEE, null, "en");
        ambiguousi18nTestMapper(SERVERNAME_NOT_MAPPED, SITEF, null, "en");
        ambiguousi18nTestMapper(SERVERNAME_NOT_MAPPED, SITEE, null, "fr");
        ambiguousi18nTestMapper(SERVERNAME_NOT_MAPPED, SITEF, null, "fr");
    }

    /**
     * When:
     * - ambiguous vanity is used by site E, F
     * - each site use the same vanity in different languages
     * - servername is mapped to site F
     *
     * Then:
     * - Only site F is able to use the vanity, because the servername allow site resolution
     */
    @Test
    public void ambiguousi18NsVanityTestUsingServernameMappedOnSiteUsingTheVanity() {
        ambiguousi18nTestMapper(SERVERNAME_TEST3, SITEE, null, "en");
        ambiguousi18nTestMapper(SERVERNAME_TEST3, SITEF, null, "en");
        ambiguousi18nTestMapper(SERVERNAME_TEST3, SITEE, null, "fr");
        ambiguousi18nTestMapper(SERVERNAME_TEST3, SITEF, AMBIGUOUS_I18N_VANITY, "fr");
    }

    private static void simpleTestMapper(String servername, String site, boolean shouldBeVanity) {
        HttpServletRequest request = mockNewServletRequest(servername);
        vanityUrlMapper.checkVanityUrl(request, "", "/default/en/sites/" + site + "/home/simple.html");
        Assert.assertEquals(shouldBeVanity ? ("/cms/render/default/" + site) : "/cms/render/default/en/sites/" + site + "/home/simple.html", request.getAttribute(VanityUrlMapper.VANITY_KEY));
    }

    private static void ambiguousTestMapper(String servername, String site, String expectedVanity) {
        HttpServletRequest request = mockNewServletRequest(servername);
        vanityUrlMapper.checkVanityUrl(request, "", "/default/en/sites/" + site + "/home/ambiguous.html");
        Assert.assertEquals(expectedVanity != null ? ("/cms/render/default" + (expectedVanity.charAt(0) != '/' ? ('/' + expectedVanity) : expectedVanity)): "/cms/render/default/en/sites/" + site + "/home/ambiguous.html", request.getAttribute(VanityUrlMapper.VANITY_KEY));
    }

    private static void ambiguousi18nTestMapper(String servername, String site, String expectedVanity, String language) {
        HttpServletRequest request = mockNewServletRequest(servername);
        vanityUrlMapper.checkVanityUrl(request, "", "/default/" + language + "/sites/" + site + "/home/i18n.html");
        Assert.assertEquals(expectedVanity != null ? ("/cms/render/default" + (expectedVanity.charAt(0) != '/' ? ('/' + expectedVanity) : expectedVanity)): "/cms/render/default/" + language + "/sites/" + site + "/home/i18n.html", request.getAttribute(VanityUrlMapper.VANITY_KEY));
    }

    private static HttpServletRequest mockNewServletRequest(String servername) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[] { HttpServletRequest.class },
                new InvocationHandler() {
                    Map<String, Object> attributes = new HashMap<>();

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("setAttribute")) {
                            attributes.put((String) args[0], args[1]);
                        } else if (method.getName().equals("getAttribute")) {
                            return attributes.get(args[0]);
                        } else if (method.getName().equals("getParameterMap")) {
                            return new HashMap<String, String[]>();
                        } else if (method.getName().equals("getContextPath")) {
                            return "";
                        } else if (method.getName().equals("getServerName")) {
                            return servername;
                        }
                        return null;
                    }
                });
    }

    private static void initSite(String siteKey, String servername, boolean addAmbiguousVanity, boolean addUniqueVanity ,boolean addSimpleVanity, boolean addI18nVanity, String language) throws Exception {
        JahiaSite site = TestHelper.createSite(siteKey, servername, TestHelper.WEB_TEMPLATES);
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            try {
                JCRNodeWrapper ambiguousPage = createPage((JCRSiteNode) session.getNode(site.getJCRLocalPath()), "ambiguous");
                JCRNodeWrapper simplePage = createPage((JCRSiteNode) session.getNode(site.getJCRLocalPath()), "simple");
                JCRNodeWrapper i18nPage = createPage((JCRSiteNode) session.getNode(site.getJCRLocalPath()), "i18n");

                if (addSimpleVanity) {
                    // simple vanity use the siteKey as url to be unique
                    vanityUrlService.saveVanityUrlMapping(simplePage, createVanity(true, true, "/" + siteKey, site.getSiteKey(), language));
                }

                if (addI18nVanity) {
                    vanityUrlService.saveVanityUrlMapping(i18nPage, createVanity(true, true, AMBIGUOUS_I18N_VANITY, site.getSiteKey(), language));
                }

                if (addAmbiguousVanity) {
                    vanityUrlService.saveVanityUrlMapping(ambiguousPage, createVanity(true, true, AMBIGUOUS_VANITY, site.getSiteKey(), language));
                }

                if (addUniqueVanity) {
                    vanityUrlService.saveVanityUrlMapping(ambiguousPage, createVanity(true, true, UNIQUE_VANITY, site.getSiteKey(), language));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    private static JCRNodeWrapper createPage(JCRSiteNode site, String pageName) throws RepositoryException {
        JCRNodeWrapper page = site.getNode("home").addNode(pageName, "jnt:page");
        page.setProperty("j:templateName", "simple");
        page.setProperty("jcr:title", pageName);
        return page;
    }

    private static VanityUrl createVanity(boolean isActive, boolean isDefault, String url, String site, String language) {
        VanityUrl vanityUrl = new VanityUrl();
        vanityUrl.setActive(isActive);
        vanityUrl.setDefaultMapping(isDefault);
        vanityUrl.setLanguage(language);
        vanityUrl.setSite(site);
        vanityUrl.setUrl(url);
        return vanityUrl;
    }
}
