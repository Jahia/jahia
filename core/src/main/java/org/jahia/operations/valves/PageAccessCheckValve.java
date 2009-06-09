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
                    String loginUrl = Jahia.getSsoValve().getRedirectUrl(processingContext);
                    if (loginUrl != null) {
                        ((ParamBean) processingContext).getResponse().sendRedirect(loginUrl);
                        return;
                    }
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
