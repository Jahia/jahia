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
import org.jahia.exceptions.JahiaException;
import javax.servlet.ServletContext;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class SiteCheckValve implements Valve {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(SiteCheckValve.class);

    public SiteCheckValve () {
    }

    public void invoke (Object context, ValveContext valveContext)
        throws PipelineException {
        ProcessingContext processingContext = (ProcessingContext) context;
        ParamBean paramBean = (ParamBean) processingContext;

        if (processingContext.getSite() == null) {
            ServletContext servletContext = paramBean.getContext();
            if (context != null) {
                try {
                    processingContext.setAttribute("Jahia_ParamBean",
                        processingContext);
                    servletContext.getRequestDispatcher(
                        "/errors/site_not_found.jsp").forward(
                        paramBean.getRequest(), paramBean.getResponse());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    JahiaException pageNotFoundException = new JahiaException(
                        "404 Page Not Found: No site specified",
                        "The requested site not found",
                        JahiaException.SECURITY_ERROR,
                        JahiaException.ERROR_SEVERITY, e);
                    throw new PipelineException(pageNotFoundException);
                }
                return;
            } else {
                JahiaException nullContext = new JahiaException(
                    "404 Page Not Found:",
                    "Context is null",
                    JahiaException.SECURITY_ERROR,
                    JahiaException.ERROR_SEVERITY);
                throw new PipelineException(nullContext);
            }
        }

        valveContext.invokeNext(context);
    }

    public void initialize () {
    }

}
