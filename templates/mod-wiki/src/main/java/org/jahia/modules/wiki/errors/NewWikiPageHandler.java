package org.jahia.modules.wiki.errors;

import org.jahia.bin.errors.ErrorHandler;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRNodeWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.jcr.PathNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.net.URLEncoder;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 2, 2009
 * Time: 4:11:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewWikiPageHandler implements ErrorHandler {
    private static final Logger logger = Logger.getLogger(NewWikiPageHandler.class);

    public boolean handle(Throwable e, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (!(e instanceof PathNotFoundException)) {
                return false;
            }
            String path = request.getPathInfo();
            path = path.substring(path.indexOf('/', 1));
            int index = path.indexOf('/', 1);
            String workspace = path.substring(1, index);
            path = path.substring(index);

            index = path.indexOf('/', 1);
            String lang = path.substring(1, index);
            path = path.substring(index);

            String parentPath = StringUtils.substringBeforeLast(path,"/");

            Locale locale = LanguageCodeConverters.languageCodeToLocale(lang);
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale);
            try {
                JCRNodeWrapper parent = session.getNode(parentPath);
                if (parent.isNodeType("jnt:page") && parent.hasProperty("j:template") &&
                        parent.getProperty("j:template").getString().equals("wikiHome")) {
                    String newName = StringUtils.substringAfterLast(path,"/");
                    newName = StringUtils.substringBefore(newName,".html");
                    String link = request.getContextPath() + request.getServletPath() + request.getPathInfo();

                    link = StringUtils.substringBeforeLast(link,"/") + ".wikiCreate.html?newPageName="+URLEncoder.encode(newName, "UTF-8");
                    response.sendRedirect(link);
                    return true;
                }
            } catch (PathNotFoundException e1) {
                return false;
            }
        } catch (Exception e1) {
            logger.error(e1,e1);
        }
        return false;
    }
}
