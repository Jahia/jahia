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
package org.jahia.ajax.gwt.client.widget.template;

import com.google.gwt.user.client.ui.*;

/**
 * User: jahia
 * Date: 16 janv. 2008
 * Time: 14:08:43
 */
public class EngineDialogBox extends DialogBox {
    private EnginePanel enginePanel;


    public EngineDialogBox(String engineTitle) {
        setTitle(engineTitle);
        enginePanel = new EnginePanel(engineTitle);
    }

    private void close() {
        hide();
    }

    protected void createEngineUI(String height) {
        // create engine panel ui
        enginePanel.createEngineUI(height);

        //set as main widget
        setWidget(enginePanel);

        // center engine
        center();

    }

    public void setEnginePane(EnginePanel enginePane) {
        this.enginePanel = enginePane;
    }

}
