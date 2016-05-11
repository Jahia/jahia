package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * This interface can be implemented by CacheKeyPartGenerator to be able to hook before and after content generation in aggregation,
 * Sometime we store data in the key, and we want to re inject this data before sub fragment generation, and clean this re injected
 * data after the fragment generation, this interface is used for that.
 *
 * Created by jkevan on 08/04/2016.
 */
public interface RenderContextTuner {

    /**
     * This will be call before a new RenderChain is started to generate a fragment, it's called by the AggregateFilter
     * during aggregation of sub fragment.
     *
     * This allow to modify the context, or store data in request that need be reuse for the sub fragment generation.
     * The main usage, is when a parent fragment is in cache and context need to modify to re generate a sub fragment
     *
     * It's the case for module params, node type restrictions coming from the parent area, etc ...
     *
     * @param keyValue the value of the current key part generator, the parsing of the key is already made by CacheKeyGenerator, so
     *                 the value is the one corresponding to the current key part generator
     * @param resource the current resource rendered
     * @param renderContext the current renderContext
     * @return The object returned will be stored and pass again to the "restoreContextAfterContentGeneration"
     *          after the fragment generation
     */
    Object prepareContextForContentGeneration(String keyValue, Resource resource, RenderContext renderContext);

    /**
     * This will be call after a RenderChain is stopped and finished to generate the sub fragment, it's called by the AggregateFilter
     * during the aggregation of sub fragment.
     *
     * This allow to restore the context, request or anything that have been modify in the "prepareContextForContentGeneration" to avoid
     * conflict with next content generation.
     *
     * It's used to clear the request in the InAreaCacheKeyPartGenerator for example
     *
     * @param keyValue the value of the current key part generator, the parsing of the key is already made by CacheKeyGenerator, so
     *                 the value is the one corresponding to the current key part generator
     * @param resource the current resource rendered
     * @param renderContext the current renderContext
     * @param original The original object returned by the "prepareContextForContentGeneration" that have been called before the
     *                 generation of the fragment
     */
    void restoreContextAfterContentGeneration(String keyValue, Resource resource, RenderContext renderContext, Object original);
}
