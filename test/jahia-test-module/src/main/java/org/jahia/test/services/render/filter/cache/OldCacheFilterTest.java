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
 * Created by jkevan on 25/05/2016.
 */
public class OldCacheFilterTest extends CacheFilterTest {
    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        CacheFilterTest.oneTimeSetUp();

        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).setDisabled(true);
        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).setDisabled(true);
        ((AbstractFilter) SpringContextSingleton.getBean("cacheFilter")).setDisabled(false);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        CacheFilterTest.oneTimeTearDown();

        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).setDisabled(false);
        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).setDisabled(false);
        ((AbstractFilter) SpringContextSingleton.getBean("cacheFilter")).setDisabled(true);
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
