/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.test.services.render.filter.cache;

import net.sf.ehcache.Element;
import org.jahia.api.Constants;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
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
import org.jahia.services.render.filter.cache.CacheKeyGenerator;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.JahiaAdminUser;
import org.jahia.test.services.render.filter.cache.base.CacheFilterTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Old implementation of AggregateCacheFilter specific unit tests
 */
public class OldCacheFilterTest extends CacheFilterTest {

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        CacheFilterTest.oneTimeSetUp();
        OldCacheFilterHttpTest.switchCacheImplem();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        CacheFilterTest.oneTimeTearDown();
    }

    @Test
    public void testCacheFilter() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(null);

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
        RenderContext context = new RenderContext(getRequest(), getResponse(), admin);
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
        attributesFilter.setConfigurationToSkipInResourceRenderedPath(new HashSet<String>(Arrays.asList("include","wrapper")));

        RenderFilter cacheFilter = (RenderFilter) SpringContextSingleton.getBean("cacheFilter");

        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = moduleCacheProvider.getKeyGenerator();
        Properties properties = new Properties();
        properties.put("cache.requestParameters", "cacheinfo,moduleinfo");
        String key = generator.generate(resource, context, properties);
        moduleCacheProvider.getCache().removeAll();

        RenderChain chain = new RenderChain(attributesFilter, cacheFilter, outFilter);

        String result = chain.doFilter(context, resource);

        final Element element = moduleCacheProvider.getCache().get(generator.replacePlaceholdersInCacheKey(context, key));
        assertNotNull("Html Cache does not contains our html rendering", element);
        assertTrue("Content Cache and rendering are not equals",((String)((CacheEntry<?>)element.getValue()).getObject()).contains(result));
    }

    @Test
    public void testDependencies() throws Exception {
        JahiaUser admin = JahiaAdminUser.getAdminUser(null);

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
        RenderContext context = new RenderContext(getRequest(), getResponse(), admin);
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
        attributesFilter.setConfigurationToSkipInResourceRenderedPath(new HashSet<String>(Arrays.asList("include","wrapper")));

        RenderFilter cacheFilter = (RenderFilter) SpringContextSingleton.getBean("cacheFilter");
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");

        moduleCacheProvider.getCache().removeAll();

        RenderChain chain = new RenderChain(attributesFilter, cacheFilter, outFilter);

        chain.doFilter(context, resource);

        final Element element1 = moduleCacheProvider.getDependenciesCache().get(node.getPath());
        assertNotNull("Node /shared should have dependencies",element1);
        assertTrue("Dependencies must not be empty",((Set<String>) element1.getValue()).size()>0);
    }
}
