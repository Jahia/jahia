/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.api.Constants;
import org.jahia.services.render.filter.ContextPlaceholdersReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRFileContent;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;

import javax.jcr.Binary;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Serves resources from the JCR repository.
 *
 * @author Thomas Draier
 * Date: Oct 13, 2008
 * Time: 2:08:59 PM
 */
public class FilesServlet extends HttpServlet {

    private static final String[] BAD_DATA = new String[] {null, null, null, null};

    public static Cache<String, byte[]> cache;
    
    private static Logger logger = LoggerFactory.getLogger(FilesServlet.class);
    
    private static final long serialVersionUID = -414690364676304370L; 

    private static final Pattern UNDERSCORES = Pattern.compile("___");

    public static final String WEBDAV_CACHE_NAME = "WebdavCache";

    static {
        try {
            cache = CacheFactory.getInstance().getCache(WEBDAV_CACHE_NAME, true);
        } catch (JahiaInitializationException e) {
            e.printStackTrace();
        }
    }

    private int cacheThreshold = 64 * 1024;

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        long timer = System.currentTimeMillis();
        int code = HttpServletResponse.SC_OK;
        try {
            String[] data = parseData(req);
            String workspace = data[0];
            String path = data[1];
            String v = data[2];
            String l = data[3];
            String t = data[4];

            if (workspace != null && StringUtils.isNotEmpty(path)) {
                JCRNodeWrapper n = getNode(data);
                if (n != null && n.isFile()) {
                    // check presence of the 'If-Modified-Since' header
                    long modifiedSince = req.getDateHeader("If-Modified-Since");
                    Date lastModified = n.getLastModifiedAsDate();
                    if (lastModified != null && modifiedSince > -1
                            && lastModified.getTime() / 1000 * 1000 <= modifiedSince) {
                        code = HttpServletResponse.SC_NOT_MODIFIED;
                        res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                        return;
                    }
        
                    JCRNodeWrapper content;

                    if (StringUtils.isNotEmpty(t) && n.hasNode(t)) {
                        content = n.getNode(t);
                        if (!content.isNodeType(Constants.NT_RESOURCE)) {
                            content = null;
                        }
                    } else {
                        content = n.getNode(Constants.JCR_CONTENT);

                    }

                    res.setContentType(content.getProperty(Constants.JCR_MIMETYPE).getString());

                    Binary binary = content.getProperty(Constants.JCR_DATA).getBinary();
                    int contentLength = (int) binary.getSize();
                    res.setContentLength(contentLength);

                    ServletOutputStream os = res.getOutputStream();
                    if (lastModified != null) {
                        // set 'Last-Modified' response header
                        res.setDateHeader("Last-Modified", lastModified.getTime());
                    }

                    if (contentLength < cacheThreshold) {
                        String cacheKey = workspace + ":" + path + ":" + (v==null ? "0" : v) + ":" + (l==null ? "" : l) + ":" + (t==null ? "" : t);
                        byte[] b = (byte[]) cache.get(cacheKey);
                        if (b == null || b.length != contentLength) {
                            InputStream is = binary.getStream();
                            if(is!=null) {
                                try {
                                    b = new byte[contentLength];
                                    int i = is.read(b);
                                    if(i>0) {
                                        cache.put(cacheKey,b);
                                    }
                                } finally {
                                    IOUtils.closeQuietly(is);
                                }
                            } else {
                                code = HttpServletResponse.SC_NOT_FOUND;
                                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                                return;
                            }
                        }
                        try {
                            os.write(b);
                        } finally {
                            IOUtils.closeQuietly(os);
                        }
                    } else {
                        InputStream is = binary.getStream();
                        if(is== null) {
                            code = HttpServletResponse.SC_NOT_FOUND;
                            res.sendError(HttpServletResponse.SC_NOT_FOUND);
                            return;
                        }
                        try {
                            IOUtils.copy(is, os);
                        } finally {
                            IOUtils.closeQuietly(is);
                            IOUtils.closeQuietly(os);
                        }
                    }
        
                    return;
                }
            }
    
            code = HttpServletResponse.SC_NOT_FOUND;
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (RepositoryException e) {
            logger.error("Cannot get file",e);

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

    protected JCRNodeWrapper getNode(String[] data) {
        JCRNodeWrapper n = null;
        JCRSessionWrapper session = null;
        try {
            session = JCRSessionFactory.getInstance().getCurrentUserSession(data[0]);
            if (data[2] != null) {
                session.setVersionDate(new Date(Long.valueOf(data[2])));
            }
            if (data[3] != null) {
                session.setVersionLabel(data[3]);
            }

            n = session.getNode(Text.escapePath(data[1]));
        } catch (RuntimeException e) {
            // throw by the session.setVersionLabel()
            logger.debug(e.getMessage(), e);
        } catch (PathNotFoundException e) {
            try {
                n = session.getNode(data[1]);
            } catch (PathNotFoundException e1){
                logger.debug(e.getMessage(), e);
            } catch (RepositoryException e1) {
                logger.error("Error accesing path: " + data[1] + " for user "
                    + (session != null ? session.getUserID() : null), e);
            }
        } catch (RepositoryException e) {
            logger.error("Error accesing path: " + data[1] + " for user "
                    + (session != null ? session.getUserID() : null), e);
        }
        return n;
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String value = config.getInitParameter("cache-threshold");
        if (value != null) {
            cacheThreshold = new Integer(value);
        }

    }

    protected String[] parseData(HttpServletRequest req) throws UnsupportedEncodingException {
        String workspace = null;
        String path = null;
        String p = req.getPathInfo();
        if (p != null && p.length() > 2) {
            int pathStart = p.indexOf("/", 1);
            workspace = pathStart > 1 ? p.substring(1, pathStart) : null;
            if (workspace != null) {
                path = p.substring(pathStart);
                if (ContextPlaceholdersReplacer.WORKSPACE_PLACEHOLDER.equals(URLDecoder.decode(
                        workspace, "UTF-8"))) {
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

        return workspace != null && path != null ? new String[] { workspace, path,
                req.getParameter("v"), req.getParameter("l"), req.getParameter("t") } : BAD_DATA;
    }

}
