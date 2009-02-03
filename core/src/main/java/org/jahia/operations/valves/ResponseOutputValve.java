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

 package org.jahia.operations.valves;

import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class ResponseOutputValve implements Valve {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ResponseOutputValve.class);

    public ResponseOutputValve() {
    }
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        ProcessingContext processingContext = (ProcessingContext) context;
        try {

            HttpServletResponse realResp = ((ParamBean)processingContext).getRealResponse();
            String curContentType = processingContext.getContentType();
            if (processingContext.getRedirectLocation() != null) {
                logger.debug(
                    "sendRedirect call detected during output generation, no other output...");
                if (!realResp.isCommitted()) {
                    realResp.sendRedirect(realResp.encodeRedirectURL(processingContext.
                        getRedirectLocation()));
                }
            } else {
                /** @todo we should really find a more elegant way to handle this
                 *  case, especially when handling a file manager download that
                 *  has already accessed the RealResponse object.
                 */
                if (!realResp.isCommitted()) {
                    logger.debug("Printing content output to real writer");
                    if (curContentType != null) {
                        realResp.setContentType(curContentType);
                    }
                } else {
                    logger.debug(
                        "Output has already been committed, aborting display...");
                }

            }

        } catch (java.io.IOException ioe) {
            logger.debug("Error retrieving real response writer object or while retrieving generated output :",
                         ioe);
        }
        valveContext.invokeNext(context);
    }
    public void initialize() {
    }

}
