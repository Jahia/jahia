package org.jahia.bin;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.jcr.RepositoryException;
import java.util.*;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 3, 2009
 * Time: 1:35:24 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Action {

    public abstract String getName();

    public abstract JCRNodeWrapper getNewNode();

    public abstract void doExecute(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext,
                                   Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception;

}
