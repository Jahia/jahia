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
package org.jahia.ajax.gwt.client.widget.actionmenu.actions;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.extjs.gxt.ui.client.widget.Info;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.service.actionmenu.ActionMenuServiceAsync;
import org.jahia.ajax.gwt.client.service.actionmenu.ActionMenuService;
import org.jahia.ajax.gwt.client.data.actionmenu.actions.GWTJahiaAction;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;

import java.util.List;

/**
 * This is the action menu icon, with a menu opening on click.
 *
 * @author rfelden
 * @version 26 f�vr. 2008 - 09:33:31
 */
public class ActionMenuIcon extends HTML {

    private final static String ACTION_MENU_DEFAULT_ICONSTYLE = "action-menu-icon" ;
    private final static String ACTION_MENU_UNAVAILABLE_ICONSTYLE = "action-menu-icon-unavailable" ;
    public static final int CONTAINER_EDIT = 2 ;
    public static final int CONTAINER_ADD = 1 ;
    public static final int CONTAINERLIST_UPDATE = 0 ;


    private ActionMenuServiceAsync actionMenuService = ActionMenuService.App.getInstance();

    private ActionMenu attachedActions ;
    private boolean disableCaching = true ;
    private String iconStyle =  null ;

    public ActionMenuIcon(final GWTJahiaPageContext page,
                             final String objectKey,
                             final String bundleName,
                             final String namePostFix,
                             final String labelKey,
                             final String iconStyle) {
        super() ;
        setStyleName(ACTION_MENU_DEFAULT_ICONSTYLE);
        if (iconStyle != null && iconStyle.trim().length() > 0) {
            this.iconStyle = iconStyle ;
            addStyleName(this.iconStyle);
        }

        if (objectKey.startsWith("ContentContainerList")) {
            // container list case
            if (labelKey == null || labelKey.length() == 0) {
                setHTML("&nbsp;&nbsp;&nbsp;&nbsp;") ;
                setSize("12px", "12px") ;
            } else {
                setHTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + labelKey) ;
            }
        } else {
            // generic case
            setHTML("&nbsp;&nbsp;&nbsp;&nbsp;") ;
            setSize("12px", "12px") ;
            if (labelKey != null && labelKey.length() > 0) {
                setTitle(labelKey);
            }
        }

        addClickListener(new ActionMenuListener(page, objectKey, bundleName, namePostFix));
    }

    /**
     * This inner class represents a click listener that builds the action menu according to the given arguments.
     */
    private class ActionMenuListener implements ClickListener {

        String objectKey ;
        GWTJahiaPageContext page ;
        String bundleName ;
        String namePostFix ;
        boolean enableAddItem ;

        public ActionMenuListener(GWTJahiaPageContext page, String objectKey, String bundleName, String namePostFix) {
            this.objectKey = objectKey ;
            this.page = page ;
            this.bundleName = bundleName ;
            this.namePostFix = namePostFix ;
        }

        public void onClick(final Widget widget) {
            if (disableCaching || attachedActions == null) {
                actionMenuService.getAvailableActions(page, objectKey, bundleName, namePostFix, new AsyncCallback<List<GWTJahiaAction>>() {
                    public void onFailure(Throwable throwable) {
                        setStyleName(ACTION_MENU_UNAVAILABLE_ICONSTYLE);
                        Log.error(throwable.toString()) ;
                        attachedActions = new ActionMenu("unavailable (error)\n" + throwable.toString()) ;
                    }

                    public void onSuccess(List<GWTJahiaAction> actions) {
                        if (actions != null) {
                            if (getStyleName().equals(ACTION_MENU_UNAVAILABLE_ICONSTYLE)) {
                                setStyleName(ACTION_MENU_DEFAULT_ICONSTYLE);
                                if (iconStyle != null && iconStyle.trim().length() > 0) {
                                    addStyleName(iconStyle);
                                }
                            }
                            attachedActions = new ActionMenu(actions);
                            showMenu(widget) ;
                        } else {
                            Log.error("Authentication error") ;
                            setStyleName(ACTION_MENU_UNAVAILABLE_ICONSTYLE);
                            Info.display("Authentication error", "Action menus are not available since you are not logged in or don't have write access on this content", "");
                        }
                    }
                });
            } else {
                showMenu(widget);
            }
        }

        public void showMenu(final Widget widget) {
            int left = (widget.getAbsoluteLeft() + 8) ;
            int top = (widget.getAbsoluteTop() + 8) ;
            attachedActions.show(left, top);
        }

    }

}
