package org.jahia.services.logout;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.PathNotFoundException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 4/11/12
 * Time: 2:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogoutTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(LogoutTest.class);

    private HttpClient client;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {

        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey("logoutSite");
        if (site == null) {
            site = TestHelper.createSite("logoutSite", "localhost", TestHelper.WEB_TEMPLATES, null, null);
        }
        Set<String> languages = new HashSet<String>();
        languages.add("en");
        site.setLanguages(languages);
        site.setDefaultLanguage("en");
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        service.updateSite(site);
        ((ParamBean) Jahia.getThreadParamBean()).getSession(true).setAttribute(ParamBean.SESSION_SITE, site);

        // Add two page and publish one
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();
        String defaultLanguage = site.getDefaultLanguage();
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        JCRSessionWrapper session = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        try {
            session.getNode("/sites/logoutSite/home/pubPage");
        }
        catch (PathNotFoundException e){
            JCRNodeWrapper n = session.getNode("/sites/logoutSite/home").addNode("pubPage","jnt:page");
            n.setProperty("j:templateNode", session.getNode("/sites/logoutSite/templates/base/simple"));
            n.setProperty("jcr:title", "title0");
            session.save();
        }
        try {
            session.getNode("/sites/logoutSite/home/privPage");
        }
        catch (PathNotFoundException e){
            JCRNodeWrapper n = session.getNode("/sites/logoutSite/home").addNode("privPage","jnt:page");
            n.setProperty("j:templateNode", session.getNode("/sites/logoutSite/templates/base/simple"));
            n.setProperty("jcr:title", "title1");
            session.save();
        }
        jcrService.publishByMainId(session.getNode("/sites/logoutSite/files").getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, null);
        jcrService.publishByMainId(session.getNode("/sites/logoutSite/home").getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, null);
        jcrService.publishByMainId(session.getNode("/sites/logoutSite/home/pubPage").getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, null);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void logoutLivePub() throws Exception{
        String returnUrl = perform("/en/sites/logoutSite/home/pubPage.html");
        assertEquals("Logout from live published page failed ", "/sites/logoutSite/home/pubPage.html", returnUrl);
    }
    
    @Test
    public void logoutEditPub() throws Exception {
        String returnUrl = perform("/cms/render/default/en/sites/logoutSite/home/pubPage.html");
        assertEquals("Logout from default published page failed ", "/sites/logoutSite/home/pubPage.html", returnUrl);
    }

    @Test
    public void logoutEditPrivate() throws Exception {
        String returnUrl = perform("/cms/render/default/en/sites/logoutSite/home/privPage.html");
        assertEquals("Logout from default unPublished page failed ", "/sites/logoutSite/home.html", returnUrl);
    }

    @Test
    public void logoutuserDashboard() throws Exception {
        String returnUrl = perform("/en/users/root.user-home.html");
        assertEquals("Logout from user dashboard page failed ", "/sites/logoutSite/home.html", returnUrl);
    }

    @Test
    public void logoutAdministration() throws Exception {
        String returnUrl = perform("/administration");
        assertEquals("Logout from administration failed ", "/administration", returnUrl);
    }

    @Test
    public void logoutFilesLive() throws Exception {
        String returnUrl = perform("/sites/logoutSite/files");
        assertEquals("Logout from live files failed ", "/sites/logoutSite/files.html", returnUrl);
    }

    @Test
    public void logoutFilesEdit() throws Exception {
        String returnUrl = perform("/cms/render/default/en/sites/logoutSite/files.html");
        assertEquals("Logout from default files failed ", "/sites/logoutSite/files.html", returnUrl);
    }


    protected String perform(String url) throws Exception {
        login();
        return logout(url);
    }
    
    protected void login() throws Exception {
        String baseurl = "http://localhost:8080" + Jahia.getContextPath() + "/cms";
        client = new HttpClient();
        PostMethod loginMethod = new PostMethod(baseurl + "/login");
        loginMethod.addParameter("username", "root");
        loginMethod.addParameter("password", "root1234");
        loginMethod.addParameter("redirectActive", "false");
        // the next parameter is required to properly activate the valve check.
        loginMethod.addParameter(LoginEngineAuthValveImpl.LOGIN_TAG_PARAMETER, "1");

        int statusCode = client.executeMethod(loginMethod);

    }

    protected String logout(String url) throws Exception {
        String baseurl = "http://localhost:8080" + Jahia.getContextPath();
        HttpMethod method = new GetMethod(baseurl + "/cms/logout");
        if (url.equals("/administration")) {
            method.setQueryString(new NameValuePair[]{
                    new NameValuePair("redirect","/administration")
            });
        }
        method.setRequestHeader("Referer",baseurl + url);
        client.executeMethod(method);
        return method.getPath();

    }

}
