/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.jahia.test.JahiaTestCase;
import org.jahia.test.services.render.filter.cache.base.CacheFilterHttpTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

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
    public static void oneTimeSetUp() {
        CacheFilterHttpTest.oneTimeSetUp();
        switchCacheImplem();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        CacheFilterHttpTest.oneTimeTearDown();
    }

    @Test
    public void testModuleError() throws IOException {
        String s = getContent(getUrl(ERROR_PAGE_PATH), JahiaTestCase.getRootUserCredentials(), "error1");
        assertThat(s).contains("<!-- Module error :");
        getContent(getUrl(ERROR_PAGE_PATH), JahiaTestCase.getRootUserCredentials(), "error2");
        // All served from cache
        assertThat(getCheckFilter(CACHE_RENDER_FILTER_1).getData("error2").getCount()).isEqualTo(1);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Error should be flushed
        getContent(getUrl(ERROR_PAGE_PATH), JahiaTestCase.getRootUserCredentials(), "error3");
        assertThat(getCheckFilter(CACHE_RENDER_FILTER_1).getData("error3").getCount()).isEqualTo(2);
    }

    @Test
    public void testModuleWait() throws IOException {
        ModuleGeneratorQueue moduleGeneratorQueue = (ModuleGeneratorQueue) SpringContextSingleton.getBean("moduleGeneratorQueue");
        long previousModuleGenerationWaitTime = moduleGeneratorQueue.getModuleGenerationWaitTime();

        try {

            moduleGeneratorQueue.setModuleGenerationWaitTime(1000);
            int counter = 1;
            HttpThread t1 = new HttpThread(getUrl(LONG_PAGE_PATH, REQUEST_ID_PREFIX + counter), JahiaTestCase.getRootUserCredentials(), REQUEST_ID_PREFIX + counter++);
            t1.start();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            String content2 = getContent(getUrl(LONG_PAGE_PATH, REQUEST_ID_PREFIX + counter), JahiaTestCase.getRootUserCredentials(), REQUEST_ID_PREFIX + counter++);
            String content3 = getContent(getUrl(LONG_PAGE_PATH, REQUEST_ID_PREFIX + counter), JahiaTestCase.getRootUserCredentials(), REQUEST_ID_PREFIX + counter++);
            try {
                t1.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            String content1 = t1.getResult();

            String content4 = getContent(getUrl(LONG_PAGE_PATH, REQUEST_ID_PREFIX + counter), JahiaTestCase.getRootUserCredentials(), REQUEST_ID_PREFIX + counter++);

            CacheFilterCheckFilter f1 = getCheckFilter(CACHE_RENDER_FILTER_1);

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
            moduleGeneratorQueue.setModuleGenerationWaitTime(previousModuleGenerationWaitTime);
        }
    }

    @Test
    public void testMaxConcurrent() throws IOException {
        testMaxConcurrent(1000);
    }
}
