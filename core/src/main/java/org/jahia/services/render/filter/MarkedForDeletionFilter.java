package org.jahia.services.render.filter;

import org.jahia.api.Constants;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * This filter hides the nodes that are "marked for deletion" nodes in preview mode
 */
public class MarkedForDeletionFilter extends AbstractFilter {

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (resource.getNode().isNodeType(Constants.JAHIAMIX_MARKED_FOR_DELETION)) {
            return "";
        }
        return null;
    }
}
