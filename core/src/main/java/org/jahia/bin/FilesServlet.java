/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin;

import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContextFactory;
import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRFileContent;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.jackrabbit.util.Text;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
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

        if (n.isFile()) {
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

                byte[] b = (byte[]) cache.get(p);
                if (b == null) {
                    is = fileContent.downloadFile();
                    b = new byte[contentLength];
                    is.read(b);
                    cache.put(p, b);
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
