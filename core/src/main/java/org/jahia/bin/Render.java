package org.jahia.bin;

import org.apache.log4j.Logger;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.*;
import java.text.MessageFormat;

/**
 * Rendering servlet. Resolves the node and the template, and renders it by executing the appropriate script
 */
public class Render extends HttpServlet {
    private static final String METHOD_DELETE = "DELETE";
    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_TRACE = "TRACE";

    private static final String HEADER_IFMODSINCE = "If-Modified-Since";
    private static final String HEADER_LASTMOD = "Last-Modified";

    private static final String LSTRING_FILE =
            "javax.servlet.http.LocalStrings";
    private static ResourceBundle lStrings =
            ResourceBundle.getBundle(LSTRING_FILE);

    private static Logger logger = Logger.getLogger(Render.class);

    private static String renderServletPath;
    private static List<String> reservedParameters;
    public static final String NODE_TYPE = "nodeType";
    public static final String NODE_NAME = "nodeName";
    public static final String NEW_NODE_OUTPUT_FORMAT = "newNodeOutputFormat";
    public static final String STAY_ON_NODE = "stayOnNode";

    static {
        reservedParameters = new ArrayList<String>();
        reservedParameters.add(NODE_TYPE);
        reservedParameters.add(NODE_NAME);
        reservedParameters.add(NEW_NODE_OUTPUT_FORMAT);
        reservedParameters.add(STAY_ON_NODE);
    }
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
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();

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

            RenderContext renderContext = createRenderContext(req, resp);
            renderContext.setTemplateWrapper("fullpage");

            try {
                if (workspace.equals("default")) {
                    if (renderContext.isEditMode()) {
                        ctx.setOperationMode("edit");
                    } else {
                        ctx.setOperationMode("preview");
                    }
                } else if (workspace.equals("live")) {
                    ctx.setOperationMode("normal");
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
            Locale locale = LanguageCodeConverters.languageCodeToLocale(lang);
            ctx.setCurrentLocale(locale);
            ctx.getSessionState().setAttribute(ParamBean.SESSION_LOCALE, locale);

            if (method.equals(METHOD_GET)) {
                long lastModified = getLastModified(req);
//                if (lastModified == -1) {
                    // servlet doesn't support if-modified-since, no reason
                    // to go through further expensive logic
                    doGet(req, resp, renderContext, ctx, path, workspace, locale);
//                } else {
//                    long ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
//                    if (ifModifiedSince < (lastModified / 1000 * 1000)) {
//                        // If the servlet mod time is later, call doGet()
//                        // Round down to the nearest second for a proper compare
//                        // A ifModifiedSince of -1 will always be less
//                        maybeSetLastModified(resp, lastModified);
//                        doGet(req, resp, renderContext, ctx, path, workspace, locale);
//                    } else {
//                        resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
//                    }
//                }

            } else if (method.equals(METHOD_HEAD)) {
                long lastModified = getLastModified(req);
                maybeSetLastModified(resp, lastModified);
                doHead(req, resp);

            } else if (method.equals(METHOD_POST)) {
                doPost(req, resp, renderContext, ctx, path, workspace, locale);

            } else if (method.equals(METHOD_PUT)) {
                doPut(req, resp, renderContext, ctx, path, workspace, locale);

            } else if (method.equals(METHOD_DELETE)) {
                doDelete(req, resp);

            } else if (method.equals(METHOD_OPTIONS)) {
                doOptions(req, resp);

            } else if (method.equals(METHOD_TRACE)) {
                doTrace(req, resp);

            } else {
                //
                // Note that this means NO servlet supports whatever
                // method was requested, anywhere on this server.
                //

                String errMsg = lStrings.getString("http.method_not_implemented");
                Object[] errArgs = new Object[1];
                errArgs[0] = method;
                errMsg = MessageFormat.format(errMsg, errArgs);

                resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg);
            }
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

    /**
     * Sets the Last-Modified entity header field, if it has not
     * already been set and if the value is meaningful.  Called before
     * doGet, to ensure that headers are set before response data is
     * written.  A subclass might have set this header already, so we
     * check.
     */
    private void maybeSetLastModified(HttpServletResponse resp, long lastModified) {
        if (resp.containsHeader(HEADER_LASTMOD))
            return;
        if (lastModified >= 0)
            resp.setDateHeader(HEADER_LASTMOD, lastModified);
    }

    protected RenderContext createRenderContext(HttpServletRequest req, HttpServletResponse resp) {
        return new RenderContext(req, resp);
    }

    private void doGet(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext, ProcessingContext ctx, String path, String workspace, Locale locale) throws RepositoryException, IOException {
        String out = render(workspace, locale, path, ctx, renderContext);

        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(out.getBytes("UTF-8").length);

        PrintWriter writer = resp.getWriter();
        writer.print(out);
        writer.close();
    }

    private void doPut(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext, ProcessingContext ctx, String path, String workspace, Locale locale) throws RepositoryException, IOException {
        JCRSessionWrapper session = ServicesRegistry.getInstance().getJCRStoreService().getThreadSession(ctx.getUser(), workspace, locale);
        Node node = session.getNode(path);
        Set<Map.Entry> set = req.getParameterMap().entrySet();
        for (Map.Entry entry : set) {
            String key = (String) entry.getKey();
            if (!NODE_TYPE.equals(key) && !NODE_NAME.equals(key)) {
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
    }

    private void doPost(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext, ProcessingContext ctx, String path, String workspace, Locale locale) throws RepositoryException, IOException {
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
            String nodeType = req.getParameter(NODE_TYPE);
            if (nodeType == null || "".equalsIgnoreCase(nodeType.trim())) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing nodeType Property");
            }
            Node newNode;
            String nodeName = req.getParameter(NODE_NAME);
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
                if (!reservedParameters.contains(key)) {
                    String[] values = (String[]) entry.getValue();
                    newNode.setProperty(key, values[0]);
                }
            }
            url = ((JCRNodeWrapper) newNode).getPath();
            session.save();
        }
        resp.setStatus(HttpServletResponse.SC_CREATED);
        String renderedURL = null;
        String outputFormat = req.getParameter(NEW_NODE_OUTPUT_FORMAT);
        if(outputFormat==null || "".equals(outputFormat.trim())) {
            outputFormat = "html";
        }
        if(url!=null) {
            String requestedURL = req.getRequestURL().toString();
            renderedURL = requestedURL.substring(0, requestedURL.indexOf(URLEncoder.encode(path,
                                                                                           "UTF-8").replaceAll("%2F","/"))) + url + "." + outputFormat;
        }
        String stayOnPage = req.getParameter(STAY_ON_NODE);
        if(stayOnPage!=null && "".equals(stayOnPage.trim())) {
            stayOnPage = null;
        }
        if(renderedURL!=null && stayOnPage==null) {
            resp.setHeader("Location", renderedURL);
            resp.sendRedirect(renderedURL);
        } else if (stayOnPage != null){
            resp.sendRedirect(stayOnPage+"."+outputFormat);
        }
    }


    public String render(String workspace, Locale locale, String path, ProcessingContext ctx, RenderContext renderContext) throws RepositoryException, IOException {
        Resource r = resolveResource(workspace, locale, path, ctx.getUser());
        renderContext.setMainResource(r);

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
