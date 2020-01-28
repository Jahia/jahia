/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
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
