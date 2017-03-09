/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import static org.apache.commons.httpclient.HttpStatus.SC_OK;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.htmlparser.jericho.Source;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.apache.xerces.impl.dv.util.Base64;
import org.jahia.data.templates.ModuleReleaseInfo;
import org.jahia.services.notification.HttpClientService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        HttpClient client = httpClientService.getHttpClient();
        // Get token from Private App Store home page
        GetMethod getMethod = new GetMethod(url + "/home.html");
        getMethod.addRequestHeader("Authorization", "Basic " + Base64.encode((releaseInfo.getUsername() + ":" + releaseInfo.getPassword()).getBytes()));
        String token = "";
        try {
            client.executeMethod(getMethod);
            Source source = new Source(getMethod.getResponseBodyAsString());
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
        } finally {
            getMethod.releaseConnection();
        }
        Part[] parts = {new StringPart("form-token",token),new FilePart("file",jar) };

        // send module
        PostMethod postMethod = new PostMethod(url + "/contents/modules-repository.createModuleFromJar.do");
        postMethod.getParams().setSoTimeout(0);
        postMethod.addRequestHeader("Authorization", "Basic " + Base64.encode((releaseInfo.getUsername() + ":" + releaseInfo.getPassword()).getBytes()));
        postMethod.addRequestHeader("accept", "application/json");
        postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
        String result = null;
        try {
            client.executeMethod(null, postMethod);
            StatusLine statusLine = postMethod.getStatusLine();

            if (statusLine != null && statusLine.getStatusCode() == SC_OK) {
                result = postMethod.getResponseBodyAsString();
            } else {
                logger.warn("Connection to URL: " + url + " failed with status " + statusLine);
            }

        } catch (HttpException e) {
            logger.error("Unable to get the content of the URL: " + url + ". Cause: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Unable to get the content of the URL: " + url + ". Cause: " + e.getMessage(), e);
        } finally {
            postMethod.releaseConnection();
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