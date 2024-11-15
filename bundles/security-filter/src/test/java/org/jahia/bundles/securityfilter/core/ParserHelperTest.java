/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.securityfilter.core;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class ParserHelperTest {

    /**
     * Test combination of origins (that can be provided by a referer) and requests
     */
    @Test
    public void isSameOriginFromHeaderTest() throws Exception {
        List<TestData> testsData = Arrays.asList(
                new TestData("http://host", "http://host", true),
                new TestData("http://host:8080", "http://host:8080", true),
                new TestData("https://host", "https://host", true),
                new TestData("https://host", "http://host", false),
                new TestData("http://host:8081", "http://host:8080", false),
                new TestData("http://host", "http://hostname", false),
                new TestData("http://hostname", "http://host", false)
                );
        for (TestData testData : testsData) {
            String origin = testData.origin;
            String request = testData.request;

            // Test origin header
            String header = "Origin";
            doTest(testData, header, origin, request);
            request = testData.request + "/";
            doTest(testData, header, origin, request);
            request = testData.request + "/host";
            doTest(testData, header, origin, request);
            request = testData.request + "/host/host";
            doTest(testData, header, origin, request);

            // Test Referer
            header = "Referer";
            origin = testData.origin;
            request = testData.request;
            doTest(testData, header, origin, request);
            origin = testData.origin + "/";
            request = testData.request;
            doTest(testData, header, origin, request);
            origin = testData.origin;
            request = testData.request + "/";
            doTest(testData, header, origin, request);
            origin = testData.origin + "/";
            request = testData.request + "/";
            doTest(testData, header, origin, request);
            origin = testData.origin + "/";
            request = testData.request + "/host";
            doTest(testData, header, origin, request);
            origin = testData.origin + "/host";
            request = testData.request + "/";
            doTest(testData, header, origin, request);
            origin = testData.origin + "/host";
            request = testData.request + "/host";
            doTest(testData, header, origin, request);
        }
    }

    private void doTest(TestData testData, String header, String origin, String request) throws URISyntaxException {
        Assert.assertEquals("Check origin " + origin + " on request " + request, testData.match, ParserHelper.isSameOriginFromHeader(getMockHttpServletRequest(request, header, testData.request), origin));
        Assert.assertEquals("Check hosted origin " + origin + " on request " + request, testData.match, ParserHelper.isSameOriginFromHeader(getMockHttpServletRequest(request, header, origin), "hosted"));
    }

    @NotNull
    private MockHttpServletRequest getMockHttpServletRequest(String request, String header, String headerValue) throws URISyntaxException {
        URI requestURI = new URI(request);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", request);
        if (requestURI.getScheme().equals("https")) {
            req.setServerPort(443);
        }
        if (requestURI.getPort() > 0) {
            req.setServerPort(requestURI.getPort());
        }
        req.setServerName(requestURI.getHost());
        req.setScheme(requestURI.getScheme());
        req.setProtocol(requestURI.getScheme());
        req.setContextPath(requestURI.getPath());
        req.addHeader(header, headerValue);
        return req;
    }

    static class TestData {
        String request;
        String origin;
        boolean match;
        public TestData(String request, String origin, boolean match) {
            this.request = request;
            this.origin = origin;
            this.match = match;
        }
    }

}
