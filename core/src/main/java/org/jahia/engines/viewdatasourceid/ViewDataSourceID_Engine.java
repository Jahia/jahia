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
