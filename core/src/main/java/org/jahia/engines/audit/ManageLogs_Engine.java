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
//  ManageLogs_Engine
//  MJ  27.02.20001
//
//  getInstance()
//  authoriseRender()
//  renderLink()
//  needsJahiaData()
//  handleActions()
//

package org.jahia.engines.audit;

import java.util.Map;

import org.jahia.data.JahiaData;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;

public class ManageLogs_Engine {

    /** The engine's name. */
    public static final String ENGINE_NAME = "audit";

    private EngineToolBox toolBox;
    private static ManageLogs_Engine instance;

    /** logging */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ManageLogs_Engine.class);

    /**
     * constructor
     */
    private ManageLogs_Engine () {
        toolBox = EngineToolBox.getInstance ();
    }

    /**
      * @return a single instance of the object
      */
     public static ManageLogs_Engine getInstance() {
         if (instance == null) {
             synchronized (ManageLogs_Engine.class) {
                 if (instance == null) {
                     instance = new ManageLogs_Engine();
                 }
             }
         }
         return instance;
     }

    /**
     * authoriseRender
     */
    public boolean authoriseRender (ProcessingContext jParams) {
        return toolBox.authoriseRender (jParams);
    }


    /**
     * renderLink no params allowed
     */
    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        return jParams.composeEngineUrl (ENGINE_NAME);
    }


    /**
     * needsJahiaData
     */
    public boolean needsJahiaData (ProcessingContext jParams) {
        return false;
    }


    /**
     * handles the engine actions
     *
     * @param jParams a ProcessingContext object
     * @param jData   a JahiaData object (not mandatory)
     */
    public void handleActions (ProcessingContext jParams, int mode, Map engineMap, JahiaData jData)
            throws JahiaException,
            JahiaSessionExpirationException {
        // initalizes the hashmap
        // Map engineMap = initEngineMap (jParams);

        switch (mode) {
            case (JahiaEngine.LOAD_MODE):
                break;
            case (JahiaEngine.UPDATE_MODE):
                break;
            case (JahiaEngine.SAVE_MODE):
                break;
        }

        // displays the screen
        // toolBox.displayScreen (jParams, engineMap);
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
