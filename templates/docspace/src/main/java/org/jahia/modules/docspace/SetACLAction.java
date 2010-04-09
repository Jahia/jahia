package org.jahia.modules.docspace;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.helper.ContentManagerHelper;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapperImpl;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.rules.User;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Mar 23, 2010
 * Time: 3:18:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class SetACLAction implements org.jahia.bin.Action  {
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
        JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(resource.getWorkspace(), resource.getLocale());
        String user = "u:" + req.getParameter("user");
        String acl = req.getParameter("acl");
        try {
            JCRNodeWrapperImpl.changePermissions(resource.getNode(), user, acl);
            jcrSessionWrapper.save();
            return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return new ActionResult(HttpServletResponse.SC_BAD_REQUEST, null, new JSONObject());
        }
    }
}
