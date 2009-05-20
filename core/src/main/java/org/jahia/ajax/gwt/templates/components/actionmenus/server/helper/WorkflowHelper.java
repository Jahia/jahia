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
