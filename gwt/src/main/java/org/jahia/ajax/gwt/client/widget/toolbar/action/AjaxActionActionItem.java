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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.toolbar.*;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Util;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Command;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.i18n.client.DateTimeFormat;


import java.util.*;

/**
 * User: jahia
 * Date: 4 juil. 2008
 * Time: 09:58:20
 */
public class AjaxActionActionItem extends BaseActionItem {
    public static final String CLASS_ACTION = "classAction";
    public static final String ACTION = "action";
    public static final String ADD_COMMENT = "addComment";
    public static final String COMMENT = "comment";
    public static final String ON_FAILURE_MESSAGE = "onFailureMessage";
    public static final String ON_SUCCESS = "onSuccess";
    public static final String INFO = "info";
    public static final String NOTIFICATION = "notification";
    public static final String WINDOW = "window";
    public static final String REDIRECT = "redirect";
    public static final String REFRESH = "refresh";
    public static final String SELECTED = "selected";
    public static final String TOGGLE = "toggle";
    private static final String CONFIRMATION = "confirmation";
    private static final String PROMPT = "prompt";
    private static final String MULTIPROMPT = "multiprompt";
    public static final String SITE_STATS = "siteStats";
    public static final String PAGE_STATS = "pageStats";
    private Map<String, String> data = new HashMap<String, String>();


    @Override
    public void onComponentSelection() {
        if (handleAddCommentProperty(getGwtToolbarItem())) {
            return;
        }

        String prompt = getPropertyValue(getGwtToolbarItem(), PROMPT);
        if (prompt != null && prompt.length() != 0) {
            MessageBox.prompt(getGwtToolbarItem().getTitle(), prompt, new Listener<MessageBoxEvent>() {
                public void handleEvent(MessageBoxEvent be) {
                    if (MessageBox.OK.equalsIgnoreCase(be.getButtonClicked().getText())) {
                        getGwtToolbarItem().getProperties() .put(COMMENT, new GWTJahiaProperty(COMMENT, be.getMessageBox().getTextBox().getValue()));
                        execute(getGwtToolbarItem());
                    }
                }
            });
            return;
        }

        String multiprompt = getPropertyValue(getGwtToolbarItem(), MULTIPROMPT);
        if (multiprompt != null && multiprompt.length() != 0) {
            MessageBox.prompt(getGwtToolbarItem().getTitle(), multiprompt, true, new Listener<MessageBoxEvent>() {
                public void handleEvent(MessageBoxEvent be) {
                    if (MessageBox.OK.equalsIgnoreCase(be.getButtonClicked().getText())) {
                        getGwtToolbarItem()
                                .getProperties()
                                .put(COMMENT,
                                        new GWTJahiaProperty(COMMENT, be.getMessageBox().getTextArea().getValue()));
                        execute(getGwtToolbarItem());
                    }
                }
            });
            return;
        }

        String confirmation = getPropertyValue(getGwtToolbarItem(), CONFIRMATION);
        if (confirmation != null && confirmation.length() != 0) {
            MessageBox.confirm(getGwtToolbarItem().getTitle(), confirmation, new Listener<MessageBoxEvent>() {
                public void handleEvent(MessageBoxEvent be) {
                    if (Dialog.YES.equalsIgnoreCase(be.getButtonClicked().getText())) {
                        execute(getGwtToolbarItem());
                    }
                }
            });
            return;
        }

        // create a selectedProperty property
        getGwtToolbarItem().getProperties().put(SELECTED, new GWTJahiaProperty(SELECTED, String.valueOf(!getGwtToolbarItem().isSelected())));

        // execute action without displaying a MessageBox
        execute(getGwtToolbarItem());
    }


    /**
     * Open dialog box
     *
     * @param gwtToolbarItem
     * @return
     */
    private boolean handleAddCommentProperty(GWTJahiaToolbarItem gwtToolbarItem) {
        final String value = getPropertyValue(gwtToolbarItem, ADD_COMMENT);
        if (value != null) {
    
            boolean addComment = Boolean.valueOf(value);
            if (addComment) {
                try {
                    showDialogBox(gwtToolbarItem);
                    return true;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }


    /**
     * Create a new toolItem
     *
     * @return
     */
    public Component createNewToolItem() {
        GWTJahiaProperty prop = getGwtToolbarItem().getProperties().get(TOGGLE);
        try {
            if (prop != null && prop.getValue() != null && Boolean.parseBoolean(prop.getValue())) {
                return new ToggleButton();
            }
        } catch (Exception e) {
            Log.error("Error when parsing 'toogle' prop.", e);
        }
        return new Button();
    }


    /**
     * @param gwtToolbarItem
     */
    private void showDialogBox(final GWTJahiaToolbarItem gwtToolbarItem) {
        // display a message box
        final MessageBox box = new MessageBox();
        box.setTitle(gwtToolbarItem.getTitle());
        box.setButtons(MessageBox.OKCANCEL);
        box.setType(MessageBox.MessageBoxType.MULTIPROMPT);
        box.setModal(true);

        // execute action listener
        final Listener<MessageBoxEvent> executeActionListener = new Listener<MessageBoxEvent>() {
            public void handleEvent(MessageBoxEvent ce) {
                Button btn = ce.getButtonClicked();
                if (btn.getText().equalsIgnoreCase(MessageBox.OK)) {
                    // add comment in the properties map
                    gwtToolbarItem.getProperties().put(COMMENT, new GWTJahiaProperty(COMMENT, box.getTextArea().getValue()));

                    // execute ajax action
                    execute(gwtToolbarItem);
                }
            }
        };
        box.addCallback(executeActionListener);

        // display box
        box.show();
    }

    /**
     * Execute the ajax action
     *
     * @param gwtToolbarItem
     */
    protected void execute(final GWTJahiaToolbarItem gwtToolbarItem) {
        ToolbarService.App.getInstance().execute(gwtToolbarItem.getProperties(), new AsyncCallback<GWTJahiaAjaxActionResult>() {
            public void onSuccess(GWTJahiaAjaxActionResult result) {
                // depending on "onSuccess" property , display info, notify, redirect or refresh
                final Map properties = gwtToolbarItem.getProperties();
                if (properties != null) {
                    GWTJahiaProperty actionProperty = (GWTJahiaProperty) properties.get(ON_SUCCESS);
                    if (actionProperty != null) {
                        String action = actionProperty.getValue();
                        if (action != null && action.length() > 0) {
                            // case of info
                            if (action.equalsIgnoreCase(INFO)) {
                                if (result != null && result.getErrors().isEmpty() && result.getValue() != null
                                        && !"".equals(result.getValue().trim())) {
                                    MessageBox box = new MessageBox();
                                    box.setButtons(MessageBox.OK);
                                    box.setIcon(MessageBox.INFO);
                                    box.setTitle(gwtToolbarItem.getTitle());
                                    box.setMessage(result.getValue());
                                    box.show();
                                }
                            }
                            // case of a notification
                            else if (action.equalsIgnoreCase(NOTIFICATION)) {
                                Info.display(gwtToolbarItem.getTitle(), result.getValue());
                            }
                            // case of redirection
                            else if (action.equalsIgnoreCase(REDIRECT)) {
                                if (result != null && result.getErrors().isEmpty() && result.getValue() != null
                                        && !"".equals(result.getValue().trim())) {
                                    Window.Location.assign(result.getValue());
                                }
                            }
                            // case of redirection
                            else if (action.equalsIgnoreCase(REFRESH)) {
                                Window.Location.reload();
                            }
                            // case of redirection
                            else if (action.equalsIgnoreCase(WINDOW)) {
                                com.extjs.gxt.ui.client.widget.Window window = new com.extjs.gxt.ui.client.widget.Window();
                                if (gwtToolbarItem.getTitle() != null) {
                                    String title = gwtToolbarItem.getTitle().replaceAll(" ", "_");
                                    window.setTitle(title);
                                }
                                window.addText(result.getValue());
                                window.setModal(true);
                                window.setResizable(true);
                                window.setClosable(true);
                                window.show();
                            }
                        }
                    }
                }
            }

            public void onFailure(Throwable throwable) {
                // display failure message
                final Map properties = gwtToolbarItem.getProperties();
                if (properties != null) {
                    GWTJahiaProperty messageProp = (GWTJahiaProperty) properties.get(ON_FAILURE_MESSAGE);
                    if (messageProp != null) {
                        String message = messageProp.getValue();
                        if (message != null && message.length() > 0) {
                            MessageBox box = new MessageBox();
                            box.setIcon(MessageBox.ERROR);
                            box.setTitle(gwtToolbarItem.getTitle());
                            box.setMessage(message);
                            box.show();
                        }
                    }
                }
            }
        });
    }

}
