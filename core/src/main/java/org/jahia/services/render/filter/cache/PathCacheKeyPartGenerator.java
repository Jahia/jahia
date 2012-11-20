package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

public class PathCacheKeyPartGenerator implements CacheKeyPartGenerator {
    public static final String MAIN_RESOURCE_KEY = "_mr_";
    public static Pattern mainResourcePattern = Pattern.compile(MAIN_RESOURCE_KEY);

    @Override
    public String getKey() {
        return "path";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext) {
        HttpServletRequest request = renderContext.getRequest();
        StringBuilder s = new StringBuilder(resource.getNode().getPath());
        if (Boolean.TRUE.equals(request.getAttribute("cache.mainResource"))) {
            s.append(MAIN_RESOURCE_KEY);
        }
        return s.toString();
    }

    public String getPath(String key) {
        return mainResourcePattern.matcher(key).replaceAll("");
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return mainResourcePattern.matcher(keyPart).replaceAll(
                renderContext.getMainResource().getNode().getCanonicalPath() + renderContext.getMainResource().getResolvedTemplate());
    }
}
