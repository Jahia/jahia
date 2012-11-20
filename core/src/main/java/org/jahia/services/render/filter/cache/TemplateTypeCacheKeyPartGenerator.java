package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

public class TemplateTypeCacheKeyPartGenerator implements CacheKeyPartGenerator {
    @Override
    public String getKey() {
        return "templateType";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext) {
        String templateType = resource.getTemplateType();
        if (renderContext.isAjaxRequest()) {
            templateType += ".ajax";
        }
        return templateType;
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

}
