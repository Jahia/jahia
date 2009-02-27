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
//  JahiaEngine Interface
//  EV      19.11.2000
//  AK      14.12.2000  kicked the renderButton method in the balls !
//  EV      03.01.2001  all methods require now ProcessingContext, no JahiaData anymore
//  AK      04.01.2001  add rendertype_include and rendertype_forward constants
//
//  authoriseRender()
//  renderLink()
//  needsJahiaData()
//  handleActions()
//

package org.jahia.engines;

import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.engines.validation.EngineValidationHelper;


public interface JahiaEngine {

    public static final String ENGINE_MODE_ATTRIBUTE = "JahiaEngine.mode";

    public static final String CLOSE_JSP = "/engines/shared/close.jsp";
    public static final String RELOAD_JSP = "/engines/shared/reload.jsp";
    public static final String CANCEL_JSP = "/engines/shared/cancel.jsp";
    public static final String REDIRECT_JSP = "/engines/redirect.jsp";
    public static final String NOT_EDITABLE_FIELD = "/engines/shared/not_editable_field.jsp";
    public static final int LOAD_MODE = 1;
    public static final int UPDATE_MODE = 2;
    public static final int SAVE_MODE = 3;
    public static final int DELETE_MODE = 4;
    public static final int CANCEL_MODE = 5;
    public static final int VALIDATE_MODE = 6;

    public static final int RENDERTYPE_INCLUDE = 1;
    public static final int RENDERTYPE_FORWARD = 2;
    public static final int RENDERTYPE_REDIRECT = 3;
    public static final int RENDERTYPE_NAMED_DISPATCHER = 4;


    public static final String RENDER_TYPE_PARAM = "renderType";
    public static final String ENGINE_NAME_PARAM = "engineName";
    public static final String ENGINE_URL_PARAM = "engineUrl";
    public static final String ENGINE_OUTPUT_FILE_PARAM = "engineOutputFile";
    public static final String ENGINE_REDIRECT_URL = "engine.redirectURL";
    public static final String EMPTY_STRING = "";


    /* the name of the attribute "language code being processing" stored in engine map */
    public static final String PROCESSING_LOCALE = "org.jahia.engines.JahiaEngine.processingLocale";

    /* the name of the attribute "engine language helper" stored in engine map */
    public static final String ENGINE_LANGUAGE_HELPER
        = "org.jahia.engines.JahiaEngine.engineLanguageHelper";

    /* the name of the attribute "engine validation helper" stored in engine map */
    public static final String ENGINE_VALIDATION_HELPER
        = "org.jahia.engines.JahiaEngine.engineValidationHelper";

    public boolean authoriseRender (ProcessingContext processingContext);

    public String renderLink (ProcessingContext processingContext, Object theObj)
            throws JahiaException;

    public boolean needsJahiaData (ProcessingContext processingContext);

    public EngineValidationHelper handleActions (ProcessingContext processingContext, JahiaData jData)
            throws JahiaException;

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public String getName ();
}
