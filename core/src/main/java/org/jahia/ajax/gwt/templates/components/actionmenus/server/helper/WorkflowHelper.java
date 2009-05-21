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
package org.jahia.ajax.gwt.templates.components.actionmenus.server.helper;

import org.jahia.ajax.gwt.client.data.actionmenu.workflow.GWTJahiaWorkflowState;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.data.beans.ContentBean;
import org.jahia.data.beans.PageBean;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 27 f�vr. 2008 - 14:49:40
 */
public class WorkflowHelper {

    private static final transient Logger logger = Logger.getLogger(WorkflowHelper.class);

    /**
     * Retrieve the full workflow state (a code corresponding to a given image).
     *
     * @param jParams the current processing context
     * @param wfKey the workflow key to retrieve state
     * @param languageCode a language code (can be null)
     * @return the workflow state as a 'xyz' String
     * @see org.jahia.services.workflow.WorkflowService
     */
    public static GWTJahiaWorkflowState getWorkflowStateForObject(final ProcessingContext jParams, final String wfKey, final String languageCode) {
        // this will contain the workflow engine url
        String url = null ;
        try {
            if (wfKey.startsWith(PageBean.TYPE)) {
                url = drawWorkflowUrl(jParams, wfKey) ;
            } else {
                ContentBean cb = JahiaObjectCreator.getContentBeanFromObjectKey(wfKey, jParams) ;
                while (cb != null && !cb.getBeanType().equals(PageBean.TYPE)) {
                    cb = cb.getParent() ;
                }
                if (cb == null) {
                    url = drawWorkflowUrl(jParams, jParams.getSite().getHomeContentPage().getObjectKey().getKey()) ;
                } else {
                    url = drawWorkflowUrl(jParams, cb.getContentObject().getObjectKey().getKey()) ;
                }
            }

        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }

        // workflow unavailable
        if (url == null) {
            return null ;
        }

        String language = languageCode ;
        if(language == null || "".equals(language.trim())){
            language = jParams.getLocale().toString();
        }
        String state ;
        try {
            state = ServicesRegistry.getInstance().getWorkflowService().getExtendedWorkflowState(JahiaObjectCreator.getContentObjectFromString(wfKey), language);
        } catch (Exception e) {
            logger.error("Error retrieving workflow state for object " + wfKey, e) ;
            return null ;
        }

        // workflow unavailable
        if (state == null) {
            return null ;
        }

        // return the workflow state as a custom gwt bean
        return new GWTJahiaWorkflowState(url, state) ;
    }

    /**
     * Retrieve the url to open the workflow engine if needed.
     *
     * @param jParams processing context
     * @param wfKey the content object key for the workflow
     * @return return workflow engine url
     * @throws JahiaException sthg bad happened
     */
    public static String drawWorkflowUrl(ProcessingContext jParams, String wfKey) throws JahiaException {
        return new StringBuilder(jParams.getContextPath())
                .append("/engines/gwtworkflow/workflow.jsp")
                .append("?startpage=").append(wfKey).toString();
    }

}
