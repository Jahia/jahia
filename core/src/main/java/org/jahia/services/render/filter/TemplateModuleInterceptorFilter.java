package org.jahia.services.render.filter;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Render;
import org.jahia.services.content.interceptor.TemplateModuleInterceptor;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * filter that will inject rendercontext in TemplateModuleInterceptor
 */
public class TemplateModuleInterceptorFilter extends AbstractFilter {


    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        TemplateModuleInterceptor.renderContextThreadLocal.set(renderContext);
        return null;
    }

    @Override
    public void finalize(RenderContext renderContext, Resource resource, RenderChain renderChain) {
        TemplateModuleInterceptor.renderContextThreadLocal.remove();
    }
}
