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

import org.jahia.pipelines.valves.ValveContext;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.PipelineException;
import org.jahia.params.ProcessingContext;
import org.jahia.data.beans.history.HistoryBean;
import org.jahia.exceptions.JahiaException;

import java.util.List;
import java.util.ArrayList;

/**
 * User: jahia
 * Date: 25 avr. 2008
 * Time: 17:00:24
 */
public class HistoryValve implements Valve {
    protected static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HistoryValve.class);
    public static final String ORG_JAHIA_TOOLBAR_HISTORY = "org.jahia.toolbar.history";

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        logger.debug("starting " + this.getClass().getName() + ".invoke()...");
        ProcessingContext processingContext = (ProcessingContext) context;

        // update history bean list
        updateHistoryBeanList(processingContext);


        // invoke next valve
        valveContext.invokeNext(context);
    }

    private void updateHistoryBeanList(ProcessingContext processingContext) {
        List<HistoryBean> historyBeanList = (List<HistoryBean>) processingContext.getSessionState().getAttribute(ORG_JAHIA_TOOLBAR_HISTORY);
        if (historyBeanList == null) {
            historyBeanList = new ArrayList<HistoryBean>();
        }

        // create a new history bean
        HistoryBean historyBean = new HistoryBean();
        historyBean.setPid(processingContext.getPageID());
        if (processingContext.getThePage() != null) {
            historyBean.setPageTitle(processingContext.getThePage().getTitle());
        }
        try {
            historyBean.setUrl(processingContext.composePageUrl(processingContext.getPageID()));
        } catch (JahiaException e) {
            logger.error(e, e);
        }

        // remove if exist
        if (historyBeanList.contains(historyBean)) {
            // if same history bean, juste remove it
             historyBeanList.remove(historyBean);
        }

        // add it to history bean position
        historyBeanList.add(0, historyBean);

        // TO Do: add it in jahia.properties
        int maxHistory = 10;
        if (historyBeanList.size() > 11) {
            historyBeanList.remove(historyBeanList.size() - 1);
        }

        processingContext.getSessionState().setAttribute(ORG_JAHIA_TOOLBAR_HISTORY, historyBeanList);
    }

    public void initialize() {
        // nothing to do here
    }
}
