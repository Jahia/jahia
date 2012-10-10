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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.settings.SettingsBean;

/**
 * Miscellaneous request/response handling methods.
 * 
 * @author Sergiy Shyrkov
 */
public final class WebUtils {

    
    /**
     * Does a URL encoding of the <code>path</code>. The characters that don't need encoding are those defined 'unreserved' in section 2.3
     * of the 'URI generic syntax' RFC 2396. Not the entire path string is escaped, but every individual part (i.e. the slashes are not
     * escaped).
     * 
     * @param path
     *            the path to encode
     * @return the escaped path
     * @throws NullPointerException
     *             if <code>path</code> is <code>null</code>.
     * @see Text#escapePath(String)
     */
    public static String escapePath(String path) {
        return path != null ? Text.escapePath(path) : null;
    }
    
    /**
     * Loads the content of the specified servlet context resource as text.
     * 
     * @param path
     *            the resource path (context relative)
     * @return the text of the specified resource content or <code>null</code> if the corresponding resource does not exist
     * @throws IOException
     *             in case of a problem reading resource content
     */
    public static String getResourceAsString(String path) throws IOException {
        String content = null;
        InputStream is = null;
        try {
            is = JahiaContextLoaderListener.getServletContext().getResourceAsStream(path);
            if (is != null) {
                content = IOUtils.toString(is);
            }
        } finally {
            IOUtils.closeQuietly(is);
        }

        return content;
    }

    /**
     * Loads the content of the servlet context resource as text looking up the specified paths until the first resource is found.
     * 
     * @param lookupPaths
     *            the resource paths to lookup (context relative)
     * @return the text of the specified resource content or <code>null</code> if the corresponding resource does not exist
     * @throws IOException
     *             in case of a problem reading resource content
     */
    public static String lookupResourceAsString(String... lookupPaths) throws IOException {
        String text = null;
        for (String path : lookupPaths) {
            text = getResourceAsString(path);
            if (text != null) {
                break;
            }
        }
        return text;
    }

    /**
     * Sets proper response headers to cache current response for the specified number of seconds.
     * 
     * @param expiresSeconds
     * @param response
     *            current response object
     */
    public static void setCacheHeaders(long expiresSeconds, HttpServletResponse response) {
        response.setHeader("Cache-Control", "public, max-age=" + expiresSeconds);
        response.setDateHeader("Expires", System.currentTimeMillis() + expiresSeconds * 1000L);
    }

    /**
     * Sets proper response headers to prevent caching of this response.
     * 
     * @param response
     *            current response object
     */
    public static void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control",
                "no-cache, no-store, must-revalidate, proxy-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 295075800000L);
    }
    
    /**
     * Decodes a <code>application/x-www-form-urlencoded</code> string using the character encoding, configured in jahia.properties.
     * 
     * @param url
     *            the string to be decoded
     * @return the decoded string
     */
    public static String urlDecode(String url) {
        try {
            return URLDecoder.decode(url, SettingsBean.getInstance().getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * Initializes an instance of this class.
     */
    private WebUtils() {
        super();
    }

}
