/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.bundles.url.df;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for transforming an absolute bundle location URL into a relative one (using dfdata or dfwar protocol) and vice versa.
 * 
 * @author Sergiy Shyrkov
 */
final class UrlTransformer {

    private static Logger logger = LoggerFactory.getLogger(UrlTransformer.class);

    /**
     * Retrieves the connection for the specified URL, preliminary resolving the absolute bundle location.
     * 
     * @param relativeUrl
     *            the relative bundle location URL with dfdata or dfwar protocol
     * @return the URL connection to the resolved bundle file
     */
    public static URLConnection getConnection(URL relativeUrl) {
        try {
            return toAbsolute(relativeUrl).openConnection();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getDataDirPath() {
        return getDirPath("jahia.data.dir");
    }

    private static String getDirPath(String property) {
        String path = System.getProperty(property);
        return '/' + (path.indexOf('\\') != -1 ? path.replace('\\', '/') : path);
    }

    private static String getWebAppDirPath() {
        return getDirPath("jahiaWebAppRoot");
    }

    /**
     * Resolves the passed relative bundle URL (with dfdata or dfwar protocols) into an absolute one.
     * 
     * @param url
     *            the relative bundle location URL
     * @return the absolute bundle URL, which corresponds to the relative one
     */
    public static URL toAbsolute(URL url) {
        logger.debug("Trying to resolve absolute bundle URL for {}", url);
        try {
            URL resolvedUrl = url;
            switch (url.getProtocol()) {
                case "dfdata":
                    resolvedUrl = new URL("file:" + getDataDirPath() + url.getPath());
                    break;
                case "dfwar":
                    resolvedUrl = new URL("file:" + getWebAppDirPath() + url.getPath());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown protocol " + url.getProtocol());
            }
            if (resolvedUrl != url && logger.isDebugEnabled()) {
                logger.debug("Resolved bundle URL {} to an absolute one: {}", url, resolvedUrl);
            }
            return resolvedUrl;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Transforms the passed absolute bundle URL into a relative one (using dfdata or dfwar protocols), if it is applicable. Otherwise
     * returns the original URL.
     * 
     * @param url
     *            the absolute bundle location URL
     * @return the relative bundle URL (using dfdata or dfwar protocols), which corresponds to the source one, if applicable; otherwise
     *         returns the original URL
     */
    public static URL toRelative(URL url) {
        logger.debug("Trying to resolve relative bundle URL for {}", url);
        try {
            URL resolvedUrl = url;
            String path = null;
            if ("file".equals(url.getProtocol())) {
                path = url.getPath();
                String dataDir = getDataDirPath();
                if (path.startsWith(dataDir)) {
                    resolvedUrl = new URL("dfdata:" + url.getPath().substring(dataDir.length()));
                } else {
                    String webappDir = getWebAppDirPath();
                    if (path.startsWith(webappDir)) {
                        resolvedUrl = new URL("dfwar:" + url.getPath().substring(webappDir.length()));
                    }
                }
            }
            if (resolvedUrl != url && logger.isDebugEnabled()) {
                logger.debug("Resolved bundle URL {} to a relative one: {}", url, resolvedUrl);
            }
            return resolvedUrl;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private UrlTransformer() {
        super();
    }
}
