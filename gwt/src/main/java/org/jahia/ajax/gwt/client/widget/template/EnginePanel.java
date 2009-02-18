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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.template;

import com.google.gwt.user.client.ui.*;

/**
 * User: jahia
 * Date: 12 f�vr. 2008
 * Time: 14:10:33
 */
public class EnginePanel extends DockPanel {
    private Panel customPanel = new HorizontalPanel();
    private Panel buttonsPane = null;
    private Widget engineIcon = new Label();

    public EnginePanel(String engineTitle) {
        setTitle(engineTitle);
        addStyleName("jahia-gwt-mainpane");
        setSpacing(1);
    }

    public void createEngineUI(String height) {
        // user panel
        UserHeaderPanel userPanel = new UserHeaderPanel();
        add(userPanel, DockPanel.NORTH);
        
        // center panel
        VerticalPanel centerPanel = new VerticalPanel();
        centerPanel.setWidth("100%");
        centerPanel.setSpacing(5);
        centerPanel.addStyleName("gwt-enginecomponent");
        centerPanel.addStyleName("gwt-enginetitleheader");

        // create title panel
        HorizontalPanel engineTitlePanel = createTitlePanel();
        centerPanel.add(engineTitlePanel);

        // buttons pane
        if (buttonsPane != null) {
            centerPanel.add(buttonsPane);
        }

        // engine pane
        customPanel.addStyleName("gwt-enginecomponent");
        customPanel.addStyleName("gwt-enginemaincomponent");
        //customPanel.setWidth("100%");
        centerPanel.add(customPanel);
        centerPanel.setCellHeight(customPanel, height);

        // Copyright Panel
        CopyrightFooterPanel copyrightFooterPanel = new CopyrightFooterPanel();
        centerPanel.add(copyrightFooterPanel);
        add(centerPanel, DockPanel.CENTER);


    }

    private HorizontalPanel createTitlePanel() {
        // engine title panel
        HorizontalPanel engineTitlePanel = new HorizontalPanel();
        if (engineIcon != null) {
            engineTitlePanel.add(engineIcon);
            engineTitlePanel.setCellWidth(engineIcon, "50px");
        }
        Label engineTitleLabel = new HTML("<h3>" + getTitle() + "</h3>");
        engineTitleLabel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        engineTitlePanel.setCellVerticalAlignment(engineTitleLabel, VerticalPanel.ALIGN_BOTTOM);
        engineTitlePanel.add(engineTitleLabel);
        engineTitlePanel.addStyleName("gwt-enginecomponent");
        engineTitlePanel.addStyleName("gwt-enginetitleheader");
        return engineTitlePanel;
    }

    public void setCustomPanel(Panel customPanel) {
        this.customPanel = customPanel;
    }

    public void setEngineIcon(Widget engineIcon) {
        this.engineIcon = engineIcon;
    }

    public Panel getButtonsPane() {
        return buttonsPane;
    }

    public void setButtonsPane(Panel buttonsPane) {
        this.buttonsPane = buttonsPane;
    }
}
