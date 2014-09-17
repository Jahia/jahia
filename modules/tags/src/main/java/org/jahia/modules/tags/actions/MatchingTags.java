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
 * Matching tag(s) action
 *
 * @author kevan
 */
public class MatchingTags extends BaseTagAction{

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String prefix = getParameter(parameters, "q", "");
        String path = getParameter(parameters, "path", renderContext.getSite().getPath());
        String limitStr = getParameter(parameters, "limit");
        Long limit = StringUtils.isNotEmpty(limitStr) ? Long.valueOf(limitStr) : 10l;
        Map<String, Long> tags = taggingService.getTagsSuggester().suggest(prefix, path, 1l, limit, 0l, true, session);
        JSONObject result = new JSONObject();
        JSONArray tagsJSON = new JSONArray();
        for(String tag : tags.keySet()){
            JSONObject tagJSON = new JSONObject();
            tagJSON.put("name", tag);
            tagJSON.put("count", tags.get(tag));
            tagsJSON.put(tagJSON);
        }
        result.put("tags", tagsJSON);
        return new ActionResult(HttpServletResponse.SC_OK, null, result);
    }
}
