package org.jahia.wiki.action;

import org.jahia.bin.Action;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 3, 2009
 * Time: 2:23:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewPageAction implements Action {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void doExecute(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext, Resource resource) throws Exception {
        JCRNodeWrapper node = resource.getNode();
        String linkName = req.getParameter("link");

        JCRNodeWrapper page = node.addNode(linkName,"jnt:page");
        page.setProperty("jcr:title", linkName);
        page.setProperty("j:template", "wiki");

        String content = req.getParameter("content");
        JCRNodeWrapper contentNode = page.addNode("content", "jnt:wikiContent");
        contentNode.setProperty("text", content);

        node.getSession().save();
        URLGenerator url = new URLGenerator(renderContext,  resource, JCRStoreService.getInstance());
        resp.sendRedirect(url.getBase() + page.getPath() + ".html");

//        String out = RenderService.getInstance().render(new Resource(resource.getNode(), "html", null,null), renderContext);
//
//        resp.setContentType(renderContext.getContentType() != null ? renderContext.getContentType() : "text/html;charset=UTF-8");
////        resp.setCharacterEncoding("UTF-8");
//        resp.setContentLength(out.getBytes("UTF-8").length);
//        if (renderContext.isEditMode()) {
//            resp.setHeader("Pragma", "no-cache");
//        }
//
//        PrintWriter writer = resp.getWriter();
//        writer.print(out);
//        writer.close();
    }
}
