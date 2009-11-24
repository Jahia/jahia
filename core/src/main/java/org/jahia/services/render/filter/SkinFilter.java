package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 24, 2009
 * Time: 3:14:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class SkinFilter extends AbstractFilter {
    public String doFilter(RenderContext renderContext, Resource resource, String output, RenderChain chain) throws IOException, RepositoryException {

        String skin = (String) renderContext.getRequest().getAttribute("skin");
        if (!StringUtils.isEmpty(skin) && !skin.equals("none")) {
            resource.pushWrapper(skin);
        }

        return chain.doFilter(renderContext, resource, output);
    }
}
