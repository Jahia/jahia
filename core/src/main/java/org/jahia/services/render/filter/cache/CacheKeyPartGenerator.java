package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: toto
 * Date: 11/20/12
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CacheKeyPartGenerator {

    public String getKey();

    public String getValue(Resource resource, RenderContext renderContext);

    public String replacePlaceholders(RenderContext renderContext, String keyPart);

}
