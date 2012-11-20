package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

public class TemplateCacheKeyPartGenerator implements CacheKeyPartGenerator {
    @Override
    public String getKey() {
        return "template";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext) {
        if (resource.getContextConfiguration().equals("page") && resource.getNode().getPath().equals(
                renderContext.getMainResource().getNode().getPath())) {
            return renderContext.getMainResource().getResolvedTemplate();
        } else {
            return resource.getResolvedTemplate();
        }
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

}
