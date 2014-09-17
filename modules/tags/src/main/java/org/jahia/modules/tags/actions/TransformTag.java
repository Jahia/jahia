package org.jahia.modules.tags.actions;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.tags.BaseTagAction;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Transform tag(s) action
 *
 * @author kevan
 */
public class TransformTag extends BaseTagAction {

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        List<String> tags = parameters.get("tag");
        JSONObject result = new JSONObject();
        JSONArray jsonTags = new JSONArray();
        if(!tags.isEmpty()){
            for (String tag : tags){
                String transformedTag = taggingService.getTagHandler().execute(tag);
                if(StringUtils.isNotEmpty(transformedTag)){
                    jsonTags.put(transformedTag);
                }
            }
        }

        result.put("tags", jsonTags);

        return new ActionResult(HttpServletResponse.SC_OK, resource.getNode().getPath(), result);
    }
}
