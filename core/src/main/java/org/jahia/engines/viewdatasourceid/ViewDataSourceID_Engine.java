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

//
//  ViewDataSourceID_Engine
//  EV      04.12.2000
//
//  getInstance()
//  authoriseRender()
//  renderLink()
//  needsJahiaData()
//  handleActions()
//

package org.jahia.engines.viewdatasourceid;

import java.io.IOException;
import java.io.PrintWriter;

import org.jahia.data.JahiaData;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.*;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;

public class ViewDataSourceID_Engine implements JahiaEngine {

    public static final String ENGINE_NAME = "viewdatasourceid";

    /** logging */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ViewDataSourceID_Engine.class);


    /**
     * constructor EV    04.12.2000
     */
    public ViewDataSourceID_Engine () {
    }


    /**
     * authoriseRender EV    04.12.2000
     */
    public boolean authoriseRender (ProcessingContext jParams) {
        return (jParams.getOperationMode () == ProcessingContext.EDIT);
    }


    /**
     * renderLink EV    04.12.2000
     */
    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        JahiaField theField = (JahiaField) theObj;
        String params = "?mode=displayid&fid=" + theField.getID ();
        return jParams.composeEngineUrl (ENGINE_NAME, params);
    }


    /**
     * needsJahiaData EV    04.12.2000 it doesn't need JahiaData !!
     */
    public boolean needsJahiaData (ProcessingContext jParams) {
        return false;
    }


    /**
     * handleActions EV    04.12.2000
     */
    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException {
        String mode = jParams.getParameter ("mode");
        String ipAddr = jParams.getRemoteAddr ();
        int fid = jParams.getFieldID ();
        if (mode != null) {
            if (mode.equals ("displayid")) {
                logger.debug (ipAddr + " is accessing ViewDataSourceID " + fid);
                displayID (jParams, fid);
            }
        }
        return null;
    }


    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName () {
        return ENGINE_NAME;
    }


    /**
     * displayID() EV    04.12.2000
     */
    public void displayID (ProcessingContext processingContext, int fieldID)
            throws JahiaException {
        try {
            ParamBean paramBean = (ParamBean) processingContext;
            PrintWriter out = paramBean.getResponse ().getWriter ();
            paramBean.getResponse ().setContentType ("text/html");
            String dsUrl = paramBean.composeEngineUrl ("datasourcefield",
                    "&mode=displaydata&fid=" + fieldID);
            StringBuffer html =
                    new StringBuffer ("<html><head><title>Data Source ID</title></head>\n");
            html.append (
                    "<body bgcolor=\"black\" onLoad=\"document.ds.dsid.focus();document.ds.dsid.select()\">\n");
            html.append ("<font color=\"white\" size=\"2\"><b>Data Source ID :</b><br>\n");
            html.append (
                    "<form name=\"ds\"><input type=\"text\" name=\"dsid\" size=\"60\" value=\"");
            html.append (dsUrl);
            html.append ("\"><br>\n");
            html.append (
                    "<input type=\"button\" value=\"Close\" onClick=\"window.close()\">\n");
            html.append ("</form></body></html>");
            out.println (html.toString ());

        } catch (IOException ie) {
            String errorMsg = "Error while returning a datasource field : " + ie.getMessage () +
                    " -> BAILING OUT";
            logger.error (errorMsg, ie);
            throw new JahiaException ("Cannot retrieve remote data",
                    errorMsg, JahiaException.DATA_ERROR, JahiaException.CRITICAL_SEVERITY, ie);
        }

    }

}
