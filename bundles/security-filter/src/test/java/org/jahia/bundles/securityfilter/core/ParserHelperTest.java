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