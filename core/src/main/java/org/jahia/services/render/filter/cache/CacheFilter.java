/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.filter.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.springframework.beans.factory.InitializingBean;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 8 janv. 2010
 */
public class CacheFilter extends AbstractFilter implements InitializingBean {
    private transient static Logger logger = Logger.getLogger(CacheFilter.class);
    private EhCacheProvider cacheProvider;
    public static final String CACHE_NAME = "CJHTMLCache";
    private Cache blockingCache;

    public Cache getBlockingCache() {
        return blockingCache;
    }

    public Cache getDependenciesCache() {
        return dependenciesCache;
    }

    private Cache dependenciesCache;
    @Override
    protected String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (!renderContext.isEditMode()) {
            boolean debugEnabled = logger.isDebugEnabled();
            String key = generateKey(resource, renderContext);
            Element element = blockingCache.get(key);
            if (element == null) {
                if(debugEnabled) logger.debug("Generating content for node : " + key);
                String renderContent = chain.doFilter(renderContext, resource);
                CacheEntry<String> cacheEntry = new CacheEntry<String>(renderContent);                
                blockingCache.put(new Element(key, cacheEntry));
                List<JCRNodeWrapper> nodeWrappers = resource.getDependencies();
                if (debugEnabled) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (JCRNodeWrapper nodeWrapper : nodeWrappers) {
                        stringBuilder.append(nodeWrapper.getPath()).append("\n");
                    }
                    logger.debug("Dependencies of " + key + " : \n" + stringBuilder.toString());
                }
                for (JCRNodeWrapper nodeWrapper : nodeWrappers) {
                    String path = nodeWrapper.getPath();
                    Element element1 = dependenciesCache.get(path);
                    Set<String> dependencies;
                    if (element1 != null) {
                        dependencies = (Set<String>) element1.getValue();
                    } else {
                        dependencies = new LinkedHashSet<String>();
                    }
                    dependencies.add(key);
                    dependenciesCache.put(new Element(path,dependencies));
                }
                return renderContent;
            } else {
                if(debugEnabled) logger.debug("Getting content from cache for node : " + key);
                return (String) ((CacheEntry) element.getValue()).getObject();
            }
        }
        return chain.doFilter(renderContext, resource);
    }

    private String generateKey(Resource resource, RenderContext context) {
        return new StringBuilder().append(resource.getNode().getPath()).append("__template__").append(
                resource.getResolvedTemplate()).append("__lang__").append(resource.getLocale()).append(
                "__site__").append(context.getSite().getSiteKey()).toString();
    }

    public void setCacheProvider(EhCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        CacheManager cacheManager = cacheProvider.getCacheManager();
        cacheManager.addCache(CACHE_NAME);
        cacheManager.addCache(CACHE_NAME+"dependencies");
        blockingCache = cacheManager.getCache(CACHE_NAME);
        dependenciesCache = cacheManager.getCache(CACHE_NAME+"dependencies");
    }
}
