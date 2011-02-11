package org.jahia.modules.defaultmodule.actions;

import org.jahia.api.Constants;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: 10.02.11
 * Time: 09:12
 * To change this template use File | Settings | File Templates.
 */
public class PublishAction extends Action {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(PublishAction.class);

    private JCRPublicationService publicationService;

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        Set<String> languages = null;
        if (session.getLocale() != null) {
            languages = Collections.singleton(session.getLocale().toString());
        }
        boolean withSubTree = true;
        if (parameters.get("withSubTree") != null) {
            String subTreeStr = parameters.get("withSubTree").get(0);
            withSubTree = Boolean.parseBoolean(subTreeStr);
        }
        publicationService.publishByMainId(resource.getNode().getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, withSubTree, new ArrayList<String>());
        return ActionResult.OK_JSON;
    }
}
