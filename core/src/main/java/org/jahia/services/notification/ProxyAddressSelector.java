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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
