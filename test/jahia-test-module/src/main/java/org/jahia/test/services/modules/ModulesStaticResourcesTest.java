/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.test.services.modules;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.http.HttpHeaders;
import org.jahia.bin.Jahia;
import org.jahia.test.JahiaTestCase;
import org.junit.Test;
import org.slf4j.Logger;

import static org.junit.Assert.assertTrue;

/**
 * @author Jerome Blanchard
 */
public class ModulesStaticResourcesTest extends JahiaTestCase {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ModulesStaticResourcesTest.class);
    private static final String RESOURCE_NAME = "test.txt";

    @Test
    public void testETagHeaderInModuleStaticResource() {
        String url = getBaseServerURL() + Jahia.getContextPath() + "/modules/jahia-test-module/css/" + RESOURCE_NAME;
        try (CloseableHttpResponse resp = getHttpClient().execute(new HttpGet(url))) {
            assertTrue(resp.containsHeader(HttpHeaders.ETAG));
        } catch (Exception e) {
            LOGGER.error("Error while getting resource", e);
        }
    }
}
