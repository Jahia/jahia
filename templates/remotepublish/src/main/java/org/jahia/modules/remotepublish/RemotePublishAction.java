package org.jahia.modules.remotepublish;

import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 22, 2010
 * Time: 2:16:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemotePublishAction implements Action, BackgroundAction {
    private static Logger logger = Logger.getLogger(RemotePublishAction.class);
    private RemotePublicationService service;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setService(RemotePublicationService service) {
        this.service = service;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRNodeWrapper node = resource.getNode();
        return service.executeRemotePublication(node);
    }

    public void executeBackgroundAction(JCRNodeWrapper node) {
        try {
            service.executeRemotePublication(node);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
