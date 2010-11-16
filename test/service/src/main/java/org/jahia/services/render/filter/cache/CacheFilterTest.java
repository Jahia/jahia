/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter.cache;

import junit.framework.TestCase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.BaseAttributesFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.JahiaAdminUser;
import org.jahia.test.TestHelper;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 12 janv. 2010
 */
public class CacheFilterTest extends TestCase {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(CacheFilterTest.class);

    private static class TestFilter extends AbstractFilter {
        @Override
        public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                throws Exception {
            return "TestFilter " + chain.doFilter(renderContext, resource);
        }
    }

    private JCRNodeWrapper node;
    private ParamBean paramBean;
    private JCRSessionWrapper session;
    protected JCRSiteNode site;

    @Override
    protected void setUp() throws Exception {
        try {
            JahiaSite site = TestHelper.createSite("test");
            paramBean = (ParamBean) Jahia.getThreadParamBean();

            paramBean.getSession(true).setAttribute(ParamBean.SESSION_SITE, site);

            /*
            JahiaData jData = new JahiaData(paramBean, false);
            paramBean.setAttribute(JahiaData.JAHIA_DATA, jData);
            */

            session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
            this.site = (JCRSiteNode) session.getNode("/sites/"+site.getSiteKey());

            JCRNodeWrapper shared = this.site.getNode("home");
            if (shared.hasNode("testContent")) {
                shared.getNode("testContent").remove();
            }
            if(shared.isVersioned()) session.checkout(shared);
            node = shared.addNode("testContent", "jnt:page");
            node.setProperty("jcr:title", "English test page");
            node.addNode("testType2", "jnt:mainContent");
            session.save();
            final JCRPublicationService service = JCRPublicationService.getInstance();
            final List<PublicationInfo> infoList = service.getPublicationInfo(
                    shared.getIdentifier(), new LinkedHashSet<String>(Arrays.asList(Locale.ENGLISH.toString())),
                    true, true, true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
            service.publish(infoList, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,Collections.<String>emptyList());
            session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);
            node = session.getNode("/sites/"+site.getSiteKey()+"/home/testContent");
        } catch (Exception e) {
            logger.warn("Exception during test setUp", e);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
            TestHelper.deleteSite("test");
            session.save();
        } catch (Exception e) {
            logger.warn("Exception during test tearDown", e);
        }
    }

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

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        context.setLiveMode(true);
        Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        context.getRequest().setAttribute("script",
                RenderService.getInstance().resolveScript(resource, context));

        // test on a resource from the default Jahia module
        BaseAttributesFilter attributesFilter = new BaseAttributesFilter();
        attributesFilter.setRenderService(RenderService.getInstance());

        RenderFilter cacheFilter = (RenderFilter) SpringContextSingleton.getInstance().getContext().getBean("cacheFilter");

        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = moduleCacheProvider.getKeyGenerator();
        final String key = (String) generator.generate(resource, context);

        RenderChain chain = new RenderChain(attributesFilter, cacheFilter, outFilter);

        String result = chain.doFilter(context, resource);

        final Element element = moduleCacheProvider.getCache().get(key);
        assertNotNull("Html Cache does not contains our html rendering", element);
        assertTrue("Content Cache and rendering are not equals",((String)((CacheEntry)element.getValue()).getObject()).contains(result));
    }

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

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        context.setLiveMode(true);
        Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        context.getRequest().setAttribute("script",
                RenderService.getInstance().resolveScript(resource, context));

        // test on a resource from the default Jahia module
        BaseAttributesFilter attributesFilter = new BaseAttributesFilter();
        attributesFilter.setRenderService(RenderService.getInstance());

        RenderFilter cacheFilter = (RenderFilter) SpringContextSingleton.getInstance().getContext().getBean("cacheFilter");
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = moduleCacheProvider.getKeyGenerator();
        final String key = (String) generator.generate(resource, context);

        RenderChain chain = new RenderChain(attributesFilter, cacheFilter, outFilter);

        String result = chain.doFilter(context, resource);

        final Element element = moduleCacheProvider.getCache().get(key);
        final Element element1 = moduleCacheProvider.getDependenciesCache().get(node.getPath());
        assertNotNull("Node /shared should have dependencies",element1);
        assertTrue("Dependencies must not be empty",((Set<String>) element1.getValue()).size()>0);
    }
/*
    public void testEventListenerFlushingOfCache() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        RenderFilter outFilter = new AbstractFilter() {
            @Override
            public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setRenderService(RenderService.getInstance());

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        context.setLiveMode(true);
        final JCRNodeWrapper user = node.getNode("testType2");
        Resource resource = new Resource(user, "html", "default", Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        context.getRequest().setAttribute("script",
                RenderService.getInstance().resolveScript(resource, context));

        // test on a resource from the default Jahia module
        BaseAttributesFilter attributesFilter = new BaseAttributesFilter();
        attributesFilter.setRenderService(RenderService.getInstance());

        RenderFilter cacheFilter = (RenderFilter) SpringContextSingleton.getInstance().getContext().getBean("cacheFilter");
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = moduleCacheProvider.getKeyGenerator();
        final String key = (String) generator.generate(resource, context);

        RenderChain chain = new RenderChain(attributesFilter, cacheFilter, outFilter);

        String result = chain.doFilter(context, resource);

        final Cache cache = moduleCacheProvider.getCache();
        Element element = cache.get(key);
        assertNotNull("Html Cache does not contains our html rendering", element);
        assertTrue("Content Cache and rendering are not equals",result.equals(((CacheEntry)element.getValue()).getObject()));

        user.setProperty("j:body","Test");

        session.save();
        assertFalse("After properties set; Html Cache should not contains our html rendering", cache.isKeyInCache(key));
        chain = new RenderChain(attributesFilter, cacheFilter, outFilter);
        result = chain.doFilter(context, resource);

         element = cache.get(key);
        assertNotNull("After properties set; Html Cache does not contains our html rendering", element);
        assertTrue("After properties set; Content Cache and rendering are not equals",result.equals(((CacheEntry)element.getValue()).getObject()));

        user.getProperty("j:body").setValue("Test Updated");

        session.save();
        assertFalse("After properties changed; Html Cache should not contains our html rendering",  cache.isKeyInCache(key));
        chain = new RenderChain(attributesFilter, cacheFilter, outFilter);
        result = chain.doFilter(context, resource);

        element = cache.get(key);
        assertNotNull("After properties changed; Html Cache does not contains our html rendering", element);
        assertTrue("After properties changed; Content Cache and rendering are not equals",result.equals(((CacheEntry)element.getValue()).getObject()));

        user.getProperty("j:body").remove();

        session.save();
        assertFalse("After properties removal; Html Cache should not contains our html rendering",  cache.isKeyInCache(key));
        chain = new RenderChain(attributesFilter, cacheFilter, outFilter);
        result = chain.doFilter(context, resource);

         element = cache.get(key);
        assertNotNull("After properties removal; Html Cache does not contains our html rendering", element);
        assertTrue("After properties removal; Content Cache and rendering are not equals",result.equals(((CacheEntry)element.getValue()).getObject()));

        user.remove();

        session.save();
        assertFalse("After node removal; Html Cache should not contains our html rendering",  cache.isKeyInCache(key));
    }*/
}
