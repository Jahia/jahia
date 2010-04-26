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

    public static final int RENDERTYPE_INCLUDE = 1;
    public static final int RENDERTYPE_FORWARD = 2;
    public static final int RENDERTYPE_REDIRECT = 3;
    public static final int RENDERTYPE_NAMED_DISPATCHER = 4;


    public static final String RENDER_TYPE_PARAM = "renderType";
    public static final String ENGINE_NAME_PARAM = "engineName";
    public static final String ENGINE_URL_PARAM = "engineUrl";
    public static final String ENGINE_OUTPUT_FILE_PARAM = "engineOutputFile";
    public static final String EMPTY_STRING = "";

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public String getName ();
}
