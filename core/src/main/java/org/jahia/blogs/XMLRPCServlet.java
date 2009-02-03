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

package org.jahia.blogs;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcServer;
import org.jahia.blogs.api.BloggerAPI;
import org.jahia.blogs.api.MetaWeblogAPI;
import org.jahia.blogs.api.MovableTypeAPI;

/**
 * Basic Servlet accepting requests from XML-RPC blog clients
 *
 * @author Xavier Lawrence
 */
public class XMLRPCServlet extends HttpServlet {
    
    // log4j logger
    static Logger log = Logger.getLogger(MetaWeblogAPIImpl.class);
    
    private transient XmlRpcServer xmlRpcServer = new XmlRpcServer();
    private BloggerAPI bloggerAPIHandler;
    private MetaWeblogAPI metaWeblogAPIHandler;
    private MovableTypeAPI movableTypeAPIHandler;
       
    /**
     * Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            bloggerAPIHandler = new BloggerAPIImpl();
            xmlRpcServer.addHandler("blogger", bloggerAPIHandler);
            
            metaWeblogAPIHandler = new MetaWeblogAPIImpl();
            xmlRpcServer.addHandler("metaWeblog", metaWeblogAPIHandler);
            
            movableTypeAPIHandler = new MovableTypeAPIImpl();
            xmlRpcServer.addHandler("mt", movableTypeAPIHandler);
            
        } catch (Exception e) {
            log.error("Initialization of XML-RPC servlet failed", e);
        }
    }
    
    /**
     */
    protected void service(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("\n\nService for BLOG REQUEST");
        
        // Set ThreadLocal variables
        ServletResources.setCurrentRequest(request);
        ServletResources.setCurrentResponse(response);
        ServletResources.setCurrentConfig(super.getServletConfig());
        
        byte[] result = xmlRpcServer.execute(request.getInputStream());
        log.debug("Result: "+ new String(result));
        
        response.setContentType("text/xml");
        response.setContentLength(result.length);
        
        OutputStream output = response.getOutputStream();
        output.write(result);
        output.flush();
        
        // Cleanup ThreadLocal variables
        ServletResources.setCurrentRequest(null);
        ServletResources.setCurrentResponse(null);
        ServletResources.setCurrentConfig(null);
        
        log.debug("END Service for BLOG REQUEST\n");
    }
}
