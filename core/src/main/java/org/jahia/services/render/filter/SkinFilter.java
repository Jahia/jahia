package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.apache.commons.lang.StringUtils;

/**
 * SkinFilter
 *
 * Filter that add skins to the current resource.
 *
 * If a skin parameter is found, the filter just adds it to the wrappers list.
 *
 */
public class SkinFilter extends AbstractFilter {
    public String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {

        String skin = (String) renderContext.getRequest().getAttribute("skin");
        if (!StringUtils.isEmpty(skin) && !skin.equals("none")) {
            resource.pushWrapper(skin);
        }

        return chain.doFilter(renderContext, resource);
    }
}
