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

import org.jahia.bin.Jahia;
import org.jahia.engines.login.Login_Engine;
import org.jahia.engines.core.Core_Engine;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class PageAccessCheckValve implements Valve {
    public PageAccessCheckValve() {
    }

    public void invoke(Object context, ValveContext valveContext)
            throws PipelineException {
        final ProcessingContext processingContext = (ProcessingContext) context;
        if (!processingContext.getEngineName().equals(Login_Engine.ENGINE_NAME) && processingContext.getContentPage() != null &&
                !processingContext.getContentPage().checkReadAccess(processingContext.getUser())) {
            if (Jahia.usesSso()) {
                try {
                    ((ParamBean) processingContext).getResponse().sendRedirect(Jahia.getSsoValve().getRedirectUrl(processingContext));
                    return;
                } catch (Exception e) {
                }
            }

            JahiaException noReadAccessException = new JahiaException(
                    "403 Forbidden - Page:" +
                            processingContext.getPageID(),
                    "No read access for page " +
                            processingContext.getPageID() +
                            " user=" + processingContext.getUser().getUsername(),
                    JahiaException.SECURITY_ERROR,
                    JahiaException.ERROR_SEVERITY);
            throw new PipelineException(noReadAccessException);
        }
        final SessionState session = processingContext.getSessionState();
        if (processingContext.getEngineName().equals(Core_Engine.ENGINE_NAME)) {
            session.setAttribute(ProcessingContext.SESSION_LAST_DISPLAYED_PAGE_ID,
                    new Integer(processingContext.getPageID()));
        }
        valveContext.invokeNext(context);
    }

    public void initialize() {
    }

}
