/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.notification;

import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Utility class that detects, based on the system properties, which HTTP proxy address should be used.
 * 
 * @author Sergiy Shyrkov
 */
final class ProxyAddressSelector {

    private static final ProxySelector PROXY_SELECTOR = ProxySelector.getDefault();

    private static Proxy filter(List<Proxy> proxies) {
        Proxy result = null;
        for (int i = 0; (result == null) && (i < proxies.size()); i++) {
            Proxy p = proxies.get(i);
            if (p.type() == Type.HTTP) {
                return p;
            }
        }
        return null;
    }

    /**
     * Detects, based on the system properties, which HTTP proxy address should be used.
     * 
     * @param url
     *            the target request URL
     * @return the proxy address that should be used for the supplied URL or <code>null</code> if no proxy should be used
     * @throws IllegalArgumentException
     *             in case the supplied URL cannot be converted into a valid URI
     */
    public static String getProxyForUrl(String url) throws IllegalArgumentException {
        Proxy proxy = null;
        if (url != null && url.length() > 0 && url.charAt(0) != '/') {
            try {
                proxy = filter(PROXY_SELECTOR.select(new URI(normalizeUrl(url))));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Cannot convert URL to URI: " + url, e);
            }
        }
        return proxy != null && proxy.address() != null ? proxy.address().toString() : null;
    }

    private static String normalizeUrl(String url) {
        if (!url.regionMatches(true, 0, "http://", 0, "http://".length())
                && !url.regionMatches(true, 0, "https://", 0, "https://".length())) {
            return "http://" + url;
        }
        return url;
    }

    private ProxyAddressSelector() {
        super();
    }
}
