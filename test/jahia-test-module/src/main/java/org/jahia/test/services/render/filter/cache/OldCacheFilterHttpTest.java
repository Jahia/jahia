/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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

import org.assertj.core.api.SoftAssertions;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.cache.AreaResourceCacheKeyPartGenerator;
import org.jahia.services.render.filter.cache.ModuleGeneratorQueue;
import org.jahia.test.services.render.filter.cache.base.CacheFilterHttpTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Old implementation of AggregateCacheFilter specific unit tests
 */
public class OldCacheFilterHttpTest extends CacheFilterHttpTest {

    protected static void switchCacheImplem() {
        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.cache.CacheFilter")).setDisabled(true);
        ((AbstractFilter) SpringContextSingleton.getBean("org.jahia.services.render.filter.AggregateFilter")).setDisabled(true);
        ((AbstractFilter) SpringContextSingleton.getBean("cacheFilter")).setDisabled(false);
        ((AreaResourceCacheKeyPartGenerator) SpringContextSingleton.getBean("areaResourceCacheKeyPartGenerator")).setDisabled(true);
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        CacheFilterHttpTest.oneTimeSetUp();
        switchCacheImplem();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        CacheFilterHttpTest.oneTimeTearDown();
    }

    @Test
    public void testModuleError() throws Exception {
        String s = getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error1");
        assertThat(s).contains("<!-- Module error :");
        getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error2");
        // All served from cache
        assertThat(getCheckFilter("CacheHttpTestRenderFilter1").getData("error2").getCount()).isEqualTo(1);
        Thread.sleep(5000);
        // Error should be flushed
        getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/error"), "root", "root1234", "error3");
        assertThat(getCheckFilter("CacheHttpTestRenderFilter1").getData("error3").getCount()).isEqualTo(2);
    }

    @Test
    public void testModuleWait() throws Exception {

        long previousModuleGenerationWaitTime = ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).getModuleGenerationWaitTime();

        try {

            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(1000);

            HttpThread t1 = new HttpThread(getUrl(SITECONTENT_ROOT_NODE + "/home/long", "testModuleWait1"), "root", "root1234", "testModuleWait1");
            t1.start();
            Thread.sleep(5000);

            String content2 = getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/long", "testModuleWait2"), "root", "root1234", "testModuleWait2");
            String content3 = getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/long", "testModuleWait3"), "root", "root1234", "testModuleWait3");

            t1.join();
            String content1 = t1.getResult();

            String content4 = getContent(getUrl(SITECONTENT_ROOT_NODE + "/home/long", "testModuleWait4"), "root", "root1234", "testModuleWait4");

            CacheFilterCheckFilter f1 = getCheckFilter("CacheHttpTestRenderFilter1");

            SoftAssertions softly = new SoftAssertions();

            softly.assertThat(content1).as("Request testModuleWait1 (length=%s, time=%s) should contain text", content1.length(), f1.getData("testModuleWait1").getTime()).contains(LONG_CREATED_ELEMENT_TEXT);
            softly.assertThat(content1).as("Request testModuleWait1 should contain page title").contains(LONG_PAGE_TITLE);
            softly.assertThat(f1.getData("testModuleWait1").getTime()).withFailMessage("First thread did not spend correct time").isGreaterThanOrEqualTo(15000);

            // Long module is left blank
            softly.assertThat(content2).as("Request testModuleWait2 (length=%s, time=%s) should not contain text", content1.length(), f1.getData("testModuleWait2").getTime()).doesNotContain(LONG_CREATED_ELEMENT_TEXT);
            softly.assertThat(content2).as("Request testModuleWait2 should contain page title").contains(LONG_PAGE_TITLE);
            softly.assertThat(f1.getData("testModuleWait2").getTime()).withFailMessage("Second thread did not spend correct time").isGreaterThanOrEqualTo(1000);

            // Entry is cached without the long module
            softly.assertThat(content3).as("Request testModuleWait3 (length=%s, time=%s) should not contain text", content1.length(), f1.getData("testModuleWait3").getTime()).doesNotContain(LONG_CREATED_ELEMENT_TEXT);
            softly.assertThat(content3).as("Request testModuleWait3 should contain page title").contains(LONG_PAGE_TITLE);
            softly.assertThat(f1.getData("testModuleWait3").getCount()).isEqualTo(1);

            // Entry is now cached with the long module
            softly.assertThat(content4).as("Request testModuleWait4 (length=%s, time=%s) should contain text", content1.length(), f1.getData("testModuleWait4").getTime()).contains(LONG_CREATED_ELEMENT_TEXT);
            softly.assertThat(content4).as("Request testModuleWait4 should contain page title").contains(LONG_PAGE_TITLE);
            softly.assertThat(f1.getData("testModuleWait4").getCount()).isEqualTo(1);

            softly.assertAll();
        } finally {
            ((ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue")).setModuleGenerationWaitTime(previousModuleGenerationWaitTime);
        }
    }

    @Test
    public void testMaxConcurrent() throws Exception {
        testMaxConcurrent(1000);
    }
}
