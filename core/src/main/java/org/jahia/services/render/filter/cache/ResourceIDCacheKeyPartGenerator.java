package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;

public class ResourceIDCacheKeyPartGenerator implements CacheKeyPartGenerator {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ResourceIDCacheKeyPartGenerator.class);

    @Override
    public String getKey() {
        return "resourceID";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext) {
        try {
            return resource.getNode().getIdentifier();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

}
