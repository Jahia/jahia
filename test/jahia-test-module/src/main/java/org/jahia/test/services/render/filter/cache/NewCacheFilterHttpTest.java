package org.jahia.test.services.render.filter.cache;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.test.services.render.filter.cache.base.CacheFilterHttpTest;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jkevan on 19/05/2016.
 */
public class NewCacheFilterHttpTest extends CacheFilterHttpTest {

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        CacheFilterHttpTest.oneTimeSetUp();

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
        // No request go after cache filter, everything is served by the cache
        assertNull(getCheckFilter("CacheHttpTestRenderFitler2").getData("error2"));
        Thread.sleep(5000);
        // Error should be flushed, and only this fragment should be regenerate
        getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error3");
        CacheFilterCheckFilter.RequestData data = getCheckFilter("CacheHttpTestRenderFitler2").getData("error3");
        assertEquals(1, data.getCount());
        assertEquals("/sites/cachetest/home/error/maincontent/simple-text.error.html", data.getRenderCalled().toArray()[0]);
    }
}
