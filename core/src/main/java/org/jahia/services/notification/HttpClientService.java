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
package org.jahia.services.notification;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.Configurable;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.cookie.IgnoreCookieSpecFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.taglibs.standard.tag.common.core.ImportSupport;
import org.apache.taglibs.standard.util.UrlUtil;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.StringResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * Utility class for HTTP communication.<br>
 * Parts of it are based on the code of the JSTL's &lt;c:import&gt; tag, for
 * reading the content of the provided URL.
 *
 * @author Sergiy Shyrkov
 */
public class HttpClientService implements ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientService.class);
    private static final String URL_NOT_PROVIDED = "Provided URL is null";
    private PoolingHttpClientConnectionManager connManager;

    public void init() {
        connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(20);
        connManager.setDefaultMaxPerRoute(2);

        // bypass proxy client
        httpClients.put(null, getNewHttpClientBuilder().build());

        // HTTPS proxy client
        if (StringUtils.isNotEmpty(System.getProperty("https.proxyHost"))) {
            CloseableHttpClient client = initHttpClient(getNewHttpClientBuilder(), "https");
            fallbackHttpClient = client;
        }

        // HTTP proxy client
        if (StringUtils.isNotEmpty(System.getProperty("http.proxyHost"))) {
            CloseableHttpClient client = initHttpClient(getNewHttpClientBuilder(), "http");
            if (fallbackHttpClient == null) {
                fallbackHttpClient = client;
            }
        }
    }

    private CloseableHttpClient initHttpClient(HttpClientBuilder builder, String protocol) {
        String host = System.getProperty(protocol + ".proxyHost");
        int port = Integer.getInteger(protocol + ".proxyPort", -1);

        HttpHost proxy = new HttpHost(protocol, host, port);
        builder.setProxy(proxy);

        String key = host + ':' + port;

        BasicCredentialsProvider credsProvider = null;
        String user = System.getProperty(protocol + ".proxyUser");
        if (StringUtils.isNotEmpty(user)) {
            credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(user, System.getProperty(protocol + ".proxyPassword").toCharArray()));
            builder.setDefaultCredentialsProvider(credsProvider);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Initialized HttpClient for {} protocol using proxy {} {} credentials", protocol.toUpperCase(), key,
                    credsProvider != null ? "with" : "without");
        }
        CloseableHttpClient client = builder.build();
        httpClients.put(key, client);
        return client;
    }

    private HttpClientBuilder getNewHttpClientBuilder() {
        HttpClientBuilder builder = HttpClients.custom();
        builder.setDefaultCookieSpecRegistry(name -> new IgnoreCookieSpecFactory());

        builder.setConnectionManager(connManager);
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(15000L, TimeUnit.MILLISECONDS)
                .setResponseTimeout(60000L, TimeUnit.MILLISECONDS)
                .build();
        builder.setDefaultRequestConfig(config);

        return builder;
    }

    /**
     * Returns <tt>true</tt> if our current URL is absolute, <tt>false</tt>
     * otherwise.
     *
     * @see ImportSupport#isAbsoluteUrl(String)
     */
    public static boolean isAbsoluteUrl(String url) {
        return UrlUtil.isAbsoluteUrl(url);
    }

    private CloseableHttpClient fallbackHttpClient;

    private Map<String, CloseableHttpClient> httpClients = new HashMap<>(3);

    private ServletContext servletContext;

    /**
     * Executes a request with GET method to the specified URL and reads the response content as a string.
     *
     * @param url a URL to connect to
     * @return the string representation of the URL connection response
     * @throws {@link IllegalArgumentException} in case of a malformed URL
     */
    public String executeGet(String url) throws IllegalArgumentException {
        return executeGet(url, null);
    }

    /**
     * Executes a request with GET method to the specified URL and reads the response content as a string.
     *
     * @param url a URL to connect to
     * @param headers request headers to be set for connection; <code>null</code> if no additional headers needs to be set
     * @return the string representation of the URL connection response
     * @throws {@link IllegalArgumentException} in case of a malformed URL
     */
    public String executeGet(String url, Map<String, String> headers) throws IllegalArgumentException {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException(URL_NOT_PROVIDED);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Asked to get content from the URL {} using GET method", url);
        }

        String content = null;

        HttpGet httpMethod = new HttpGet(url);
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpMethod.addHeader(header.getKey(), header.getValue());
            }
        }
        try (CloseableHttpResponse response = getHttpClient(url).execute(httpMethod)) {
            if (response.getCode() == SC_OK) {
                content = EntityUtils.toString(response.getEntity());
            } else {
                logger.warn("Connection to URL: {} failed with status {}", url, response.getCode());
            }
        } catch (IOException | ParseException e) {
            logger.error("Unable to get the content of the URL: {}. Cause: {}", url, e.getMessage(), e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Retrieved {} characters as a response", content != null ? content.length() : 0);
            if (logger.isTraceEnabled()) {
                logger.trace("Content: {}", content);
            }
        }

        return content;
    }

    /**
     * Executes a request with POST method to the specified URL and reads the response content as a string.
     *
     * @param url a URL to connect to
     * @param parameters the request parameter to submit; <code>null</code> if no parameters are passed
     * @param headers request headers to be set for connection; <code>null</code> if no additional headers needs to be set
     * @return the string representation of the URL connection response
     * @throws {@link IllegalArgumentException} in case of a malformed URL
     */
    public String executePost(String url, Map<String, String> parameters, Map<String, String> headers) throws IllegalArgumentException {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException(URL_NOT_PROVIDED);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Asked to get content from the URL {} using POST method with parameters {}", url, parameters);
        }

        String content = null;

        HttpPost httpMethod = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<>();

        if (parameters != null && !parameters.isEmpty()) {
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                nvps.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }
        }

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpMethod.addHeader(header.getKey(), header.getValue());
            }
        }

        httpMethod.setEntity(new UrlEncodedFormEntity(nvps));

        try (CloseableHttpResponse response = getHttpClient(url).execute(httpMethod)) {
            if (response.getCode() == SC_OK) {
                content = EntityUtils.toString(response.getEntity());
            } else {
                logger.warn("Connection to URL: {} failed with status {}", url, response.getCode());
            }

        } catch (IOException | ParseException e) {
            logger.error("Unable to get the content of the URL: {}. Cause: {}", url, e.getMessage(), e);
        }

        logContent(content);

        return content;
    }

    /**
     * Connects to the specified absolute URL and reads its content as a string.
     *
     * @param url the absolute URL to connect to
     * @return the string representation of the URL connection response
     * @throws {@link IllegalArgumentException} in case of a malformed URL
     */
    public String getAbsoluteResourceAsString(String url) throws IllegalArgumentException {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException(URL_NOT_PROVIDED);
        }
        if (!isAbsoluteUrl(url)) {
            throw new IllegalArgumentException("Cannot handle non-absolute URL: " + url);
        }

        return executeGet(url);
    }

    /**
     * Retrieves the content of the resource, specified by the provided URL. The
     * resource should be available in the current servlet context.
     *
     * @param url the URL of the resource to be retrieved
     * @return the string content of the resource
     * @throws {@link IllegalArgumentException} in case of a malformed URL
     * @see ServletContext#getResourceAsStream(String)
     */
    public String getContextResourceAsString(String url) throws IllegalArgumentException {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException(URL_NOT_PROVIDED);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Asked to get content from the context resource: {}", url);
        }

        String content = null;
        InputStream is = servletContext.getResourceAsStream(url);
        if (is != null) {
            StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(is, writer, SettingsBean.getInstance().getCharacterEncoding());
                content = writer.toString();
            } catch (IOException e) {
                logger.warn("Error reading content of the resource {}. Cause: {}", url, e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            logger.warn("Unable to find context resource at path {}", url);
        }

        logContent(content);

        return content;
    }

    /**
     * @return the httpClient
     * @deprecated since 7.1.2.3 Please, use {@link #getHttpClient(String)} instead which properly handles proxy server settings, if any
     */
    @Deprecated
    public HttpClient getHttpClient() {
        return getHttpClient(null);
    }

    /**
     * Detects the instance of the {@link HttpClient} that is the most appropriate to handle the specified URL, considering proxy settings,
     * if any.
     *
     * @param url
     *            the target URL to be used for the HTTP client request
     * @return an instance of the {@link HttpClient} that is the most appropriate to handle the specified URL, considering proxy settings,
     *         if any
     */
    public CloseableHttpClient getHttpClient(String url) {
        CloseableHttpClient selectedClient = null;
        if (url == null) {
            // we have no information about the target URL
            selectedClient = fallbackHttpClient;
        } else {
            // the target URL is provided
            // do we have a choice between multiple clients?
            if (httpClients.size() > 1) {
                try {
                    String key = ProxyAddressSelector.getProxyForUrl(url);
                    logger.debug("Using proxy address {} for URL {}", key, url);
                    selectedClient = httpClients.get(key);
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }

        if (selectedClient == null) {
            selectedClient = httpClients.get(null);
        }

        return selectedClient;
    }

    public RequestConfig.Builder getRequestConfigBuilder(HttpClient httpClient) {
        if (httpClient instanceof Configurable) {
            RequestConfig config = ((Configurable) httpClient).getConfig();
            return RequestConfig.copy(config);
        }
        return RequestConfig.custom();
    }

    /**
     * Retrieves the content of the specified resource as a string. If the
     * absolute URL is provided, an HTTP connection is used to get the response.
     * Otherwise the resource is considered to be current context resource and
     * is retrieved using {@link ServletContext#getResource(String)} method.
     *
     * @param url the resource URL
     * @return the string representation of the resource content
     * @throws {@link IllegalArgumentException} in case of a malformed URL
     */
    public String getResourceAsString(String url) throws IllegalArgumentException {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException(URL_NOT_PROVIDED);
        }
        return isAbsoluteUrl(url) ? getAbsoluteResourceAsString(url) : getContextResourceAsString(url);
    }

    /**
     * Retrieves resource content, specified by the provided URL.
     *
     * @param url the URL of the resource to be retrieved
     * @return the string content of the resource
     * @throws {@link IllegalArgumentException} in case of a malformed URL
     */
    public String getResourceAsString(String url, HttpServletRequest request, HttpServletResponse response)
            throws IllegalArgumentException {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException(URL_NOT_PROVIDED);
        }
        if (isAbsoluteUrl(url)) {
            return getAbsoluteResourceAsString(url);
        }
        if (!url.startsWith("/")) {
            throw new IllegalArgumentException("Provided relative URL does not start with a '/'");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Asked to get content from the URL: {}", url);
        }

        String content = null;

        RequestDispatcher rd = request.getRequestDispatcher(url);
        if (rd != null) {
            StringResponseWrapper wrapper = new StringResponseWrapper(response);
            try {
                rd.include(request, wrapper);
                if (wrapper.getStatus() < 200 || wrapper.getStatus() > 299) {
                    logger.warn("Unable to get the content of the resource {}. Got response status code: {}", url, wrapper.getStatus());
                } else {
                    content = wrapper.getString();
                }
            } catch (Exception e) {
                logger.warn("Unable to get the content of the resource {}. Cause: {}", url, e.getMessage(), e);
            }
        } else {
            logger.warn("Unable to get a RequestDispatcher for the path {}", url);
        }

        logContent(content);

        return content;
    }

    private void logContent(String content) {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieved {} characters as a response", content != null ? content.length() : 0);
            if (logger.isTraceEnabled()) {
                logger.trace("Content: {}", content);
            }
        }
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void shutdown() {
        logger.info("Shutting down HttpClient...");
        try {
            connManager.close();
        } catch (Exception e) {
            logger.warn("Error shutting down HttpClient. Cause: " + e.getMessage(), e);
        }
        logger.info("...done");
    }
}
