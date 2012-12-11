/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render.filter.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;

import net.sf.ehcache.Element;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.BaseAttributesFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.services.render.filter.cache.CacheKeyGenerator;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRGroup;
import org.jahia.services.usermanager.jcr.JCRGroupManagerProvider;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.test.JahiaAdminUser;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.utils.Patterns;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 12 janv. 2010
 */
public class CacheFilterTest extends JahiaTestCase {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(CacheFilterTest.class);
    private final static String TESTSITE_NAME = "test";    

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JahiaSite site = TestHelper.createSite(TESTSITE_NAME);
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
            JCRNodeWrapper siteNode = (JCRSiteNode) session.getNode("/sites/"+site.getSiteKey());
            
            String templatesFolder = "/sites/"+site.getSiteKey() + "/templates";
            InputStream importStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("imports/importTemplatesForCacheTest.xml");
            session.importXML(templatesFolder + "/base", importStream,
                    ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
            importStream.close();
            session.save();   
            
            JCRNodeWrapper shared = siteNode.getNode("home");
            if (shared.hasNode("testContent")) {
                shared.getNode("testContent").remove();
            }
            if(shared.isVersioned()) session.checkout(shared);
            JCRNodeWrapper node = shared.addNode("testContent", "jnt:page");
            node.setProperty("jcr:title", "English test page");
            node.setProperty("j:templateNode", session.getNode(
                    templatesFolder + "/base/pagetemplate/subpagetemplate"));            
            node.addNode("testType2", "jnt:mainContent");
            session.save();
            final JCRPublicationService service = JCRPublicationService.getInstance();
            
            List<PublicationInfo> infoList = service.getPublicationInfo(
                    session.getNode(
                            templatesFolder + "/base/pagetemplate").getIdentifier(), new LinkedHashSet<String>(Arrays.asList(Locale.ENGLISH.toString())),
                    true, true, true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
            service.publishByInfoList(infoList, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,Collections.<String>emptyList());
            
            infoList = service.getPublicationInfo(
                    shared.getIdentifier(), new LinkedHashSet<String>(Arrays.asList(Locale.ENGLISH.toString())),
                    true, true, true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
            service.publishByInfoList(infoList, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,Collections.<String>emptyList());
            
            session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);
            node = session.getNode("/sites/"+site.getSiteKey()+"/home/testContent");
        } catch (Exception e) {
            logger.warn("Exception during test setUp", e);
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
    }
    
    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }    

    @Test
    public void testCacheFilter() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        RenderFilter outFilter = new AbstractFilter() {
            @Override
            public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setRenderService(RenderService.getInstance());
        
        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper node = liveSession.getNode("/sites/"+TESTSITE_NAME+"/home/testContent");
        ParamBean paramBean = (ParamBean)Jahia.getThreadParamBean();        
        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(node.getResolveSite());
        context.setServletPath("/render");
        ChannelService channelService = (ChannelService) SpringContextSingleton.getInstance().getContext().getBean("ChannelService");
        context.setChannel(channelService.getChannel(Channel.GENERIC_CHANNEL));
        Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        context.setWorkspace(liveSession.getWorkspace().getName());
        context.getRequest().setAttribute("script",
                RenderService.getInstance().resolveScript(resource, context));

        // test on a resource from the default Jahia module
        BaseAttributesFilter attributesFilter = new BaseAttributesFilter();
        attributesFilter.setRenderService(RenderService.getInstance());

        RenderFilter cacheFilter = (RenderFilter) SpringContextSingleton.getInstance().getContext().getBean("cacheFilter");

        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = moduleCacheProvider.getKeyGenerator();
        context.getRequest().setAttribute("cache.requestParameters", Patterns.COMMA.split("cacheinfo,moduleinfo"));
        String key = generator.generate(resource, context);
        moduleCacheProvider.getCache().removeAll();
        
        RenderChain chain = new RenderChain(attributesFilter, cacheFilter, outFilter);

        String result = chain.doFilter(context, resource);
        // AggregateCacheFilter will replace the value of the query string param by their real value as we have no params in the request we have to empty it
        // Trouble here was that it is an empty treemap to string and not empty string or null
        
        /*
        Matcher m = AggregateCacheFilter.QUERYSTRING_REGEXP.matcher(key);
        if (m.matches()) {
            String qsString = m.group(2);
            key = key.replace(qsString,new TreeMap<String, String>().toString());
        }
        */
        final Element element = moduleCacheProvider.getCache().get(key);
        assertNotNull("Html Cache does not contains our html rendering", element);
        assertTrue("Content Cache and rendering are not equals",((String)((CacheEntry<?>)element.getValue()).getObject()).contains(result));
    }
    
    @Test
    public void testFixForEmptyCacheBug() throws Exception {
        String firstResponse = null;
        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);
        final JCRNodeWrapper node = liveSession.getNode("/sites/"+TESTSITE_NAME+"/home/testContent");        
        HttpClient client = new HttpClient();
        GetMethod nodeGet = new GetMethod(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/live/en" +
            node.getPath() + ".html");
        try {
            int responseCode = client.executeMethod(nodeGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            firstResponse = nodeGet.getResponseBodyAsString();
            logger.info("Response body=[" + firstResponse + "]");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        
        JCRTemplate.getInstance().doExecuteWithSystemSession(
                JahiaUserManagerService.GUEST_USERNAME,
                Constants.LIVE_WORKSPACE, Locale.ENGLISH, new JCRCallback<String>() {
                    public String doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        ParamBean paramBean = (ParamBean)Jahia.getThreadParamBean();
                        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), session.getUser());
                        context.setSite(node.getResolveSite());
                        ChannelService channelService = (ChannelService) SpringContextSingleton.getInstance().getContext().getBean("ChannelService");
                        context.setChannel(channelService.getChannel(Channel.GENERIC_CHANNEL));                        
                        JCRNodeWrapper pageContentNode = session
                                .getNode("/sites/"
                                        + TESTSITE_NAME
                                        + "/templates/base/pagetemplate/subpagetemplate/pagecontent");
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

                        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton
                                .getInstance().getContext()
                                .getBean("ModuleCacheProvider");
                        CacheKeyGenerator generator = moduleCacheProvider
                                .getKeyGenerator();
                        String key = generator.generate(resource,
                                context);
                        String resourceId = StringUtils.substringAfterLast(key,
                                "#");
                        String firstpart = StringUtils.substringBeforeLast(key,
                                "#");
                        firstpart = StringUtils.substringBeforeLast(firstpart,
                                "#");
                        for (Object existingKey : moduleCacheProvider
                                .getCache().getKeys()) {
                            String existingKeyAsString = (String) existingKey;
                            if (existingKeyAsString.startsWith(firstpart)
                                    && existingKeyAsString.endsWith(resourceId)) {
                                moduleCacheProvider.getCache().remove(
                                        existingKey);
                            }
                        }

                        return key;
                    }
                });

        try {
            int responseCode = client.executeMethod(nodeGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            String responseBody = nodeGet.getResponseBodyAsString();
            logger.info("Response body=[" + responseBody + "]");
            if (firstResponse != null) {
                assertTrue(
                        "First and second response are not equal",
                        responseBody.replaceAll("(?m)^[ \t]*\r?\n", "").replaceAll("(?m)^[ \t]+", "").replaceAll("\r\n", "\n").equals(
                                firstResponse.replaceAll("(?m)^[ \t]*\r?\n", "").replaceAll("(?m)^[ \t]+", "").replaceAll("\r\n", "\n")));
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }    
    
    @Test
    public void testDependencies() throws Exception {
        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        RenderFilter outFilter = new AbstractFilter() {
            @Override
            public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setRenderService(RenderService.getInstance());
        
        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper node = liveSession.getNode("/sites/"+TESTSITE_NAME+"/home/testContent");        
        ParamBean paramBean = (ParamBean)Jahia.getThreadParamBean();
        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(node.getResolveSite());
        ChannelService channelService = (ChannelService) SpringContextSingleton.getInstance().getContext().getBean("ChannelService");
        context.setChannel(channelService.getChannel(Channel.GENERIC_CHANNEL));        
        context.setServletPath("/render");
        context.setWorkspace(liveSession.getWorkspace().getName());
        Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        context.getRequest().setAttribute("script",
                RenderService.getInstance().resolveScript(resource, context));

        // test on a resource from the default Jahia module
        BaseAttributesFilter attributesFilter = new BaseAttributesFilter();
        attributesFilter.setRenderService(RenderService.getInstance());

        RenderFilter cacheFilter = (RenderFilter) SpringContextSingleton.getInstance().getContext().getBean("cacheFilter");
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        
        moduleCacheProvider.getCache().removeAll();

        RenderChain chain = new RenderChain(attributesFilter, cacheFilter, outFilter);

        chain.doFilter(context, resource);

        final Element element1 = moduleCacheProvider.getDependenciesCache().get(node.getPath());
        assertNotNull("Node /shared should have dependencies",element1);
        assertTrue("Dependencies must not be empty",((Set<String>) element1.getValue()).size()>0);
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
        String templatesFolder = "/sites/"+TESTSITE_NAME + "/templates";
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRSiteNode site = (JCRSiteNode) session.getNode("/sites/"+TESTSITE_NAME);
        // Create three users
        final JCRUserManagerProvider userManagerProvider = JCRUserManagerProvider.getInstance();
        final JCRUser userAB = userManagerProvider.createUser("userAB", "password", new Properties());
        final JCRUser userAC = userManagerProvider.createUser("userAC", "password", new Properties());
        final JCRUser userBC = userManagerProvider.createUser("userBC", "password", new Properties());
        // Create three groups
        final JCRGroupManagerProvider groupManagerProvider = JCRGroupManagerProvider.getInstance();
        final JCRGroup groupA = groupManagerProvider.createGroup(site.getID(), "groupA", new Properties(), false);
        final JCRGroup groupB = groupManagerProvider.createGroup(site.getID(), "groupB", new Properties(), false);
        final JCRGroup groupC = groupManagerProvider.createGroup(site.getID(), "groupC", new Properties(), false);
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
        node.setProperty("j:templateNode", session.getNode(
                templatesFolder + "/base/pagetemplate/subpagetemplate"));
        final JCRNodeWrapper list = node.addNode("maincontent", "jnt:contentList");
        final JCRNodeWrapper contentA = list.addNode("contentA", "jnt:mainContent");
        contentA.setProperty("body","Content__A__");
        final JCRNodeWrapper contentB = list.addNode("contentB", "jnt:mainContent");
        contentB.setProperty("body","Content__B__");
        final JCRNodeWrapper contentC = list.addNode("contentC", "jnt:mainContent");
        contentC.setProperty("body","Content__C__");
        // Set acls for each content
        contentA.setAclInheritanceBreak(true);
        contentA.grantRoles("g:" + groupA.getGroupname(), new LinkedHashSet<String>(Arrays.asList("reader")));
        contentB.setAclInheritanceBreak(true);
        contentB.grantRoles("g:" + groupB.getGroupname(), new LinkedHashSet<String>(Arrays.asList("reader")));
        contentC.setAclInheritanceBreak(true);
        contentC.grantRoles("g:" + groupC.getGroupname(), new LinkedHashSet<String>(Arrays.asList("reader")));
        session.save();
        // Publish all
        final JCRPublicationService service = JCRPublicationService.getInstance();

        List<PublicationInfo> infoList = service.getPublicationInfo(
                session.getNode(
                        templatesFolder + "/base/pagetemplate").getIdentifier(), new LinkedHashSet<String>(Arrays.asList(Locale.ENGLISH.toString())),
                true, true, true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
        service.publishByInfoList(infoList, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,Collections.<String>emptyList());

        infoList = service.getPublicationInfo(
                shared.getIdentifier(), new LinkedHashSet<String>(Arrays.asList(Locale.ENGLISH.toString())),
                true, true, true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
        service.publishByInfoList(infoList, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,Collections.<String>emptyList());
        // Login as userAB using httpclient
        checkContentForUser(node, "userAB", "Content__A__", "Content__B__", "Content__C__");
        // Login as userAC using httpclient
        checkContentForUser(node, "userAC", "Content__A__", "Content__C__", "Content__B__");
        // Login as userBC using httpclient
        checkContentForUser(node, "userBC", "Content__B__", "Content__C__", "Content__A__");
    }

    private void checkContentForUser(JCRNodeWrapper node, String username, CharSequence firstContent, CharSequence secondContent,
                                     String missingContent) throws IOException, RepositoryException {
        HttpClient client = new HttpClient();
        PostMethod loginMethod = new PostMethod(getBaseServerURL() + Jahia.getContextPath() + "/cms/login");
        loginMethod.addParameter("username", username);
        loginMethod.addParameter("password", "password");
        loginMethod.addParameter("redirectActive", "false");

        int statusCode = client.executeMethod(loginMethod);
        assertTrue(statusCode== HttpStatus.SC_OK);
        
        // Use httpclient to render page
        GetMethod nodeGet = new GetMethod(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/live/en" +
                node.getPath() + ".html");
        try {
            int responseCode = client.executeMethod(nodeGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            String firstResponse = nodeGet.getResponseBodyAsString();
            // Assert existence of content
            assertTrue("Page for "+username+" should contains "+firstContent,firstResponse.contains(firstContent));
            assertTrue("Page for "+username+" should contains "+secondContent,firstResponse.contains(secondContent));
            assertFalse("Page for "+username+" should not contains "+missingContent,firstResponse.contains(missingContent));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            nodeGet.releaseConnection();
        }
        String baseurl = getBaseServerURL() + Jahia.getContextPath();
        HttpMethod method = new GetMethod(baseurl + "/cms/logout");
        try {
            client.executeMethod(method);
        } finally {
            method.releaseConnection();
        }
    }
}
