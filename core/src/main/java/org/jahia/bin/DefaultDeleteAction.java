package org.jahia.bin;

import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class DefaultDeleteAction extends Action {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(DefaultPostAction.class);

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        Node node = session.getNode(urlResolver.getPath());
        Node parent = node.getParent();
        node.remove();
        session.save();
        String url = parent.getPath();
        session.save();
        final String requestWith = req.getHeader("x-requested-with");


        if (req.getHeader("accept").contains("application/json") && requestWith != null &&
                requestWith.equals("XMLHttpRequest")) {
            return new ActionResult(HttpServletResponse.SC_OK, url, new JSONObject());
        } else {
            return new ActionResult(HttpServletResponse.SC_NO_CONTENT, url, new JSONObject());
        }
    }
}
