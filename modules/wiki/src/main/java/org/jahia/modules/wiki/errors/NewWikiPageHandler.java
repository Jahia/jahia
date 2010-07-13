package org.jahia.modules.wiki.errors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.URLResolver;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Wiki page creation handler.
 * User: toto
 * Date: Dec 2, 2009
 * Time: 4:11:46 PM
 */
public class NewWikiPageHandler implements ErrorHandler {
    private static final Logger logger = Logger.getLogger(NewWikiPageHandler.class);

    public boolean handle(Throwable e, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            if (!(e instanceof PathNotFoundException)) {
                return false;
            }
            URLResolver urlResolver = new URLResolver(request.getPathInfo(), request.getServerName());

            String parentPath = StringUtils.substringBeforeLast(urlResolver.getPath(), "/");
            String newName = StringUtils.substringAfterLast(urlResolver.getPath(), "/");
            newName = StringUtils.substringBefore(newName, ".html");
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(
                    urlResolver.getWorkspace(), urlResolver.getLocale());
            try {
                JCRNodeWrapper parent = session.getNode(parentPath);
                JCRNodeWrapper parentOfType = JCRContentUtils.getParentOfType(parent, "jnt:page");
                if (parentOfType == null) {
                    return false;
                }
                NodeIterator nodeIterator = JCRContentUtils.getChildrenOfType(parentOfType, "jnt:template");
                NodeIterator iterator = JCRContentUtils.getDescendantNodes(parentOfType, "jnt:wikiPageFormCreation");
                boolean searchForExistingPages = false;
                JCRNodeWrapper pageForSearch = parentOfType;
                if (iterator.hasNext()) {
                    parentOfType = (JCRNodeWrapper) iterator.nextNode();
                    searchForExistingPages = true;
                } else {
                    parentOfType = null;
                }
                while (nodeIterator.hasNext()) {
                    JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.nextNode();
                    NodeIterator descendantNodes = JCRContentUtils.getDescendantNodes(nodeWrapper,
                                                                                      "jnt:wikiPageFormCreation");
                    if (descendantNodes.hasNext()) {
                        parentOfType = parent;
                        searchForExistingPages = false;
                        break;
                    }
                }
                if (searchForExistingPages) {
                    try {
                        JCRNodeWrapper node = pageForSearch.getNode(newName);
                        String link = request.getContextPath() + request.getServletPath() + "/" + StringUtils.substringBefore(
                                request.getPathInfo().substring(1),
                                "/") + "/" + urlResolver.getWorkspace() + "/" + urlResolver.getLocale() + node.getPath();

                        link += ".html";
                        response.sendRedirect(link);
                    } catch (PathNotFoundException e1) {
                        logger.debug("Wiki page not found ask for creation",e1);
                    }
                }
                if (null != parentOfType) {
                    String link = request.getContextPath() + request.getServletPath() + "/" + StringUtils.substringBefore(
                            request.getPathInfo().substring(1),
                            "/") + "/" + urlResolver.getWorkspace() + "/" + urlResolver.getLocale() + parentOfType.getPath();

                    link += ".html?newPageName=" + URLEncoder.encode(newName, "UTF-8");
                    response.sendRedirect(link);
                    return true;
                }
            } catch (PathNotFoundException e1) {
                return false;
            }
        } catch (Exception e1) {
            logger.error(e1, e1);
        }
        return false;
    }
}
