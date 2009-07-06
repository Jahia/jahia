package org.jahia.bin;

import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.sites.JahiaSite;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Node;
import javax.jcr.ItemNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Rendering servlet. Resolves the node and the template, and renders it by executing the appropriate script
 *
 */
public class Render extends HttpServlet {
    private static Logger logger = Logger.getLogger(Render.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        
        ProcessingContext ctx = null;
        
        String path = req.getPathInfo();

        try {
            ctx = Jahia.createParamBean(req, resp, req.getSession());

            int index = path.indexOf('/', 1);
            String workspace = path.substring(1, index);
            path = path.substring(index);

            StringBuffer out = render(workspace, path, ctx, req, resp);

            resp.setContentType("text/html");
            resp.setContentLength(out.length());

            PrintWriter writer = resp.getWriter();
            writer.print(out.toString());
            writer.close();
        } catch (PathNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServletException(e);
        } finally {
            if (logger.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("Rendered [").append(req.getRequestURI());
                if (ctx != null && ctx.getUser() != null) {
                    sb.append("] user=[").append(ctx.getUser().getUsername());
                }
                sb.append("] ip=[").append(req.getRemoteAddr()).append(
                        "] sessionID=[").append(req.getSession(true).getId())
                        .append("] in [").append(
                                System.currentTimeMillis() - startTime).append(
                                "ms]");

                logger.info(sb.toString());
            }
            
        }

    }

    public StringBuffer render(String workspace, String path, ProcessingContext ctx, HttpServletRequest request, HttpServletResponse response) throws RepositoryException, IOException {
        try {
            if (workspace.equals("default")) {
                ctx.setOperationMode("edit");
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        Resource r = resolveResource(workspace, path, ctx.getUser());
        Node current = r.getNode();
        try {
            while (true) {
                if (current.isNodeType("jnt:jahiaVirtualsite") || current.isNodeType("jnt:virtualsite")) {
                    String sitename = current.getName();
                    try {
                        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(sitename);
                        ctx.setSite(site);
                    } catch (JahiaException e) {
                        logger.error(e.getMessage(), e);
                    }
                    break;
                }
                current = current.getParent();
            }
        } catch (ItemNotFoundException e) {
            // no site
        }

        return RenderService.getInstance().render(r, request, response);
    }

    /**
     * Creates a resource from the specified path.
     *
     * The path should looks like : [nodepath][.templatename].[templatetype]
     * or [nodepath].[templatetype]
     *
     *
     *
     * @param workspace The workspace where to get the node
     * @param path The path of the node, in the specified workspace
     * @param user Current user
     * @return The resource, if found
     *
     * @throws PathNotFoundException if the resource cannot be resolved
     * @throws RepositoryException
     */
    private Resource resolveResource(String workspace, String path, JahiaUser user) throws RepositoryException {
        JCRSessionWrapper session = ServicesRegistry.getInstance().getJCRStoreService().getThreadSession(user, workspace);

        JCRNodeWrapper node = null;

        String ext = null;
        String tpl = null;

        while (true) {
            int i = path.lastIndexOf('.');
            if (i > path.lastIndexOf('/')) {
                if (ext == null) {
                    ext = path.substring(i+1);
                } else {
                    tpl = path.substring(i+1);
                }
                path = path.substring(0,i);
            } else {
                throw new PathNotFoundException("not found");
            }
            try {
                node = session.getNode(path);
                break;
            } catch (PathNotFoundException e) {
            }
        }
        Resource r = new Resource(node, ext, tpl);
        return r;
    }


}
