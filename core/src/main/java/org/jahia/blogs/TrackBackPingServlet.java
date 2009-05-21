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
package org.jahia.blogs;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jahia.blogs.actions.AddTrackBackPingAction;

/**
 * Simple Servlet reacting to POST methods containing TrackBack ping requests
 *
 * @author Xavier Lawrence
 */
public class TrackBackPingServlet extends HttpServlet {
    
    public static final String XML_HEADER = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    public static final String RESPONSE = "<response>";
    public static final String END_RESPONSE = "</response>";
    public static final String ERROR = "<error>";
    public static final String END_ERROR = "</error>";
    public static final String MESSAGE = "<message>";
    public static final String END_MESSAGE = "</message>";
    
    // log4j logger
    static Logger log = Logger.getLogger(TrackBackPingServlet.class);
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        StringBuffer resp = new StringBuffer();
        
        // Set ThreadLocal variables
        ServletResources.setCurrentRequest(request);
        ServletResources.setCurrentResponse(response);
        ServletResources.setCurrentConfig(super.getServletConfig());
        
        try {
            String postID = request.getParameter("entryID");
            if (postID == null || postID.length() < 1) {
                throw new ServletException("Missing required parameter \"entryID\"");
            }
            
            String url = request.getParameter("url");
            if (url == null || url.length() < 1) {
                throw new ServletException("Missing required parameter \"url\"");
            }
            
            String title = request.getParameter("title");
            String blogName = request.getParameter("blog_name");
            String excerpt = request.getParameter("excerpt");
            
            log.debug("\n\nAddTrackBackPingAction: "+postID+", "+url+", "+title+
                    ", "+blogName+", "+excerpt);
            
            AddTrackBackPingAction action = new AddTrackBackPingAction(postID, 
                    title, excerpt, url, blogName);
            action.execute();
            
            resp.append(XML_HEADER);
            resp.append(RESPONSE);
            resp.append(ERROR);
            resp.append(0);
            resp.append(END_ERROR);
            resp.append(END_RESPONSE);
            
        } catch (Exception e) {
            resp.append(XML_HEADER);
            resp.append(RESPONSE);
            resp.append(ERROR);
            resp.append(1);
            resp.append(END_ERROR);
            resp.append(MESSAGE);
            resp.append(e.getMessage());
            resp.append(END_MESSAGE);
            resp.append(END_RESPONSE);
            
            response.setStatus(500);
            
            log.error(e.getMessage(), e);
        }
        
        OutputStream output = response.getOutputStream();
        output.write(resp.toString().getBytes("UTF-8"));
        output.flush();
        
        // Cleanup ThreadLocal variables
        ServletResources.setCurrentRequest(null);
        ServletResources.setCurrentResponse(null);
        ServletResources.setCurrentConfig(null);
    }
}
