package org.jahia.modules.blog;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.bin.Render;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 1/12/11
 * Time: 17:41
 * To change this template use File | Settings | File Templates.
 */
public class AddBlogEntryAction extends Action {

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRSessionWrapper jcrSessionWrapper = resource.getNode().getSession();
        JCRNodeWrapper node = resource.getNode();
        if (!node.hasNode("blog-content")) {
            node.checkout();
            node = node.addNode("blog-content", "jnt:contentList");
        } else {
            node = node.getNode("blog-content");
        }

        JCRNodeWrapper newNode = createNode(req, parameters, node, "jnt:blogContent","");
        jcrSessionWrapper.save();
        return new ActionResult(HttpServletResponse.SC_OK, newNode.getPath(), Render.serializeNodeToJSON(newNode));
    }
}
