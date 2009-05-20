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
