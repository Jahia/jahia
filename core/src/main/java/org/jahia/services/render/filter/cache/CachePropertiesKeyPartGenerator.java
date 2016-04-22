package org.jahia.services.render.filter.cache;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Properties;

/**
 * Key part generator that handle cache properties, store info in request before content generation
 * this infos are used in the CacheFilter to cache or not the content, or add dependency on main resource if needed
 * Doing this calculation here because AggregateFilter and CacheFilter are both using this infos to generate the key
 * or get infos, this centralize the calculation in one time at key generation for a given fragment
 *
 * Created by jkevan on 22/04/2016.
 */
public class CachePropertiesKeyPartGenerator implements CacheKeyPartGenerator, ContextModifierCacheKeyPartGenerator{
    private static final Logger logger = LoggerFactory.getLogger(CachePropertiesKeyPartGenerator.class);

    @Override
    public String getKey() {
        return "cache";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext, Properties properties) {
        try {
            // check v parameter
            if (renderContext.getRequest().getParameter(CacheFilter.V) != null && renderContext.isLoggedIn()) {
                return StringUtils.EMPTY;
            }

            // check ec parameter
            final String ecParameter = renderContext.getRequest().getParameter(CacheFilter.EC);
            if (ecParameter != null) {
                if (ecParameter.equals(resource.getNode().getIdentifier())) {
                    return StringUtils.EMPTY;
                }
                for (Resource parent : renderContext.getResourcesStack()) {
                    if (ecParameter.equals(parent.getNode().getIdentifier())) {
                        return StringUtils.EMPTY;
                    }
                }
            }

            // check if we have a valid cache expiration
            final String cacheExpiration = properties.getProperty(CacheFilter.CACHE_EXPIRATION);
            Long expiration = cacheExpiration != null ? Long.parseLong(cacheExpiration) : -1;
            if (expiration != 0L) {
                // fragment is cacheable
                return "true".equals(properties.getProperty("cache.mainResource")) ? ("mr_" + cacheExpiration): cacheExpiration;
            } else {
                return StringUtils.EMPTY;
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return Boolean.FALSE.toString();
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

    @Override
    public Object prepareContentForContentGeneration(String keyValue, Resource resource, RenderContext renderContext) {
        // Store infos in request
        if(StringUtils.isEmpty(keyValue)) {
            renderContext.getRequest().setAttribute("cacheFilter.fragmentNotCacheable", true);
        } else {
            if(keyValue.startsWith("mr_")) {
                renderContext.getRequest().setAttribute("cacheFilter.fragmentDependsOnMR", true);
                keyValue = keyValue.substring(3);
            }
            renderContext.getRequest().setAttribute("cacheFilter.fragmentExpiration", keyValue);
        }
        return null;
    }

    @Override
    public void restoreContextAfterContentGeneration(String keyValue, Resource resource, RenderContext renderContext, Object previous) {
        // clean request for fragment
        renderContext.getRequest().removeAttribute("cacheFilter.fragmentNotCacheable");
        renderContext.getRequest().removeAttribute("cacheFilter.fragmentDependsOnMR");
        renderContext.getRequest().removeAttribute("cacheFilter.fragmentExpiration");
    }
}
