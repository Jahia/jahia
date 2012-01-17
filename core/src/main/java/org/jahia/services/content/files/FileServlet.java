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

package org.jahia.services.content.files;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jcr.Binary;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.servlets.RangeUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.spi.commons.conversion.MalformedPathException;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.Cache;
import org.jahia.services.content.*;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.render.filter.ContextPlaceholdersReplacer;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jahia.services.content.JCRTemplate.*;

/**
 * Serves resources from the JCR repository.
 *
 * @author Thomas Draier
 * Date: Oct 13, 2008
 * Time: 2:08:59 PM
 */
public class FileServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(FileServlet.class);

    private static final long serialVersionUID = -414690364676304370L;

    protected static final Pattern UNDERSCORES = Pattern.compile("___");

    protected FileCacheManager cacheManager;

    protected int cacheThreshold = 64 * 1024;

    protected boolean cacheForLoggedUsers = true;

    protected String characterEncoding = null;

    protected MetricsLoggingService loggingService;

    private JCRSessionFactory sessionFactory;

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {
        long timer = System.currentTimeMillis();
        int code = HttpServletResponse.SC_OK;
        try {
            FileKey fileKey = parseKey(req);

            if (fileKey != null && fileKey.getWorkspace() != null
                    && StringUtils.isNotEmpty(fileKey.getPath())) {

                Cache<String, FileLastModifiedCacheEntry> lastModifiedCache = cacheManager.getLastModifiedCache();

                FileLastModifiedCacheEntry lastModifiedEntry = lastModifiedCache.get(fileKey
                        .getCacheKey());
                if (isNotModified(fileKey, lastModifiedEntry, req, res)) {
                    // resource is not changed
                    code = HttpServletResponse.SC_NOT_MODIFIED;
                    res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    logAccess(fileKey, req, "ok-not-modified");
                    return;
                }

                Cache<String, Map<String, FileCacheEntry>> contentCache = cacheManager.getContentCache();

                Map<String, FileCacheEntry> entries = contentCache.get(fileKey.getCacheKey());
                FileCacheEntry fileEntry = entries != null ? entries.get(fileKey.getThumbnail())
                        : null;
                if (fileEntry == null) {
                    JCRNodeWrapper n = getNode(fileKey);
                    if (n == null || !n.isFile()) {
                        // cannot find it or it is not a file
                        code = HttpServletResponse.SC_NOT_FOUND;
                        res.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }

                    Date lastModifiedDate = n.getLastModifiedAsDate();
                    long lastModified = lastModifiedDate != null ? lastModifiedDate.getTime() : 0;
                    String eTag = generateETag(n.getIdentifier(), lastModified);
                    lastModifiedEntry = new FileLastModifiedCacheEntry(eTag, lastModified);
                    lastModifiedCache.put(fileKey.getCacheKey(), lastModifiedEntry);

                    if (isNotModified(fileKey, lastModifiedEntry, req, res)) {
                        // resource is not changed
                        code = HttpServletResponse.SC_NOT_MODIFIED;
                        res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                        logAccess(fileKey, req, "ok-not-modified");
                        return;
                    }

                    fileEntry = getFileEntry(fileKey, n, lastModifiedEntry);
                    if (fileEntry != null && fileEntry.getData() != null) {
                        entries = contentCache.get(fileKey.getCacheKey());
                        if (entries == null) {
                            entries = new HashMap<String, FileCacheEntry>(1);
                        }
                        entries.put(fileKey.getThumbnail(), fileEntry);
                        contentCache.put(fileKey.getCacheKey(), entries);
                        logAccess(fileKey, req, "ok");
                    }
                } else {
                    if (lastModifiedEntry == null) {
                        lastModifiedEntry = new FileLastModifiedCacheEntry(fileEntry.getETag(), fileEntry.getLastModified());
                        lastModifiedCache.put(fileKey.getCacheKey(), lastModifiedEntry);
                    }
                    logAccess(fileKey, req, "ok-cached");
                    if (logger.isDebugEnabled()) {
                        logger.debug("Serving cached file entry {}", fileKey.toString());
                    }
                }

                if (fileEntry != null) {
                    List<RangeUtils.Range> ranges = RangeUtils.parseRange(req, res, fileEntry.getETag(), fileEntry.getLastModified(), fileEntry.getContentLength());

                    if (fileKey.getPath().indexOf('%', fileKey.getPath().lastIndexOf('/')) != -1) {
                        res.setHeader(
                                "Content-Disposition",
                                "inline; filename=\""
                                        + JCRContentUtils.unescapeLocalNodeName(StringUtils
                                                .substringAfterLast(fileKey.getPath(), "/")) + "\"");
                    }
                    res.setDateHeader("Last-Modified", fileEntry.getLastModified());
                    res.setHeader("ETag", fileEntry.getETag());
                    InputStream is = null;

                    if (fileEntry.getData() != null) {
                        // writing in-memory data
                        is = new ByteArrayInputStream(fileEntry.getData());
                    } else if (fileEntry.getBinary() != null) {
                        // spool from an input stream
                        is = fileEntry.getBinary().getStream();
                    } else {
                        code = HttpServletResponse.SC_NOT_FOUND;
                        res.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }

                    if (ranges == null || (ranges == RangeUtils.FULL)) {
                        res.setContentType(fileEntry.getMimeType());
                        if (fileEntry.getContentLength() <= Integer.MAX_VALUE) {
                            res.setContentLength((int) fileEntry.getContentLength());
                        } else {
                            res.setHeader("Content-Length", Long.toString(fileEntry.getContentLength()));
                        }
                        ServletOutputStream os = res.getOutputStream();
                        IOUtils.copy(is, os);
                        os.flush();
                        os.close();
                    } else {
                        res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                        if (ranges.size() == 1) {
                            res.setContentType(fileEntry.getMimeType());
                            RangeUtils.Range range = (RangeUtils.Range) ranges.get(0);
                            res.addHeader("Content-Range", "bytes "
                                               + range.start
                                               + "-" + range.end + "/"
                                               + range.length);
                            long length = range.end - range.start + 1;
                            if (length < Integer.MAX_VALUE) {
                                res.setContentLength((int) length);
                            } else {
                                // Set the content-length as String to be able to use a long
                                res.setHeader("Content-Length", "" + length);
                            }
                            ServletOutputStream os = res.getOutputStream();
                            RangeUtils.copy(is, os, range);
                            IOUtils.closeQuietly(is);
                            IOUtils.closeQuietly(os);

                        } else {
                            res.setContentType("multipart/byteranges; boundary="
                                                + RangeUtils.MIME_SEPARATION);

                            try {
                                res.setBufferSize(RangeUtils.getOutput());
                            } catch (IllegalStateException e) {
                                // Silent catch
                            }
                            ServletOutputStream os = res.getOutputStream();
                            RangeUtils.copy(is, os, ranges.iterator(),
                                    fileEntry.getMimeType());
                            IOUtils.closeQuietly(is);
                            IOUtils.closeQuietly(os);
                        }
                    }
                    if ((fileEntry.getData() == null) && (fileEntry.getBinary() != null)) {
                        fileEntry.getBinary().dispose();
                        fileEntry.setBinary(null);
                    }

                } else {
                    code = HttpServletResponse.SC_NOT_FOUND;
                    res.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else {
                code = HttpServletResponse.SC_NOT_FOUND;
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get file", e);

            code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Served [{}] with status code [{}] in [{}ms]",
                        new Object[] {
                                req.getRequestURI()
                                        + (req.getQueryString() != null ? "?"
                                                + req.getQueryString() : ""), code,
                                (System.currentTimeMillis() - timer) });
            }
        }
    }

    protected String generateETag(String uuid, long lastModified) {
        return "\"" + StringUtils.replace(StringUtils.defaultIfEmpty(uuid, "unknown"), "\"", "")
                + "-" + lastModified + "\"";
    }

    protected JCRNodeWrapper getContentNode(JCRNodeWrapper n, String thumbnail)
            throws RepositoryException {
        JCRNodeWrapper content;
        if (StringUtils.isNotEmpty(thumbnail) && n.hasNode(thumbnail)) {
            // thumbnail requested -> try to find it
            content = n.getNode(thumbnail);
            if (!content.isNodeType(Constants.NT_RESOURCE)) {
                content = null;
            }
        } else {
            try {
                content = n.getNode(Constants.JCR_CONTENT);
            } catch (PathNotFoundException e) {
                logger.warn("Cannot find " + Constants.JCR_CONTENT + " sub-node in the {} node.",
                        n.getPath());
                content = null;
            }
        }

        return content;
    }

    protected FileCacheEntry getFileEntry(FileKey fileKey, JCRNodeWrapper node,
            FileLastModifiedCacheEntry lastModifiedEntry) throws RepositoryException, IOException {
        FileCacheEntry fileEntry = null;

        JCRNodeWrapper content = getContentNode(node, fileKey.getThumbnail());
        if (content == null) {
            return null;
        }

        Binary binary = null;
        try {
            binary = content.getProperty(Constants.JCR_DATA).getBinary();
        } catch (PathNotFoundException e) {
            logger.warn("Unable to get " + Constants.JCR_DATA + " property for node {}",
                    content.getPath());
            return null;
        }

        int contentLength = (int) binary.getSize();

        fileEntry = new FileCacheEntry(lastModifiedEntry.getETag(), content.getProperty(
                Constants.JCR_MIMETYPE).getString(), contentLength,
                lastModifiedEntry.getLastModified());
        if (contentLength <= cacheThreshold && isVisibleForGuest(node)) {
            InputStream is = null;
            try {
                is = binary.getStream();
                fileEntry.setData(IOUtils.toByteArray(is));
            } finally {
                IOUtils.closeQuietly(is);
                binary.dispose();
            }
        } else {
            fileEntry.setBinary(binary);
        }

        return fileEntry;
    }

    protected JCRNodeWrapper getNode(FileKey fileKey) {
        JCRNodeWrapper n = null;
        JCRSessionWrapper session = null;
        try {
            session = JCRSessionFactory.getInstance().getCurrentUserSession(fileKey.getWorkspace());
            if (fileKey.getVersionDate() != null) {
                session.setVersionDate(new Date(Long.valueOf(fileKey.getVersionDate())));
            }
            if (fileKey.getVersionLabel() != null) {
                session.setVersionLabel(fileKey.getVersionLabel());
            }

            n = session.getNode(fileKey.getPath());
        } catch (RuntimeException e) {
            // throw by the session.setVersionLabel()
            logger.debug(e.getMessage(), e);
        } catch (PathNotFoundException e) {
            logger.debug(e.getMessage(), e);
        } catch (RepositoryException e) {
            if (e.getCause() != null && e.getCause() instanceof MalformedPathException) {
                logger.debug(e.getMessage(), e);
            } else {
                logger.error("Error accesing path: " + fileKey.getPath() + " for user "
                        + (session != null ? session.getUserID() : null), e);
            }
        }
        return n;
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        characterEncoding = SettingsBean.getInstance().getCharacterEncoding();
        String value = config.getInitParameter("cache-threshold");
        if (value != null) {
            cacheThreshold = new Integer(value);
        }

        value = config.getInitParameter("cache-for-logged-in-users");
        if (value != null) {
            cacheForLoggedUsers = new Boolean(value);
        }

        try {
            cacheManager = FileCacheManager.getInstance();
        } catch (JahiaRuntimeException e) {
            throw new ServletException(e.getCause());
        }

        if (SettingsBean.getInstance().isFileServletStatisticsEnabled()) {
            try {
                loggingService = (MetricsLoggingService) SpringContextSingleton
                        .getBean("loggingService");

                sessionFactory = JCRSessionFactory.getInstance();
            } catch (Exception e) {
                logger.error("Unable to get the logging service instance. Metrics logging will be disabled.");
            }
        }
    }

    protected boolean isNotModified(FileKey fileKey, FileLastModifiedCacheEntry lastModifiedEntry,
            HttpServletRequest request, HttpServletResponse response) {
        if (lastModifiedEntry != null) {
            // check presence of the 'If-None-Match' header
            String eTag = request.getHeader("If-None-Match");
            if (eTag != null && eTag.equals(lastModifiedEntry.getETag())) {
                return true;
            }
            // check presence of the 'If-Modified-Since' header
            long modifiedSince = request.getDateHeader("If-Modified-Since");
            if (modifiedSince > -1 && lastModifiedEntry.getLastModified() > 0
                    && lastModifiedEntry.getLastModified() / 1000 * 1000 <= modifiedSince) {
                return true;
            }
        }

        return false;
    }

    protected void logAccess(FileKey fileKey, HttpServletRequest req, String status) {
        if (loggingService == null || !loggingService.isEnabled()) {
            return;
        }

        HttpSession httpSession = req.getSession(false);
        String sessionID = httpSession != null ? httpSession.getId() : req.getRequestedSessionId();
        loggingService.logContentEvent(sessionFactory.getCurrentUser().getName(),
                req.getRemoteAddr(), sessionID, "", fileKey.getPath(), "", "fileAccessed",
                status);
    }

    protected FileKey parseKey(HttpServletRequest req) throws UnsupportedEncodingException {
        String workspace = null;
        String path = null;
        String p = req.getPathInfo();
        if (p != null && p.length() > 2) {
            int pathStart = p.indexOf("/", 1);
            workspace = pathStart > 1 ? p.substring(1, pathStart) : null;
            if (workspace != null) {
                path = p.substring(pathStart);
                if (ContextPlaceholdersReplacer.WORKSPACE_PLACEHOLDER.equals(URLDecoder.decode(
                        workspace, characterEncoding))) {
                    // Hack for CK Editor links
                    workspace = Constants.EDIT_WORKSPACE;
                }
                if (JCRContentUtils.isValidWorkspace(workspace)) {
                    if (path != null && path.contains("___")) {
                        path = UNDERSCORES.matcher(path).replaceAll(":");
                    }
                } else {
                    // unknown workspace
                    workspace = null;
                }
            }
        }

        return workspace != null && path != null ? new FileKey(workspace, JCRContentUtils.escapeNodePath(path),
                req.getParameter("v"), req.getParameter("l"), StringUtils.defaultIfEmpty(
                        req.getParameter("t"), StringUtils.EMPTY)) : null;
    }

    private boolean isVisibleForGuest(JCRNodeWrapper n) throws RepositoryException {
        final String nodeId = n.getIdentifier();
        if (!n.getSession().getUserID().equals(JahiaUserManagerService.GUEST_USERNAME)) {
            if (cacheForLoggedUsers) {
                try {
                    getInstance().doExecuteWithUserSession(JahiaUserManagerService.GUEST_USERNAME, n.getSession().getWorkspace().getName(), n.getSession().getLocale(), new JCRCallback<JCRNodeWrapper>() {
                        public JCRNodeWrapper doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
                            return session.getNodeByIdentifier(nodeId);
                        }
                    });
                } catch (ItemNotFoundException e) {
                    // not accessible by guest
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

}
