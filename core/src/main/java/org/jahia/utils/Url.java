/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
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
        if (!isLocalhost(request.getServerName()) &&
                !isLocalhost(node.getResolveSite().getServerName()) &&
                !StringUtils.isEmpty(node.getResolveSite().getServerName()) &&
                !request.getServerName().equals(node.getResolveSite().getServerName())
                ) {
            int serverPort = request.getServerPort();
            if ("http".equals(request.getScheme()) && (request.getServerPort() == 80)) {
                serverPort = -1;
            } else if ("https".equals(request.getScheme()) && (request.getServerPort() == 443)) {
                serverPort = -1;
            }
            nodeURL = new URL(request.getScheme(), node.getResolveSite().getServerName(), serverPort, nodeURL).toString();
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
        StringBuilder url = new StringBuilder();
        String scheme = request.getScheme();
        String host = request.getServerName();

        int port = SettingsBean.getInstance().getSiteURLPortOverride();

        if (port == 0) {
            port = request.getServerPort();
        }

        url.append(scheme).append("://").append(host);

        if (!(("http".equals(scheme) && (port == 80)) ||
              ("https".equals(scheme) && (port == 443)))) {
            url.append(":").append(port);
        }

        return url.toString();
    }
}
