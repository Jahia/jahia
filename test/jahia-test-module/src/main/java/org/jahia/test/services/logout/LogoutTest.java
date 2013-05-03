/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.test.services.logout;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.jcr.PathNotFoundException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
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
            site = TestHelper.createSite(SITE_KEY, "localhost", TestHelper.WEB_TEMPLATES, null, null);
        }
        Set<String> languages = new HashSet<String>();
        languages.add("en");
        site.setLanguages(languages);
        site.setDefaultLanguage("en");
        service.updateSystemSitePermissions(site);
        service.setDefaultSite(site);
        
        setSessionSite(site);

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
        assertEquals("Logout from default unPublished page failed ", "/sites/"+SITE_KEY+"/home.html", returnUrl);
    }

    @Test
    public void logoutuserDashboard() throws Exception {
        String returnUrl = perform("/en/users/root.user-home.html");
        assertEquals("Logout from user dashboard page failed ", "/sites/"+SITE_KEY+"/home.html", returnUrl);
    }

    @Test
    public void logoutAdministration() throws Exception {
        String returnUrl = perform("/administration");
        assertEquals("Logout from administration failed ", "/administration", returnUrl);
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
        String baseurl = getBaseServerURL() + Jahia.getContextPath();
        HttpMethod method = new GetMethod(baseurl + "/cms/logout");
        if (url.equals("/administration")) {
            method.setQueryString(new NameValuePair[]{
                    new NameValuePair("redirect","/administration")
            });
        }
        method.setRequestHeader("Referer",baseurl + url);
        getHttpClient().executeMethod(method);
        return StringUtils.isEmpty(Jahia.getContextPath())
                || !(method.getPath().startsWith(Jahia.getContextPath())) ? method
                .getPath() : StringUtils.substringAfter(method.getPath(),
                Jahia.getContextPath());

    }

}
