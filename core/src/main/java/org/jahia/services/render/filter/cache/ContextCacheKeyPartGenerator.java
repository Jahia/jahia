package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

public class ContextCacheKeyPartGenerator implements CacheKeyPartGenerator {
    @Override
    public String getKey() {
        return "context";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext) {
        return String.valueOf(resource.getContextConfiguration());
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }
}
