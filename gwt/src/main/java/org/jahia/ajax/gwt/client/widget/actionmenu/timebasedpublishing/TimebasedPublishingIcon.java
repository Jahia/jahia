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

import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.service.actionmenu.ActionMenuService;
import org.jahia.ajax.gwt.client.service.actionmenu.ActionMenuServiceAsync;
import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingDetails;
import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingState;
import org.jahia.ajax.gwt.client.widget.actionmenu.StateIcon;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * Time-based publishing state icon.
 *
 * @author rfelden
 * @version 26 f�vr. 2008 - 09:34:45
 */
public class TimebasedPublishingIcon extends StateIcon {

    private ActionMenuServiceAsync actionMenuService = ActionMenuService.App.getInstance();

    public TimebasedPublishingIcon(final GWTJahiaTimebasedPublishingState state, final GWTJahiaPageContext page) {
        super() ;
        final String iconStyle = state.getState() + "_tbp" ;
        setStyleName(iconStyle);
        addClickListener(new ClickListener() {
            public void onClick(final Widget widget) {
                actionMenuService.getTimebasedPublishingDetails(page, state, new AsyncCallback<GWTJahiaTimebasedPublishingDetails>() {
                    public void onFailure(Throwable throwable) {
                        Window.alert("Timebased publishing details request failed\n" + throwable.getLocalizedMessage()) ;
                    }
                    public void onSuccess(GWTJahiaTimebasedPublishingDetails details) {
                        if (details != null) {
                            final TimebasedPublishingDetailsPopup popup = new TimebasedPublishingDetailsPopup(details, iconStyle) ;
                            int left = (widget.getAbsoluteLeft() + 8) ;
                            int top = (widget.getAbsoluteTop() + 8) ;
                            popup.setPosition(left, top);
                            popup.show();
                        } else {
                            // no details available
                        }
                    }
                });
            }
        });
    }

}
