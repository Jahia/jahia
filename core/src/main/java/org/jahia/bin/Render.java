/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.jcr.*;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Rendering controller. Resolves the node and the template, and renders it by executing the appropriate script.
 */
public class Render extends HttpServlet implements Controller, ServletConfigAware {
    protected static final String METHOD_DELETE = "DELETE";
    protected static final String METHOD_HEAD = "HEAD";
    protected static final String METHOD_GET = "GET";
    protected static final String METHOD_OPTIONS = "OPTIONS";
    protected static final String METHOD_POST = "POST";
    protected static final String METHOD_PUT = "PUT";
    protected static final String METHOD_TRACE = "TRACE";

    protected static final String HEADER_IFMODSINCE = "If-Modified-Since";
    protected static final String HEADER_LASTMOD = "Last-Modified";

    protected static final Set<String> reservedParameters;

    private static Logger logger = Logger.getLogger(Render.class);

    public static final String NODE_TYPE = "nodeType";
    public static final String NODE_NAME = "nodeName";
    public static final String NEW_NODE_OUTPUT_FORMAT = "newNodeOutputFormat";
    public static final String STAY_ON_NODE = "stayOnNode";
    public static final String METHOD_TO_CALL = "methodToCall";

    private MetricsLoggingService loggingService;

    static {
        reservedParameters = new HashSet<String>();
        reservedParameters.add(NODE_TYPE);
        reservedParameters.add(NODE_NAME);
        reservedParameters.add(NEW_NODE_OUTPUT_FORMAT);
        reservedParameters.add(STAY_ON_NODE);
        reservedParameters.add(METHOD_TO_CALL);
    }

    private transient ServletConfig servletConfig;

    /**
     * Returns the time the <code>HttpServletRequest</code>
     * object was last modified,
     * in milliseconds since midnight January 1, 1970 GMT.
     * If the time is unknown, this method returns a negative
     * number (the default).
     *
     * <p>Servlets that support HTTP GET requests and can quickly determine
     * their last modification time should override this method.
     * This makes browser and proxy caches work more effectively,
     * reducing the load on server and network resources.
     *
     * @return a <code>long</code> integer specifying
     * the time the <code>HttpServletRequest</code> object was
     * last modified, in milliseconds since midnight, January 1,
     * 1970 GMT, or -1 if the time is not known
     */
    protected long getLastModified(Resource resource, RenderContext renderContext) throws RepositoryException, IOException {
//        Node node = resource.getNode();
//        if (node.hasProperty("jcr:lastModified")) {
//            return node.getProperty("jcr:lastModified").getDate().getTime().getTime();
//        }
        return -1;
    }

    /**
     * Sets the Last-Modified entity header field, if it has not
     * already been set and if the value is meaningful.  Called before
     * doGet, to ensure that headers are set before response data is
     * written.  A subclass might have set this header already, so we
     * check.
     */
    protected void maybeSetLastModified(HttpServletResponse resp, long lastModified) {
        if (resp.containsHeader(HEADER_LASTMOD))
            return;
        if (lastModified >= 0)
            resp.setDateHeader(HEADER_LASTMOD, lastModified);
    }

    protected RenderContext createRenderContext(HttpServletRequest req, HttpServletResponse resp, JahiaUser user) {
        return new RenderContext(req, resp, user);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext, Resource resource) throws RepositoryException, RenderException, IOException {
        loggingService.startProfiler("MAIN");
        String out = RenderService.getInstance().render(resource, renderContext);
        resp.setContentType(renderContext.getContentType() != null ? renderContext.getContentType() : "text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(out.getBytes("UTF-8").length);
        if (renderContext.isEditMode()) {
            resp.setHeader("Pragma", "no-cache");
        }

        PrintWriter writer = resp.getWriter();
        writer.print(out);
        writer.close();
        String sessionID = "";
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            sessionID = httpSession.getId();
        }
        loggingService.stopProfiler("MAIN");
        loggingService.logContentEvent(renderContext.getUser().getName(), req.getRemoteAddr(),sessionID, resource.getNode().getPath(),resource.getNode().getPrimaryNodeType().getName(),"pageViewed",req.getHeader("User-Agent"),req.getHeader("Referer"));
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext, String path, String workspace, Locale locale) throws RepositoryException, IOException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale);
        JCRNodeWrapper node = session.getNode(path);
        session.checkout(node);
        Set<Map.Entry<String, String[]>> set = req.getParameterMap().entrySet();
        for (Map.Entry<String, String[]> entry : set) {
            String key = entry.getKey();
            if (!reservedParameters.contains(key)) {
                String[] values = entry.getValue();
                final ExtendedPropertyDefinition propertyDefinition = ((JCRNodeWrapper) node).getApplicablePropertyDefinition(
                        key);
                if (propertyDefinition.isMultiple()) {
                    node.setProperty(key, values);
                } else if (propertyDefinition.getRequiredType()==PropertyType.DATE) {
                    // Expecting ISO date yyyy-MM-dd'T'HH:mm:ss
                    try {
                        final Date date = new SimpleDateFormat(DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()).parse(
                                values[0]);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        node.setProperty(key,calendar);
                    } catch (ParseException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
	                node.setProperty(key, values[0]);
                }
            }
        }
        session.save();
        final String requestWith = req.getHeader("x-requested-with");
        if(req.getHeader("accept").contains("application/json") && requestWith !=null && requestWith.equals("XMLHttpRequest")) {
            try {
                serializeNodeToJSON(resp, node);
            } catch (JSONException e) {
                logger.error(e.getMessage(),e);
            }
        } else {
            performRedirect(null, null, req, resp);
        }
        String sessionID = "";
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            sessionID = httpSession.getId();
        }
        loggingService.logContentEvent(renderContext.getUser().getName(), req.getRemoteAddr(),sessionID, path,node.getPrimaryNodeType().getName(),"nodeUpdated",
                                           new JSONObject(req.getParameterMap()).toString());
    }

    private void serializeNodeToJSON(HttpServletResponse resp, JCRNodeWrapper node) throws RepositoryException, IOException, JSONException {
        final Map<String, String> stringMap = node.getPropertiesAsString();
        Map<String,String > map = new HashMap<String, String>(stringMap.size());
        for (Map.Entry<String, String> stringStringEntry : stringMap.entrySet()) {
            map.put(stringStringEntry.getKey().replace(":","_"),stringStringEntry.getValue());
        }
        JSONObject nodeJSON = new JSONObject(map);
        nodeJSON.write(resp.getWriter());
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext, String path, String workspace, Locale locale) throws RepositoryException, IOException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale);
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
                return;
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
            Set<Map.Entry<String, String[]>> set = req.getParameterMap().entrySet();
            for (Map.Entry<String, String[]> entry : set) {
                String key = entry.getKey();
                if (!reservedParameters.contains(key)) {
                    String[] values = entry.getValue();
                    if (((JCRNodeWrapper)newNode).getApplicablePropertyDefinition(key).isMultiple()) {
                    	newNode.setProperty(key, values);
                    } else {
                        newNode.setProperty(key, values[0]);
                    }
                }
            }
            url = ((JCRNodeWrapper) newNode).getPath();
            session.save();
        }
        resp.setStatus(HttpServletResponse.SC_CREATED);
        performRedirect(url, path, req, resp);
        String sessionID = "";
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            sessionID = httpSession.getId();
        }
        loggingService.logContentEvent(renderContext.getUser().getName(), req.getRemoteAddr(),sessionID, path,req.getParameter(NODE_TYPE),"nodeCreated",
                                       new JSONObject(req.getParameterMap()).toString());
    }

	protected void doDelete(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext, String path, String workspace, Locale locale) throws RepositoryException, IOException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale);
        Node node = session.getNode(path);
        Node parent = node.getParent();
        node.remove();
        session.save();
        String url = parent.getPath();
        session.save();
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        performRedirect(url, path, req, resp);
    }

	protected void performRedirect(String url, String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

    /**
     * Creates a resource from the specified path.
     * <p/>
     * The path should looks like : [nodepath][.templatename].[templatetype]
     * or [nodepath].[templatetype]
     *
     * @param workspace The workspace where to get the node
     * @param locale    current locale
     * @param path      The path of the node, in the specified workspace
     * @param ctx       request context
     * @return The resource, if found
     * @throws PathNotFoundException if the resource cannot be resolved
     * @throws RepositoryException
     */
	protected Resource resolveResource(String workspace, Locale locale, String path, ProcessingContext ctx) throws RepositoryException {
        if (logger.isDebugEnabled()) {
        	logger.debug("Resolving resource for workspace '" + workspace + "' locale '" + locale + "' and path '" + path + "'");
        }
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale);

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
                try {
                    // node unreadable ?
                    final String p = path;
                    JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            return session.getNode(p);
                        }
                    }, null, workspace);
                    throw new AccessDeniedException(path);
                } catch (PathNotFoundException e1) {
                    // continue
                }
            }
        }
        Resource r = new Resource(node, ext, null, tpl);
        if (logger.isDebugEnabled()) {
        	logger.debug("Resolved resource: " + r);
        }

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
                        ctx.getSessionState().setAttribute(ProcessingContext.SESSION_SITE, site);
                        ctx.getSessionState().setAttribute(ProcessingContext.SESSION_LAST_REQUESTED_PAGE_ID, site.getHomePageID());
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


        return r;
    }

	public ModelAndView handleRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String method = req.getMethod();
        if(req.getParameter(METHOD_TO_CALL)!=null) {
            method = req.getParameter(METHOD_TO_CALL).toUpperCase();
        }
        long startTime = System.currentTimeMillis();

        ProcessingContext paramBean = null;

        String path = req.getPathInfo();

        try {
            Object old = req.getSession(true).getAttribute(ParamBean.SESSION_SITE);
            paramBean = Jahia.createParamBean(req, resp, req.getSession());
            req.getSession(true).setAttribute(ParamBean.SESSION_SITE, old);
            
            JahiaData jData = new JahiaData(paramBean, false);
            paramBean.setAttribute(JahiaData.JAHIA_DATA, jData);

            path = path.substring(path.indexOf('/', 1));

            int index = path.indexOf('/', 1);
            String workspace = path.substring(1, index);
            path = path.substring(index);

            index = path.indexOf('/', 1);
            String lang = path.substring(1, index);
            path = path.substring(index);

            RenderContext renderContext = createRenderContext(req, resp, paramBean.getUser());
            if ("live".equals(workspace) && renderContext.isEditMode()) {
                throw new AccessDeniedException();
            }


            try {
                if (workspace.equals("default")) {
                    if (renderContext.isEditMode()) {
                        paramBean.setOperationMode("edit");
                    } else {
                        paramBean.setOperationMode("preview");
                    }
                } else if (workspace.equals("live")) {
                    paramBean.setOperationMode("normal");
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
            Locale locale = LanguageCodeConverters.languageCodeToLocale(lang);
            paramBean.setCurrentLocale(locale);
            paramBean.getSessionState().setAttribute(ParamBean.SESSION_LOCALE, locale);

            if (method.equals(METHOD_GET)) {
                Resource resource = resolveResource(workspace, locale, path, paramBean);
                renderContext.setMainResource(resource);
                renderContext.setSite(paramBean.getSite());
                renderContext.setSiteNode(JCRSessionFactory.getInstance().getCurrentUserSession(workspace,locale).getNode("/sites/" + paramBean.getSite().getSiteKey()));
                if (!"true".equals(req.getParameter("ajaxcall"))) {
                    if (renderContext.isEditMode()) {
                        renderContext.setIncludeSubModules(false);
                    }
                    resource.pushWrapper("wrapper.fullpage");
                    resource.pushWrapper("wrapper.bodywrapper");
                } else {
                    renderContext.getModuleParams().put("forcedRenderOptions", "none");
                    renderContext.getModuleParams().put("forcedSkin", "none");
                }

                long lastModified = getLastModified(resource, renderContext);

                if (lastModified == -1) {
                    // servlet doesn't support if-modified-since, no reason
                    // to go through further expensive logic
                    doGet(req, resp, renderContext, resource);
                } else {
                    long ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
                    if (ifModifiedSince < (lastModified / 1000 * 1000)) {
                        // If the servlet mod time is later, call doGet()
                        // Round down to the nearest second for a proper compare
                        // A ifModifiedSince of -1 will always be less
                        maybeSetLastModified(resp, lastModified);
                        doGet(req, resp, renderContext, resource);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    }
                }
            } else if (method.equals(METHOD_HEAD)) {
                doHead(req, resp);

            } else if (method.equals(METHOD_POST)) {
                doPost(req, resp, renderContext, path, workspace, locale);

            } else if (method.equals(METHOD_PUT)) {
                doPut(req, resp, renderContext, path, workspace, locale);

            } else if (method.equals(METHOD_DELETE)) {
                doDelete(req, resp,renderContext, path, workspace, locale);

            } else if (method.equals(METHOD_OPTIONS)) {
                doOptions(req, resp);

            } else if (method.equals(METHOD_TRACE)) {
                doTrace(req, resp);

            } else {
                //
                // Note that this means NO servlet supports whatever
                // method was requested, anywhere on this server.
                //
                resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            }
        } catch (Exception e) {
            List<ErrorHandler> handlers = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getErrorHandler();
            for (ErrorHandler handler : handlers) {
                if (handler.handle(e, req, resp)) {
                    return null;
                }
            }
            DefaultErrorHandler.getInstance().handle(e, req, resp);
        } finally {
            if (logger.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("Rendered [").append(req.getRequestURI());
                if (paramBean != null && paramBean.getUser() != null) {
                    sb.append("] user=[").append(paramBean.getUser().getUsername());
                }
                sb.append("] ip=[").append(req.getRemoteAddr()).append(
                        "] sessionID=[").append(req.getSession(true).getId())
                        .append("] in [").append(
                        System.currentTimeMillis() - startTime).append(
                        "ms]");
                logger.info(sb.toString());
            }
        }
	    return null;
    }

	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
    }

	@Override
    public ServletConfig getServletConfig() {
	    return servletConfig;
    }

	@Override
    public String getServletName() {
	    return getServletConfig().getServletName();
    }

	public static String getRenderServletPath() {
	    // TODO move this into configuration
	    return "/cms/render";
    }

    public void setLoggingService(MetricsLoggingService loggingService) {
        this.loggingService = loggingService;
    }
}
