package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.servlet.http.HttpServletRequest;

public class SiteCacheKeyPartGenerator implements CacheKeyPartGenerator {
    @Override
    public String getKey() {
        return "site";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext) {
        HttpServletRequest request = renderContext.getRequest();
        URLResolver urlResolver = (URLResolver) renderContext.getRequest().getAttribute("urlResolver");
        return (urlResolver == null ||
                urlResolver.getSiteKeyByServerName() == null ? new StringBuilder().append(
                renderContext.getSite().getSiteKey()).append(":").append("virtualhost").append(":").append(
                request.getParameter("jsite")).toString() : new StringBuilder().append(
                renderContext.getSite().getSiteKey()).append(":").append(request.getParameter(
                "jsite")).toString());
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

}
