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
package org.jahia.services.templates;

import net.htmlparser.jericho.Source;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.apache.maven.model.Model;
import org.apache.xerces.impl.dv.util.Base64;
import org.jahia.data.templates.ModuleReleaseInfo;
import org.jahia.services.notification.HttpClientService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * Helper class for Private App Store related operations.
 *
 * @author Sergiy Shyrkov
 */
class ForgeHelper {

    private static Logger logger = LoggerFactory.getLogger(ForgeHelper.class);
    
    private HttpClientService httpClientService;

    /**
     * Manage Private App Store
     */
    String createForgeModule(ModuleReleaseInfo releaseInfo, File jar) throws IOException {

        String moduleUrl = null;
        final String url = releaseInfo.getForgeUrl();
        CloseableHttpClient client = httpClientService.getHttpClient(url);
        // Get token from Private App Store home page
        HttpGet getMethod = new HttpGet(url + "/home.html");
        getMethod.addHeader("Authorization", "Basic " + Base64.encode((releaseInfo.getUsername() + ":" + releaseInfo.getPassword()).getBytes()));
        String token = getToken(client, getMethod);
        HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("form-token",token)
                .addBinaryBody("file",jar)
                .build();
        // send module
        HttpPost postMethod = new HttpPost(url + "/contents/modules-repository.createModuleFromJar.do");

        postMethod.setConfig(httpClientService.getRequestConfigBuilder(client).setResponseTimeout(Timeout.DISABLED).build());
        postMethod.addHeader("Authorization", "Basic " + Base64.encode((releaseInfo.getUsername() + ":" + releaseInfo.getPassword()).getBytes()));
        postMethod.addHeader("accept", "application/json");
        postMethod.setEntity(entity);
        String result = null;
        try (CloseableHttpResponse response = client.execute(postMethod)) {
            if (response.getCode() == SC_OK) {
                result = EntityUtils.toString(response.getEntity());
            } else {
                logger.warn("Connection to URL: {} failed with status {}", url, response.getCode());
            }
        } catch (IOException | ParseException e) {
            logger.error("Unable to get the content of the URL: {}. Cause: {}", url, e.getMessage(), e);
        }

        if (StringUtils.isNotEmpty(result)) {
            try {
                JSONObject json = new JSONObject(result);
                if (!json.isNull("moduleAbsoluteUrl")) {
                    moduleUrl = json.getString("moduleAbsoluteUrl");
                } else if (!json.isNull("error")) {
                    throw new IOException(json.getString("error"));
                } else {
                    logger.warn("Cannot find 'moduleAbsoluteUrl' entry in the create module actin response: {}", result);
                    throw new IOException("unknown");
                }
            } catch (JSONException e) {
                logger.error("Unable to parse the response of the module creation action. Cause: " + e.getMessage(), e);
            }
        }

        return moduleUrl;
    }

    private String getToken(CloseableHttpClient client, HttpGet getMethod) throws IOException {
        String token = "";
        try (CloseableHttpResponse response = client.execute(getMethod)) {
            Source source = new Source(EntityUtils.toString(response.getEntity()));
            if (source.getFirstElementByClass("file_upload") != null) {
                List<net.htmlparser.jericho.Element> els = source
                        .getFirstElementByClass("file_upload").getAllElements(
                                "input");
                for (net.htmlparser.jericho.Element el : els) {
                    if (StringUtils.equals(el.getAttributeValue("name"),
                            "form-token")) {
                        token = el.getAttributeValue("value");
                    }
                }
            } else {
                throw new IOException(
                        "Unable to get Private App Store site information, please check your credentials");
            }
        } catch (ParseException e) {
            throw new IOException(e);
        }
        return token;
    }

    String computeModuleJarUrl(String releaseVersion, ModuleReleaseInfo releaseInfo, Model model) {
        StringBuilder url = new StringBuilder(64);
        url.append(releaseInfo.getRepositoryUrl());
        if (!releaseInfo.getRepositoryUrl().endsWith("/")) {
            url.append("/");
        }
        String groupId = model.getGroupId();
        if (groupId == null && model.getParent() != null) {
            groupId = model.getParent().getGroupId();
        }
        url.append(StringUtils.replace(groupId, ".", "/"));
        url.append("/");
        url.append(model.getArtifactId());
        url.append("/");
        url.append(releaseVersion);
        url.append("/");
        url.append(model.getArtifactId());
        url.append("-");
        url.append(releaseVersion);
        url.append(".");
        String packaging = model.getPackaging();
        url.append(packaging == null || packaging.equals("bundle") ? "jar" : packaging);

        return url.toString();
    }

    /**
     * Injects an instance of the {@link HttpClientService}.
     * 
     * @param httpClientService
     *            an instance of the service
     */
    public void setHttpClientService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

}