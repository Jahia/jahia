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