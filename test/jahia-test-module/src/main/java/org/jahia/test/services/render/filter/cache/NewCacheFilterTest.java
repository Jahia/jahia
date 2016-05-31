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
    public void testLatch() throws Exception {
        testLatch("/sites/"+TESTSITE_NAME+"/home/testContent", false);
    }


    @Test
    public void testLatchOnError() throws Exception {
        testLatch("/sites/"+TESTSITE_NAME+"/home", true);
    }

    public void testLatch(String path, boolean onError) throws Exception {
        JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);

        // r1 will generate the fragment in 3000+ ms
        CacheRenderThread r1 = new CacheRenderThread(sessionWrapper, path, Resource.CONFIGURATION_MODULE, 3000, onError);
        // r2 will try to get the fragment waiting for r1 to finish, r2 will wait 1000ms as configured and throw an error
        // because r1 is not quite fast
        CacheRenderThread r2 = new CacheRenderThread(sessionWrapper, path, Resource.CONFIGURATION_MODULE, null, false);

        r1.start();
        Thread.sleep(500);
        r2.start();

        r2.join();
        r1.join();


        assertNull(r1.error);
        assertNull(r2.error);
        assertNotNull(r2.result);
        assertNotNull(r1.result);
        if (onError) {
            assertTrue(r1.result[0].contains("<!-- Module error : Error filter triggered in render chain-->"));
            // error is cached so r2 should also see an error when r1 release the latch
            assertTrue(r2.result[0].contains("<!-- Module error : Error filter triggered in render chain-->"));
        } else {
            assertTrue(r1.result[0].contains("render for:/sites/test/home/testContent"));
            assertTrue(r2.result[0].contains("render for:/sites/test/home/testContent"));
        }

        // r1 is the long thread, it take 3000ms to generate the fragment
        assertTrue("Long thread don't spent the correct time to generate the fragment", r1.timer > 3000 && r1.timer < 4000);
        // waiting thread r2 wait that r1 release the latch, r2 is started 500ms after r1 so the fragment should be resolve in 2500ms approximately
        assertTrue("Waiting thread don't spent the correct time to get the fragment", r2.timer > 2300 && r2.timer < 3000);


        // new r3 thread alone with no wait
        CacheRenderThread r3 = new CacheRenderThread(sessionWrapper, path, Resource.CONFIGURATION_MODULE, null, false);
        r3.start();
        r3.join();

        assertNull(r3.error);
        assertNotNull(r3.result);
        assertTrue(r3.result[0].contains("render for:/sites/test/home"));

        // free thread r3 should really speed, 1ms approximately
        assertTrue("Free thread spent too much time generating the fragment: " + r3.timer, r3.timer >=  0 && r3.timer < 100);
    }

    // TODO: test not cacheable fragments
    // TODO: test not cacheable fragments + latch (not cacheable fragment shouldn't aquier latchs)

    @Test
    public void testMaxWait() throws Exception{
        long previousModuleGenerationWaitTime = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).getModuleGenerationWaitTime();

        try {
            // set generation wait to 1000 ms
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(1000);

            JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);

            // r1 will generate the fragment in 3000+ ms
            CacheRenderThread r1 = new CacheRenderThread(sessionWrapper, "/sites/"+TESTSITE_NAME+"/home/testContent", Resource.CONFIGURATION_PAGE, 3000, false);
            // r2 will try to get the fragment waiting for r1 to finish, r2 will wait 1000ms as configured and throw an error
            // because r1 is not quite fast
            CacheRenderThread r2 = new CacheRenderThread(sessionWrapper, "/sites/"+TESTSITE_NAME+"/home/testContent", Resource.CONFIGURATION_PAGE, null, false);

            r1.start();
            Thread.sleep(500);
            r2.start();

            r2.join();
            r1.join();

            assertNull(r1.error);
            assertNotNull(r1.result);
            assertNull(r2.result);
            assertTrue(r2.error != null && r2.error.getMessage().contains("Module generation takes too long due to module not generated fast enough (1000 ms)"));
            assertTrue("Long thread don't spent the correct time to generate the fragment", r1.timer > 3000 && r1.timer < 4000);
            assertTrue("Waiting thread don't spent the correct time waiting before throw error", r2.timer > 1000 && r2.timer < 2000);
        } finally {
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(previousModuleGenerationWaitTime);
        }
    }

    @Test
    public void testMaxConcurrent() throws Exception{
        long previousModuleGenerationWaitTime = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).getModuleGenerationWaitTime();
        int previousModuleGenerateInParallel = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).getMaxModulesToGenerateInParallel();

        try {
            // set generation wait to 1000 ms
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(1000);
            // set number of fragment generate in parallel to 1 for the test
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setMaxModulesToGenerateInParallel(1);

            JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);

            // r1 will generate the fragment in 3000+ ms
            CacheRenderThread r1 = new CacheRenderThread(sessionWrapper, "/sites/"+TESTSITE_NAME+"/home/testContent", Resource.CONFIGURATION_PAGE, 3000, false);
            // t2 will try to generate an other fragment in parallel, but should do an error
            // because r1 is not quite fast and is already generating a fragment
            CacheRenderThread r2 = new CacheRenderThread(sessionWrapper, "/sites/"+TESTSITE_NAME+"/home", Resource.CONFIGURATION_PAGE, null, false);


            r1.start();
            Thread.sleep(500);
            r2.start();

            r2.join();
            r1.join();

            assertNull(r1.error);
            assertNotNull(r1.result);
            assertNull(r2.result);
            assertTrue(r2.error != null && r2.error.getMessage().contains("Module generation takes too long due to maximum parallel processing reached (1)"));
            assertTrue("Long thread don't spent the correct time to generate the fragment", r1.timer > 3000 && r1.timer < 4000);
            assertTrue("Waiting thread don't spent the correct time waiting before throw error", r2.timer > 1000 && r2.timer < 2000);
        } finally {
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(previousModuleGenerationWaitTime);
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setMaxModulesToGenerateInParallel(previousModuleGenerateInParallel);
        }
    }

    @Test
    public void testRenderError() throws Exception{
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);

        // test module error
        Exception exception = null;
        String[] result = null;
        try {
            result = cacheFilterRender(sessionWrapper, "/sites/"+TESTSITE_NAME+"/home/testContent", Resource.CONFIGURATION_MODULE, null, true);
        } catch (Exception e) {
            exception = e;
        }
        //error should be cache
        final Element element = moduleCacheProvider.getCache().get(result[2]);

        assertNull(exception);
        assertTrue("<!-- Module error : Error filter triggered in render chain-->".equals(result[0]));
        assertNotNull("Html Cache does not contains our error rendering", element);
        assertTrue("Error Cache and rendering are not equals",((String)((CacheEntry<?>)element.getValue()).getObject()).contains(result[0]));

        // test page error
        exception = null;
        result = null;
        try {
            result = cacheFilterRender(sessionWrapper, "/sites/"+TESTSITE_NAME+"/home/testContent", Resource.CONFIGURATION_PAGE, null, true);
        } catch (Exception e) {
            exception = e;
        }
        assertNull(result);
        assertTrue(exception != null && exception.getMessage().contains("Error filter triggered in render chain"));
    }

    public String[] cacheFilterRender() throws Exception {
        return cacheFilterRender(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH),
                    "/sites/"+TESTSITE_NAME+"/home/testContent", Resource.CONFIGURATION_PAGE, null, false);
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

    public static HttpServletResponse mockNewServletResponse() {
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

    public static String[] cacheFilterRender(JCRSessionWrapper sessionWrapper, final String nodePath, String resourceConfig, final Integer waitBeforeGenerate,
                                              final boolean renderError) throws Exception {
        RenderFilter outFilter = new AbstractFilter() {
            @Override
            public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
                return "render for:" + nodePath;
            }

            @Override
            public String getDescription() {
                return "out filter";
            }

            @Override
            public float getPriority() {
                return 19;
            }
        };

        RenderFilter waitFilter = new AbstractFilter() {
            @Override
            public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
                if(waitBeforeGenerate != null) {
                    logger.info("wait filter is waiting " + waitBeforeGenerate);
                    Thread.sleep(waitBeforeGenerate);
                    logger.info("wait is finished !!");
                }
                return null;
            }

            @Override
            public String getDescription() {
                return "wait filter";
            }

            @Override
            public float getPriority() {
                return 17;
            }
        };

        RenderFilter errorFilter = new AbstractFilter() {
            class ErrorFilterException extends Exception {
                public ErrorFilterException(String message) {
                    super(message);
                }
            }

            @Override
            public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
                if (renderError) {
                    throw new ErrorFilterException("Error filter triggered in render chain");
                }
                return null;
            }

            @Override
            public String getDescription() {
                return "error filter";
            }

            @Override
            public float getPriority() {
                return 18;
            }
        };

        outFilter.setRenderService(RenderService.getInstance());
        waitFilter.setRenderService(RenderService.getInstance());
        errorFilter.setRenderService(RenderService.getInstance());

        JCRNodeWrapper node = sessionWrapper.getNode(nodePath);
        RenderContext context = new RenderContext(mockNewServletRequest(), mockNewServletResponse(), JahiaAdminUser.getAdminUser(null));
        context.setSite(node.getResolveSite());
        context.setServletPath("/render");
        ChannelService channelService = (ChannelService) SpringContextSingleton.getInstance().getContext().getBean("ChannelService");
        context.setChannel(channelService.getChannel(Channel.GENERIC_CHANNEL));
        Resource resource = new Resource(node, "html", null, resourceConfig);
        context.setMainResource(resource);
        context.setWorkspace(sessionWrapper.getWorkspace().getName());
        context.getRequest().setAttribute("script",
                RenderService.getInstance().resolveScript(resource, context));

        RenderFilter cacheFilter = (RenderFilter) SpringContextSingleton.getInstance().getContext().getBean("org.jahia.services.render.filter.cache.CacheFilter");

        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = moduleCacheProvider.getKeyGenerator();
        Properties properties = new Properties();
        properties.put("cache.requestParameters", "ec,v,cacheinfo,moduleinfo");
        String key = generator.generate(resource, context, properties);
        String finalKey = generator.replacePlaceholdersInCacheKey(context, key);
        moduleCacheProvider.getCache().removeAll();

        RenderChain chain = new RenderChain(cacheFilter, waitFilter, errorFilter, outFilter);

        // init module map with keys, coming from AggregateFilter in normal behavior
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
        boolean renderError;
        String nodePath;
        String resourceConfig;
        public String[] result;
        public Exception error;
        public long timer;

        public CacheRenderThread(JCRSessionWrapper sessionWrapper, String nodePath, String resourceConfig, Integer wait, boolean renderError) {
            this.sessionWrapper = sessionWrapper;
            this.resourceConfig = resourceConfig;
            this.wait = wait;
            this.renderError = renderError;
            this.nodePath = nodePath;
        }

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            try {
                result = cacheFilterRender(sessionWrapper, nodePath, resourceConfig, wait, renderError);
                timer = System.currentTimeMillis() - time;
            } catch (Exception e) {
                error = e;
                logger.error(e.getMessage(), e);
                timer = System.currentTimeMillis() - time;
            }
        }
    }
}
