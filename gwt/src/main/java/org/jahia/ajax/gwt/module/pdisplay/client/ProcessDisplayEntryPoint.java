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
package org.jahia.ajax.gwt.module.pdisplay.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.widget.process.ProcessDisplayPanel;
import org.jahia.ajax.gwt.client.messages.Messages;
//import org.jahia.ajax.gwt.templates.commons.client.util.DOMUtil;


/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 6 d�c. 2007
 * Time: 13:32:02
 * To change this template use File | Settings | File Templates.
 */
public class ProcessDisplayEntryPoint implements EntryPoint {
    public static final String ID = "pdisplay";

    public void onModuleLoad() {
        // init panel
        RootPanel jahiaTypePanel = RootPanel.get(ID);

        // create panel depending on state
        jahiaTypePanel.add(new ProcessDisplayPanel());

    }

    public static String getResource(String key) {
        return Messages.getResource(key);
    }

}
