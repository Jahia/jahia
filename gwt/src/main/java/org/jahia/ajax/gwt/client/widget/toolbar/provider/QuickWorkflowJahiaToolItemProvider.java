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
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
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
public class QuickWorkflowJahiaToolItemProvider extends AbstractJahiaToolItemProvider {

    public SelectionListener<ComponentEvent> getSelectListener(final GWTJahiaToolbarItem gwtToolbarItem) {
        return new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                Map<String, GWTJahiaProperty> props = gwtToolbarItem.getProperties() ;
                if (!props.containsKey("mode") || props.get("mode").getValue().equals("quick")) {
                    new QuickWorkflowDialog(gwtToolbarItem).show();
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
        };
    }

    public ToolItem createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        Log.debug("Workflow toolitem: "+gwtToolbarItem.getTitle()+","+gwtToolbarItem.isDisplayTitle());
        return new TextToolItem();
    }

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
            Button cancel = new Button("Cancel", new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    hide() ;
                }
            }) ;
            execute = new Button("OK", new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
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
            setButtonBar(buttons);
        }

        public void show() {
            setSize(300, 150);
            super.show();
        }
    }
}
