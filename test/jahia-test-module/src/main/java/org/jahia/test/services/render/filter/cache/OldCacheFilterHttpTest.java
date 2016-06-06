package org.jahia.test.services.render.filter.cache;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.cache.ModuleGeneratorQueue;
import org.jahia.test.services.render.filter.cache.base.CacheFilterHttpTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * Created by jkevan on 19/05/2016.
 */
public class OldCacheFilterHttpTest extends CacheFilterHttpTest {

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        CacheFilterHttpTest.oneTimeSetUp();

        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).setDisabled(true);
        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).setDisabled(true);
        ((AbstractFilter) SpringContextSingleton.getBean("cacheFilter")).setDisabled(false);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        CacheFilterHttpTest.oneTimeTearDown();

        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).setDisabled(false);
        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).setDisabled(false);
        ((AbstractFilter) SpringContextSingleton.getBean("cacheFilter")).setDisabled(true);
    }


    @Test
    public void testModuleError() throws Exception {
        String s = getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error1");
        assertTrue(s.contains("<!-- Module error :"));
        getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error2");
        // All served from cache
        assertEquals(1, getCheckFilter("CacheHttpTestRenderFitler1").getData("error2").getCount());
        Thread.sleep(5000);
        // Error should be flushed
        getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error3");
        assertEquals(2, getCheckFilter("CacheHttpTestRenderFitler1").getData("error3").getCount());
    }

    @Test
    public void testModuleWait() throws Exception {

        long previousModuleGenerationWaitTime = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).getModuleGenerationWaitTime();

        try {

            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(1000);
            URL url = getUrl(SITECONTENT_ROOT_NODE + "/home/long");
            HttpThread t1 = new HttpThread(url, "root", "root1234", "testModuleWait1");
            t1.start();
            Thread.sleep(200);

            HttpThread t2 = new HttpThread(url, "root", "root1234", "testModuleWait2");
            t2.start();
            t2.join();

            String content = getContent(url, "root", "root1234", "testModuleWait3");

            t1.join();

            String content1 = getContent(url, "root", "root1234", "testModuleWait4");

            // Long module is left blank
            assertFalse(t2.getResult().contains("Very long to appear"));
            assertTrue(t2.getResult().contains("<h2 class=\"pageTitle\">long</h2>"));
            assertTrue("Second thread did not spend correct time", getCheckFilter("CacheHttpTestRenderFitler1").getData("testModuleWait2").getTime() > 1000 && getCheckFilter("CacheHttpTestRenderFitler1").getData("testModuleWait2").getTime() < 5900);

            // Entry is cached without the long module
            assertFalse(content.contains("Very long to appear"));
            assertTrue(content.contains("<h2 class=\"pageTitle\">long</h2>"));
            assertEquals(1, getCheckFilter("CacheHttpTestRenderFitler1").getData("testModuleWait3").getCount());

            assertTrue(t1.getResult().contains("Very long to appear"));
            assertTrue(t1.getResult().contains("<h2 class=\"pageTitle\">long</h2>"));
            assertTrue("First thread did not spend correct time", getCheckFilter("CacheHttpTestRenderFitler1").getData("testModuleWait1").getTime() > 6000 && getCheckFilter("CacheHttpTestRenderFitler1").getData("testModuleWait1").getTime() < 10000);

            // Entry is now cached with the long module
            assertTrue(content1.contains("Very long to appear"));
            assertTrue(content1.contains("<h2 class=\"pageTitle\">long</h2>"));
            assertEquals(1, getCheckFilter("CacheHttpTestRenderFitler1").getData("testModuleWait4").getCount());
        } finally {
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(previousModuleGenerationWaitTime);
        }
    }

    @Test
    public void testMaxConcurrent() throws Exception {
        testMaxConcurrent(1000);
    }
}
