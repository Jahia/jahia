package org.jahia.services.render.filter;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 1/17/11
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class AreaResourceFilter extends AbstractFilter {

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
            JCRNodeWrapper node = resource.getNode();

            final HttpServletRequest request = renderContext.getRequest();
            if (node.isNodeType("jnt:area")) {
                chain.pushAttribute(request,"areaListResource",resource);
            } else if (node.isNodeType("jmix:list")) {
                 chain.pushAttribute(request,"areaResource",request.getAttribute("areaListResource"));
                 chain.pushAttribute(request,"areaListResource",null);
            }
        return null;
    }
}
