package org.jahia.bin;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.RenderContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Rendering servlet. Resolves the node and the template, and renders it by executing the appropriate script
 */
public class Render extends HttpServlet {
    private static Logger logger = Logger.getLogger(Render.class);

    private static String renderServletPath;

    @Override
    public void init() throws ServletException {
        super.init();
        if (getServletConfig().getInitParameter("render-servlet-path") != null) {
            renderServletPath = getServletConfig().getInitParameter("render-servlet-path");
        }
    }

    public static String getRenderServletPath() {
        return renderServletPath;
    }

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

            index = path.indexOf('/', 1);
            String lang = path.substring(1, index);
            path = path.substring(index);

            String baseUrl = StringUtils.removeEnd(req.getRequestURI(), path);
            req.setAttribute("baseUrl", baseUrl);
            req.getSession().setAttribute("baseUrl", baseUrl);

            RenderContext renderContext = new RenderContext(req, resp);
            renderContext.setTemplateWrapper("fullpage");

            String out = render(workspace, lang, path, ctx, renderContext);

            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
            resp.setContentLength(out.getBytes("UTF-8").length);

            PrintWriter writer = resp.getWriter();
            writer.print(out);
            writer.close();
        } catch (PathNotFoundException e) {
        	ErrorHandler.getInstance().handle(e, req, resp);
        } catch (Exception e) {
        	ErrorHandler.getInstance().handle(e, req, resp);
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        ProcessingContext ctx = null;

        String path = req.getPathInfo();

        try {
            ctx = Jahia.createParamBean(req, resp, req.getSession());

            int index = path.indexOf('/', 1);
            String workspace = path.substring(1, index);
            path = path.substring(index);

            index = path.indexOf('/', 1);
            String lang = path.substring(1, index);
            path = path.substring(index);
            try {
                if (workspace.equals("default")) {
                    ctx.setOperationMode("edit");
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
            Locale locale = LanguageCodeConverters.languageCodeToLocale(lang);
            ctx.setCurrentLocale(locale);
            JCRSessionWrapper session = ServicesRegistry.getInstance().getJCRStoreService().getThreadSession(ctx.getUser(), workspace, locale);
            String[] subPaths = path.split("/");
            String lastPath = subPaths[subPaths.length - 1];
            StringBuffer realPath = new StringBuffer();
            Node node = null;
            for (String subPath : subPaths) {
                if (!"".equals(subPath.trim()) && !"*".equals(subPath) && !subPath.equals(lastPath)) {
                    realPath.append("/").append(subPath);
                    try {
                        node = session.getNode(realPath.toString());
                    } catch (PathNotFoundException e) {
                        if (node != null) {
                            node = node.addNode(subPath, "jnt:folder");
                        }
                    }
                }
            }
            String url = null;
            if (node != null) {
                String nodeType = req.getParameter("nodeType");
                if (nodeType == null || "".equalsIgnoreCase(nodeType.trim())) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing nodeType Property");
                }
                Node newNode;
                String nodeName = req.getParameter("nodeName");
                if(!"*".equals(lastPath)) {
                    nodeName = lastPath;
                }
                if (nodeName == null || "".equals(nodeName.trim())) {
                    String[] strings = nodeType.split(":");
                    if(strings.length>0) {
                        nodeName = strings[1] + Math.round(Math.random()*100000);
                    } else {
                        nodeName = strings[1] + Math.round(Math.random()*100000);
                    }
                }
                try {
                    newNode = session.getNode(realPath+"/"+nodeName);
                } catch (PathNotFoundException e) {
                    newNode = node.addNode(nodeName, nodeType);
                }
                Set<Map.Entry> set = req.getParameterMap().entrySet();
                for (Map.Entry entry : set) {
                    String key = (String) entry.getKey();
                    if (!"nodeType".equals(key) && !"nodeName".equals(key)) {
                        String[] values = (String[]) entry.getValue();
                        newNode.setProperty(key, values[0]);
                    }
                }
                url = ((JCRNodeWrapper) newNode).getPath();
                session.save();
            }
            resp.setStatus(HttpServletResponse.SC_CREATED);
            if(url!=null) {
                String requestedURL = req.getRequestURL().toString();                
                resp.setHeader("Location",requestedURL.substring(0,requestedURL.indexOf(path))+url+".html");
            }
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

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        ProcessingContext ctx = null;

        String path = req.getPathInfo();

        try {
            ctx = Jahia.createParamBean(req, resp, req.getSession());

            int index = path.indexOf('/', 1);
            String workspace = path.substring(1, index);
            path = path.substring(index);

            index = path.indexOf('/', 1);
            String lang = path.substring(1, index);
            path = path.substring(index);
            try {
                if (workspace.equals("default")) {
                    ctx.setOperationMode("edit");
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
            Locale locale = LanguageCodeConverters.languageCodeToLocale(lang);
            ctx.setCurrentLocale(locale);
            JCRSessionWrapper session = ServicesRegistry.getInstance().getJCRStoreService().getThreadSession(ctx.getUser(), workspace, locale);
            Node node = session.getNode(path);
            Set<Map.Entry> set = req.getParameterMap().entrySet();
            for (Map.Entry entry : set) {
                String key = (String) entry.getKey();
                if (!"nodeType".equals(key) && !"nodeName".equals(key)) {
                    String[] values = (String[]) entry.getValue();
                    node.setProperty(key, values[0]);
                }
            }
            session.save();
            StringBuffer out = new StringBuffer("Successfully updated");

            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
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

    public String render(String workspace, String lang, String path, ProcessingContext ctx, RenderContext renderContext) throws RepositoryException, IOException {
        try {
            if (workspace.equals("default")) {
                if (renderContext.isEditMode()) {
                    ctx.setOperationMode("edit");
                } else {
                    ctx.setOperationMode("preview");
                }
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        Locale locale = LanguageCodeConverters.languageCodeToLocale(lang);
        ctx.setCurrentLocale(locale);

        Resource r = resolveResource(workspace, locale, path, ctx.getUser());
        Node current = r.getNode();
        try {
            while (true) {
                if (current.isNodeType("jnt:jahiaVirtualsite") || current.isNodeType("jnt:virtualsite")) {
                    String sitename = current.getName();
                    try {
                        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(sitename);
                        ctx.setSite(site);
                        ctx.setContentPage(site.getHomeContentPage());
                        ctx.setThePage(site.getHomePage());
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

        return RenderService.getInstance().render(r, renderContext);
    }

    /**
     * Creates a resource from the specified path.
     * <p/>
     * The path should looks like : [nodepath][.templatename].[templatetype]
     * or [nodepath].[templatetype]
     *
     * @param workspace The workspace where to get the node
     * @param path      The path of the node, in the specified workspace
     * @param user      Current user
     * @return The resource, if found
     * @throws PathNotFoundException if the resource cannot be resolved
     * @throws RepositoryException
     */
    private Resource resolveResource(String workspace, Locale locale, String path, JahiaUser user) throws RepositoryException {
        if (logger.isDebugEnabled()) {
        	logger.debug("Resolving resource for workspace '" + workspace + "' locale '" + locale + "' and path '" + path + "'");
        }
        JCRSessionWrapper session = ServicesRegistry.getInstance().getJCRStoreService().getThreadSession(user, workspace, locale);

        JCRNodeWrapper node = null;

        String ext = null;
        String tpl = null;

        while (true) {
            int i = path.lastIndexOf('.');
            if (i > path.lastIndexOf('/')) {
                if (ext == null) {
                    ext = path.substring(i + 1);
                } else if (tpl == null) {
                    tpl = path.substring(i + 1);
                } else {
                    tpl = path.substring(i + 1) + "." + tpl;
                }
                path = path.substring(0, i);
            } else {
                throw new PathNotFoundException("not found");
            }
            try {
                node = session.getNode(path);
                break;
            } catch (PathNotFoundException e) {
            }
        }
        Resource r = new Resource(node, ext, null, tpl);
        if (logger.isDebugEnabled()) {
        	logger.debug("Resolved resource: " + r);
        }
        return r;
    }


}
