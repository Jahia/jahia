package org.jahia.modules.portal;

import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.helper.ContentManagerHelper;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Feb 11, 2010
 * Time: 6:09:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoveOnTopAction implements org.jahia.bin.Action {
    private String name;
    private ContentManagerHelper contentManager;

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void doExecute(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext, Resource resource) throws Exception {
        name = req.getParameter("name");
        String sourcePath = req.getParameter("source");
        String targetPath = req.getParameter("target");
        String position = req.getParameter("position");
        if ("after".equals(position)) {
            contentManager.moveOnTopOf(null,sourcePath,targetPath, JCRSessionFactory.getInstance().getCurrentUserSession(resource.getWorkspace(), resource.getLocale()));
        }
        else {
            contentManager.moveAtEnd(sourcePath,targetPath, JCRSessionFactory.getInstance().getCurrentUserSession(resource.getWorkspace(), resource.getLocale()));            
        }
    }
}
