package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

public class WrappedCacheKeyPartGenerator implements CacheKeyPartGenerator {
    @Override
    public String getKey() {
        return "wrapped";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext) {
        return String.valueOf(resource.hasWrapper());
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

}
