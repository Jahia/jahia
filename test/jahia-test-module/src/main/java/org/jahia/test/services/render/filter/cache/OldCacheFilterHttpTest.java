package org.jahia.test.services.render.filter.cache;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.test.services.render.filter.cache.base.CacheFilterHttpTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jkevan on 19/05/2016.
 */
public class OldCacheFilterHttpTest extends CacheFilterHttpTest {

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        CacheFilterHttpTest.oneTimeSetUp();

        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).setDisabled(true);
        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).setDisabled(true);
        ((AbstractFilter) SpringContextSingleton.getBean("oldTestCacheFilter")).setDisabled(false);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        CacheFilterHttpTest.oneTimeTearDown();

        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).setDisabled(false);
        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).setDisabled(false);
        ((AbstractFilter) SpringContextSingleton.getBean("oldTestCacheFilter")).setDisabled(true);
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
}
