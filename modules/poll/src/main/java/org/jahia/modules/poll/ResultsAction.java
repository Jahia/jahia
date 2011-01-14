package org.jahia.modules.poll;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Nov 19, 2010
 * Time: 5:44:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResultsAction extends Action {
     private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(VoteAction.class);

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
     return new ActionResult(HttpServletResponse.SC_OK, null, VoteAction.generateJSONObject(renderContext.getMainResource().getNode()));
    }
}
