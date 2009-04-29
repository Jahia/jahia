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
