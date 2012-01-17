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

package org.jahia.services.render.filter;

import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.JahiaAdminUser;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for the conditional execution of the rendering filters.
 *
 * @author Sergiy Shyrkov
 */
public class ConditionalFilterTest {

    private static class TestFilter extends AbstractFilter {
        @Override
        public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                throws Exception {
            return "TestFilter " + previousOut;
        }
    }

    private JCRNodeWrapper node;
    private ParamBean paramBean;
    private JCRSessionWrapper session;
    private JCRSiteNode site;

    @Before
    public void setUp() throws Exception {
        JahiaSite site = TestHelper.createSite("test");

        paramBean = (ParamBean) Jahia.getThreadParamBean();

        paramBean.getSession(true).setAttribute(ParamBean.SESSION_SITE, site);

        session = JCRSessionFactory.getInstance().getCurrentUserSession();
        this.site = (JCRSiteNode) session.getNode("/sites/" + site.getSiteKey());

        JCRNodeWrapper shared = session.getNode("/sites/" + site.getSiteKey() + "/contents");
        if (shared.hasNode("testContent")) {
            shared.getNode("testContent").remove();
        }
        node = shared.addNode("testContent", "jnt:contentFolder");
        node.addNode("testType", "jnt:contentList");
        node.addNode("testType2", "jnt:mainContent");
        node.addNode("testMixin", "jnt:bigText").addMixin("jmix:tagged");

        session.save();
    }

    @After
    public void tearDown() throws Exception {
        TestHelper.deleteSite("test");
        session.save();
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testModules() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        AbstractFilter outFilter = new AbstractFilter() {
            @Override
            public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setPriority(20);
        outFilter.setRenderService(RenderService.getInstance());

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node.getNode("testType"), "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        context.getRequest().setAttribute("script", RenderService.getInstance().resolveScript(resource, context));

        // test on a resource from the default Jahia module
        BaseAttributesFilter baseAttributesFilter = new BaseAttributesFilter();
        baseAttributesFilter.setPriority(0);
        baseAttributesFilter.setRenderService(RenderService.getInstance());

        TestFilter conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnModules("Default Jahia Templates");
        conditionalFilter.setPriority(10);

        RenderChain chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);

        String result = chain.doFilter(context, resource);

        assertTrue("TestFilter is not applied for resource from the 'Default Jahia Templates' module", result.contains(
                "TestFilter"));

        // test on a resource from the default Jahia module
        resource = new Resource(node.getNode("testType"), "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnModules("Jahia Test");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for resource that does not belong to the 'Jahia Test' module",
                !result.contains("TestFilter"));

        // test multiple modules condition
        resource = new Resource(node.getNode("testType"), "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnModules("Jahia Test, Default Jahia Templates");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for resource from the 'Default Jahia Templates' module", result.contains(
                "TestFilter"));

        // test NOT condition
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnModules("Jahia Test, Default Jahia Templates");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for resource from the 'Default Jahia Templates' module", !result.contains(
                "TestFilter"));

        // test NOT condition (inverted)
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnModules("Jahia Test, Jahia Rating");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for resource from the 'Default Jahia Templates' module", result.contains(
                "TestFilter"));
    }

    @Test
    public void testNodeTypes() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        AbstractFilter outFilter = new AbstractFilter() {
            @Override
            public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setPriority(20);
        outFilter.setRenderService(RenderService.getInstance());

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node.getNode("testType"), "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);

        // test on a node that has jnt:contentList type
        BaseAttributesFilter baseAttributesFilter = new BaseAttributesFilter();
        baseAttributesFilter.setRenderService(RenderService.getInstance());
        baseAttributesFilter.setPriority(0);

        TestFilter conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnNodeTypes("jnt:contentList");
        conditionalFilter.setPriority(10);

        RenderChain chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);

        String result = chain.doFilter(context, resource);

        assertTrue("TestFilter is not applied for node, having jnt:tag type", result.contains("TestFilter"));

        // test on a node that does not have jnt:contentList type
        resource = new Resource(node.getNode("testType2"), "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for node that does not have jnt:contentList type", !result.contains(
                "TestFilter"));

        // test multiple node types condition
        resource = new Resource(node.getNode("testType2"), "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnNodeTypes("jnt:page, jnt:mainContent");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for node, having jnt:mainContent type", result.contains("TestFilter"));

        // test mixin type jmix:tagged
        resource = new Resource(node.getNode("testMixin"), "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnNodeTypes("jmix:tagged");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for node, having jmix:tagged mixin type", result.contains("TestFilter"));

        // test NOT condition
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnNodeTypes("jmix:tagged, jnt:page");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for node, having jmix:tagged mixin type", !result.contains("TestFilter"));

        // test NOT condition (inverted)
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnNodeTypes("jmix:my");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for node, not having jmix:my mixin type", result.contains("TestFilter"));
    }

    @Test
    public void testTemplates() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        AbstractFilter outFilter = new AbstractFilter() {
            @Override
            public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setPriority(20);
        outFilter.setRenderService(RenderService.getInstance());

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node.getNode("testType"), "html", "mine", Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);

        // test on a resource with 'mine' template
        BaseAttributesFilter baseAttributesFilter = new BaseAttributesFilter();
        baseAttributesFilter.setRenderService(RenderService.getInstance());
        baseAttributesFilter.setPriority(0);

        TestFilter conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnTemplates("mine");
        conditionalFilter.setPriority(10);

        RenderChain chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);

        String result = chain.doFilter(context, resource);

        assertTrue("TestFilter is not applied for resource, having 'mine' template", result.contains("TestFilter"));

        // test on a resource with 'others' template
        resource = new Resource(node.getNode("testType"), "html", "others", Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for resource that does not have 'mine' template", !result.contains(
                "TestFilter"));

        // test multiple templates condition
        resource = new Resource(node.getNode("testType"), "html", "mine", Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnTemplates("others,mine");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for resource, having 'mine' template", result.contains("TestFilter"));

        // test NOT condition
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnTemplates("others,mine");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for resource, having 'mine' template", !result.contains("TestFilter"));

        // test NOT condition (inverted)
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnTemplates("unknown");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for resource, not having 'unknown' template", result.contains(
                "TestFilter"));
    }

    @Test
    public void testTemplateTypes() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        AbstractFilter outFilter = new AbstractFilter() {
            @Override
            public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setPriority(20);
        outFilter.setRenderService(RenderService.getInstance());

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node.getNode("testType"), "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);

        // test on a resource with 'html' template type
        BaseAttributesFilter baseAttributesFilter = new BaseAttributesFilter();
        baseAttributesFilter.setRenderService(RenderService.getInstance());
        baseAttributesFilter.setPriority(0);

        TestFilter conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnTemplateTypes("html");
        conditionalFilter.setPriority(10);

        RenderChain chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);

        String result = chain.doFilter(context, resource);

        assertTrue("TestFilter is not applied for resource, having 'html' template type", result.contains(
                "TestFilter"));

        // test on a resource with 'xml' template type
        resource = new Resource(node.getNode("testType"), "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnTemplateTypes("rss");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for resource that does not have 'rss' template", !result.contains(
                "TestFilter"));

        // test multiple template types condition
        resource = new Resource(node.getNode("testType"), "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setApplyOnTemplateTypes("xml,html");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for resource, having 'html' template type", result.contains(
                "TestFilter"));

        // test NOT condition
        resource = new Resource(node.getNode("testType"), "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnTemplateTypes("xml,html");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is applied for resource, having 'html' template type", !result.contains("TestFilter"));

        // test NOT condition (inverted)
        conditionalFilter = new TestFilter();
        conditionalFilter.setRenderService(RenderService.getInstance());
        conditionalFilter.setSkipOnTemplateTypes("csv,rss");
        conditionalFilter.setPriority(10);
        chain = new RenderChain(baseAttributesFilter, conditionalFilter, outFilter);
        result = chain.doFilter(context, resource);
        assertTrue("TestFilter is not applied for resource, not having 'rss' template type", result.contains(
                "TestFilter"));
    }

}