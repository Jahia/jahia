/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXException;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Content export handler.
 *
 * @author rincevent
 * @since JAHIA 6.5
 *        Created : 2 avr. 2010
 */
public class Export extends JahiaController implements ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(Export.class);
    public static final String CLEANUP = "cleanup";
    private static final String CONTROLLER_MAPPING = "/export";
    private static final String EXPORT_SITES_REQUIRED_PERMISSION = "adminVirtualSites";
    private static final Pattern URI_PATTERN = Pattern.compile(CONTROLLER_MAPPING + "/("
            + Constants.LIVE_WORKSPACE + "|" + Constants.EDIT_WORKSPACE + ")/(.*)\\.(xml|zip)");
    private static final String MEDIATYPE_ZIP = "application/zip";
    private static final String MEDIATYPE_XML = "text/xml";
    public static String getExportServletPath() {
        // TODO move this into configuration
        return "/cms" + CONTROLLER_MAPPING;
    }

    private String cleanupXsl;
    
    private boolean downloadExportedXmlAsFile;

    private ImportExportService importExportService;
    private String templatesCleanupXsl;

    /**
     * Process the request and return a ModelAndView object which the DispatcherServlet
     * will render. A <code>null</code> return value is not an error: It indicates that
     * this object completed request processing itself, thus there is no ModelAndView
     * to render.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render, or <code>null</code> if handled directly
     * @throws Exception in case of errors
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        try {

            checkUserLoggedIn();

            Matcher m = getMatcher(request);
            String workspace = m.group(1);
            String nodePath = String.format("/%s",m.group(2));
            String exportFormat = m.group(3);
            JCRNodeWrapper exportRoot = null;
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace);
            Map<String, Object> params = getRequestParams(request);

            if (StringUtils.isNotEmpty(request.getParameter("exportformat"))) {
                exportFormat = validateExportFormatType(request.getParameter("exportformat"));
            }

            switch (exportFormat.trim().toLowerCase()) {
                case "all":
                    handleExportFormatTypeAll(response, params, session);
                    break;
                case "site":
                    handleExportFormatTypeSite(request, response, params, session);
                    break;
                case "xml":
                    exportRoot = request.getParameter("root") != null ? session.getNode(request.getParameter("root")) : null;
                    handleExportFormatTypeXML(request, response, nodePath, params, session, exportRoot);
                    break;
                case "zip":
                    exportRoot = request.getParameter("root") != null ? session.getNode(request.getParameter("root")) : null;
                    handleExportFormatTypeZip(request, response, nodePath, params, session, exportRoot);
                    break;
                default:
                    logger.error("Export format type entered is {} which is not recognized or handled yet", exportFormat);
                    throw new JahiaBadRequestException("exportFormat parameter needs to be one of ALL, SITE, XML, or ZIP");
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot export due to some IO exception ", e);
            } else {
                logger.warn("Cannot export due to some IO exception {}", e.getMessage());
            }
            DefaultErrorHandler.getInstance().handle(e, request, response);
        } catch (Exception e) {
            logger.error("Cannot export", e);
            DefaultErrorHandler.getInstance().handle(e, request, response);
        }

        return null;
    }

    private String validateExportFormatType(String exportformat) {
        String formatType = exportformat.trim().toLowerCase();
        if (formatType.equals("zip") || formatType.equals("xml") || formatType.equals("site") || formatType.equals("all")) {
            return formatType;
        }
        logger.error("Unable to handle export format type {}.", exportformat);
        throw new JahiaBadRequestException("Unable to handle export format");
    }

    private Matcher getMatcher(HttpServletRequest request) {
        Matcher m = StringUtils.isNotEmpty(request.getPathInfo()) ? URI_PATTERN.matcher(request.getPathInfo()) : null;
        if (m == null || !m.matches()) {
            throw new JahiaBadRequestException("Requested URI '" + request.getRequestURI()
                    + "' is malformed");
        }
        return m;
    }

    private void handleExportFormatTypeZip(HttpServletRequest request, HttpServletResponse response, String nodePath, Map<String, Object> params,
            JCRSessionWrapper session, JCRNodeWrapper exportRoot)
            throws RepositoryException, IOException, SAXException, TransformerException, JahiaForbiddenAccessException {
        JCRNodeWrapper sessionNode = session.getNode(nodePath);
        OutputStream outputStream = response.getOutputStream();
        Cookie exportedNodeCookie = new Cookie("exportedNode", sessionNode.getIdentifier());
        exportedNodeCookie.setSecure(request.isSecure());
        response.setContentType(MEDIATYPE_ZIP);
        //make sure this file is not cached by the client (or a proxy middleman)
        WebUtils.setNoCacheHeaders(response);

        if ("template".equals(request.getParameter(CLEANUP))) {
            params.put(ImportExportService.XSL_PATH, templatesCleanupXsl);
        } else if ("simple".equals(request.getParameter(CLEANUP))) {
            params.put(ImportExportService.XSL_PATH, cleanupXsl);
        }
        if (request.getParameter("live") == null || Boolean.valueOf(request.getParameter("live"))) {
            params.put(ImportExportService.INCLUDE_LIVE_EXPORT, Boolean.TRUE);
        }
        exportedNodeCookie.setMaxAge(60);
        exportedNodeCookie.setPath("/");
        response.addCookie(exportedNodeCookie);
        importExportService.exportZip(sessionNode, exportRoot, outputStream, params);
        outputStream.close();
    }

    private void handleExportFormatTypeXML(HttpServletRequest request, HttpServletResponse response, String nodePath, Map<String, Object> params,
            JCRSessionWrapper session, JCRNodeWrapper exportRoot)
            throws RepositoryException, IOException, SAXException, TransformerException {
        JCRNodeWrapper sessionNode = session.getNode(nodePath);
        OutputStream outputStream = response.getOutputStream();
        Cookie exportedNodeCookie = new Cookie("exportedNode", sessionNode.getIdentifier());
        exportedNodeCookie.setSecure(request.isSecure());
        response.setContentType(MEDIATYPE_XML);
        //make sure this file is not cached by the client (or a proxy middleman)
        WebUtils.setNoCacheHeaders(response);
        if (downloadExportedXmlAsFile) {
            WebUtils.setFileDownloadHeaders(response, String.format("%s.xml", StringUtils.substringBeforeLast(sessionNode.getName(), ".")));
        }

        if ("template".equals(request.getParameter(CLEANUP))) {
            params.put(ImportExportService.XSL_PATH, templatesCleanupXsl);
        } else if ("simple".equals(request.getParameter(CLEANUP))) {
            params.put(ImportExportService.XSL_PATH, cleanupXsl);
        }
        exportedNodeCookie.setMaxAge(60);
        exportedNodeCookie.setPath("/");
        response.addCookie(exportedNodeCookie);
        //No export log for the node export
        importExportService.exportNode(sessionNode, exportRoot, outputStream, params);
        outputStream.close();
    }

    private void handleExportFormatTypeSite(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params,
            JCRSessionWrapper session)
            throws JahiaException, IOException, RepositoryException, SAXException, TransformerException {

        List<JCRSiteNode> sites = getJcrSiteNodes(request);
        JahiaUser currentUser = session.getUser();
        boolean isElevated = false;
        try {
            if (!sites.isEmpty()) {
                isElevated = elevateSecurityPrivilege(session);
                response.setContentType(MEDIATYPE_ZIP);
                //make sure this file is not cached by the client (or a proxy middleman)
                WebUtils.setNoCacheHeaders(response);

                params.put(ImportExportService.INCLUDE_ROLES, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_MOUNTS, Boolean.TRUE);
                params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
                params.put(ImportExportService.XSL_PATH, cleanupXsl);
                params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_TEMPLATES, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_SITE_INFOS, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_DEFINITIONS, Boolean.TRUE);
                if (request.getParameter("live") == null || Boolean.valueOf(request.getParameter("live"))) {
                    params.put(ImportExportService.INCLUDE_LIVE_EXPORT, Boolean.TRUE);
                }
                handleUserParameter(request, params);

                OutputStream outputStream = response.getOutputStream();
                importExportService.exportSites(outputStream, params, sites);
                outputStream.close();
            }
        } finally {
            if (isElevated) {
                restoreSecurityPrivilege(currentUser);
            }
        }
    }

    private void handleUserParameter(HttpServletRequest request, Map<String, Object> params) {
        final String userParameter = "users";
        if (request.getParameter(userParameter) == null && SettingsBean.getInstance().getPropertiesFile().getProperty("siteExportUsersDefaultValue") != null) {
            boolean siteExportUsersDefaultValue = Boolean
                    .parseBoolean(SettingsBean.getInstance().getPropertiesFile().getProperty("siteExportUsersDefaultValue"));
            if (siteExportUsersDefaultValue) {
                params.put(ImportExportService.INCLUDE_USERS, Boolean.TRUE);
            } else {
                params.remove(ImportExportService.INCLUDE_USERS);
            }
        } else if (request.getParameter(userParameter) != null) {
            if (Boolean.parseBoolean(request.getParameter(userParameter))) {
                params.put(ImportExportService.INCLUDE_USERS, Boolean.TRUE);
            } else {
                params.remove(ImportExportService.INCLUDE_USERS);
            }
        } else {
            params.put(ImportExportService.INCLUDE_USERS, Boolean.TRUE);
        }
    }

    private List<JCRSiteNode> getJcrSiteNodes(HttpServletRequest request) throws JahiaException {
        List<JCRSiteNode> sites = new ArrayList<>();
        String[] sitekeys = request.getParameterValues("sitebox");
        if (sitekeys != null) {
            for (String sitekey : sitekeys) {
                JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(sitekey);
                sites.add((JCRSiteNode) site);
            }
        }
        return sites;
    }

    private void handleExportFormatTypeAll(HttpServletResponse response, Map<String, Object> params, JCRSessionWrapper session)
            throws IOException, JahiaException, RepositoryException, SAXException, TransformerException {
        JahiaUser currentUser = session.getUser();
        boolean isElevated = false;
        try {
            isElevated = elevateSecurityPrivilege(session);
            response.setContentType(MEDIATYPE_ZIP);
            //make sure this file is not cached by the client (or a proxy middleman)
            WebUtils.setNoCacheHeaders(response);

            params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
            params.put(ImportExportService.INCLUDE_TEMPLATES, Boolean.TRUE);
            params.put(ImportExportService.INCLUDE_SITE_INFOS, Boolean.TRUE);
            params.put(ImportExportService.INCLUDE_DEFINITIONS, Boolean.TRUE);
            params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
            params.put(ImportExportService.XSL_PATH, cleanupXsl);

            OutputStream outputStream = response.getOutputStream();
            importExportService.exportAll(outputStream, params);
            outputStream.close();
        } finally {
            if (isElevated) {
                restoreSecurityPrivilege(currentUser);
            }
        }
    }

    /**
     * Set the current user of the session to root
     */
    private boolean elevateSecurityPrivilege(JCRSessionWrapper session) throws JahiaForbiddenAccessException, RepositoryException {
        if (!session.getUser().isRoot()) {
            checkAuthorization(session);
            logger.debug("Elevating security");
            JCRSessionFactory.getInstance().setCurrentUser(JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser());
        }
        return !JCRSessionFactory.getInstance().getCurrentUser().equals(session.getUser());
    }

    /**
     * Restore access back to the current user before
     */
    private void restoreSecurityPrivilege(JahiaUser user) {
        logger.debug("Restoring security permission back");
        JCRSessionFactory.getInstance().setCurrentUser(user);
    }

    /**
     * Check if current user has permission to perform export requests
     * @param session
     * @throws RepositoryException
     * @throws JahiaForbiddenAccessException
     */
    private void checkAuthorization(JCRSessionWrapper session) throws RepositoryException, JahiaForbiddenAccessException {
        if (JahiaUserManagerService.isGuest(session.getUser())) {
            throw new JahiaUnauthorizedException("User guest is not allowed to export site content");
        } else if (!session.getRootNode().hasPermission(EXPORT_SITES_REQUIRED_PERMISSION)) {
            throw new JahiaForbiddenAccessException("User does not have sufficient permission to perform export of site content");
        }
    }

    /**
     * Get the parameters from the request
     * @param request
     * @return parameter mapping from the request
     * @throws IOException
     * @throws JahiaBadRequestException
     */
    private Map<String, Object> getRequestParams(HttpServletRequest request) throws IOException {
        Map<String, Object> params = new HashMap<>(6);
        params.put(ImportExportService.VIEW_CONTENT, Boolean.valueOf(request.getParameter("viewContent")));
        params.put(ImportExportService.VIEW_VERSION, Boolean.valueOf(request.getParameter("viewVersion")));
        params.put(ImportExportService.VIEW_ACL, Boolean.valueOf(request.getParameter("viewAcl")));
        params.put(ImportExportService.VIEW_METADATA, Boolean.valueOf(request.getParameter("viewMetadata")));
        params.put(ImportExportService.VIEW_JAHIALINKS, Boolean.valueOf(request.getParameter("viewLinks")));
        params.put(ImportExportService.VIEW_WORKFLOW, Boolean.valueOf(request.getParameter("viewWorkflow")));

        String exportPath = request.getParameter("exportPath");
        if (StringUtils.isNotBlank(exportPath)) {
            String serverDirectory = ImportExportBaseService.updatedServerDirectoryPath(exportPath);
            if (serverDirectory != null && ImportExportBaseService.isValidServerDirectory(serverDirectory)) {
                params.put(ImportExportService.SERVER_DIRECTORY, serverDirectory);
            } else {
                logger.error("Failed validation to the path {}. Check logs for more details", serverDirectory);
                throw new JahiaBadRequestException("exportPath parameter is invalid.");
            }
        }
        return params;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        cleanupXsl = servletContext.getRealPath("/WEB-INF/etc/repository/export/" + "cleanup.xsl");
        templatesCleanupXsl = servletContext.getRealPath("/WEB-INF/etc/repository/export/"
                + "templatesCleanup.xsl");
    }

    public void setImportExportService(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    public void setDownloadExportedXmlAsFile(boolean downloadExportedXmlAsFile) {
        this.downloadExportedXmlAsFile = downloadExportedXmlAsFile;
    }
}
