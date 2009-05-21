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
package org.jahia.ajax.actionmenus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxAction;
import org.jahia.content.ContentObject;
import org.jahia.params.ProcessingContext;
import org.jahia.services.workflow.WorkflowService;

/**
 * Retrieves the workflow state of a ContentObject
 *
 * @author Xavier Lawrence
 */
public class GetWorkflowState extends AjaxAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetWorkflowState.class);

    private static final WorkflowService workflowService = servicesRegistry.getWorkflowService();
    private static final Map imageMap = new ConcurrentHashMap(16);
    
    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        long start_wf=System.currentTimeMillis();
        try {
            final ProcessingContext jParams = retrieveProcessingContext(request, response);

            final String key = getParameter(request, KEY);
            final ContentObject currentObject = getContentObjectFromString(key);
            if (logger.isDebugEnabled()) {
                logger.debug("Getting Workflow State for: " + key);
            }
            String language = getParameter(request,"flaglang","");
            if (language.trim().length() == 0) {
                language = jParams.getLocale().toString();
            }
            String state = workflowService.getExtendedWorkflowState(currentObject, language);
            
            final String imagePath = new StringBuilder(64).append(
                    jParams.settings().getJahiaEnginesHttpPath()).append(
                    "images/icons/workflow/").append(state).append(".png")
                    .toString();

            if (logger.isDebugEnabled()) {
                logger.debug("imagePath: " + imagePath);
            }
            final ServletContext context = super.getServlet().getServletContext();
            response.setContentType("image/png");
            setNoCacheHeaders(response);
            String imageName = imagePath.substring(request.getContextPath().length());
            final byte[] buff;
            if(!imageMap.containsKey(imageName)) {
                final InputStream image = new BufferedInputStream(context.getResourceAsStream(imageName));
                buff = new byte[image.available()];
                image.read(buff);
                image.close();
                imageMap.put(imageName,buff);
            } else {
                buff = (byte[])imageMap.get(imageName);
            }
            final OutputStream out = response.getOutputStream();
            out.write(buff);
            out.flush();

        } catch (Exception e) {
            handleException(e, request, response);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("WFSTATS: elapsed:"+(System.currentTimeMillis()-start_wf));
        }
        return null;
    }
}