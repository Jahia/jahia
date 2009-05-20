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
package org.jahia.ajax.gwt.module.jobreport.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.widget.process.JobReport;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 19, 2008
 * Time: 11:51:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class JobReportEntryPoint implements EntryPoint {

    public static final String ID = "jobreport";



    public void onModuleLoad() {
        // init panel
        RootPanel jahiaTypePanel = RootPanel.get(ID);

        String name = DOM.getElementAttribute(jahiaTypePanel.getElement(), "name");
        String groupName = DOM.getElementAttribute(jahiaTypePanel.getElement(), "groupName");

        jahiaTypePanel.add(new JobReport(name, groupName));
    }
}
