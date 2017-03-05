package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * This interface can be implemented by a CacheKeyPartGenerator to be able to hook before and after content generation in aggregation,
 * Sometimes we store data in the key, and we want to re-inject this data before sub-fragment generation, and clean this re-injected
 * data after the fragment generation. This interface is used for that.
 *
 * Created by jkevan on 08/04/2017.
 */
public interface RenderContextTuner {

    /**
     * This will be called before a new RenderChain is started to generate a sub-fragment.
     *
     * This allows to modify the context, or store data in request that need be reused for the sub-fragment generation.
     * This is the case for module parameters, node type restrictions coming from the parent area, etc.
     *
     * @param value the value parsed from the key by this key part generator
     * @param resource the current resource rendered
     * @param renderContext the current renderContext
     * @return An object that represents the original state of the key part in the render context
     */
    Object prepareContextForContentGeneration(String value, Resource resource, RenderContext renderContext);

    /**
     * This will be called after a RenderChain finished to generate a sub-fragment.
     *
     * This allows to restore the context, request or anything that have been modified in the "prepareContextForContentGeneration".
     *
     * @param value the value parsed from the key by this key part generator
     * @param resource the current resource rendered
     * @param renderContext the current renderContext
     * @param original the original object previously retrieved via prepareContextForContentGeneration() invocation
     */
    void restoreContextAfterContentGeneration(String value, Resource resource, RenderContext renderContext, Object original);
}
