package org.jahia.bin;

import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class DefaultDeleteAction extends Action {

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        Node node = session.getNode(urlResolver.getPath());
        Node parent = node.getParent();

        if (!parent.isCheckedOut()) {
            parent.checkout();
        }
        if (!node.isCheckedOut()) {
            node.checkout();
        }
        node.remove();
        session.save();
        String url = parent.getPath();
        session.save();
        final String requestWith = req.getHeader("x-requested-with");


        if (req.getHeader("accept").contains("application/json") && requestWith != null &&
                requestWith.equals("XMLHttpRequest")) {
            return ActionResult.OK_JSON;
        } else {
            return new ActionResult(HttpServletResponse.SC_NO_CONTENT, url, new JSONObject());
        }
    }
}
