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
