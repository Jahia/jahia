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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;

import java.util.Map;

/**
 * User: rfelden
 * Date: 15 oct. 2008 - 15:49:33
 */
public class QuickWorkflowActionItem extends BaseActionItem {


    @Override
    public void onSelection() {
        Map<String, GWTJahiaProperty> props = getGwtToolbarItem().getProperties() ;
        if (!props.containsKey("mode") || props.get("mode").getValue().equals("quick")) {
            new QuickWorkflowDialog(getGwtToolbarItem()).show();
        } else {
            final String action = props.get("action").getValue() ;
            final String language = props.get("language").getValue() ;
            final String objectKey = props.get("objectKey").getValue() ;

            ToolbarService.App.getInstance().quickAddToBatch(objectKey, language, action, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Log.error(throwable.toString()) ;
                }
                public void onSuccess(Object o) {
                }
            });

        }
    }

    /**
     * A window with a "comment form"
     */
    private class QuickWorkflowDialog extends Window {
        private Button execute ;
        private TextArea comments ;

        public QuickWorkflowDialog(final GWTJahiaToolbarItem gwtToolbarItem) {
            super();

            Map<String, GWTJahiaProperty> props = gwtToolbarItem.getProperties() ;
            final String action = props.get("action").getValue() ;
            final String label = props.get("label").getValue() ;
            final String language = props.get("language").getValue() ;
            final String objectKey = props.get("objectKey").getValue() ;

            //setLayout(new FitLayout());
            setHeading(label);
            setResizable(false);
            setModal(true);

            comments = new TextArea();
            if (GXT.isIE) {
                comments.setSize(278, 84);
            } else {
                comments.setSize(278, 90);
            }

            add(comments);

            ButtonBar buttons = new ButtonBar() ;
            Button cancel = new Button("Cancel", new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    hide() ;
                }
            }) ;
            execute = new Button("OK", new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    if (action.equalsIgnoreCase("publishAll")) {
                        ToolbarService.App.getInstance().publishAll(comments.getRawValue(), new AsyncCallback() {
                            public void onFailure(Throwable throwable) {
                                Log.error(throwable.toString()) ;
                                hide() ;
                            }
                            public void onSuccess(Object o) {
                                hide() ;
                            }
                        });
                    } else {
                        ToolbarService.App.getInstance().quickValidate(objectKey, language, action, comments.getRawValue(), new AsyncCallback() {
                            public void onFailure(Throwable throwable) {
                                Log.error(throwable.toString()) ;
                                hide() ;
                            }
                            public void onSuccess(Object o) {
                                hide() ;
                            }
                        });
                    }
                }
            }) ;
            buttons.add(execute) ;
            buttons.add(cancel) ;
            execute.setIconStyle("wf-button_ok");
            cancel.setIconStyle("wf-button_cancel");
            setButtonAlign(Style.HorizontalAlignment.CENTER);
            setTopComponent(buttons);
        }

        public void show() {
            setSize(300, 150);
            super.show();
        }
    }
}
