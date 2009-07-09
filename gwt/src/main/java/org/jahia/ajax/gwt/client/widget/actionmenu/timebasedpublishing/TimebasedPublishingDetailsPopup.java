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
package org.jahia.ajax.gwt.client.widget.actionmenu.timebasedpublishing;

import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingDetails;
import org.jahia.ajax.gwt.client.util.EngineOpener;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
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
        item.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent componentEvent) {
                EngineOpener.openEngine(details.getUrl());
            }
        }) ;
        ButtonBar bar = new ButtonBar() ;
        bar.add(item) ;
        setTopComponent(bar);

        setIconStyle(iconStyle);
        setHeading(details.getTitle());
        setSize(270,130);
    }

}
