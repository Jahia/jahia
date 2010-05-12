package org.jahia.modules.bookmarks;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.helper.ContentManagerHelper;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static org.jahia.api.Constants.JAHIANT_TASKS;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: May 12, 2010
 * Time: 4:06:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddAction implements Action {
    private String name;
    private ContentManagerHelper contentManager;
    final String bookmarkPath = "bookmarks";
    public String getName() {
        return name;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        // test if bookmark node is present
        JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(resource.getWorkspace(), resource.getLocale());
        JCRNodeWrapper userBookmarks = null;
        try {
            userBookmarks = jcrSessionWrapper.getNode("/users/" + renderContext.getUser().getName() + "/" + bookmarkPath);
        } catch (PathNotFoundException pnf) {
            userBookmarks =  contentManager.addNode(jcrSessionWrapper.getNode("/users/" + renderContext.getUser().getName()), bookmarkPath, "jnt:bookmarks", null, null);
            userBookmarks.saveSession();
        }
        if (userBookmarks != null &&  !contentManager.checkExistence(userBookmarks.getPath() + "/" + req.getParameter("jcr:title").replace(" ","-"), jcrSessionWrapper)) {
            JCRNodeWrapper bookmark = contentManager.addNode(userBookmarks, req.getParameter("jcr:title").replace(" ","-"), "jnt:bookmark", null, null);
            bookmark.setProperty("date", new GregorianCalendar());
            if (req.getParameter("url") != null) { bookmark.setProperty("url", req.getParameter("url")); }
            bookmark.setProperty("jcr:title", req.getParameter("jcr:title"));
            bookmark.saveSession();
        }
        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject());
    }
}
