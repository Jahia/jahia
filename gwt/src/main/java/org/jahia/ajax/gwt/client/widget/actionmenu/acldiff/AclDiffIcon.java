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
