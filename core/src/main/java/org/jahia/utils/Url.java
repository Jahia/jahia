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
package org.jahia.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * User: david
 * Date: 1/19/11
 * Time: 3:01 PM
 */
public class Url {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Url.class);
    public static final Set<String> LOCALHOSTS = Collections.singleton("localhost");

    /**
     * Encode facet filter URL parameter
     * @param inputString facet filter parameter
     * @return filter encoded for URL query parameter usage
     */

    public static String encodeUrlParam(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }
        // Compress the bytes
        byte[] output = new byte[2048];
        Deflater compresser = new Deflater();
        try {
            compresser.setInput(inputString.getBytes("UTF-8"));
            compresser.finish();
            int compressedDataLength = compresser.deflate(output);
            byte[] copy = new byte[compressedDataLength];
            System.arraycopy(output, 0, copy, 0,
                    Math.min(output.length, compressedDataLength));
            return Base64.encodeBase64URLSafeString(copy);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Not able to encode facet URL: " + inputString, e);
        }

        return inputString;
    }

    /**
     * Decode facet filter URL parameter
     * @param inputString enocded facet filter URL query parameter
     * @return decoded facet filter parameter
     */
    public static String decodeUrlParam(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }
        byte[] input = Base64.decodeBase64(inputString);
        // Decompress the bytes
        Inflater decompresser = new Inflater();
        decompresser.setInput(input, 0, input.length);
        byte[] result = new byte[2048];
        String outputString = "";
        try {
            int resultlength = decompresser.inflate(result);
            decompresser.end();
            outputString = new String(result, 0, resultlength, "UTF-8");
        } catch (DataFormatException e) {
            logger.warn("Not able to decode facet URL: " + inputString, e);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Not able to decode facet URL: " + inputString, e);
        }
        return outputString;
    }

    public static boolean isLocalhost(String host) {
    	return host != null && LOCALHOSTS.contains(host);
    }

    public static String appendServerNameIfNeeded(JCRNodeWrapper node, String nodeURL, HttpServletRequest request) throws RepositoryException, MalformedURLException {
        if (!SettingsBean.getInstance().isUrlRewriteUseAbsoluteUrls()) {
            return nodeURL;
        }

        String requestServerName = request.getServerName();
        if (isLocalhost(requestServerName)) {
            return nodeURL;
        }

        String serverName = node.getResolveSite().getServerName();
        if (!StringUtils.isEmpty(serverName) && !isLocalhost(serverName)
                && !requestServerName.equals(serverName)) {
            int serverPort = SettingsBean.getInstance().getSiteURLPortOverride();

            if (serverPort == 0) {
                serverPort = request.getServerPort();
            }
            if (serverPort == 80 && "http".equals(request.getScheme()) || serverPort == 443 && "https".equals(request.getScheme())) {
                serverPort = -1;
            }
            nodeURL = new URL(request.getScheme(), serverName, serverPort, nodeURL).toString();
        }
        return nodeURL;
    }

    /**
     * Returns the server URL, including scheme, host and port.
     * The URL is in the form <code><scheme><host>:<port></code>,
     * e.g. <code>http://www.jahia.org:8080</code>. The port is omitted in case
     * of standard HTTP (80) and HTTPS (443) ports.
     *
     * @return the server URL, including scheme, host and port
     */
    public static String getServer(HttpServletRequest request) {
        return getServer(request.getScheme(), request.getServerName(), request.getServerPort());
    }

    public static String getServer(HttpServletRequest request, String servername) {
        return getServer(request.getScheme(), servername, request.getServerPort());
    }

    public static String getServer(String scheme, String host, int port) {
        StringBuilder url = new StringBuilder();

        int portOverride = SettingsBean.getInstance().getSiteURLPortOverride();
        if (portOverride != 0) {
            port = portOverride;
        }

        url.append(scheme).append("://").append(host);

        if (!(port == 80 && "http".equals(scheme) || port == 443 && "https".equals(scheme))) {
            url.append(":").append(port);
        }

        return url.toString();
    }

    /**
     * Simple help method to encode uri
     * If the encodingStr is null set to "UTF-8" by default
     *
     * @param uri               [String]
     * @param encodingStr       [String]
     * @return encoded uri
     * @throws IOException io specific exceptions
     */
    public static String encodeUri(String uri, String encodingStr) throws IOException {
        String encodedUri = null;
        String encoding = (encodingStr != null && !encodingStr.isEmpty()) ? encodingStr : StandardCharsets.UTF_8.name();
        try {
            final URI redirectUri = new URI(uri);
            final UriComponents uriComponents = UriComponentsBuilder.fromUri(redirectUri).build();
            encodedUri = uriComponents.encode(encoding).toUriString();
        } catch (URISyntaxException ex) {
            logger.error("Impossible to build URI from {}", uri);
            encodedUri = UriUtils.encodeQuery(uri, encoding);
        } catch (NullPointerException npe) {
            logger.error("The URI to encode is null.");
        }
        return encodedUri;
    }
}
