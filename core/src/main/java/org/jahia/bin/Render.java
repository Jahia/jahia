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
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.bin.errors.ErrorHandler;
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
import org.jahia.services.render.URLResolver;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.tools.files.FileUpload;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Rendering controller. Resolves the node and the template, and renders it by executing the appropriate script.
 */
public class Render extends HttpServlet implements Controller,
        ServletConfigAware {
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

    // Here we define the constants for the reserved keywords for post methods
    public static final String NODE_TYPE = "nodeType";
    public static final String NODE_NAME = "nodeName";
    public static final String NEW_NODE_OUTPUT_FORMAT = "newNodeOutputFormat";
    public static final String REDIRECT_TO = "redirectTo";
    public static final String METHOD_TO_CALL = "methodToCall";
    public static final String AUTO_CHECKIN = "autoCheckin";
    public static final String CAPTCHA = "captcha";
    public static final String TARGETDIRECTORY = "targetDirectory";
    public static final String NORMALIZE_NODE_NAME = "normalizeNodeName";

    private MetricsLoggingService loggingService;
    private JahiaTemplateManagerService templateService;

    static {
        reservedParameters = new HashSet<String>();
        reservedParameters.add(NODE_TYPE);
        reservedParameters.add(NODE_NAME);
        reservedParameters.add(NEW_NODE_OUTPUT_FORMAT);
        reservedParameters.add(REDIRECT_TO);
        reservedParameters.add(METHOD_TO_CALL);
        reservedParameters.add(AUTO_CHECKIN);
        reservedParameters.add(CAPTCHA);
        reservedParameters.add(TARGETDIRECTORY);
        reservedParameters.add(Constants.JCR_MIXINTYPES);
        reservedParameters.add(NORMALIZE_NODE_NAME);
    }

    private transient ServletConfig servletConfig;

    /**
     * Returns the time the <code>HttpServletRequest</code> object was last modified, in milliseconds since midnight January 1, 1970 GMT. If
     * the time is unknown, this method returns a negative number (the default).
     * 
     * <p>
     * Servlets that support HTTP GET requests and can quickly determine their last modification time should override this method. This
     * makes browser and proxy caches work more effectively, reducing the load on server and network resources.
     * 
     * @return a <code>long</code> integer specifying the time the <code>HttpServletRequest</code> object was last modified, in milliseconds
     *         since midnight, January 1, 1970 GMT, or -1 if the time is not known
     */
    protected long getLastModified(Resource resource,
            RenderContext renderContext) throws RepositoryException,
            IOException {
        // Node node = resource.getNode();
        // if (node.hasProperty("jcr:lastModified")) {
        // return node.getProperty("jcr:lastModified").getDate().getTime().getTime();
        // }
        return -1;
    }

    /**
     * Sets the Last-Modified entity header field, if it has not already been set and if the value is meaningful. Called before doGet, to
     * ensure that headers are set before response data is written. A subclass might have set this header already, so we check.
     */
    protected void maybeSetLastModified(HttpServletResponse resp,
            long lastModified) {
        if (resp.containsHeader(HEADER_LASTMOD))
            return;
        if (lastModified >= 0)
            resp.setDateHeader(HEADER_LASTMOD, lastModified);
    }

    protected RenderContext createRenderContext(HttpServletRequest req,
            HttpServletResponse resp, JahiaUser user) {
        return new RenderContext(req, resp, user);
    }

    protected Date getVersionDate(HttpServletRequest req){
        // we assume here that the date has been passed as milliseconds.
        String msString = req.getParameter("v");
        try {
            long msLong = Long.parseLong(msString);
            return new Date(msLong);
        } catch (NumberFormatException nfe) {
            logger.warn("Invalid version date found in URL " + msString);
            return null;
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp,
            RenderContext renderContext, Resource resource)
            throws RepositoryException, RenderException, IOException {
        loggingService.startProfiler("MAIN");
        String out = RenderService.getInstance()
                .render(resource, renderContext);
        resp.setContentType(renderContext.getContentType() != null ? renderContext
                .getContentType() : "text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(out.getBytes("UTF-8").length);

        PrintWriter writer = resp.getWriter();
        writer.print(out);
        writer.close();
        String sessionID = "";
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            sessionID = httpSession.getId();
        }
        loggingService.stopProfiler("MAIN");
        loggingService.logContentEvent(renderContext.getUser().getName(), req
                .getRemoteAddr(), sessionID, resource.getNode().getPath(),
                resource.getNode().getPrimaryNodeType().getName(),
                "pageViewed", req.getHeader("User-Agent"), req
                        .getHeader("Referer"));
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp,
            RenderContext renderContext, URLResolver urlResolver)
            throws RepositoryException, IOException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(urlResolver.getWorkspace(),
                        urlResolver.getLocale());
        JCRNodeWrapper node = session.getNode(urlResolver.getPath());
        session.checkout(node);
        Set<Map.Entry<String, String[]>> set = req.getParameterMap().entrySet();
        for (Map.Entry<String, String[]> entry : set) {
            String key = entry.getKey();
            if (!reservedParameters.contains(key)) {
                String[] values = entry.getValue();
                final ExtendedPropertyDefinition propertyDefinition = ((JCRNodeWrapper) node)
                        .getApplicablePropertyDefinition(key);
                if (propertyDefinition.isMultiple()) {
                    node.setProperty(key, values);
                } else if (propertyDefinition.getRequiredType() == PropertyType.DATE) {
                    // Expecting ISO date yyyy-MM-dd'T'HH:mm:ss
                    DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(values[0]);
                    node.setProperty(key,dateTime.toCalendar(Locale.ENGLISH));
                } else {
                    node.setProperty(key, values[0]);
                }
            }
        }
        session.save();
        if (req.getParameter(AUTO_CHECKIN) != null
                && req.getParameter(AUTO_CHECKIN).length() > 0) {
            node.checkin();
        }
        final String requestWith = req.getHeader("x-requested-with");
        if (req.getHeader("accept").contains("application/json")
                && requestWith != null && requestWith.equals("XMLHttpRequest")) {
            try {
                serializeNodeToJSON(resp, node);
            } catch (JSONException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            performRedirect(null, null, req, resp,toParameterMapOfListOfString(req));
        }
        String sessionID = "";
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            sessionID = httpSession.getId();
        }
        loggingService.logContentEvent(renderContext.getUser().getName(), req
                .getRemoteAddr(), sessionID, urlResolver.getPath(), node.getPrimaryNodeType()
                .getName(), "nodeUpdated",
                new JSONObject(req.getParameterMap()).toString());
    }

    public static void serializeNodeToJSON(HttpServletResponse resp,
            JCRNodeWrapper node) throws RepositoryException, IOException,
            JSONException {
        final PropertyIterator stringMap = node.getProperties();
        Map<String, String> map = new HashMap<String, String>();
        while (stringMap.hasNext()) {
            JCRPropertyWrapper propertyWrapper = (JCRPropertyWrapper) stringMap
                    .next();
            final int type = propertyWrapper.getType();
            final String name = propertyWrapper.getName().replace(":", "_");
            if (type == PropertyType.WEAKREFERENCE
                    || type == PropertyType.REFERENCE) {
                if (!propertyWrapper.isMultiple()) {
                    map.put(name, ((JCRNodeWrapper) propertyWrapper.getNode())
                            .getWebdavUrl());
                }
            } else {
                if (!propertyWrapper.isMultiple()) {
                    map.put(name, propertyWrapper.getValue().getString());
                }
            }
        }
        JSONObject nodeJSON = new JSONObject(map);
        nodeJSON.write(resp.getWriter());
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp,
            RenderContext renderContext, URLResolver urlResolver)
            throws Exception {
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        if (checkForUploadedFiles(resp, urlResolver.getWorkspace(), urlResolver
                .getLocale(), parameters)) {
            if (parameters.isEmpty()) {
                return;
            }
        }
        if (parameters.isEmpty()) {
            parameters = toParameterMapOfListOfString(req);
        }
        String kaptchaExpected = (String) req.getSession().getAttribute(
                com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);
        String kaptchaReceived = parameters.get(CAPTCHA)!=null?parameters.get(CAPTCHA).get(0):null;
        req.getSession().removeAttribute("formDatas");
        req.getSession().removeAttribute("formError");
        if (kaptchaExpected!= null && (kaptchaReceived == null || !kaptchaReceived.equalsIgnoreCase(kaptchaExpected))) {
            Map<String, String[]> formDatas = new HashMap<String, String[]>();
            Set<Map.Entry<String, List<String>>> set = parameters.entrySet();
            for (Map.Entry<String,List<String>> entry : set) {
                formDatas.put(entry.getKey(),entry.getValue().toArray(new String[entry.getValue().size()]));
            }
            req.getSession().setAttribute("formDatas",formDatas);
            req.getSession().setAttribute("formError","Your captcha is invalid");
            performRedirect("",urlResolver.getPath(),req, resp, parameters);
            return;
        }
        if (urlResolver.getPath().endsWith(".do")) {
            Resource resource = urlResolver.getResource(getVersionDate(req));
            renderContext.setMainResource(resource);
            renderContext.setSite(Jahia.getThreadParamBean().getSite());
            renderContext.setSiteNode(JCRSessionFactory.getInstance()
                    .getCurrentUserSession(urlResolver.getWorkspace(),
                            urlResolver.getLocale()).getNode(
                            "/sites/"
                                    + Jahia.getThreadParamBean().getSite()
                                            .getSiteKey()));

            Action action = templateService.getActions().get(
                    resource.getResolvedTemplate());
            if (action == null) {
                resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            } else {
                action.doExecute(req, resp, renderContext, resource,parameters,urlResolver);
            }
            return;
        }
        JCRNodeWrapper newNode;
        Action defaultPostActionResult = new DefaultPostActionResult();
        defaultPostActionResult.doExecute(req, resp, renderContext, null, parameters, urlResolver);
        newNode = defaultPostActionResult.getNewNode();
        if (newNode != null) {
            String url = newNode.getPath();
            resp.setStatus(HttpServletResponse.SC_CREATED);
            final String requestWith = req.getHeader("x-requested-with");
            if (req.getHeader("accept").contains("application/json") && requestWith != null && requestWith.equals(
                    "XMLHttpRequest")) {
                try {
                    serializeNodeToJSON(resp, newNode);
                } catch (JSONException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                performRedirect(url, urlResolver.getPath(), req, resp, parameters);
            }
        }
        String sessionID = "";
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            sessionID = httpSession.getId();
        }
        loggingService.logContentEvent(renderContext.getUser().getName(), req
                .getRemoteAddr(), sessionID, urlResolver.getPath(), (String) ((List) parameters.get(NODE_TYPE)).get(0), "nodeCreated", new JSONObject(parameters).toString());
    }

    private Map<String, List<String>> toParameterMapOfListOfString(HttpServletRequest req) {
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        for (Object key : req.getParameterMap().keySet()) {
            if (key != null) {
                parameters.put((String) key, new ArrayList(Arrays.asList((String[]) req.getParameterMap().get(key))));
            }
        }
        return parameters;
    }

    private boolean checkForUploadedFiles(HttpServletResponse resp, String workspace, Locale locale,
                                          Map<String, List<String>> parameters) throws RepositoryException, IOException {
        final ParamBean paramBean = (ParamBean) Jahia.getThreadParamBean();
        final FileUpload fileUpload = paramBean.getFileUpload();
        if (fileUpload != null && fileUpload.getFileItems() != null && fileUpload.getFileItems().size() > 0) {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale);
            JCRNodeWrapper userDirectory = ((JCRUser) paramBean.getUser()).getNode(session);
            String target = userDirectory.getPath() + "/files";
            boolean isTargetDirectoryDefined = fileUpload.getParameterNames().contains(TARGETDIRECTORY);
            final String requestWith = paramBean.getRealRequest().getHeader("x-requested-with");
            boolean isAjaxRequest = paramBean.getRealRequest().getHeader("accept").contains(
                    "application/json") && requestWith != null && requestWith.equals("XMLHttpRequest") || fileUpload.getParameterMap().isEmpty();
            if (isTargetDirectoryDefined) {
                target = (fileUpload.getParameterValues(TARGETDIRECTORY))[0];
            }
            final JCRNodeWrapper targetDirectory = session.getNode(target);            
            List<String> uuids = new ArrayList<String>();
            List<String> files = new ArrayList<String>();

            // If target directory is defined or if it is an ajax request then save the file now
            // otherwise we delay the save of the file to the node creation
            if (isTargetDirectoryDefined || isAjaxRequest) {
                final Map<String, DiskFileItem> stringDiskFileItemMap = fileUpload.getFileItems();
                for (Map.Entry<String, DiskFileItem> itemEntry : stringDiskFileItemMap.entrySet()) {
                    final JCRNodeWrapper wrapper = targetDirectory.uploadFile(itemEntry.getValue().getName(),
                                                                              itemEntry.getValue().getInputStream(),
                                                                              itemEntry.getValue().getContentType());
                    uuids.add(wrapper.getIdentifier());
                    files.add(itemEntry.getValue().getName());
                }
                fileUpload.markFilesAsConsumed();
                session.save();
            }

            if (!isAjaxRequest) {
                parameters.putAll(fileUpload.getParameterMap());
                if(isTargetDirectoryDefined)
                    parameters.put(NODE_NAME, files);
                return true;
            } else {
                try {
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                    Map<String, Object> map = new LinkedHashMap<String, Object>();
                    map.put("uuids", uuids);
                    JSONObject nodeJSON = new JSONObject(map);
                    nodeJSON.write(resp.getWriter());
                    return true;
                } catch (JSONException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        if(fileUpload != null && fileUpload.getParameterMap()!=null && !fileUpload.getParameterMap().isEmpty()) {
            parameters.putAll(fileUpload.getParameterMap());
        }
        return false;
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp,
            RenderContext renderContext, URLResolver urlResolver)
            throws RepositoryException, IOException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(urlResolver.getWorkspace(),
                        urlResolver.getLocale());
        Node node = session.getNode(urlResolver.getPath());
        Node parent = node.getParent();
        node.remove();
        session.save();
        String url = parent.getPath();
        session.save();
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        performRedirect(url, urlResolver.getPath(), req, resp,toParameterMapOfListOfString(req));
    }

    public static void performRedirect(String url, String path,
            HttpServletRequest req, HttpServletResponse resp,Map<String, List<String>> parameters)
            throws IOException {
        String renderedURL = null;
        List<String> stringList = parameters.get(NEW_NODE_OUTPUT_FORMAT);
        String outputFormat = (stringList!=null&& stringList.size()>0)?stringList.get(0):null;
        if (outputFormat == null || "".equals(outputFormat.trim())) {
            outputFormat = "html";
        }
        if (url != null) {
            String requestedURL = req.getRequestURL().toString();
            renderedURL = requestedURL.substring(0, requestedURL
                    .indexOf(URLEncoder.encode(path, "UTF-8").replace("%2F",
                            "/").replace("+", "%20")))
                    + url + "." + outputFormat;
        }
        stringList = parameters.get(REDIRECT_TO);
        String stayOnPage = (stringList!=null&& stringList.size()>0)?stringList.get(0):null;
        if (stayOnPage != null && "".equals(stayOnPage.trim())) {
            stayOnPage = null;
        }
        if (renderedURL != null && stayOnPage == null) {
            resp.setHeader("Location", renderedURL);
            resp.sendRedirect(renderedURL);
        } else if (stayOnPage != null) {
            resp.sendRedirect(stayOnPage + "." + outputFormat);
        }
    }

    public ModelAndView handleRequest(HttpServletRequest req,
            HttpServletResponse resp) throws Exception {
        String method = req.getMethod();
        if (req.getParameter(METHOD_TO_CALL) != null) {
            method = req.getParameter(METHOD_TO_CALL).toUpperCase();
        }
        long startTime = System.currentTimeMillis();

        ProcessingContext paramBean = null;

        try {
            Object old = req.getSession(true).getAttribute(
                    ParamBean.SESSION_SITE);
            paramBean = Jahia.createParamBean(req, resp, req.getSession());
            req.getSession(true).setAttribute(ParamBean.SESSION_SITE, old);
            
            URLResolver urlResolver = new URLResolver(req.getPathInfo(), paramBean.getSiteKey());            

            req.getSession().setAttribute("workspace",
                    urlResolver.getWorkspace());

            RenderContext renderContext = createRenderContext(req, resp,
                    paramBean.getUser());
            renderContext.setLiveMode(Constants.LIVE_WORKSPACE
                    .equals(urlResolver.getWorkspace()));

            try {
                if (Constants.EDIT_WORKSPACE.equals(urlResolver.getWorkspace())) {
                    if (renderContext.isEditMode()) {
                        paramBean.setOperationMode(ProcessingContext.EDIT);
                    } else {
                        paramBean.setOperationMode(ProcessingContext.PREVIEW);
                    }
                } else if (renderContext.isLiveMode()) {
                    paramBean.setOperationMode(ProcessingContext.NORMAL);
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
            paramBean.setCurrentLocale(urlResolver.getLocale());
            paramBean.getSessionState().setAttribute(ParamBean.SESSION_LOCALE,
                    urlResolver.getLocale());

            if (method.equals(METHOD_GET)) {
                Resource resource = urlResolver.getResource(getVersionDate(req));
                final JahiaSite site = paramBean.getSite();
                renderContext.setMainResource(resource);
                renderContext.setSite(site);
                renderContext.setSiteNode(JCRSessionFactory.getInstance()
                        .getCurrentUserSession(urlResolver.getWorkspace(),
                                urlResolver.getLocale()).getNode(
                                "/sites/" + site.getSiteKey()));
                resource.pushWrapper("wrapper.fullpage");
                resource.pushWrapper("wrapper.bodywrapper");

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
                doPost(req, resp, renderContext, urlResolver);

            } else if (method.equals(METHOD_PUT)) {
                doPut(req, resp, renderContext, urlResolver);

            } else if (method.equals(METHOD_DELETE)) {
                doDelete(req, resp, renderContext, urlResolver);

            } else if (method.equals(METHOD_OPTIONS)) {
                doOptions(req, resp);

            } else if (method.equals(METHOD_TRACE)) {
                doTrace(req, resp);

            } else {
                //
                // Note that this means NO servlet supports whatever
                // method was requested, anywhere on this server.
                //
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            List<ErrorHandler> handlers = ServicesRegistry.getInstance()
                    .getJahiaTemplateManagerService().getErrorHandler();
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
                    sb.append("] user=[").append(
                            paramBean.getUser().getUsername());
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

    public void setTemplateService(JahiaTemplateManagerService templateService) {
        this.templateService = templateService;
    }

    public static Set<String> getReservedParameters() {
        return reservedParameters;
    }
}
