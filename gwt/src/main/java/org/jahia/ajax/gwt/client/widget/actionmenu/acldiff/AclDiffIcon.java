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
package org.jahia.ajax.gwt.client.widget.actionmenu.acldiff;

import org.jahia.ajax.gwt.client.data.actionmenu.acldiff.GWTJahiaAclDiffDetails;
import org.jahia.ajax.gwt.client.data.actionmenu.acldiff.GWTJahiaAclDiffState;
import org.jahia.ajax.gwt.client.widget.actionmenu.StateIcon;
import org.jahia.ajax.gwt.client.service.actionmenu.ActionMenuServiceAsync;
import org.jahia.ajax.gwt.client.service.actionmenu.ActionMenuService;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.allen_sauer.gwt.log.client.Log;

/**
 * User: rfelden
 * Date: 23 janv. 2009 - 10:22:40
 */
public class AclDiffIcon extends StateIcon {

    private ActionMenuServiceAsync actionMenuService = ActionMenuService.App.getInstance();

    public AclDiffIcon(final GWTJahiaAclDiffState state, final GWTJahiaPageContext page) {
        super() ;
        if (state != null) {
            final String iconStyle = "acldiff" ;
            setStyleName(iconStyle);
            addClickListener(new ClickListener() {
                public void onClick(final Widget widget) {
                    actionMenuService.getAclDiffDetails(page, state.getObjectKey(), new AsyncCallback<GWTJahiaAclDiffDetails>() {
                        public void onFailure(Throwable throwable) {
                            Window.alert("ACL difference details request failed\n" + throwable.getLocalizedMessage()) ;
                        }
                        public void onSuccess(GWTJahiaAclDiffDetails details) {
                            if (details != null) {
                                Log.debug("disp details") ;
                                final AclDiffDetailsPopup popup = new AclDiffDetailsPopup(details, iconStyle) ;
                                int left = (widget.getAbsoluteLeft() + 8) ;
                                int top = (widget.getAbsoluteTop() + 8) ;
                                popup.setPosition(left, top);
                                popup.show();
                            } else {
                                Log.debug("null details") ;
                            }
                        }
                    });
                }
            });
        }
    }

}
