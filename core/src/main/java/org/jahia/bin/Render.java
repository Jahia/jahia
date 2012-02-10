/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.io.IOUtils;
import org.jahia.api.Constants;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.applications.pluto.JahiaPortalURLParserImpl;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.render.*;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.files.FileUpload;
import org.jahia.utils.Url;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.servlet.ServletConfig;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Rendering controller. Resolves the node and the template, and renders it by executing the appropriate script.
 */
public class Render extends HttpServlet implements Controller, ServletConfigAware {

    private static final long serialVersionUID = 5377039107890340659L;

    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_TRACE = "TRACE";

    protected static final String HEADER_IFMODSINCE = "If-Modified-Since";
    protected static final String HEADER_LASTMOD = "Last-Modified";

    protected static final Set<String> reservedParameters;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Render.class);

    // Here we define the constants for the reserved keywords for post methods
    public static final String NODE_TYPE = "jcrNodeType";
    public static final String NODE_NAME = "jcrNodeName";
    public static final String NODE_NAME_PROPERTY = "jcrNodeNameProperty";
    public static final String NEW_NODE_OUTPUT_FORMAT = "jcrNewNodeOutputFormat";
    public static final String REDIRECT_TO = "jcrRedirectTo";
    public static final String REDIRECT_HTTP_RESPONSE_CODE = "jcrRedirectResponseCode";
    public static final String METHOD_TO_CALL = "jcrMethodToCall";
    public static final String AUTO_CHECKIN = "jcrAutoCheckin";
    public static final String CAPTCHA = "jcrCaptcha";
    public static final String TARGETDIRECTORY = "jcrTargetDirectory";
    public static final String TARGETNAME = "jcrTargetName";
    public static final String NORMALIZE_NODE_NAME = "jcrNormalizeNodeName";
    public static final String VERSION = "jcrVersion";
    public static final String SUBMIT = "jcrSubmit";
    public static final String AUTO_ASSIGN_ROLE = "jcrAutoAssignRole";
    public static final String ALIAS_USER = "alias";
    public static final String PARENT_TYPE = "jcrParentType";
    public static final String RETURN_CONTENTTYPE = "jcrReturnContentType";
    public static final String RETURN_CONTENTTYPE_OVERRIDE = "jcrReturnContentTypeOverride";
    public static final String RESOURCE_ID = "jcrResourceID";
    public static final String REMOVE_MIXIN = "jcrRemoveMixin";
    public static final String COOKIE_VALUE = "jcrCookieValue";
    public static final String COOKIE_NAME = "jcrCookieName";
    public static final String COOKIE_PATH = "jcrCookiePath";
    public static final String CONTRIBUTE_POST = "jcrContributePost";
    public static final String MARK_FOR_DELETION = "jcrMarkForDeletion";
    public static final String MARK_FOR_DELETION_MESSAGE = "jcrDeletionMessage";
    public static final String PREVIEW_DATE = "prevdate";

    private static final List<String> REDIRECT_CODE_MOVED_PERMANENTLY = new ArrayList<String>(
            Arrays.asList(new String[]{String.valueOf(HttpServletResponse.SC_MOVED_PERMANENTLY)}));
    private static final List<String> LIST_WITH_EMPTY_STRING = new ArrayList<String>(Arrays.asList(new String[]{StringUtils.EMPTY}));

    private MetricsLoggingService loggingService;
    private JahiaTemplateManagerService templateService;
    private Action defaultPostAction;
    private Action defaultDeleteAction = new DefaultDeleteAction();

    protected SettingsBean settingsBean;
    private RenderService renderService;
    private JCRSessionFactory jcrSessionFactory;
    private URLResolverFactory urlResolverFactory;

    private Integer sessionExpiryTime = null;
    private static Integer cookieExpirationInDays = 1;

    private Set<String> allowedMethods = new HashSet<String>();

    static {
        reservedParameters = new HashSet<String>();
        reservedParameters.add(NODE_TYPE);
        reservedParameters.add(NODE_NAME);
        reservedParameters.add(NODE_NAME_PROPERTY);
        reservedParameters.add(NEW_NODE_OUTPUT_FORMAT);
        reservedParameters.add(REDIRECT_TO);
        reservedParameters.add(METHOD_TO_CALL);
        reservedParameters.add(AUTO_CHECKIN);
        reservedParameters.add(CAPTCHA);
        reservedParameters.add(TARGETDIRECTORY);
        reservedParameters.add(TARGETNAME);
        reservedParameters.add(Constants.JCR_MIXINTYPES);
        reservedParameters.add(NORMALIZE_NODE_NAME);
        reservedParameters.add(VERSION);
        reservedParameters.add(SUBMIT);
        reservedParameters.add(AUTO_ASSIGN_ROLE);
        reservedParameters.add(PARENT_TYPE);
        reservedParameters.add(RETURN_CONTENTTYPE);
        reservedParameters.add(RETURN_CONTENTTYPE_OVERRIDE);
        reservedParameters.add(COOKIE_NAME);
        reservedParameters.add(COOKIE_VALUE);
        reservedParameters.add(COOKIE_PATH);
        reservedParameters.add(CONTRIBUTE_POST);
        reservedParameters.add(MARK_FOR_DELETION);
    }

    private transient ServletConfig servletConfig;


    /**
     * Returns the time the <code>HttpServletRequest</code> object was last modified, in milliseconds since midnight January 1, 1970 GMT. If
     * the time is unknown, this method returns a negative number (the default).
     * <p/>
     * <p/>
     * Servlets that support HTTP GET requests and can quickly determine their last modification time should override this method. This
     * makes browser and proxy caches work more effectively, reducing the load on server and network resources.
     *
     * @return a <code>long</code> integer specifying the time the <code>HttpServletRequest</code> object was last modified, in milliseconds
     *         since midnight, January 1, 1970 GMT, or -1 if the time is not known
     */
    protected long getLastModified(Resource resource, RenderContext renderContext)
            throws RepositoryException, IOException {
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
    protected void maybeSetLastModified(HttpServletResponse resp, long lastModified) {
        if (resp.containsHeader(HEADER_LASTMOD)) {
            return;
        }
        if (lastModified >= 0) {
            resp.setDateHeader(HEADER_LASTMOD, lastModified);
        }
    }

    protected RenderContext createRenderContext(HttpServletRequest req, HttpServletResponse resp, JahiaUser user) {
        RenderContext context = new RenderContext(req, resp, user);
        context.setServletPath(getRenderServletPath());
        return context;
    }

    protected Date getVersionDate(HttpServletRequest req) {
        // we assume here that the date has been passed as milliseconds.
        String msString = req.getParameter("v");
        if (msString == null) {
            return null;
        }
        try {
            long msLong = Long.parseLong(msString);
            if (logger.isDebugEnabled()) {
                logger.debug("Display version of date : " + SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(msLong)));
            }
            return new Date(msLong);
        } catch (NumberFormatException nfe) {
            logger.warn("Invalid version date found in URL " + msString);
            return null;
        }
    }

    protected String getVersionLabel(HttpServletRequest req) {
        return req.getParameter("l");
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext,
                         Resource resource, long startTime) throws RepositoryException, RenderException, IOException {
        loggingService.startProfiler("MAIN");
        resp.setCharacterEncoding(settingsBean.getCharacterEncoding());
        String out = renderService.render(resource, renderContext).trim();
        if (renderContext.getRedirect() != null && !resp.isCommitted()) {
            resp.sendRedirect(renderContext.getRedirect());
        } else {
            resp.setContentType(
                    renderContext.getContentType() != null ? renderContext.getContentType() : "text/html; charset=UTF-8");
//            resp.setContentLength(out.getBytes("UTF-8").length);
            resp.getWriter().print(out);
//            resp.getWriter().close();
        }
        String sessionID = "";
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            sessionID = httpSession.getId();
        }
        loggingService.stopProfiler("MAIN");
        if (loggingService.isEnabled()) {
            loggingService.logContentEvent(renderContext.getUser().getName(), req.getRemoteAddr(), sessionID,
                    resource.getNode().getIdentifier(), resource.getNode().getPath(), resource.getNode().getPrimaryNodeType().getName(), "pageViewed",
                    req.getHeader("User-Agent"), req.getHeader("Referer"), Long.toString(System.currentTimeMillis() - startTime));
        }
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext,
                         URLResolver urlResolver) throws RepositoryException, IOException {
        JCRSessionWrapper session = jcrSessionFactory.getCurrentUserSession(urlResolver.getWorkspace(), urlResolver.getLocale());
        JCRNodeWrapper node = session.getNode(urlResolver.getPath());
        session.checkout(node);
        @SuppressWarnings("unchecked")
        Map<String, String[]> parameters = req.getParameterMap();
        if (parameters.containsKey(REMOVE_MIXIN)) {
            String[] mixinTypes = (String[]) parameters.get(REMOVE_MIXIN);
            for (String mixinType : mixinTypes) {
                node.removeMixin(mixinType);
            }
        }
        if (parameters.containsKey(Constants.JCR_MIXINTYPES)) {
            String[] mixinTypes = (String[]) parameters.get(Constants.JCR_MIXINTYPES);
            for (String mixinType : mixinTypes) {
                node.addMixin(mixinType);
            }
        }
        Set<Map.Entry<String, String[]>> set = parameters.entrySet();
        try {
            for (Map.Entry<String, String[]> entry : set) {
                String key = entry.getKey();
                if (!reservedParameters.contains(key)) {
                    String[] values = entry.getValue();
                    final ExtendedPropertyDefinition propertyDefinition =
                            ((JCRNodeWrapper) node).getApplicablePropertyDefinition(key);
                    if (propertyDefinition == null) {
                        continue;
                    }
                    if (propertyDefinition.isMultiple()) {
                        node.setProperty(key, values);
                    } else if (propertyDefinition.getRequiredType() == PropertyType.DATE) {
                        // Expecting ISO date yyyy-MM-dd'T'HH:mm:ss
                        DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(values[0]);
                        node.setProperty(key, dateTime.toCalendar(Locale.ENGLISH));
                    } else {
                        node.setProperty(key, values[0]);
                    }
                }
            }
        } catch (ConstraintViolationException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "bad parameter");
            return;
        }
        session.save();
        if (req.getParameter(AUTO_CHECKIN) != null && req.getParameter(AUTO_CHECKIN).length() > 0) {
            session.getWorkspace().getVersionManager().checkpoint(node.getPath());
        }
        final String requestWith = req.getHeader("x-requested-with");
        if (req.getHeader("accept").contains("application/json") && requestWith != null &&
                requestWith.equals("XMLHttpRequest")) {
            try {
                serializeNodeToJSON(node).write(resp.getWriter());
            } catch (JSONException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            performRedirect(null, null, req, resp, toParameterMapOfListOfString(req), false);
        }
        addCookie(req, resp);
        String sessionID = "";
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            sessionID = httpSession.getId();
        }
        if (loggingService.isEnabled()) {
            loggingService.logContentEvent(renderContext.getUser().getName(), req.getRemoteAddr(), sessionID,
                    node.getIdentifier(), urlResolver.getPath(), node.getPrimaryNodeType().getName(), "nodeUpdated",
                    new JSONObject(req.getParameterMap()).toString());
        }
    }

    public static void addCookie(HttpServletRequest req, HttpServletResponse resp) {
        if (req.getParameter(COOKIE_NAME) != null && req.getParameter(COOKIE_VALUE) != null) {
            Cookie cookie = new Cookie(req.getParameter(COOKIE_NAME), req.getParameter(COOKIE_VALUE));
            cookie.setMaxAge(60 * 60 * 24 * cookieExpirationInDays);
            if (req.getParameter(COOKIE_PATH) != null)
                cookie.setPath(req.getParameter(COOKIE_PATH));
            else {
                cookie.setPath("/");
            }
            resp.addCookie(cookie);
        }
    }

    public static JSONObject serializeNodeToJSON(JCRNodeWrapper node)
            throws RepositoryException, IOException, JSONException {
        final PropertyIterator stringMap = node.getProperties();
        Map<String, String> map = new HashMap<String, String>();
        while (stringMap.hasNext()) {
            JCRPropertyWrapper propertyWrapper = (JCRPropertyWrapper) stringMap.next();
            final int type = propertyWrapper.getType();
            final String name = JCRContentUtils.replaceColon(propertyWrapper.getName());
            if (!Constants.forbiddenPropertiesToSerialize.contains(propertyWrapper.getDefinition().getName())) {
                if (type == PropertyType.WEAKREFERENCE || type == PropertyType.REFERENCE) {
                    if (!propertyWrapper.isMultiple()) {
                        map.put(name, ((JCRNodeWrapper) propertyWrapper.getNode()).getUrl());
                    }
                } else {
                    if (!propertyWrapper.isMultiple()) {
                        map.put(name, propertyWrapper.getValue().getString());
                    }
                }
            }
        }
        JSONObject nodeJSON = new JSONObject(map);
        return nodeJSON;
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext,
                          URLResolver urlResolver) throws Exception {
        if (req.getParameter(JahiaPortalURLParserImpl.PORTLET_INFO) != null) {
            Resource resource = urlResolver.getResource();
            renderContext.setMainResource(resource);
            JCRSiteNode site = resource.getNode().getResolveSite();
            renderContext.setSite(site);
            doGet(req, resp, renderContext, resource, System.currentTimeMillis());
            return;
        }
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        if (checkForUploadedFiles(req, resp, urlResolver.getWorkspace(), urlResolver.getLocale(), parameters, urlResolver)) {
            if (parameters.isEmpty()) {
                return;
            }
        }
        if (parameters.isEmpty()) {
            parameters = toParameterMapOfListOfString(req);
        }

        req.getSession().removeAttribute("formDatas");
        req.getSession().removeAttribute("formError");

        Action action;
        Resource resource = null;
        if (urlResolver.getPath().endsWith(".do")) {
            resource = urlResolver.getResource();
            renderContext.setMainResource(resource);
            try {
                JCRSiteNode site = resource.getNode().getResolveSite();
                renderContext.setSite(site);
            } catch (RepositoryException e) {
            }

            action = templateService.getActions().get(resource.getResolvedTemplate());
        } else {
            final String path = urlResolver.getPath();

            String resourcePath = JCRTemplate.getInstance().doExecuteWithSystemSession(null,
                    urlResolver.getWorkspace(), urlResolver.getLocale(), new JCRCallback<String>() {
                public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    String resourcePath = path.endsWith("*") ? StringUtils.substringBeforeLast(path, "/") : path;
                    do {
                        try {
                            session.getNode(resourcePath);
                            break;
                        } catch (PathNotFoundException e) {
                            resourcePath = StringUtils.substringBeforeLast(resourcePath, "/");
                        }
                    } while (resourcePath.contains("/"));
                    return resourcePath;
                }
            });

            resource = urlResolver.getResource(resourcePath + ".html");
            renderContext.setMainResource(resource);
            try {
                JCRSiteNode site = resource.getNode().getResolveSite();
                renderContext.setSite(site);
            } catch (RepositoryException e) {
            }
            action = defaultPostAction;
        }
        if (action == null) {
            if (urlResolver.getPath().endsWith(".do")) {
                logger.error("Couldn't resolve action named [" + resource.getResolvedTemplate() + "]");
            }
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        } else {
            doAction(req, resp, urlResolver, renderContext, resource, action, parameters);
        }
    }

    private Map<String, List<String>> toParameterMapOfListOfString(HttpServletRequest req) {
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        for (Object key : req.getParameterMap().keySet()) {
            if (key != null) {
                parameters.put((String) key, new ArrayList<String>(Arrays.asList((String[]) req.getParameterMap().get(key))));
            }
        }
        return parameters;
    }

    private boolean checkForUploadedFiles(HttpServletRequest req, HttpServletResponse resp, String workspace,
                                          Locale locale, Map<String, List<String>> parameters,
                                          URLResolver urlResolver)
            throws RepositoryException, IOException {

        if (isMultipartRequest(req)) {
            // multipart is processed only if it's not a portlet request.
            // otherwise it's the task the portlet
            if (!isPortletRequest(req)) {
                final String savePath = settingsBean.getTmpContentDiskPath();
                final File tmp = new File(savePath);
                if (!tmp.exists()) {
                    tmp.mkdirs();
                }
                try {
                    final FileUpload fileUpload = new FileUpload(req, savePath, Integer.MAX_VALUE);
                    req.setAttribute(FileUpload.FILEUPLOAD_ATTRIBUTE, fileUpload);
                    if (fileUpload.getFileItems() != null && fileUpload.getFileItems().size() > 0) {
                        boolean isTargetDirectoryDefined = fileUpload.getParameterNames().contains(TARGETDIRECTORY);
                        boolean isContributePost = fileUpload.getParameterNames().contains(CONTRIBUTE_POST);
                        final String requestWith = req.getHeader("x-requested-with");
                        boolean isAjaxRequest =
                                req.getHeader("accept").contains("application/json") && requestWith != null &&
                                        requestWith.equals("XMLHttpRequest") || fileUpload.getParameterMap().isEmpty();
                        List<String> uuids = new LinkedList<String>();
                        List<String> files = new ArrayList<String>();
                        List<String> urls = new LinkedList<String>();
                        // If target directory is defined or if it is an ajax request then save the file now
                        // otherwise we delay the save of the file to the node creation
                        if (isContributePost || isTargetDirectoryDefined || isAjaxRequest) {
                            JCRSessionWrapper session =
                                    jcrSessionFactory.getCurrentUserSession(workspace, locale);
                            String target;
                            if (isTargetDirectoryDefined) {
                                target = (fileUpload.getParameterValues(TARGETDIRECTORY))[0];
                            } else if (isContributePost) {
                                String path = urlResolver.getPath();
                                path = (path.endsWith("*") ? StringUtils.substringBeforeLast(path, "/") : path);
                                JCRNodeWrapper sessionNode = session.getNode(path);
                                JCRSiteNode siteNode = sessionNode.getResolveSite();
                                if (siteNode != null) {
                                    String s = sessionNode.getResolveSite().getPath() + "/files/contributed/";
                                    String name = sessionNode.getPrimaryNodeTypeName().replaceAll(":", "_") + "_" + sessionNode.getName();
                                    target = s + name;
                                    try {
                                        session.getNode(target);
                                    } catch (RepositoryException e) {
                                        JCRNodeWrapper node = session.getNode(s);
                                        session.checkout(node);
                                        node.addNode(name, "jnt:folder");
                                        session.save();
                                    }
                                } else {
                                    target = sessionNode.getPath() + "/files";
                                    if (!sessionNode.hasNode("files")) {
                                        session.checkout(sessionNode);
                                        sessionNode.addNode("files", "jnt:folder");
                                        session.save();
                                    }
                                }
                            } else {
                                String path = urlResolver.getPath();
                                target = (path.endsWith("*") ? StringUtils.substringBeforeLast(path, "/") : path);
                            }
                            final JCRNodeWrapper targetDirectory = session.getNode(target);

                            boolean isVersionActivated = fileUpload.getParameterNames().contains(VERSION) ?
                                    (fileUpload.getParameterValues(VERSION))[0].equals("true") : false;

                            final Map<String, DiskFileItem> stringDiskFileItemMap = fileUpload.getFileItems();
                            for (Map.Entry<String, DiskFileItem> itemEntry : stringDiskFileItemMap.entrySet()) {
                                //if node exists, do a checkout before
                                String name = itemEntry.getValue().getName();

                                if (fileUpload.getParameterNames().contains(TARGETNAME)) {
                                    name = (fileUpload.getParameterValues(TARGETNAME))[0];
                                }

                                name = JCRContentUtils.escapeLocalNodeName(FilenameUtils.getName(name));
                                
                                JCRNodeWrapper fileNode = targetDirectory.hasNode(name) ?
                                        targetDirectory.getNode(name) : null;
                                if (fileNode != null && isVersionActivated) {
                                    session.checkout(fileNode);
                                }
                                // checkout parent directory
                                session.getWorkspace().getVersionManager().checkout(targetDirectory.getPath());
                                InputStream is = null;
                                JCRNodeWrapper wrapper = null;
                                try {
                                    is = itemEntry.getValue().getInputStream();
                                    wrapper = targetDirectory.uploadFile(name, is, itemEntry
                                            .getValue().getContentType());
                                } finally {
                                    IOUtils.closeQuietly(is);
                                }
                                uuids.add(wrapper.getIdentifier());
                                urls.add(wrapper.getAbsoluteUrl(req));
                                files.add(itemEntry.getValue().getName());
                                if (isVersionActivated) {
                                    if (!wrapper.isVersioned()) {
                                        wrapper.versionFile();
                                    }
                                    session.save();
                                    // Handle potential move of the node after save
                                    wrapper = session.getNodeByIdentifier(wrapper.getIdentifier());
                                    wrapper.checkpoint();
                                }
                            }
                            fileUpload.disposeItems();
                            fileUpload.markFilesAsConsumed();
                            session.save();
                        }

                        if (!isAjaxRequest && !isContributePost) {
                            parameters.putAll(fileUpload.getParameterMap());
                            if (isTargetDirectoryDefined) {
                                parameters.put(NODE_NAME, files);
                            }
                            return true;
                        } else {
                            try {
                                resp.setStatus(HttpServletResponse.SC_CREATED);
                                Map<String, Object> map = new LinkedHashMap<String, Object>();
                                map.put("uuids", uuids);
                                map.put("urls", urls);
                                JSONObject nodeJSON = new JSONObject(map);
                                nodeJSON.write(resp.getWriter());
                                return true;
                            } catch (JSONException e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                    if (fileUpload.getParameterMap() != null && !fileUpload.getParameterMap().isEmpty()) {
                        parameters.putAll(fileUpload.getParameterMap());
                    }
                } catch (IOException e) {
                    logger.error("Cannot parse multipart data !", e);
                }
            } else {
                logger.debug("Mulipart request is not processed. It's the task of the portlet");
            }
        }

        return false;
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext,
                            URLResolver urlResolver) throws Exception {
        doAction(req, resp, urlResolver, renderContext, null, defaultDeleteAction, toParameterMapOfListOfString(req));
    }

    public boolean isMultipartRequest(final HttpServletRequest req) {
        final String contentType = req.getHeader("Content-Type");

        return ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0));
    }

    /**
     * If the request is a portlet request, it returns true, otherwise returns false.
     *
     * @param req An HttpServletRequest.
     * @return True if request is a portlet request.
     */
    public boolean isPortletRequest(final HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.contains(ProcessingContext.PLUTO_PREFIX)) {
            StringTokenizer st = new StringTokenizer(pathInfo, "/", false);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                // remder/resource url
                if (token.startsWith(ProcessingContext.PLUTO_PREFIX + ProcessingContext.PLUTO_RESOURCE)) {
                    return true;
                } else if (token.startsWith(ProcessingContext.PLUTO_PREFIX + ProcessingContext.PLUTO_ACTION)) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * This method allows you to define where you want to redirect the user after request.
     *
     * @param url
     * @param path
     * @param req
     * @param resp
     * @param parameters
     * @param bypassCache If true we will append a parameter to the URL that should match the id of the resource to refresh
     * @throws IOException
     */
    public static void performRedirect(String url, String path, HttpServletRequest req, HttpServletResponse resp,
                                       Map<String, List<String>> parameters, boolean bypassCache) throws IOException {
        String renderedURL = null;

        List<String> stringList = parameters.get(NEW_NODE_OUTPUT_FORMAT);
        String outputFormat =
                !CollectionUtils.isEmpty(stringList) && stringList.get(0) != null ? stringList.get(0) : "html";

        stringList = parameters.get(REDIRECT_HTTP_RESPONSE_CODE);
        int responseCode = !CollectionUtils.isEmpty(stringList) && !StringUtils.isBlank(stringList.get(0)) ?
                Integer.parseInt(stringList.get(0)) : HttpServletResponse.SC_SEE_OTHER;

        stringList = parameters.get(REDIRECT_TO);
        String stayOnPage =
                !CollectionUtils.isEmpty(stringList) && !StringUtils.isBlank(stringList.get(0)) ? StringUtils.substringBeforeLast(stringList.get(0),";") :
                        "";

        if (!StringUtils.isEmpty(stayOnPage)) {
            renderedURL = stayOnPage + (!StringUtils.isEmpty(outputFormat) ? "." + outputFormat : "");
        } else if (!StringUtils.isEmpty(url)) {
            String requestedURL = req.getRequestURI();
//            String encodedPath = URLEncoder.encode(path, "UTF-8").replace("%2F", "/").replace("+", "%20");
            String decodedURL = URLDecoder.decode(requestedURL, "UTF-8");

            int index = decodedURL.indexOf(path);

            renderedURL = decodedURL.substring(0, index) + url +
                    (!StringUtils.isEmpty(outputFormat) ? "." + outputFormat : "");
        }
        if (bypassCache) {
            stringList = parameters.get(RESOURCE_ID);
            String formuuid = !CollectionUtils.isEmpty(stringList) && !StringUtils.isBlank(stringList.get(
                    0)) ? stringList.get(0) : UUID.randomUUID().toString();
            renderedURL = renderedURL + "?ec=" + formuuid;
        }
        if (!StringUtils.isEmpty(renderedURL)) {
            if (StringUtils.isEmpty(stayOnPage)) {
                resp.setHeader("Location", resp.encodeRedirectURL(renderedURL));
            } else if (responseCode == HttpServletResponse.SC_SEE_OTHER) {
                resp.setHeader("Location", resp.encodeRedirectURL(renderedURL));
            }
            if (responseCode == HttpServletResponse.SC_FOUND) {
                resp.sendRedirect(resp.encodeRedirectURL(renderedURL));
            } else {
                resp.setStatus(responseCode);
            }
        }
    }

    public ModelAndView handleRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (isDisabled()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        String method = req.getMethod();
        if (req.getParameter(METHOD_TO_CALL) != null) {
            method = req.getParameter(METHOD_TO_CALL).toUpperCase();
        }
        if (!isMethodAllowed(method)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        long startTime = System.currentTimeMillis();
        String sessionId = null;
        try {
            final HttpSession session = req.getSession();
            if (logger.isInfoEnabled()) {
                sessionId = session.getId();
            }
            Date date = getVersionDate(req);
            String versionLabel = getVersionLabel(req);
            URLResolver urlResolver = urlResolverFactory.createURLResolver(req.getPathInfo(), req.getServerName(), req);
            urlResolver.setVersionDate(date);
            urlResolver.setVersionLabel(versionLabel);

            req.setAttribute("urlResolver", urlResolver);

            // check permission
            try {
                if (!hasAccess(urlResolver.getNode())) {
                    if (JahiaUserManagerService.isGuest(jcrSessionFactory.getCurrentUser())) {
                        throw new JahiaUnauthorizedException();
                    } else {
                        throw new JahiaForbiddenAccessException();
                    }
                }
            } catch (PathNotFoundException e) {

            }

            session.setAttribute("workspace", urlResolver.getWorkspace());

            if (sessionExpiryTime != null && session.getMaxInactiveInterval() != sessionExpiryTime * 60) {
                session.setMaxInactiveInterval(sessionExpiryTime * 60);
            }

            RenderContext renderContext =
                    createRenderContext(req, resp, jcrSessionFactory.getCurrentUser());
            renderContext.setLiveMode(Constants.LIVE_WORKSPACE.equals(urlResolver.getWorkspace()));
            renderContext.setPreviewMode(!renderContext.isEditMode() && !renderContext.isContributionMode() && !renderContext.isLiveMode());
            urlResolver.setRenderContext(renderContext);
            req.getSession().setAttribute(ParamBean.SESSION_LOCALE, urlResolver.getLocale());
            jcrSessionFactory.setCurrentLocale(urlResolver.getLocale());
            if (renderContext.isPreviewMode() && req.getParameter(ALIAS_USER) != null && !JahiaUserManagerService.isGuest(jcrSessionFactory.getCurrentUser())) {
                jcrSessionFactory.setCurrentAliasedUser(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(req.getParameter(ALIAS_USER)));
            }
            if (renderContext.isPreviewMode() && req.getParameter(PREVIEW_DATE) != null && !JahiaUserManagerService.isGuest(jcrSessionFactory.getCurrentUser())) {
                Calendar previewDate = Calendar.getInstance();
                previewDate.setTime(new Date(new Long(req.getParameter(PREVIEW_DATE))));
                jcrSessionFactory.setCurrentPreviewDate(previewDate);
            }
            if (method.equals(METHOD_GET)) {
                if (!StringUtils.isEmpty(urlResolver.getRedirectUrl())) {
                    Map<String, List<String>> parameters = new HashMap<String, List<String>>();
                    parameters.put(NEW_NODE_OUTPUT_FORMAT, LIST_WITH_EMPTY_STRING);
                    parameters.put(REDIRECT_HTTP_RESPONSE_CODE, REDIRECT_CODE_MOVED_PERMANENTLY);

                    performRedirect(urlResolver.getRedirectUrl(), StringUtils.isEmpty(urlResolver.getVanityUrl()) ?
                            "/" + urlResolver.getLocale().toString() + urlResolver.getPath() :
                            urlResolver.getVanityUrl(), req, resp, parameters, false);
                } else {
                    Resource resource;

                    resource = urlResolver.getResource();
                    renderContext.setMainResource(resource);

                    JCRSiteNode site = resource.getNode().getResolveSite();
                    if (!Url.isLocalhost(req.getServerName()) && !renderContext.isEditMode()) {
                        JCRSessionWrapper session1 = resource.getNode().getSession();
                        if (urlResolver.getSiteKey() != null && !site.getSiteKey().equals(urlResolver.getSiteKey())) {
                            site = (JCRSiteNode) session1.getNode("/sites/" + urlResolver.getSiteKey());
                        } else if (urlResolver.getSiteKeyByServerName() != null && !site.getSiteKey().equals(
                                urlResolver.getSiteKeyByServerName())) {
                            site = (JCRSiteNode) session1.getNode("/sites/" + urlResolver.getSiteKeyByServerName());
                        }
                    }
                    if ((site == null && resource.getNode().getPath().startsWith("/sites/")) || (site != null
                            && (renderContext.getEditModeConfigName() == null
                            || !renderContext.getEditModeConfigName().equals(Studio.STUDIO_MODE))
                            && !(renderContext.isLiveMode() ? site.getActiveLanguagesAsLocales()
                                    .contains(urlResolver.getLocale()) : site
                                    .getLanguagesAsLocales().contains(urlResolver.getLocale())))) {
                        throw new PathNotFoundException("This language does not exist on this site");
                    }
                    renderContext.setSite(site);
//                    resource.pushWrapper("wrapper.fullpage");
//                    resource.pushBodyWrapper();

                    if (urlResolver.getPath().endsWith(".do")) {
                        Action action = templateService.getActions().get(resource.getResolvedTemplate());
                        Map<String, List<String>> parameters = toParameterMapOfListOfString(req);
                        doAction(req, resp, urlResolver, renderContext, resource, action, parameters);
                    } else {
                        long lastModified = getLastModified(resource, renderContext);

                        if (lastModified == -1) {
                            // servlet doesn't support if-modified-since, no reason
                            // to go through further expensive logic
                            doGet(req, resp, renderContext, resource, startTime);
                        } else {
                            long ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
                            if (ifModifiedSince < (lastModified / 1000 * 1000)) {
                                // If the servlet mod time is later, call doGet()
                                // Round down to the nearest second for a proper compare
                                // A ifModifiedSince of -1 will always be less
                                maybeSetLastModified(resp, lastModified);
                                doGet(req, resp, renderContext, resource, startTime);
                            } else {
                                resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                            }
                        }
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
            List<ErrorHandler> handlers = templateService.getErrorHandler();
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
                if (jcrSessionFactory.getCurrentUser() != null) {
                    sb.append("] user=[").append(jcrSessionFactory.getCurrentUser().getUsername());
                }
                sb.append("] ip=[").append(req.getRemoteAddr()).append("] sessionID=[")
                        .append(sessionId).append("] in [")
                        .append(System.currentTimeMillis() - startTime).append("ms]");
                logger.info(sb.toString());
            }
        }
        return null;
    }

    protected boolean isDisabled() {
        return false;
    }

    private void doAction(HttpServletRequest req, HttpServletResponse resp, URLResolver urlResolver,
                          RenderContext renderContext, Resource resource, Action action,
                          Map<String, List<String>> parameters) throws Exception {

        String token = parameters.get("form-token")!=null?parameters.get("form-token").get(0):null;
        if (token != null) {
            final String requestWith = req.getHeader("x-requested-with");
            boolean isAjaxRequest =
                    req.getHeader("accept").contains("application/json") && requestWith != null &&
                            requestWith.equals("XMLHttpRequest");

            @SuppressWarnings("unchecked")
            Map<String, Map<String, List<String>>> toks = (Map<String, Map<String, List<String>>>) req.getSession().getAttribute("form-tokens");
            if (toks != null && toks.containsKey(token)) {
                Map<String, List<String>> m = toks.remove(token);
                if (m == null) {
                    Map<String, String[]> formDatas = new HashMap<String, String[]>();
                    Set<Map.Entry<String, List<String>>> set = parameters.entrySet();
                    for (Map.Entry<String, List<String>> params : set) {
                        formDatas.put(params.getKey(), params.getValue().toArray(new String[params.getValue().size()]));
                    }
                    String errorMessage = JahiaResourceBundle.getJahiaInternalResource("failure.captcha", urlResolver.getLocale(), "Your captcha is invalid");
                    if (!isAjaxRequest) {
                        req.getSession().setAttribute("formDatas", formDatas);
                        req.getSession().setAttribute("formError", errorMessage);
                        performRedirect(urlResolver.getRedirectUrl(), urlResolver.getPath(), req, resp, parameters, true);
                    } else {
                        resp.setContentType("application/json");
                        Map<String,String> res = new HashMap<String,String>();
                        res.put("status", errorMessage);
                        new JSONObject(res).write(resp.getWriter());
                    }
                    return;
                }
                Map<String, List<String>> values = new HashMap<String, List<String>>(m);

                // Validate form token
                List<String> stringList1 = values.remove("form-action");
                String formAction = stringList1.isEmpty()?null:stringList1.get(0);
                String characterEncoding = SettingsBean.getInstance().getCharacterEncoding();
                if (formAction == null ||
                        (!URLDecoder.decode(req.getRequestURI(), characterEncoding).equals(URLDecoder.decode(formAction, characterEncoding)) &&
                        !URLDecoder.decode(resp.encodeURL(req.getRequestURI()), characterEncoding).equals(URLDecoder.decode(formAction, characterEncoding)))
                        ) {
                    throw new AccessDeniedException();
                }
                if (!req.getMethod().equalsIgnoreCase(values.remove("form-method").get(0))) {
                    throw new AccessDeniedException();
                }
                for (Map.Entry<String, List<String>> entry : values.entrySet()) {
                    List<String> stringList = entry.getValue();
                    List<String> parameterValues = parameters.get(entry.getKey());
                    if (parameterValues == null || !CollectionUtils.isEqualCollection(stringList, parameterValues)) {
                        if (entry.getKey().equals(CAPTCHA)) {
                            Map<String, String[]> formDatas = new HashMap<String, String[]>();
                            Set<Map.Entry<String, List<String>>> set = parameters.entrySet();
                            for (Map.Entry<String, List<String>> params : set) {
                                formDatas.put(params.getKey(), params.getValue().toArray(new String[params.getValue().size()]));
                            }
                            String errorMessage = JahiaResourceBundle.getJahiaInternalResource("failure.captcha", urlResolver.getLocale(), "Your captcha is invalid");
                            if (!isAjaxRequest) {
                                req.getSession().setAttribute("formDatas", formDatas);
                                req.getSession().setAttribute("formError", errorMessage);
                                performRedirect(renderContext.getMainResource().getNode().getPath(), urlResolver.getPath(), req, resp, parameters,
                                        true);
                            } else {
                                resp.setContentType("application/json");
                                Map<String,String> res = new HashMap<String,String>();
                                res.put("status", errorMessage);
                                new JSONObject(res).write(resp.getWriter());
                            }
                            return;
                        }
                        throw new AccessDeniedException();
                    }
                }

                final Action originalAction = action;
                action = new SystemAction() {
                    @Override
                    public ActionResult doExecuteAsSystem(HttpServletRequest req, RenderContext renderContext, JCRSessionWrapper systemSession, Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
                        return originalAction.doExecute(req, renderContext, resource, systemSession, parameters, urlResolver);
                    }
                };
            }
        }

        if (!(action instanceof SystemAction)) {
            if (action.getRequiredWorkspace() != null
                    && !action.getRequiredWorkspace().equals(urlResolver.getWorkspace())) {
                throw new PathNotFoundException("Action is not supported for this workspace");
            }
            if (action.isRequireAuthenticatedUser() && !renderContext.isLoggedIn()) {
                throw new AccessDeniedException("Action '" + action.getName() + "' requires an authenticated user");
            }
            if (!action.isPermitted(urlResolver.getNode())) {
                throw new AccessDeniedException("Action '" + action.getName() + "' requires '" + action.getRequiredPermission() + "' permission.");
            }
        }

        JCRSessionWrapper session = null;
        if (resource != null) {
            session = resource.getNode().getSession();
        } else {
            session = JCRSessionFactory.getInstance().getCurrentUserSession(urlResolver.getWorkspace(),
                    urlResolver.getLocale());
        }
        ActionResult result = action.doExecute(req, renderContext, resource, session, parameters, urlResolver);
        if (result != null) {
            if (result.getResultCode() < 300) {
                resp.setStatus(result.getResultCode());
                addCookie(req,resp);
                if (result.getJson() != null && 
                        ("json".equals(parameters.get(RETURN_CONTENTTYPE) != null ? parameters.get(RETURN_CONTENTTYPE).get(0) : "") 
                                || req.getHeader("accept") != null && req.getHeader("accept").contains("application/json"))) {
                    try {
                        resp.setContentType(parameters.get(RETURN_CONTENTTYPE_OVERRIDE) != null ? parameters.get(RETURN_CONTENTTYPE_OVERRIDE).get(0) : "application/json");
                        result.getJson().write(resp.getWriter());
                    } catch (JSONException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    if (!result.isAbsoluteUrl()) {
                        performRedirect(result.getUrl(), urlResolver.getPath(), req, resp, parameters, false);
                    } else {
                        resp.sendRedirect(resp.encodeRedirectURL(result.getUrl()));
                    }
                }
            } else {
                resp.sendError(result.getResultCode());
            }
        }
    }

    protected boolean isMethodAllowed(String method) {
        return allowedMethods.isEmpty() || allowedMethods.contains(method);
    }

    protected boolean hasAccess(JCRNodeWrapper node) {
        return true;
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

    public void setSessionExpiryTime(int sessionExpiryTime) {
        this.sessionExpiryTime = sessionExpiryTime;
    }

    public void setDefaultPostAction(Action defaultPostActionResult) {
        this.defaultPostAction = defaultPostActionResult;
    }

    public static Set<String> getReservedParameters() {
        return reservedParameters;
    }

    /**
     * @param settingsBean the settingsBean to set
     */
    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    /**
     * @param renderService the renderService to set
     */
    public void setRenderService(RenderService renderService) {
        this.renderService = renderService;
    }

    /**
     * @param jcrSessionFactory the jcrSessionFactory to set
     */
    public void setJcrSessionFactory(JCRSessionFactory jcrSessionFactory) {
        this.jcrSessionFactory = jcrSessionFactory;
    }

    public void setCookieExpirationInDays(Integer cookieExpirationInDays) {
        Render.cookieExpirationInDays = cookieExpirationInDays;
    }

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

    /**
     * Specifies the set of allowed HTTP methods.
     *
     * @param allowedMethods the set of allowed HTTP methods
     */
    public void setAllowedMethods(Set<String> allowedMethods) {
        this.allowedMethods = new HashSet<String>(allowedMethods.size());
        for (String method : allowedMethods) {
            this.allowedMethods.add(method.toUpperCase());
        }
    }
}
