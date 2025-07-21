/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin;

import net.htmlparser.jericho.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.spi.commons.conversion.MalformedPathException;
import org.apache.hc.core5.http.io.entity.PathEntity;
import org.apache.jackrabbit.spi.commons.conversion.NameException;
import org.jahia.api.Constants;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.exceptions.RequiredActionModeException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.render.*;
import org.jahia.services.seo.urlrewrite.SessionidRemovalResponseWrapper;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.files.FileUpload;
import org.jahia.utils.Url;
import org.jahia.utils.i18n.Messages;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.jcr.*;
import javax.servlet.ServletConfig;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;

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

    // Here we define the constants for the reserved keywords for post methods
    public static final String NODE_TYPE = "jcrNodeType";
    public static final String NODE_NAME = "jcrNodeName";
    public static final String NODE_NAME_PROPERTY = "jcrNodeNameProperty";
    public static final String NEW_NODE_OUTPUT_FORMAT = "jcrNewNodeOutputFormat";
    public static final String REDIRECT_TO = "jcrRedirectTo";
    public static final String REDIRECT_HTTP_RESPONSE_CODE = "jcrRedirectResponseCode";
    public static final String METHOD_TO_CALL = "jcrMethodToCall";
    public static final String AUTO_CHECKIN = "jcrAutoCheckin";
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
    public static final String COOKIE_HTTP_ONLY = "jcrCookieHttpOnly";
    public static final String CONTRIBUTE_POST = "jcrContributePost";
    public static final String MARK_FOR_DELETION = "jcrMarkForDeletion";
    public static final String MARK_FOR_DELETION_MESSAGE = "jcrDeletionMessage";
    public static final String PREVIEW_DATE = "prevdate";
    public static final String DISABLE_XSS_FILTERING = "disableXSSFiltering";
    public static final List<String> EVENT_ATTRIBUTE_NAMES = Arrays.asList(
            "onblur", "onchange", "onclick", "ondblclick", "onfocus", "onkeydown", "onkeypress", "onkeyup", "onload",
            "onmousedown", "onmousemove", "onmouseover", "onmouseout", "onmouseup", "onselect", "onsubmit", "onabort",
            "oncanplay", "oncanplaythrough", "oncontextmenu", "ondrag", "ondragend", "ondragenter", "ondragleave",
            "ondragstart", "ondrop", "ondurationchange", "onemptied", "onended", "onerror", "onformchange",
            "onforminput", "oninput", "oninvalid", "onloadeddata", "onloadedmetadata", "onloadstart", "onmousewheel",
            "onpause", "onplay", "onplaying", "onprogress", "onratechange", "onreadystatechange", "onscroll",
            "onseeked", "onseeking", "onshow", "onstalled", "onsuspend", "ontimeupdate", "onvolumechange", "onwaiting"
    );
    public static final Pattern TAG_MISSING_END_BIGGERTHAN_PATTERN = Pattern.compile("<([^<>]*)(?=<|$)");
    public static final Pattern TAG_MISSING_START_LESSERTHAN_PATTERN = Pattern.compile("(^|(?<=>))([^<>]*)>");
    public static final String ALLOWS_MULTIPLE_SUBMITS = "allowsMultipleSubmits";

    private static final List<String> REDIRECT_CODE_MOVED_PERMANENTLY = new ArrayList<String>(
            Arrays.asList(new String[]{String.valueOf(HttpServletResponse.SC_MOVED_PERMANENTLY)}));
    private static final List<String> LIST_WITH_EMPTY_STRING = new ArrayList<String>(Arrays.asList(new String[]{StringUtils.EMPTY}));

    public static final String PLUTO_PREFIX = "__";
    public static final String PLUTO_ACTION = "ac";
    public static final String PLUTO_RESOURCE = "rs";

    protected static final Set<String> RESERVED_PARAMETERS;

    static {
        RESERVED_PARAMETERS = new HashSet<String>();
        RESERVED_PARAMETERS.add(NODE_TYPE);
        RESERVED_PARAMETERS.add(NODE_NAME);
        RESERVED_PARAMETERS.add(NODE_NAME_PROPERTY);
        RESERVED_PARAMETERS.add(NEW_NODE_OUTPUT_FORMAT);
        RESERVED_PARAMETERS.add(REDIRECT_TO);
        RESERVED_PARAMETERS.add(METHOD_TO_CALL);
        RESERVED_PARAMETERS.add(AUTO_CHECKIN);
        RESERVED_PARAMETERS.add(TARGETDIRECTORY);
        RESERVED_PARAMETERS.add(TARGETNAME);
        RESERVED_PARAMETERS.add(Constants.JCR_MIXINTYPES);
        RESERVED_PARAMETERS.add(NORMALIZE_NODE_NAME);
        RESERVED_PARAMETERS.add(VERSION);
        RESERVED_PARAMETERS.add(SUBMIT);
        RESERVED_PARAMETERS.add(AUTO_ASSIGN_ROLE);
        RESERVED_PARAMETERS.add(PARENT_TYPE);
        RESERVED_PARAMETERS.add(RETURN_CONTENTTYPE);
        RESERVED_PARAMETERS.add(RETURN_CONTENTTYPE_OVERRIDE);
        RESERVED_PARAMETERS.add(COOKIE_NAME);
        RESERVED_PARAMETERS.add(COOKIE_VALUE);
        RESERVED_PARAMETERS.add(COOKIE_PATH);
        RESERVED_PARAMETERS.add(COOKIE_HTTP_ONLY);
        RESERVED_PARAMETERS.add(CONTRIBUTE_POST);
        RESERVED_PARAMETERS.add(MARK_FOR_DELETION);
        RESERVED_PARAMETERS.add(DISABLE_XSS_FILTERING);
        RESERVED_PARAMETERS.add(ALLOWS_MULTIPLE_SUBMITS);
    }

    private static final String PARAM_IS_WEBFLOW_REQUEST = Render.class.getName() + ".isWebflowRequest";

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Render.class);

    private transient String workspace;
    private transient MetricsLoggingService loggingService;
    private transient JahiaTemplateManagerService templateService;
    private transient DefaultPostAction defaultPostAction;
    private transient Action defaultPutAction;
    private transient Action defaultDeleteAction;
    private transient Action webflowAction;
    private transient Map<String, String> defaultContentType = new HashMap<>();

    private transient SettingsBean settingsBean;
    private transient RenderService renderService;
    private transient JCRSessionFactory jcrSessionFactory;
    private transient URLResolverFactory urlResolverFactory;

    private transient Integer sessionExpiryTime = null;
    private Integer cookieExpirationInDays = 1;

    private Set<String> allowedMethods = new HashSet<String>();

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
     * since midnight, January 1, 1970 GMT, or -1 if the time is not known
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
        int index = req.getPathInfo().indexOf('/', 1);
        if (index == -1 || index == req.getPathInfo().length() - 1) {
            throw new JahiaBadRequestException("Invalid path");
        }
        context.setServletPath(req.getServletPath() + req.getPathInfo().substring(0, index));
        return context;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext,
                         Resource resource, long startTime) throws RepositoryException, RenderException, IOException {
        loggingService.startProfiler("MAIN");
        resp.setCharacterEncoding(getSettingsBean().getCharacterEncoding());
        String out = renderService.render(resource, renderContext).trim();
        if (renderContext.getRedirect() != null && !resp.isCommitted()) {
            resp.sendRedirect(renderContext.getRedirect());
        } else {
            resp.setContentType(
                    renderContext.getContentType() != null ? renderContext.getContentType() : getDefaultContentType(resource.getTemplateType()));
            resp.getWriter().print(out);
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
                         URLResolver urlResolver) throws Exception {
        doAction(req, resp, urlResolver, renderContext, null, defaultPutAction, toParameterMapOfListOfString(req));
    }

    public void addCookie(HttpServletRequest req, HttpServletResponse resp) {
        String cookieValue = req.getParameter(COOKIE_VALUE);
        if (req.getParameter(COOKIE_NAME) != null && cookieValue != null) {
            Cookie cookie = new Cookie(req.getParameter(COOKIE_NAME), cookieValue);
            cookie.setHttpOnly(true);
            if (req.getParameter(COOKIE_HTTP_ONLY) != null) {
                cookie.setHttpOnly(Boolean.valueOf(req.getParameter(COOKIE_HTTP_ONLY)));
            }
            //Cookie value should only be a node identifier
            //If workspace is not set, action might have been called in live (rating/forum)
            try {
                jcrSessionFactory.getCurrentUserSession(StringUtils.isEmpty(workspace) ? Constants.LIVE_WORKSPACE : workspace).getNodeByUUID(cookieValue);
            } catch (RepositoryException e) {
                throw new JahiaBadRequestException("Cookie value should be a node UUID");
            }
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
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();

        // LEGACY CASE: old file upload implem, not secured, but can be re-enabled in by configuration.
        // - Handling a mix of all cases: contribute mode, jcrTargetDirectory, ajax upload, default post action setup, all in one method mixed impossible to maintain 75% is dead code
        // - in secured mode, old implem is replaced by a simplified version that is only handling file upload from Ajax requests because it's secured.
        if ((!settingsBean.isJahiaSecuredFileUpload() && checkForUploadedFiles(req, resp, urlResolver.getWorkspace(), urlResolver.getLocale(), parameters, urlResolver)) ||
                // CASE 1: Ajax file upload is the only one secured by permission check on targeted directory
                (settingsBean.isJahiaSecuredFileUpload() && checkForSecuredAjaxFileUpload(req, resp, urlResolver.getWorkspace(), urlResolver.getLocale(), urlResolver))) {
            if (parameters.isEmpty()) {
                // return early, most of the time it means that the request was an Ajax file upload request
                return;
            }
        }

        Action action;
        Resource resource;
        boolean isWebflowRequest = isWebflowRequest(req);
        if (urlResolver.getPath().endsWith(".do") || isWebflowRequest) {
            resource = urlResolver.getResource();
            renderContext.setMainResource(resource);
            try {
                resolveSite(req, urlResolver, renderContext, resource);
            } catch (RepositoryException e) {
                logger.warn("Cannot get site for action context", e);
            }

            if (isWebflowRequest) {
                // CASE 2: Webflow request, mostly used for admin UI, in secured mode:
                // - only allow files from the multipart request for server settings and site settings.
                action = webflowAction;
                if (settingsBean.isJahiaSecuredFileUpload() && isMultipartRequest(req)) {
                    boolean allowsMultipartFiles = renderContext.isEditMode() && resource.getNode() !=null &&
                            (resource.getNode().isNodeType("jnt:globalSettings") || resource.getNode().isNodeType("jnt:virtualsite") );
                    securedMultiPartRequestParsing(req, allowsMultipartFiles, parameters);
                }
            } else {
                // CASE 3: Regular action request, we don't know what could be the usage of the multipart request elements in the action, in secured mode:
                // - ensure action at least required authenticated users OR requires permissions AND then enforce action requirements check and control !
                //   <template:tokenizedForm> is not affecting this security check and will not be able to bypass it.
                action = templateService.getActions().get(resource.getResolvedTemplate());
                if (action != null) {
                    if (settingsBean.isJahiaSecuredFileUpload() && isMultipartRequest(req)) {
                        boolean allowsMultipartFiles = false;
                        try {
                            checkActionRequirements(action, renderContext, urlResolver);
                            allowsMultipartFiles = (action.isRequireAuthenticatedUser() || StringUtils.isNotEmpty(action.getRequiredPermission()));
                        } catch (Exception e) {
                            // Action requirement check failed, we ignore and simply don't allow multipart files in the request.
                        }
                        securedMultiPartRequestParsing(req, allowsMultipartFiles, parameters);
                    }
                }
            }
        } else {
            final String path = urlResolver.getPath();

            String resourcePath = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null,
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

            // CASE 4: Default post action, is aimed to created/modify a node, we will resolve the targeted parent node, and check permissions on it.
            action = defaultPostAction;
            if (settingsBean.isJahiaSecuredFileUpload() && isMultipartRequest(req)) {
                boolean allowsMultipartFiles = false;
                try {
                    JCRNodeWrapper resolvedParent = defaultPostAction.resolveParent(resource.getNode().getSession(), urlResolver, null, false);
                    if (resolvedParent != null) {
                        allowsMultipartFiles = resolvedParent.hasPermission("jcr:addChildNodes");
                    }
                } catch (Exception e) {
                    // error resolving parent, we ignore and simply don't allow multipart files in the request.
                }
                securedMultiPartRequestParsing(req, allowsMultipartFiles, parameters);
            }
        }

        if (action == null) {
            if (urlResolver.getPath().endsWith(".do")) {
                logger.error("Couldn't resolve action named [" + resource.getResolvedTemplate() + "]");
            }
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        } else {
            if (parameters.isEmpty()) {
                // happens in case request is not multipart, queryString parameters are used in this case.
                parameters = toParameterMapOfListOfString(req);
            }
            doAction(req, resp, urlResolver, renderContext, resource, action, parameters);
        }
    }

    private Map<String, List<String>> toParameterMapOfListOfString(HttpServletRequest req) {
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        Map<?, ?> parameterMap = req.getParameterMap();

        boolean doXSSFilter = true;
        String token = parameterMap.get("form-token") != null ? ((String[]) parameterMap.get("form-token"))[0] : null;
        if (token != null) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, List<String>>> toks = (Map<String, Map<String, List<String>>>) req.getSession().getAttribute("form-tokens");
            if (toks != null && toks.containsKey(token)) {
                doXSSFilter = !toks.get(token).containsKey(DISABLE_XSS_FILTERING) || toks.get(token).get(DISABLE_XSS_FILTERING).contains("false");
            }
        }
        for (Object key : parameterMap.keySet()) {
            if (key != null) {
                String[] parameterValues = (String[]) parameterMap.get(key);
                List<String> stringList;
                if (doXSSFilter && !RESERVED_PARAMETERS.contains(key)) {
                    stringList = new ArrayList<String>();
                    for (String s : parameterValues) {
                        stringList.add(xssFilter(s));
                    }
                } else {
                    stringList = new ArrayList<String>(Arrays.asList(parameterValues));
                }
                parameters.put((String) key, stringList);
            }
        }
        return parameters;
    }

    private String xssFilter(String stringValue) {

        // fail fast if we find no start or end of a tag
        if (!stringValue.contains("<") && !stringValue.contains(">")) {
            return stringValue;
        }

        // fix for https://jira.jahia.org/browse/QA-4337, attack with unclosed tags. These regexp will encode unclosed tags.
        stringValue = TAG_MISSING_END_BIGGERTHAN_PATTERN.matcher(stringValue).replaceAll("&lt;$1");
        stringValue = TAG_MISSING_START_LESSERTHAN_PATTERN.matcher(stringValue).replaceAll("$1&gt;");

        Source source = new Source(stringValue);
        OutputDocument outputDocument = new OutputDocument(source);
        List<Element> elements = source.getAllElements();

        for (Element element : elements) {
            if (HTMLElementName.SCRIPT.equals(element.getName())) {
                StartTag startTag = element.getStartTag();
                outputDocument.remove(startTag);
                if (!startTag.isSyntacticalEmptyElementTag()) {
                    EndTag endTag = element.getEndTag();
                    if (endTag != null) {
                        outputDocument.remove(endTag);
                    }
                }
            }
            final Attributes attributes = element.getAttributes();
            if (attributes != null) {
                for (Attribute attribute : attributes) {
                    String name = attribute.getName();
                    if (name != null && EVENT_ATTRIBUTE_NAMES.contains(name.toLowerCase())) {
                        outputDocument.remove(attribute);
                    }
                }
            }
        }

        return outputDocument.toString();
    }

    /**
     * Very unsecure, will allow any files from multipart requests, storing them on file system in temporary folder..
     * Do not use unless you know what you are doing.
     * @deprecated
     */
    @Deprecated
    private boolean checkForUploadedFiles(HttpServletRequest req, HttpServletResponse resp, String workspace,
                                          Locale locale, Map<String, List<String>> parameters,
                                          URLResolver urlResolver)
            throws RepositoryException, IOException {

        if (isMultipartRequest(req)) {
            final String savePath = getSettingsBean().getTmpContentDiskPath();
            final File tmp = new File(savePath);
            if (!tmp.exists()) {
                tmp.mkdirs();
            }
            try {
                final FileUpload fileUpload = new FileUpload(req, savePath, Integer.MAX_VALUE);
                req.setAttribute(FileUpload.FILEUPLOAD_ATTRIBUTE, fileUpload);
                if (fileUpload.getFileItems() != null && fileUpload.getFileItems().size() > 0) {
                    boolean isTargetDirectoryDefined = fileUpload.getParameterNames().contains(TARGETDIRECTORY);
                    boolean isAction = urlResolver.getPath().endsWith(".do");
                    boolean isContributePost = fileUpload.getParameterNames().contains(CONTRIBUTE_POST);
                    final String requestWith = req.getHeader("x-requested-with");
                    boolean isAjaxRequest =
                            req.getHeader("accept") != null && req.getHeader("accept").contains("application/json") && requestWith != null &&
                                    requestWith.contains("XMLHttpRequest") || fileUpload.getParameterMap().isEmpty();
                    List<String> uuids = new LinkedList<String>();
                    List<String> files = new ArrayList<String>();
                    List<String> urls = new LinkedList<String>();
                    // If target directory is defined or if it is an ajax request then save the file now
                    // otherwise we delay the save of the file to the node creation
                    if (!isAction && (isContributePost || isTargetDirectoryDefined || isAjaxRequest)) {
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
                                String name = JCRContentUtils.replaceColon(sessionNode.getPrimaryNodeTypeName()) + "_" + sessionNode.getName();
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
                            session.checkout(targetDirectory);
                            InputStream is = null;
                            JCRNodeWrapper wrapper = null;
                            try {
                                is = itemEntry.getValue().getInputStream();
                                wrapper = targetDirectory.uploadFile(name, is, JCRContentUtils
                                        .getMimeType(name, itemEntry.getValue()
                                                .getContentType()));
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

                    if (isAction || (!isAjaxRequest && !isContributePost)) {
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
        }

        return false;
    }

    /**
     * New method extracted from previous checkForUploadedFiles method to handle Ajax file upload only
     * As we are able to check for permissions on the target directory before processing the multipart request, we can allows file upload accordingly.
     */
    private boolean checkForSecuredAjaxFileUpload(HttpServletRequest req, HttpServletResponse resp, String workspace,
                                                  Locale locale, URLResolver urlResolver) throws RepositoryException {

        boolean isAction = urlResolver.getPath().endsWith(".do");
        final String requestWith = req.getHeader("x-requested-with");
        boolean isAjaxRequest =
                req.getHeader("accept") != null && req.getHeader("accept").contains("application/json") && requestWith != null &&
                        requestWith.contains("XMLHttpRequest");

        if (isMultipartRequest(req) && !isAction && isAjaxRequest) {
            try {
                List<String> uuids = new LinkedList<>();
                List<String> urls = new LinkedList<>();

                String path = urlResolver.getPath();
                String target = path.endsWith("*") ? StringUtils.substringBeforeLast(path, "/") : path;

                JCRSessionWrapper session = jcrSessionFactory.getCurrentUserSession(workspace, locale);
                final JCRNodeWrapper targetDirectory = session.getNode(target);

                FileUpload fileUpload = securedMultiPartRequestParsing(req, targetDirectory.hasPermission("jcr:addChildNodes"), null);
                if (fileUpload.getFileItems() != null && !fileUpload.getFileItems().isEmpty()) {

                    boolean isVersionActivated = fileUpload.getParameterNames().contains(VERSION) && (fileUpload.getParameterValues(VERSION))[0].equals("true");

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
                        session.checkout(targetDirectory);
                        InputStream is = null;
                        JCRNodeWrapper wrapper = null;
                        try {
                            is = itemEntry.getValue().getInputStream();
                            wrapper = targetDirectory.uploadFile(name, is, JCRContentUtils
                                    .getMimeType(name, itemEntry.getValue()
                                            .getContentType()));
                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                        uuids.add(wrapper.getIdentifier());
                        urls.add(wrapper.getAbsoluteUrl(req));
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
            } catch (IOException e) {
                logger.error("Cannot parse multipart data for ajax file upload !", e);
            }
        }

        return false;
    }

    /**
     * Parses the multipart request and returns a FileUpload object if the request is multipart.
     * If the request is not multipart, it returns null.
     * Prior to calling this method,
     * - ensure that the request is indeed multipart by using: isMultipartRequest(req).
     * - calculate and check all necessary permissions and protection to allow or disallow files in the multipart request.
     *
     * @param req the HttpServletRequest
     * @param allowsFiles whether files are allowed in the multipart request
     * @param parameters a map to store parameters from the multipart request
     * @return a FileUpload object or null if the request is not multipart
     * @throws IOException if an I/O error occurs
     */
    private FileUpload securedMultiPartRequestParsing(HttpServletRequest req, boolean allowsFiles, Map<String, List<String>> parameters) throws IOException {
        // check if we already have a FileUpload processed in the request
        FileUpload fileUpload = (FileUpload) req.getAttribute(FileUpload.FILEUPLOAD_ATTRIBUTE);
        if (fileUpload == null) {
            final String savePath = settingsBean.getTmpContentDiskPath();
            final File tmp = new File(savePath);
            if (!tmp.exists()) {
                tmp.mkdirs();
            }

            fileUpload = new FileUpload(req, savePath, Integer.MAX_VALUE, allowsFiles);
            req.setAttribute(FileUpload.FILEUPLOAD_ATTRIBUTE, fileUpload);
            if (fileUpload.getParameterMap() != null && parameters != null) {
                parameters.putAll(fileUpload.getParameterMap());

                // compatibility with old file upload code (mostly used by DefaultPostAction)
                if (fileUpload.getParameterNames().contains(TARGETDIRECTORY)) {
                    List<String> files = new ArrayList<>();
                    final Map<String, DiskFileItem> stringDiskFileItemMap = fileUpload.getFileItems();
                    for (Map.Entry<String, DiskFileItem> itemEntry : stringDiskFileItemMap.entrySet()) {
                        files.add(itemEntry.getValue().getName());
                    }
                    if (!files.isEmpty()) {
                        parameters.put(NODE_NAME, files);
                    }
                }
            }
        }
        return fileUpload;
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
                !CollectionUtils.isEmpty(stringList) && !StringUtils.isBlank(stringList.get(0)) ? StringUtils.substringBeforeLast(stringList.get(0), ";") :
                        null;

        if (!Login.isAuthorizedRedirect(req, stayOnPage, true)) {
            logger.warn("Unauthorized attempt redirect to {}", stayOnPage);
            stayOnPage = null;
        }

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
                    0)) ? stringList.get(0) : null;
            if (formuuid != null) {
                renderedURL = renderedURL + "?ec=" + formuuid;
            }
        }
        if (!StringUtils.isEmpty(renderedURL)) {
            String redirect = resp.encodeRedirectURL(renderedURL);
            if (SettingsBean.getInstance().isDisableJsessionIdParameter()) {
                redirect = SessionidRemovalResponseWrapper.removeJsessionId(redirect);
            }
            if (StringUtils.isEmpty(stayOnPage) || (responseCode == HttpServletResponse.SC_SEE_OTHER)) {
                resp.setHeader("Location", Url.encodeUri(redirect, "UTF-8"));
            }
            if (responseCode == HttpServletResponse.SC_FOUND) {
                resp.sendRedirect(redirect);
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

        if (isInReadOnlyMode()) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
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
            URLResolver urlResolver = urlResolverFactory.createURLResolver(req.getPathInfo(), req.getServerName(), workspace, req);

            req.setAttribute("urlResolver", urlResolver);

            session.setAttribute("workspace", urlResolver.getWorkspace());

            if (sessionExpiryTime != null && session.getMaxInactiveInterval() != sessionExpiryTime * 60) {
                session.setMaxInactiveInterval(sessionExpiryTime * 60);
            }

            RenderContext renderContext =
                    createRenderContext(req, resp, jcrSessionFactory.getCurrentUser());
            renderContext.setWorkspace(urlResolver.getWorkspace());

            urlResolver.setRenderContext(renderContext);
            req.getSession().setAttribute(Constants.SESSION_LOCALE, urlResolver.getLocale());
            jcrSessionFactory.setCurrentLocale(urlResolver.getLocale());
            if (renderContext.isPreviewMode() && req.getParameter(ALIAS_USER) != null && !JahiaUserManagerService.isGuest(jcrSessionFactory.getCurrentUser())) {
                JahiaUserManagerService userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
                JCRUserNode userNode = userManagerService.lookupUser(req.getParameter(ALIAS_USER), urlResolver.getSiteKey());
                if (userNode != null) {
                    jcrSessionFactory.setCurrentAliasedUser(userNode.getJahiaUser());
                }
            }

            // check permission
            try {
                if (!hasAccess(urlResolver.getNode())) {
                    if (JahiaUserManagerService.isGuest(jcrSessionFactory.getCurrentUser())) {
                        throw new JahiaUnauthorizedException();
                    } else {
                        throw new JahiaForbiddenAccessException();
                    }
                }
            } catch (AccessDeniedException e) {
                if (SettingsBean.getInstance().getString("protectedResourceAccessStrategy", "silent").equalsIgnoreCase("silent")) {
                    throw new PathNotFoundException("'" + urlResolver.getPath() + "' not found");
                }
            } catch (RepositoryException e) {
                if (e instanceof NamespaceException || e.getCause() instanceof NameException) {
                    // Stop execution but wrap in PathNotFoundException to prevent
                    // generating stack trace in jahia-errors; log 404 instead
                    logger.debug(e.getMessage());
                    throw new PathNotFoundException("'" + urlResolver.getPath() + "' not found");
                } else if (!(e instanceof PathNotFoundException)) {
                    throw e;
                }
            }

            renderContext.setSiteInfo(urlResolver.getSiteInfo());

            if (renderContext.isPreviewMode() && req.getParameter(PREVIEW_DATE) != null && !JahiaUserManagerService.isGuest(jcrSessionFactory.getCurrentUser())) {
                Calendar previewDate = Calendar.getInstance();
                previewDate.setTime(new Date(new Long(req.getParameter(PREVIEW_DATE))));
                jcrSessionFactory.setCurrentPreviewDate(previewDate);
            }
            if (method.equals(METHOD_GET)) {
                Resource resource;
                resource = urlResolver.getResource();
                if (!StringUtils.isEmpty(urlResolver.getRedirectUrl()) && (StringUtils.isEmpty(resource.getTemplate()) || StringUtils.equals(resource.getTemplate(), "default"))) {
                    Map<String, List<String>> parameters = new HashMap<String, List<String>>();
                    parameters.put(NEW_NODE_OUTPUT_FORMAT, LIST_WITH_EMPTY_STRING);
                    parameters.put(REDIRECT_HTTP_RESPONSE_CODE, REDIRECT_CODE_MOVED_PERMANENTLY);

                    performRedirect(urlResolver.getRedirectUrl(), StringUtils.isEmpty(urlResolver.getVanityUrl()) ?
                            "/" + urlResolver.getLocale().toString() + urlResolver.getPath() :
                            urlResolver.getVanityUrl(), req, resp, parameters, false);
                } else {
                    renderContext.setMainResource(resource);
                    if (renderContext.getSite() == null) {
                        resolveSite(req, urlResolver, renderContext, resource);
                    }
//                    resource.pushWrapper("wrapper.fullpage");

                    if (urlResolver.getPath().endsWith(".do")) {
                        Action action = templateService.getActions().get(resource.getResolvedTemplate());
                        Map<String, List<String>> parameters = toParameterMapOfListOfString(req);
                        if (action != null) {
                            doAction(req, resp, urlResolver, renderContext, resource, action, parameters);
                        } else {
                            logger.error("Action {} does not exist", resource.getResolvedTemplate());
                            throw new PathNotFoundException("Action does not exist");
                        }
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
            FileUpload fileUpload = (FileUpload) req.getAttribute(FileUpload.FILEUPLOAD_ATTRIBUTE);
            if (fileUpload != null) {
                fileUpload.disposeItems();
                fileUpload.markFilesAsConsumed();
            }
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

    public static boolean isWebflowRequest(HttpServletRequest req) {
        boolean webflowRequest = false;
        Boolean flag = (Boolean) req.getAttribute(PARAM_IS_WEBFLOW_REQUEST);
        if (flag == null) {
            if (req.getMethod().equals(METHOD_POST)) {
                Enumeration<?> parameterNames = req.getParameterNames();
                while (parameterNames.hasMoreElements()) {
                    String s = (String) parameterNames.nextElement();
                    if (s.startsWith("webflowexecution")) {
                        webflowRequest = true;
                        break;
                    }
                }
            }
            req.setAttribute(PARAM_IS_WEBFLOW_REQUEST, Boolean.valueOf(webflowRequest));
        } else {
            webflowRequest = flag.booleanValue();
        }
        return webflowRequest;
    }

    private void resolveSite(HttpServletRequest req, URLResolver urlResolver, RenderContext renderContext, Resource resource) throws RepositoryException, JahiaForbiddenAccessException {
        // If Site has not been resolved by the servlet (so far only dashboard mode is doing that
        JCRSiteNode site = resource.getNode().getResolveSite();
        if (!Url.isLocalhost(req.getServerName()) && !renderContext.isEditMode()) {
            JCRSessionWrapper session1 = resource.getNode().getSession();
            if (urlResolver.getSiteKey() != null &&
                    (site == null || !site.getSiteKey().equals(urlResolver.getSiteKey()))) {
                site = (JCRSiteNode) session1.getNode("/sites/" + urlResolver.getSiteKey());
            } else if (renderContext.isLiveMode() && urlResolver.getSiteKeyByServerName() != null &&
                    (site == null || !site.getSiteKey().equals(
                            urlResolver.getSiteKeyByServerName()))) {
                site = (JCRSiteNode) session1.getNode("/sites/" + urlResolver.getSiteKeyByServerName());
            }
        }
        String jsite = null;
        HttpServletRequest request = renderContext.getRequest();
        if (request != null) {
            jsite = request.getParameter("jsite");
        }
        if (jsite == null && renderContext.getMainResource() != null) {
            jsite = (String) renderContext.getMainResource().getModuleParams().get("jsite");
        }
        if (jsite != null) {
            try {
                site = (JCRSiteNode) resource.getNode().getSession().getNodeByIdentifier(jsite);
            } catch (ItemNotFoundException e) {
                if (JahiaUserManagerService.isGuest(jcrSessionFactory.getCurrentUser())) {
                    throw new JahiaUnauthorizedException();
                } else {
                    throw new JahiaForbiddenAccessException();
                }
            }
        }
        if (resource.getNode().getPath().startsWith("/sites/") &&
                (site == null || (!site.getPath().startsWith("/modules/") &&
                        !site.isAllowsUnlistedLanguages() &&
                        !(renderContext.isLiveMode() ? site.getActiveLiveLanguagesAsLocales().contains(
                                urlResolver.getLocale()) : site.getLanguagesAsLocales().contains(
                                urlResolver.getLocale()))))) {
            throw new PathNotFoundException("This language does not exist on this site");
        }
        renderContext.setSite(site);
    }


    protected boolean isDisabled() {
        return false;
    }

    protected boolean isInReadOnlyMode() {
        return false;
    }

    private void doAction(HttpServletRequest req, HttpServletResponse resp, URLResolver urlResolver,
                          RenderContext renderContext, Resource resource, Action action,
                          Map<String, List<String>> parameters) throws Exception {
        final Action originalAction = action;

        int tokenResult = TokenChecker.NO_TOKEN;
        if (!isWebflowRequest(req)) {
            tokenResult = TokenChecker.checkToken(req, resp, parameters);
        }

        switch (tokenResult) {
            case TokenChecker.NO_TOKEN:
                break;
            case TokenChecker.INVALID_TOKEN:
                throw new AccessDeniedException("Invalid token.");
            case TokenChecker.INVALID_HIDDEN_FIELDS:
                throw new AccessDeniedException();
            case TokenChecker.VALID_TOKEN:
                if (req.getSession().getAttribute("formDatas") != null
                        || req.getSession().getAttribute("formError") != null) {
                    req.getSession().removeAttribute("formDatas");
                    req.getSession().removeAttribute("formError");
                }
                action = new SystemAction() {
                    @Override
                    public ActionResult doExecuteAsSystem(HttpServletRequest req, RenderContext renderContext, JCRSessionWrapper systemSession, Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
                        return originalAction.doExecute(req, renderContext, resource, systemSession, parameters, urlResolver);
                    }
                };
        }

        checkActionRequirements(action, originalAction, renderContext, urlResolver);

        JCRSessionWrapper session = null;
        if (resource != null) {
            session = resource.getNode().getSession();
        } else {
            session = JCRSessionFactory.getInstance().getCurrentUserSession(urlResolver.getWorkspace(),
                    urlResolver.getLocale());
        }
        ActionResult result;
        try {
            result = action.doExecute(req, renderContext, resource, session, parameters, urlResolver);
        } catch (Exception e) {
            logger.error("An error occurs when executing action {}", action.getName(), e);
            throw e;
        }
        if (result != null) {
            boolean returnJSON = "json".equals(parameters.get(RETURN_CONTENTTYPE) != null ? parameters.get(RETURN_CONTENTTYPE).get(0) : "")
                    || req.getHeader("accept") != null && req.getHeader("accept").contains("application/json");
            if (result.getResultCode() < 300 || returnJSON) {
                resp.setStatus(result.getResultCode());
                addCookie(req, resp);
                if (result.getJson() != null && returnJSON) {
                    try {
                        String contentType = parameters.get(RETURN_CONTENTTYPE_OVERRIDE) != null ? StringUtils.defaultIfEmpty(parameters.get(RETURN_CONTENTTYPE_OVERRIDE).get(0), null) : null;
                        if (contentType == null) {
                            contentType = "application/json; charset=UTF-8";
                        } else if (!contentType.toLowerCase().contains("charset")) {
                            // append the charset
                            contentType += "; charset=UTF-8";
                        }
                        resp.setContentType(contentType);
                        result.getJson().write(resp.getWriter());
                    } catch (JSONException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    if (!result.isAbsoluteUrl()) {
                        performRedirect(result.getUrl(), urlResolver.getPath(), req, resp, parameters, false);
                    } else {
                        String redirectUrl = resp.encodeRedirectURL(result.getUrl());
                        if (SettingsBean.getInstance().isDisableJsessionIdParameter()) {
                            redirectUrl = SessionidRemovalResponseWrapper.removeJsessionId(redirectUrl);
                        }
                        resp.sendRedirect(redirectUrl);
                    }
                }
            } else {
                resp.sendError(result.getResultCode());
            }
        }
    }

    private static void checkActionRequirements(Action action, final Action originalAction, RenderContext renderContext,
                                                URLResolver urlResolver) throws RepositoryException {
        if (action.getRequiredMethods() != null && !action.getRequiredMethods().contains(renderContext.getRequest().getMethod())) {
            throw new RequiredActionModeException("Action requires " + action.getRequiredMethods());
        }

        if (!(action instanceof SystemAction)) {
            if (action.getRequiredWorkspace() != null
                    && !action.getRequiredWorkspace().equals(urlResolver.getWorkspace())) {
                throw new PathNotFoundException("Action is not supported for this workspace");
            }
            if (action.isRequireAuthenticatedUser() && !renderContext.isLoggedIn()) {
                throw new AccessDeniedException("Action '" + action.getName() + "' requires an authenticated user");
            }
            if (StringUtils.isNotEmpty(action.getRequiredPermission()) && !action.isPermitted(urlResolver.getNode())) {
                throw new AccessDeniedException("Action '" + action.getName() + "' requires '" + action.getRequiredPermission() + "' permission.");
            }
        }
    }

    /**
     * Performs all required checks for this action.
     *
     * @param action        the action to check requirements for
     * @param renderContext current render context instance
     * @param urlResolver   the URL resolver instance
     * @throws RepositoryException in case of requirements violation
     * @since 7.2.3.2
     */
    public static void checkActionRequirements(Action action, RenderContext renderContext, URLResolver urlResolver)
            throws RepositoryException {
        checkActionRequirements(action, null, renderContext, urlResolver);
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

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
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

    public void setDefaultPostAction(DefaultPostAction defaultPostActionResult) {
        this.defaultPostAction = defaultPostActionResult;
    }

    public void setDefaultPutAction(Action defaultPutActionResult) {
        this.defaultPutAction = defaultPutActionResult;
    }

    public void setDefaultDeleteAction(Action defaultDeleteAction) {
        this.defaultDeleteAction = defaultDeleteAction;
    }

    public void setWebflowAction(Action webflowAction) {
        this.webflowAction = webflowAction;
    }

    public static Set<String> getReservedParameters() {
        return RESERVED_PARAMETERS;
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
        this.cookieExpirationInDays = cookieExpirationInDays;
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

    public String getDefaultContentType(String templateType) {
        if (templateType != null && defaultContentType.get(templateType) != null) {
            return defaultContentType.get(templateType);
        }
        return "text/html; charset=UTF-8";
    }

    public void setDefaultContentType(Map<String, String> defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    public SettingsBean getSettingsBean() {
        return settingsBean;
    }
}
