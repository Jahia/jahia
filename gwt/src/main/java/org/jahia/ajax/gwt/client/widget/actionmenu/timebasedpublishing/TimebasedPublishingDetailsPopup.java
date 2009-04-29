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
package org.jahia.ajax.gwt.client.widget.actionmenu.timebasedpublishing;

import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingDetails;
import org.jahia.ajax.gwt.client.util.EngineOpener;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

/**
 * This represents the detailed state of an object using timebased publishing.
 *
 * @author rfelden
 * @version 26 f�vr. 2008 - 17:43:45
 */
public class TimebasedPublishingDetailsPopup extends Window {

    public TimebasedPublishingDetailsPopup(final GWTJahiaTimebasedPublishingDetails details, String iconStyle) {
        super() ;
        setLayout(new FitLayout());
        Grid layout = new Grid(4, 2) ;
        layout.setWidget(0, 0, new Label(details.getSchedulingTypeLabel()));
        layout.setWidget(0, 1, new Label(details.getSchedulingTypeValue()));
        layout.setWidget(1, 0, new Label(details.getCurrentStatusLabel()));
        layout.setWidget(1, 1, new Label(details.getCurrentStatusValue()));
        layout.setWidget(2, 0, new Label(details.getPublicationDateLabel()));
        layout.setWidget(2, 1, new Label(details.getPublicationDateValue()));
        layout.setWidget(3, 0, new Label(details.getExpirationDateLabel()));
        layout.setWidget(3, 1, new Label(details.getExpirationDateValue())) ;
        add(layout);

        Button item = new Button("Edit") ;
        item.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent componentEvent) {
                EngineOpener.openEngine(details.getUrl());
            }
        }) ;
        ButtonBar bar = new ButtonBar() ;
        bar.add(item) ;
        setButtonBar(bar);

        setIconStyle(iconStyle);
        setHeading(details.getTitle());
        setSize(270,130);
    }

}
