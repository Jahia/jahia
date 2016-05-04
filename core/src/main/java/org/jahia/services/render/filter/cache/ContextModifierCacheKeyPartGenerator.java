package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * Created by jkevan on 08/04/2016.
 */
public interface ContextModifierCacheKeyPartGenerator {

    Object prepareContentForContentGeneration(String keyValue, Resource resource, RenderContext renderContext);

    void restoreContextAfterContentGeneration(String keyValue, Resource resource, RenderContext renderContext, Object previous);
}
