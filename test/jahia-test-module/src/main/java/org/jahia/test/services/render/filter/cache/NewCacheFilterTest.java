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
import org.jahia.services.render.filter.AggregateFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.cache.CacheKeyGenerator;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.services.render.filter.cache.ModuleGeneratorQueue;
import org.jahia.test.JahiaAdminUser;
import org.jahia.test.services.render.filter.cache.base.CacheFilterTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by jkevan on 25/05/2016.
 */
public class NewCacheFilterTest extends CacheFilterTest{
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(CacheFilterTest.class);

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        CacheFilterTest.oneTimeSetUp();

        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).setDisabled(false);
        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).setDisabled(false);
        ((AbstractFilter) SpringContextSingleton.getBean("oldTestCacheFilter")).setDisabled(true);
    }

    @Test
    public void testCacheFilter() throws Exception {
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        String[] result = cacheFilterRender();
        final Element element = moduleCacheProvider.getCache().get(result[2]);
        assertNotNull("Html Cache does not contains our html rendering", element);
        assertTrue("Content Cache and rendering are not equals",((String)((CacheEntry<?>)element.getValue()).getObject()).contains(result[0]));
    }

    @Test
    public void testDependencies() throws Exception {
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        cacheFilterRender();
        final Element element1 = moduleCacheProvider.getDependenciesCache().get("/sites/"+TESTSITE_NAME+"/home/testContent");
        assertNotNull("Node /shared should have dependencies",element1);
        assertTrue("Dependencies must not be empty",((Set<String>) element1.getValue()).size()>0);
    }

    @Test
    public void testMaxWait() throws Exception{
        long previousModuleGenerationWaitTime = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).getModuleGenerationWaitTime();

        try {
            // set generation wait to 1000 ms
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(1000);

            JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);

            // r1 will generate the fragment in 3000+ ms
            CacheRenderThread r1 = new CacheRenderThread(sessionWrapper, 3000);
            // r2 will try to get the fragment waiting for r1 to finish, r2 will wait 1000ms as configured and throw an error
            // because r1 is not quite fast
            CacheRenderThread r2 = new CacheRenderThread(sessionWrapper, null);


            r1.start();
            Thread.sleep(500);
            r2.start();

            r2.join();
            r1.join();

            assertNull(r1.error);
            assertNotNull(r1.result);
            assertNull(r2.result);
            assertNotNull(r2.error);
            assertTrue("Long thread don't spent the correct time to generate the fragment", r1.timer > 3000 && r1.timer < 4000);
            assertTrue("Waiting thread don't spent the correct time waiting before throw error", r2.timer > 1000 && r2.timer < 2000);
        } finally {
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(previousModuleGenerationWaitTime);
        }
    }



    public String[] cacheFilterRender() throws Exception {
        return cacheFilterRender(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH));
    }

    public String[] cacheFilterRender(JCRSessionWrapper sessionWrapper) throws Exception {
        return cacheFilterRender(sessionWrapper, null);
    }

    public static HttpServletRequest mockNewServletRequest() {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[] { HttpServletRequest.class },
                new InvocationHandler() {
                    Map<String, Object> attributes = new HashMap<String, Object>();

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("setAttribute")) {
                            attributes.put((String) args[0], args[1]);
                        } if (method.getName().equals("getAttribute")) {
                            return attributes.get(args[0]);
                        } if (method.getName().equals("getParameterMap")) {
                            return new HashMap<String, String[]>();
                        }
                        return null;
                    }
                });
    }

    public static HttpServletResponse mockNewServletRespons() {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class[]{HttpServletResponse.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return null;
                    }
                });
    }

    public static String[] cacheFilterRender(JCRSessionWrapper sessionWrapper, final Integer waitBeforeGenerate) throws Exception {
        RenderFilter outFilter = new AbstractFilter() {
            @Override
            public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "out";
            }

            @Override
            public String getDescription() {
                return "out filter";
            }
        };

        RenderFilter waitFilter = new AbstractFilter() {
            @Override
            public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                if(waitBeforeGenerate != null) {
                    logger.info("wait filter is waiting " + waitBeforeGenerate);
                    Thread.sleep(waitBeforeGenerate);
                    logger.info("wait is finished !!");
                }
                return previousOut;
            }

            @Override
            public String getDescription() {
                return "wait filter";
            }
        };

        outFilter.setRenderService(RenderService.getInstance());

        waitFilter.setRenderService(RenderService.getInstance());

        JCRNodeWrapper node = sessionWrapper.getNode("/sites/"+TESTSITE_NAME+"/home/testContent");
        RenderContext context = new RenderContext(mockNewServletRequest(), mockNewServletRespons(), JahiaAdminUser.getAdminUser(null));
        context.setSite(node.getResolveSite());
        context.setServletPath("/render");
        ChannelService channelService = (ChannelService) SpringContextSingleton.getInstance().getContext().getBean("ChannelService");
        context.setChannel(channelService.getChannel(Channel.GENERIC_CHANNEL));
        Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        context.setWorkspace(sessionWrapper.getWorkspace().getName());
        context.getRequest().setAttribute("script",
                RenderService.getInstance().resolveScript(resource, context));

        RenderFilter cacheFilter = (RenderFilter) SpringContextSingleton.getInstance().getContext().getBean("org.jahia.services.render.filter.cache.CacheFilter");

        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = moduleCacheProvider.getKeyGenerator();
        Properties properties = new Properties();
        properties.put("cache.requestParameters", "cacheinfo,moduleinfo");
        String key = generator.generate(resource, context, properties);
        String finalKey = generator.replacePlaceholdersInCacheKey(context, key);
        moduleCacheProvider.getCache().removeAll();

        RenderChain chain = new RenderChain(cacheFilter, waitFilter, outFilter);

        Map<String, Object> moduleMap = new HashMap<>();
        moduleMap.put(AggregateFilter.RENDERING_KEY, key);
        moduleMap.put(AggregateFilter.RENDERING_FINAL_KEY, finalKey);
        context.getRequest().setAttribute("moduleMap", moduleMap);

        String result = chain.doFilter(context, resource);

        return new String[]{result, key, finalKey};
    }

    public static class CacheRenderThread extends Thread {
        JCRSessionWrapper sessionWrapper;
        Integer wait;
        public String[] result;
        public Exception error;
        public long timer;

        public CacheRenderThread(JCRSessionWrapper sessionWrapper, Integer wait) {
            this.sessionWrapper = sessionWrapper;
            this.wait = wait;
        }

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            try {
                result = cacheFilterRender(sessionWrapper, wait);
                timer = System.currentTimeMillis() - time;
            } catch (Exception e) {
                error = e;
                logger.error(e.getMessage(), e);
                timer = System.currentTimeMillis() - time;
            }
        }
    }
}
