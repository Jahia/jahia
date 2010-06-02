package org.jahia.bin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

/**
 * Defines an action to be performed when a form data is submitted to render servlet.
 * User: toto
 * Date: Dec 3, 2009
 * Time: 1:35:24 PM
 */
public interface Action {

    public abstract String getName();

    public abstract ActionResult doExecute(HttpServletRequest req, RenderContext renderContext,
                                           Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception;

}
