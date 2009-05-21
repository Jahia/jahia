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
