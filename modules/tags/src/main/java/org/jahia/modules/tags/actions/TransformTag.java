package org.jahia.modules.tags.actions;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.tags.TaggingService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Transform tag(s) action
 *
 * @author kevan
 */
public class TransformTag extends Action{
    private TaggingService taggingService;

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {

        List<String> transformedTags = new LinkedList<String>();
        if(!CollectionUtils.isEmpty(parameters.get("tag"))){
            transformedTags = Lists.transform(parameters.get("tag"), new Function<String, String>() {
                @Nullable
                @Override
                public String apply(@Nullable String input) {
                    return taggingService.getTagHandler().execute(input);
                }
            });
        }

        JSONObject result = new JSONObject();
        JSONArray tags = new JSONArray();
        for (String transfomedTag : transformedTags){
            tags.put(transfomedTag);
        }
        result.put("tags", tags);

        return new ActionResult(HttpServletResponse.SC_OK, resource.getNode().getPath(), result);
    }

    public void setTaggingService(TaggingService taggingService) {
        this.taggingService = taggingService;
    }
}
