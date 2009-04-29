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
 package org.jahia.engines.applications;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.StringTokenizer;

import org.jahia.data.JahiaData;
import org.jahia.engines.*;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;

/**
 * <p>Title: Full screen application dispatcher engine</p> <p>Description: The goal of this
 * engine is to allow applications to "take" over the HTML screen, without Jahia giving up it's
 * hand. This can be useful for all types of applications.</p> <p>Copyright: Copyright (c)
 * 2002</p> <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class FullScreenDispatcherEngine implements JahiaEngine {

    /** The engine's name */
    public static final String ENGINE_NAME = "appdispatcher";

    /** logging */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (FullScreenDispatcherEngine.class);

    public FullScreenDispatcherEngine () {
    }

    public boolean authoriseRender (ProcessingContext jParams) {
        // render is always authorized as even guest users might need this.
        // Actually the rendering is controlled more by the application than
        // by the user.
        return true;
    }

    public String renderLink (ProcessingContext jParams, Object theObj) throws JahiaException {
        String resultURL;

        Properties paramPairs = new Properties ();
        // paramPairs.setProperty("fieldid", )
        paramPairs.setProperty (ProcessingContext.PAGE_ID_PARAMETER,
                Integer.toString (jParams.getPageID ()));

        resultURL = jParams.composeEngineUrl (ENGINE_NAME, paramPairs, EMPTY_STRING);
        return resultURL;
    }

    public boolean needsJahiaData (ProcessingContext jParams) {
        return false;
    }

    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException {
        String appUniqueIDStr = jParams.getParameter ("appid");
        if (appUniqueIDStr == null) {
            throw new JahiaException (
                    "Error while displaying application",
                    "Missing appID parameter for engine " +
                    FullScreenDispatcherEngine.ENGINE_NAME,
                    JahiaException.APPLICATION_ERROR,
                    JahiaException.ERROR_SEVERITY);
        }
        StringTokenizer strToken = new StringTokenizer (appUniqueIDStr, "_");
        String fieldIDStr = strToken.nextToken ();
        String appIDStr = strToken.nextToken ();
        int fieldID = Integer.parseInt (fieldIDStr);
        String appOutput =
                ServicesRegistry.getInstance ().getApplicationsDispatchService ()
                .getAppOutput (fieldID, appIDStr, (ParamBean) jParams);
        try {
            PrintWriter out = ((ParamBean)jParams).getResponse ().getWriter ();
            out.print (appOutput);
            out.flush ();
        } catch (IOException ioe) {
            throw new JahiaException (
                    "Error while displaying application output",
                    "Couldn't get HttpResponse PrintWriter instance",
                    JahiaException.APPLICATION_ERROR,
                    JahiaException.ERROR_SEVERITY,
                    ioe
            );
        }
        return null; // nothing to validate
    }

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName () {
        return ENGINE_NAME;
    }
}
