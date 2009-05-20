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