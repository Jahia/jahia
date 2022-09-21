/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.provisioning.impl.operations;

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.xerces.impl.dv.util.Base64;
import org.jahia.bundles.config.OsgiConfigService;
import org.jahia.services.modulemanager.spi.Config;
import org.jahia.services.modulemanager.util.PropertiesValues;
import org.jahia.services.notification.HttpClientService;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Add maven repository operation
 */
@Component(service = Operation.class, property = "type=addMavenRepository")
public class AddMavenRepository implements Operation {
    private static final Logger logger = LoggerFactory.getLogger(AddMavenRepository.class);
    public static final String ADD_MAVEN_REPOSITORY = "addMavenRepository";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String DELIMITER = "//";
    private OsgiConfigService configService;
    private HttpClientService httpClientService;

    @Reference
    public void setHttpClientService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    @Reference
    protected void setConfigService(OsgiConfigService configService) {
        this.configService = configService;
    }

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.get(ADD_MAVEN_REPOSITORY) instanceof String;
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        try {
            Config settings = configService.getConfig("org.ops4j.pax.url.mvn");
            PropertiesValues values = settings.getValues();
            Set<String> vals = new LinkedHashSet<>(Arrays.asList(values.getProperty("org.ops4j.pax.url.mvn.repositories").split("[, ]+")));
            String url = buildMavenUrl(entry);
            if (!vals.contains(url)) {
                int checkUrl = getUrlResponseCode(url);
                if (HttpServletResponse.SC_OK == checkUrl) {
                    vals.add(url);
                    values.setProperty("org.ops4j.pax.url.mvn.repositories", StringUtils.join(vals, ", "));
                    configService.storeConfig(settings);
                } else {
                    logger.warn("Url not valid. Response code {}", checkUrl);
                }
            } else {
                logger.warn("Added url already exists");
            }
        } catch (Exception e) {
            logger.error("Cannot update configurations because {}, set class in debug for more details", e.getMessage());
            logger.debug("unable to register entry {}", Joiner.on(",").withKeyValueSeparator("=").useForNull("").join(entry), e);
        }
    }
    // Check url validity
    private int getUrlResponseCode(String url) throws IOException {
        // Build url without userInfo
        URI baseUri = URI.create(url);
        String baseUrl = baseUri.getScheme() + "://" + baseUri.getHost() + (baseUri.getPort() > 0 ? (":" + baseUri.getPort()) : "") + StringUtils.substringBefore(baseUri.getPath(), "@");
        HttpGet method = new HttpGet(baseUrl);
        if (baseUri.getUserInfo() != null) {
            method.addHeader("Authorization", "Basic " + Base64.encode(baseUri.getUserInfo().getBytes()));
        }
        try (CloseableHttpResponse response = httpClientService.getHttpClient(baseUrl).execute(method)) {
            return response.getCode();
        }
    }

    private String buildMavenUrl(Map<String, Object> entry) throws UnsupportedEncodingException {
        final String url = (String) entry.get(ADD_MAVEN_REPOSITORY);
        String username = entry.get(USERNAME) != null ? URLEncoder.encode((String) entry.get(USERNAME), "UTF-8") : null;
        int pos = StringUtils.indexOf(url, DELIMITER) + DELIMITER.length();
        // Add credentials to url if:
        // - username is set
        // - url starts with http
        // - "//" is part of the url
        if (username != null && StringUtils.startsWith(url, "http") && pos > DELIMITER.length()) {
            String password = entry.get(PASSWORD) != null ? URLEncoder.encode((String) entry.get(PASSWORD), "UTF-8") : "";
            return  StringUtils.substring(url, 0, pos) + username + ":" + password + "@" + StringUtils.substring(url,  pos);
        }
        return url;
    }
}
