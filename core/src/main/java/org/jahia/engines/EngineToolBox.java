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
// $Id$

//
//  EV  10.01.20001
//

package org.jahia.engines;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;

import java.util.Map;


public class EngineToolBox {

    private static final EngineToolBox instance = new EngineToolBox ();

    private static final String MSG_INTERNAL_ERROR = "Audit Log Manager internal error";
    private static final String JSP_LOGFORM = "/engines/audit/viewlogs.jsp";
    //private static final String JSP_SENDLOG = "/engines/audit/sendlogs.jsp";
    private static final String JSP_FLUSHCONFIRM = "/engines/audit/flushconfirm.jsp";

    /** Logger instance */
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger (EngineToolBox.class);


    private EngineToolBox() {
    }


    /**
     * returns a single instance of the object
     */
    public static EngineToolBox getInstance() {
        return instance;
    }


    /**
     * authoriseRender
     */
    public boolean authoriseRender(final ProcessingContext jParams) {
        return jParams.getOperationMode().equals(ProcessingContext.EDIT);
    } // end authoriseRender


    /**
     * displays the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void displayScreen(final ProcessingContext jParams, final Map<String, Object> engineMap) throws JahiaException {
        EngineRenderer.getInstance().render(jParams, engineMap);
    } // end displayScreen


    /**
     * loads log data for the JSP file
     *
     * @param jParams    a ParamBean object (with request and response)
     * @param objectType an <code>int</code> representing the type of the object processed
     * @param engineMap  then engine map, to be forwarded to the JSP file
     */
    public void loadLogData(final ProcessingContext jParams, final int objectType, final Map<String, Object> engineMap) throws JahiaException {
    }

}
