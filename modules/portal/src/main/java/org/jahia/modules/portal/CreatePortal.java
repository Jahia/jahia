package org.jahia.modules.portal;

import org.apache.bsf.utils.http.HttpScriptResponse;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 2/11/11
 * Time: 14:17
 * To change this template use File | Settings | File Templates.
 */
public class CreatePortal extends Action {

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String portalPath = parameters.get("portalPath").get(0);
        resource.getNode().checkout();
        if (parameters.get("defaultPortal") != null && parameters.get("defaultPortal").size() > 0) {
            JCRNodeWrapper portal = session.getNodeByUUID(parameters.get("defaultPortal").get(0));
            portal.copy(resource.getNode(), portalPath, false);
        } else {
            resource.getNode().addNode(portalPath, "jnt:portal");
        }
             resource.getNode().getSession().save();
        JCRNodeWrapper newNode = resource.getNode().getNode(portalPath);

        return new ActionResult(HttpScriptResponse.SC_OK, newNode.getPath(), null);
    }
}
