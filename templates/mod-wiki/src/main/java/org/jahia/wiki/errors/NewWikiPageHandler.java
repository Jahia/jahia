package org.jahia.wiki.errors;

import org.jahia.bin.errors.ErrorHandler;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.URLGenerator;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 2, 2009
 * Time: 4:11:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewWikiPageHandler implements ErrorHandler {

    public boolean handle(Throwable e, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
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
            JCRNodeWrapper parent = session.getNode(parentPath);
            if (parent.isNodeType("jnt:page") && parent.hasProperty("j:template") &&
                    parent.getProperty("j:template").getString().equals("wikiHome")) {

                String newName = StringUtils.substringAfterLast(path,"/");
                newName = StringUtils.substringBefore(newName,".");
                System.out.println("----->new "+ newName);

                String link = request.getContextPath() + request.getServletPath() + request.getPathInfo();
                link = StringUtils.substringBeforeLast(link,"/") + ".wikiCreate.html?link="+newName;
//                URLGenerator url = (URLGenerator) request.getAttribute("url");
//                String link = url.getBase() + parent.getPath() + ".create.html?link="+newName;
                response.sendRedirect(link);
                return true;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return false;
    }
}
