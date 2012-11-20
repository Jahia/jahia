package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.servlet.http.HttpServletRequest;

public class CustomCacheKeyPartGenerator implements CacheKeyPartGenerator {
    @Override
    public String getKey() {
        return "custom";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext) {
        HttpServletRequest request = renderContext.getRequest();

        return (String) resource.getModuleParams().get("module.cache.additional.key") +
                (String) request.getAttribute("module.cache.additional.key");
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

}
