package org.jahia.modules.docspace;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Mar 26, 2010
 * Time: 5:27:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublishFile implements org.jahia.bin.Action {
    private static Logger logger = Logger.getLogger(SetACLAction.class);
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(
                resource.getWorkspace(), resource.getLocale());
        try {
            final JCRPublicationService service = JCRPublicationService.getInstance();
            final Set<String> languages = Collections.singleton(resource.getLocale().toString());
            final PublicationInfo publicationInfo = service.getPublicationInfo(resource.getNode().getIdentifier(), languages,
                                                                               false, false);
            if (publicationInfo.getStatus() == PublicationInfo.UNPUBLISHABLE) {
                service.publish(resource.getNode().getParent().getPath(), resource.getWorkspace(),
                                Constants.LIVE_WORKSPACE, languages, false);
            }
            boolean publishChildren = req.getParameter("publishChildren")!=null && Boolean.valueOf(req.getParameter("publishChildren"));
            service.publish(resource.getNode().getPath(), resource.getWorkspace(), Constants.LIVE_WORKSPACE, languages,
                    publishChildren);
            jcrSessionWrapper.save();
            return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return new ActionResult(HttpServletResponse.SC_BAD_REQUEST, null, new JSONObject());
        }
    }
}
