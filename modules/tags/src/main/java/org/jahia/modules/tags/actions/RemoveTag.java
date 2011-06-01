package org.jahia.modules.tags.actions;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


public class RemoveTag extends Action {

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRSessionWrapper jcrSessionWrapper = resource.getNode().getSession();
        JCRNodeWrapper node = resource.getNode();
        String tagToDelete = req.getParameter("tag");
        Map<String,String> res = new HashMap<String,String>();
        if (tagToDelete != null) {
            JCRNodeWrapper tag = session.getNode("/sites/" + urlResolver.getSiteKey() + "/tags/" + tagToDelete.trim());
            Map<String, String> properties = node.getPropertiesAsString();
            String[] tags = properties.get("j:tags").split(" ");
            ArrayList<String> tagsList = new ArrayList<String>();
            tagsList.addAll(Arrays.asList(tags));
            if (tagsList.contains(tag.getIdentifier())) {
                if (tagsList.size() > 0) {
                    for (int i = 0; i < tagsList.size(); i++) {
                        if (tagsList.get(i).equals(tag.getIdentifier())) {
                            tagsList.remove(i);
                        }
                    }
                    String[] str = tagsList.toArray(new String[tagsList.size()]);
                    node.setProperty("j:tags", str);
                    jcrSessionWrapper.save();
                } else {
                     node.removeMixin("jmix:tagged");
                }
            }
            res.put("size", String.valueOf(tagsList.size()));
        }
        return new ActionResult(HttpServletResponse.SC_OK, node.getPath(), new JSONObject(res));
    }
}
