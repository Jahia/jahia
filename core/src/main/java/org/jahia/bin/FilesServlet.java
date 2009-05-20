/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ProcessingContextFactory;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheFactory;
import org.jahia.services.content.JCRFileContent;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Serves resources from the JCR repository.
 *
 * @author Thomas Draier
 *         Date: Oct 13, 2008
 *         Time: 2:08:59 PM
 */
public class FilesServlet extends HttpServlet {

    private static Logger logger = Logger.getLogger(FilesServlet.class);

    public static Cache cache;

    private int cacheThreshold = 64 * 1024;

    static {
        try {
            cache = CacheFactory.getInstance().createCacheInstance("WebdavCache");
        } catch (JahiaInitializationException e) {
            e.printStackTrace();
        }
    }

    protected static ProcessingContextFactory pcf;

    private static ProcessingContextFactory getProcessingContextFactory() {
        if (pcf == null) {
            pcf = (ProcessingContextFactory) SpringContextSingleton
                    .getInstance().getContext()
                    .getBean(ProcessingContextFactory.class.getName());
        }
        return pcf;
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String value = config.getInitParameter("cache-threshold");
        if (value != null) {
            cacheThreshold = new Integer(value);
        }

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        JahiaUser jahiaUser = (JahiaUser) req.getSession().getAttribute(ProcessingContext.SESSION_USER);
        String p = req.getRequestURI();
        if (!req.getContextPath().equals("/") && p.startsWith(req.getContextPath())) {
            p = p.substring(req.getContextPath().length());
        }
        if (p.startsWith(req.getServletPath())) {
            p = p.substring(req.getServletPath().length());
        }
        p = Text.unescape(p);

        JCRNodeWrapper n = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(p, jahiaUser);

        boolean valid = false;
        String v = req.getParameter("v");
        if (v != null) {
            n = n.getFrozenVersion(v);
            if (n != null) {
                valid = true;
            }
        } else {
            valid = n.isFile();
        }

        if (valid) {
            // check presence of the 'If-Modified-Since' header
            long modifiedSince = req.getDateHeader("If-Modified-Since");
            Date lastModified = n.getLastModifiedAsDate();
            if (lastModified != null && modifiedSince > -1
                    && lastModified.getTime() / 1000 * 1000 <= modifiedSince) {
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            JCRFileContent fileContent = n.getFileContent();
            res.setContentType(fileContent.getContentType());
            int contentLength = (int) fileContent.getContentLength();
            res.setContentLength(contentLength);

            ServletOutputStream os = res.getOutputStream();
            if (lastModified != null) {
                // set 'Last-Modified' response header
                res.setDateHeader("Last-Modified", lastModified.getTime());
            }
            InputStream is = null;

            if (contentLength < cacheThreshold) {
                String cacheKey = p + ":" + (v==null ? "0" : v);
                byte[] b = (byte[]) cache.get(cacheKey);
                if (b == null) {
                    is = fileContent.downloadFile();
                    b = new byte[contentLength];
                    is.read(b);
                    cache.put(p, cacheKey);
                    IOUtils.closeQuietly(is);
                }
                is = new ByteArrayInputStream(b);
            } else {
                is = fileContent.downloadFile();
            }

            IOUtils.copy(is, os);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);

            return;
        }

        res.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

}
