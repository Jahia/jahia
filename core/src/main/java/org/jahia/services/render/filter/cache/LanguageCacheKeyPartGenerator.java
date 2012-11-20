package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

public class LanguageCacheKeyPartGenerator implements CacheKeyPartGenerator {
    @Override
    public String getKey() {
        return "language";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext) {
        return resource.getLocale().toString();
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

}
