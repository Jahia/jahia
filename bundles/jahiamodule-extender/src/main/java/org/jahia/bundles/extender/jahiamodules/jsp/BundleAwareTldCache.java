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
package org.jahia.bundles.extender.jahiamodules.jsp;

import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.TldCache;
import org.apache.tomcat.util.descriptor.tld.TaglibXml;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.jahia.osgi.BundleUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.lang.reflect.Field;
import java.util.*;

/**
 * A custom implementation of {@link TldCache} that supports caching of TLDs located in OSGI bundles.
 *
 * @author cmoitrier
 */
public final class BundleAwareTldCache extends TldCache {
    private static final Logger logger = LoggerFactory.getLogger(BundleAwareTldCache.class);

    private final ServletContext servletContext;
    private final Map<Long, TldCache> perBundleCache = new HashMap<>();

    public BundleAwareTldCache(ServletContext servletContext, TldCache webappTldCache) {
        super(servletContext, Collections.emptyMap(), Collections.emptyMap());
        this.servletContext = servletContext;

        try {
            sync(webappTldCache, this);
        } catch (ReflectiveOperationException e) {
            logger.error("Unable to sync TLD cache", e);
        }
    }

    @Override
    public TldResourcePath getTldResourcePath(String uri) {
        TldResourcePath path = super.getTldResourcePath(uri);
        if (path != null) {
            return path;
        }

        for (TldCache cache : perBundleCache.values()) {
            path = cache.getTldResourcePath(uri);
            if (path != null) {
                return path;
            }
        }

        return null;
    }

    @Override
    public TaglibXml getTaglibXml(TldResourcePath tldResourcePath) throws JasperException {
        TaglibXml taglibXml = super.getTaglibXml(tldResourcePath);
        if (taglibXml != null) {
            return taglibXml;
        }

        for (TldCache cache : perBundleCache.values()) {
            taglibXml = cache.getTaglibXml(tldResourcePath);
            if (taglibXml != null) {
                return taglibXml;
            }
        }

        return null;
    }

    void add(Bundle bundle, Map<String, TldResourcePath> uriTldResourcePathMap, Map<TldResourcePath, TaglibXml> tldResourcePathTaglibXmlMap) {
        if (!uriTldResourcePathMap.isEmpty()) {
            logger.info("Adding {} taglib(s) from bundle {}: {}", uriTldResourcePathMap.size(), BundleUtils.getDisplayName(bundle), uriTldResourcePathMap.keySet());
            perBundleCache.put(bundle.getBundleId(), new TldCache(servletContext, uriTldResourcePathMap, tldResourcePathTaglibXmlMap));
        }
    }

    void remove(Bundle bundle) {
        TldCache cache = perBundleCache.remove(bundle.getBundleId());
        if (cache != null) {
            try {
                Set<String> tldUris = getTldUris(cache);
                logger.info("Removing {} taglib(s) from bundle {}: {}", tldUris.size(), BundleUtils.getDisplayName(bundle), tldUris);
            } catch (ReflectiveOperationException e) {
                logger.error("Unable to list cached TLD URIs from bundle " + BundleUtils.getDisplayName(bundle), e);
            }
        }
    }

    private static void sync(TldCache from, TldCache to) throws ReflectiveOperationException {
        Field uriTldResourcePathMap = TldCache.class.getDeclaredField("uriTldResourcePathMap");
        uriTldResourcePathMap.setAccessible(true);
        uriTldResourcePathMap.set(to, uriTldResourcePathMap.get(from));

        Field tldResourcePathTaglibXmlMap = TldCache.class.getDeclaredField("tldResourcePathTaglibXmlMap");
        tldResourcePathTaglibXmlMap.setAccessible(true);
        tldResourcePathTaglibXmlMap.set(to, tldResourcePathTaglibXmlMap.get(from));
    }

    private static Set<String> getTldUris(TldCache cache) throws ReflectiveOperationException {
        Field field =  TldCache.class.getDeclaredField("uriTldResourcePathMap");
        field.setAccessible(true);

        Map<String, TldResourcePath> uriTldResourcePathMap = (Map<String, TldResourcePath>) field.get(cache);
        return new HashSet<>(uriTldResourcePathMap.keySet());
    }

}
