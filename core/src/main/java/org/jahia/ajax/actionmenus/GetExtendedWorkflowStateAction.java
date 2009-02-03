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

package org.jahia.ajax.actionmenus;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxDispatchAction;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.workflow.WorkflowService;

/**
 * Ajax-based action handler for retrieving the workflow state of the content
 * object.
 * 
 * @author Sergiy Shyrkov
 */
public class GetExtendedWorkflowStateAction extends AjaxDispatchAction {

    private static final transient Logger logger = Logger
            .getLogger(GetExtendedWorkflowStateAction.class);

    private static WorkflowService workflowService = ServicesRegistry
            .getInstance().getWorkflowService();

    public ActionForward perform(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        long startTime = System.currentTimeMillis();
        try {
            String pid = getParameter(request, "pid");
            String key = getParameter(request, "key");
            if (StringUtils.isEmpty(pid) || StringUtils.isEmpty(key)) {
                throw new JahiaBadRequestException(
                        "Required parameter missing. Required parameters are 'pid' and 'key'.");
            }
            ProcessingContext jParams = retrieveProcessingContext(request,
                    response, "/op/edit/pid/" + pid, false);

            String language = jParams.getLocale().toString();
            String originalKeyString = key;
            if (originalKeyString.indexOf('#') != -1
                    && originalKeyString.indexOf('#') < originalKeyString
                            .length() - 1) {
                key = originalKeyString.substring(0, originalKeyString
                        .indexOf('#'));
                language = originalKeyString.substring(originalKeyString
                        .indexOf('#') + 1);
            }

            ContentObject currentObject = (ContentObject) ContentObject
                    .getInstance(ObjectKey.getInstance(key));
            if (logger.isDebugEnabled()) {
                logger.debug("Getting workflow state for: " + key
                        + " language: " + language);
            }

            String state = workflowService.getExtendedWorkflowState(
                    currentObject, language);

            Map states = new HashMap(1);
            states.put(originalKeyString, state);

            writeResponse(toJSON(states), response);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("GetExtendedWorkflowStateAction.perform took: "
                    + (System.currentTimeMillis() - startTime));
        }
        return null;
    }

    public ActionForward performBatch(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        long startTime = System.currentTimeMillis();
        try {
            String pid = getParameter(request, "pid");
            String keys = getParameter(request, "keys");
            if (StringUtils.isEmpty(pid) || StringUtils.isEmpty(keys)) {
                throw new JahiaBadRequestException(
                        "Required parameter missing. Required parameters are 'pid' and 'keys'.");
            }
            ProcessingContext jParams = retrieveProcessingContext(request,
                    response, "/op/edit/pid/" + pid, false);


            if (logger.isDebugEnabled()) {
                logger.debug("Getting workflow state for: " + keys);
            }

            String[] objKeys = keys.split("\\|");
            Map states = new HashMap(objKeys.length);

            for (int i = 0; i < objKeys.length; i++) {
                try {
                    String language = jParams.getLocale().toString();
                    String originalKeyString = objKeys[i];
                    if (originalKeyString.indexOf('#') != -1
                            && originalKeyString.indexOf('#') < originalKeyString
                                    .length() - 1) {
                        objKeys[i] = originalKeyString.substring(0,
                                originalKeyString.indexOf('#'));
                        language = originalKeyString
                                .substring(originalKeyString.indexOf('#') + 1);
                    }

                    ContentObject currentObject = (ContentObject) ContentObject
                            .getInstance(ObjectKey.getInstance(objKeys[i]));
                    // might have been deleted in the meantime (JAHIA-3023)
                    if (currentObject != null) {
                        String state = workflowService
                                .getExtendedWorkflowState(currentObject,
                                        language);

                        states.put(originalKeyString, state);
                    }
                } catch (Exception ex) {
                    logger.error("Error retrieving workflow state for key: "
                            + objKeys[i], ex);
                }
            }

            writeResponse(toJSON(states), response);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("GetExtendedWorkflowStateAction.performBatch took: "
                    + (System.currentTimeMillis() - startTime));
        }
        return null;
    }

    private String toJSON(Map wfStates) {
        StringBuffer json = new StringBuffer(32 * wfStates.size());
        for (Iterator iterator = wfStates.entrySet().iterator(); iterator
                .hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (json.length() > 0) {
                json.append(",");
            }
            json.append("{\"key\":\"").append(entry.getKey()).append(
                    "\",\"state\":\"").append(entry.getValue()).append("\"}");
        }
        json.insert(0, "[").append("]");
        return json.toString();
    }

    public ActionForward unspecified(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        return performBatch(mapping, form, request, response);
    }

    private void writeResponse(String json, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        // response.setContentType("text/plain");
        setNoCacheHeaders(response);
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}
