package org.jahia.services.tags;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by kevan on 17/09/14.
 */
abstract public class BaseTagAction extends Action{
    protected TaggingService taggingService;

    @Override
    abstract public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception;

    public void setTaggingService(TaggingService taggingService) {
        this.taggingService = taggingService;
    }
}
