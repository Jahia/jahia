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
