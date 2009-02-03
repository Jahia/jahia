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
