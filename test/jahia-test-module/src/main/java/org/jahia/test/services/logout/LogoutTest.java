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
package org.jahia.test.services.logout;

import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.seo.urlrewrite.UrlRewriteService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.PathNotFoundException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * User: david
 * Date: 4/11/12
 * Time: 2:13 PM
 */
public class LogoutTest extends JahiaTestCase {

    private static final String SITE_KEY = "logoutSite";

    private static UrlRewriteService engine;

    private static JahiaSite defaultSite = null;

    private static boolean seoRulesEnabled = false;
    private static boolean seoRemoveCmsPrefix = false;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        engine = (UrlRewriteService) SpringContextSingleton.getBean("UrlRewriteService");
        if (!engine.isSeoRulesEnabled()) {
            engine.setSeoRulesEnabled(true);
            seoRulesEnabled = true;
        }
        if (!engine.isSeoRemoveCmsPrefix()) {
            engine.setSeoRemoveCmsPrefix(true);
            seoRemoveCmsPrefix = true;
        }
        if (seoRulesEnabled || seoRemoveCmsPrefix) {
            engine.afterPropertiesSet();
        }

        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        defaultSite = service.getDefaultSite();

        JahiaSite site = service.getSiteByKey(SITE_KEY);
        if (site == null) {
            site = TestHelper.createSite(SITE_KEY, "localhost", TestHelper.WEB_TEMPLATES);
            site = (JahiaSite) JCRSessionFactory.getInstance().getCurrentUserSession().getNode(site.getJCRLocalPath());
        }
        Set<String> languages = new HashSet<String>();
        languages.add("en");
        site.setLanguages(languages);
        site.setDefaultLanguage("en");
        service.updateSystemSitePermissions(site);
        service.setDefaultSite(site);

        JahiaSite siteForServerName = service.getSiteByServerName(getRequest().getServerName());

        // Add two page and publish one
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();
        String defaultLanguage = site.getDefaultLanguage();
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        JCRSessionWrapper session = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        try {
            session.getNode("/sites/"+SITE_KEY+"/home/pubPage");
        }
        catch (PathNotFoundException e){
            JCRNodeWrapper n = session.getNode("/sites/"+SITE_KEY+"/home").addNode("pubPage","jnt:page");
            n.setProperty("j:templateName", "simple");
            n.setProperty("jcr:title", "title0");
            session.save();
        }
        try {
            session.getNode("/sites/"+SITE_KEY+"/home/privPage");
        }
        catch (PathNotFoundException e){
            JCRNodeWrapper n = session.getNode("/sites/"+SITE_KEY+"/home").addNode("privPage","jnt:page");
            n.setProperty("j:templateName", "simple");
            n.setProperty("jcr:title", "title1");
            session.save();
        }
        jcrService.publishByMainId(session.getNode("/sites/"+SITE_KEY+"/files").getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, null);
        jcrService.publishByMainId(session.getNode("/sites/"+SITE_KEY+"/search-results").getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, null);
        jcrService.publishByMainId(session.getNode("/sites/"+SITE_KEY+"/home").getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, null);
        jcrService.publishByMainId(session.getNode("/sites/"+SITE_KEY+"/home/pubPage").getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, null);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        if (defaultSite != null) {
            ServicesRegistry.getInstance().getJahiaSitesService().setDefaultSite(defaultSite);
            defaultSite = null;
        }
        TestHelper.deleteSite(SITE_KEY);
        JCRSessionFactory.getInstance().closeAllSessions();

        if (seoRulesEnabled || seoRemoveCmsPrefix) {
            engine.setSeoRulesEnabled(!seoRulesEnabled);
            engine.setSeoRemoveCmsPrefix(!seoRemoveCmsPrefix);
            engine.afterPropertiesSet();
            seoRulesEnabled = false;
            seoRemoveCmsPrefix = false;
        }
        engine = null;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void logoutLivePub() throws Exception{
        String returnUrl = perform("/en/sites/"+SITE_KEY+"/home/pubPage.html");
        assertEquals("Logout from live published page failed ", "/sites/"+SITE_KEY+"/home/pubPage.html", returnUrl);
    }

    @Test
    public void logoutEditPub() throws Exception {
        String returnUrl = perform("/cms/render/default/en/sites/"+SITE_KEY+"/home/pubPage.html");
        assertEquals("Logout from default published page failed ", "/sites/"+SITE_KEY+"/home/pubPage.html", returnUrl);
    }

    @Test
    public void logoutEditPrivate() throws Exception {
        String returnUrl = perform("/cms/render/default/en/sites/"+SITE_KEY+"/home/privPage.html");
        assertEquals("Logout from default unPublished page failed ", "/sites/" + SITE_KEY + "/home.html", returnUrl);
    }

    @Test
    public void logoutUserDashboard() throws Exception {
        String returnUrl = perform("/en/users/root.user-home.html");
        assertEquals("Logout from user dashboard page failed ", "/sites/" + SITE_KEY + "/home.html", returnUrl);
    }

    @Test
    public void logoutFilesLive() throws Exception {
        String returnUrl = perform("/sites/"+SITE_KEY+"/files");
        assertEquals("Logout from live files failed ", "/sites/"+SITE_KEY+"/files.html", returnUrl);
    }

    @Test
    public void logoutFilesEdit() throws Exception {
        String returnUrl = perform("/cms/render/default/en/sites/"+SITE_KEY+"/files.html");
        assertEquals("Logout from default files failed ", "/sites/"+SITE_KEY+"/files.html", returnUrl);
    }


    protected String perform(String url) throws Exception {
        loginRoot();
        return logout(url);
    }

    protected String logout(String url) throws Exception {
        String returnUrl = null;
        String baseurl = getBaseServerURL() + Jahia.getContextPath();
        HttpGet method = new HttpGet(baseurl + "/cms/logout");
        method.addHeader("Referer", baseurl + url);
        HttpClientContext context = HttpClientContext.create();

        try (CloseableHttpResponse response = getHttpClient().execute(method, context)) {
            String path = context.getRedirectLocations().get(context.getRedirectLocations().size() - 1).getPath();
            returnUrl = StringUtils.isEmpty(Jahia.getContextPath()) || !(path.startsWith(Jahia.getContextPath())) ?
                    path : StringUtils.substringAfter(path, Jahia.getContextPath());
        }
       return returnUrl;
    }

}
