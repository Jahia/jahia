package org.jahia.services.content.nodetypes.renderer;

import org.jahia.services.render.RenderContext;
import org.jahia.services.content.JCRPropertyWrapper;

import javax.jcr.RepositoryException;
import java.util.Map;

/**
 * Defines the renderer for a choice list.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 18 nov. 2009
 */
public interface ChoiceListRenderer {
    Map<String,Object> getObjectRendering(RenderContext context, JCRPropertyWrapper propertyWrapper) throws RepositoryException;
    String getStringRendering(RenderContext context, JCRPropertyWrapper propertyWrapper) throws RepositoryException;
}
