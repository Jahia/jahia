/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.filter.cache;

import junit.framework.TestCase;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.BaseAttributesFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.TestHelper;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 12 janv. 2010
 */
public class CacheFilterTest extends TestCase {
    private transient static Logger logger = Logger.getLogger(CacheFilterTest.class);

    private static class TestFilter extends AbstractFilter {
        @Override
        protected String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
            return "TestFilter " + chain.doFilter(renderContext, resource);
        }
    }

    private JCRNodeWrapper node;
    private ParamBean paramBean;
    private JCRSessionWrapper session;
    private JahiaSite site;

    @Override
    protected void setUp() throws Exception {
        site = TestHelper.createSite("test");

        paramBean = (ParamBean) Jahia.getThreadParamBean();

        paramBean.getSession(true).setAttribute(ParamBean.SESSION_SITE, site);

        JahiaData jData = new JahiaData(paramBean, false);
        paramBean.setAttribute(JahiaData.JAHIA_DATA, jData);

        session = JCRSessionFactory.getInstance().getCurrentUserSession();

        JCRNodeWrapper shared = session.getNode("/shared");
        if (shared.hasNode("testContent")) {
            shared.getNode("testContent").remove();
        }
        if(shared.isVersioned()) session.checkout(shared);
        node = shared.addNode("testContent", "jnt:folder");
        node.addNode("testType", "jnt:tag");
        node.addNode("testType2", "jnt:user");
        node.addNode("testMixin", "jnt:content").addMixin("jmix:tagged");

        session.save();
    }

    @Override
    protected void tearDown() throws Exception {
        TestHelper.deleteSite("test");
        node.remove();
        session.save();
    }

    public void testCacheFilter() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        RenderFilter outFilter = new AbstractFilter() {
            @Override
            protected String execute(RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setRenderService(RenderService.getInstance());

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node.getNode("testType2"), "html", null, null);
        context.setMainResource(resource);
        context.getRequest().setAttribute("script",
                RenderService.getInstance().resolveScript(resource, context));

        // test on a resource from the default Jahia module
        BaseAttributesFilter attributesFilter = new BaseAttributesFilter();
        attributesFilter.setRenderService(RenderService.getInstance());

        CacheFilter cacheFilter = (CacheFilter) SpringContextSingleton.getInstance().getContext().getBean("cacheFilter");

        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = (CacheKeyGenerator) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheKeyGenerator");
        final String key = (String) generator.generate(resource, context);

        RenderChain chain = new RenderChain(attributesFilter, cacheFilter, outFilter);

        String result = chain.doFilter(context, resource);

        final Element element = moduleCacheProvider.getCache().get(key);
        assertNotNull("Html Cache does not contains our html rendering", element);
        assertTrue("Content Cache and rendering are not equals",result.equals(((CacheEntry)element.getValue()).getObject()));
    }

    public void testDependencies() throws Exception {
        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        RenderFilter outFilter = new AbstractFilter() {
            @Override
            protected String execute(RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setRenderService(RenderService.getInstance());

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node, "html", null, null);
        context.setMainResource(resource);
        context.getRequest().setAttribute("script",
                RenderService.getInstance().resolveScript(resource, context));

        // test on a resource from the default Jahia module
        BaseAttributesFilter attributesFilter = new BaseAttributesFilter();
        attributesFilter.setRenderService(RenderService.getInstance());

        CacheFilter cacheFilter = (CacheFilter) SpringContextSingleton.getInstance().getContext().getBean("cacheFilter");
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = (CacheKeyGenerator) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheKeyGenerator");
        final String key = (String) generator.generate(resource, context);

        RenderChain chain = new RenderChain(attributesFilter, cacheFilter, outFilter);

        String result = chain.doFilter(context, resource);

        final Element element = moduleCacheProvider.getCache().get(key);
        final Element element1 = moduleCacheProvider.getDependenciesCache().get(node.getPath());
        assertNotNull("Node /shared should have dependencies",element1);
        assertTrue("Dependencies must not be empty",((Set<String>) element1.getValue()).size()>0);
    }

    public void testEventListenerFlushingOfCache() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        RenderFilter outFilter = new AbstractFilter() {
            @Override
            protected String execute(RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }
        };
        outFilter.setRenderService(RenderService.getInstance());

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        final JCRNodeWrapper user = node.getNode("testType2");
        Resource resource = new Resource(user, "html", null, null);
        context.setMainResource(resource);
        context.getRequest().setAttribute("script",
                RenderService.getInstance().resolveScript(resource, context));

        // test on a resource from the default Jahia module
        BaseAttributesFilter attributesFilter = new BaseAttributesFilter();
        attributesFilter.setRenderService(RenderService.getInstance());

        CacheFilter cacheFilter = (CacheFilter) SpringContextSingleton.getInstance().getContext().getBean("cacheFilter");
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = (CacheKeyGenerator) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheKeyGenerator");
        final String key = (String) generator.generate(resource, context);

        RenderChain chain = new RenderChain(attributesFilter, cacheFilter, outFilter);

        String result = chain.doFilter(context, resource);

        final BlockingCache cache = moduleCacheProvider.getCache();
        Element element = cache.get(key);
        assertNotNull("Html Cache does not contains our html rendering", element);
        assertTrue("Content Cache and rendering are not equals",result.equals(((CacheEntry)element.getValue()).getObject()));

        user.setProperty("j:firstName","Test");
        
        session.save();
        assertFalse("After properties set; Html Cache should not contains our html rendering", cache.isKeyInCache(key));
        chain = new RenderChain(attributesFilter, cacheFilter, outFilter);
        result = chain.doFilter(context, resource);

         element = cache.get(key);
        assertNotNull("After properties set; Html Cache does not contains our html rendering", element);
        assertTrue("After properties set; Content Cache and rendering are not equals",result.equals(((CacheEntry)element.getValue()).getObject()));

        user.getProperty("j:firstName").setValue("Test Updated");

        session.save();
        assertFalse("After properties changed; Html Cache should not contains our html rendering",  cache.isKeyInCache(key));
        chain = new RenderChain(attributesFilter, cacheFilter, outFilter);
        result = chain.doFilter(context, resource);

        element = cache.get(key);
        assertNotNull("After properties changed; Html Cache does not contains our html rendering", element);
        assertTrue("After properties changed; Content Cache and rendering are not equals",result.equals(((CacheEntry)element.getValue()).getObject()));

        user.getProperty("j:firstName").remove();

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
    }
}
