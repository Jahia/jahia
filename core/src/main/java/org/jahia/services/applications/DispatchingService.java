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
//  JahiaApplicationsService
//  EV      29.11.2000
//
//  getAppOutput( fieldID, appID, params )
//

package org.jahia.services.applications;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.services.JahiaService;

/**
 * This service generates the dispatching and aggregation on an application.
 * This functionality is central to the portal behavior of Jahia, allowing it
 * to display multiple applications on a web page and interact with them
 * simultaneously.
 *
 * @author Serge Huber
 * @author Eric Vassalli
 * @version 1.0
 */
public abstract class DispatchingService extends JahiaService {

    /**
     * Dispatches processing to an application, and retrieves it's output for
     * Jahia to aggregate
     *
     * @param fieldID     identifier of Jahia's field
     * @param entryPointIDStr application identifier passed as a String (converted
     *                    from an integer)
     * @param jParams     Jahia's ProcessingContext object, containing the standard request /
     *                    response pair, the servlet context, and additional information
     *
     * @throws JahiaException generated if there was a problem dispatching,
     *                        during processing of the application, or when recuperating the application's
     *                        output.
     * @return String containing the output of the application
     */
    public abstract String getAppOutput (int fieldID, String entryPointIDStr, ParamBean jParams)
            throws JahiaException;

} // end JahiaApplicationsService
