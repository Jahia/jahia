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

import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.tiles.TilesRequestProcessor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Extends TilesRequestProcessor.
 * Before processing request, call request.setCharacterEncoding(encoding) if the contentType
 * contains a charset= part
 *
 */
public class JahiaRequestProcessor extends TilesRequestProcessor {

    protected String charSetEncoding = null;

    public String getCharSetEncoding() {
        return charSetEncoding;
    }

    public void setCharSetEncoding(String charSetEncoding) {
        this.charSetEncoding = charSetEncoding;
    }

    /**
     * Initialize this request processor instance.
     *
     * @param servlet The ActionServlet we are associated with.
     * @param moduleConfig The ModuleConfig we are associated with.
     * @throws ServletException If an error occurs during initialization.
     */
    public void init(ActionServlet servlet, ModuleConfig moduleConfig)
        throws ServletException {
        super.init(servlet, moduleConfig);
        String contentType = moduleConfig.getControllerConfig().getContentType();
        if ( contentType != null ){
            int pos = contentType.indexOf("charset=");
            if ( pos != -1 ){
                this.charSetEncoding = contentType.substring(pos+8);
            }
        }
    }

    /**
     * <p>Process an <code>HttpServletRequest</code> and create the
     * corresponding <code>HttpServletResponse</code> or dispatch
     * to another resource.</p>
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a processing exception occurs
     */
    public void process(HttpServletRequest request,
                        HttpServletResponse response)
        throws IOException, ServletException {

        this.setCharacterEncoding(request);
        super.process(request,response);

    }

    protected void setCharacterEncoding(HttpServletRequest request)
            throws UnsupportedEncodingException {
        if ( this.charSetEncoding != null && request != null ){
            request.setCharacterEncoding(this.charSetEncoding);
        }
    }

}
