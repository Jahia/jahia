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
package org.jahia.ajax.gwt.client.module;

import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.service.definition.ContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.ContentDefinitionService;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaNodeService;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.Style;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * GWT module for displaying an input form with Captcha image.
 * User: toto
 * Date: Dec 1, 2008
 * Time: 10:57:45 AM
 */
public class FormJahiaModule extends JahiaModule {

    public String getJahiaModuleType() {
        return JahiaType.FORM;
    }

    public void onModuleLoad(final GWTJahiaPageContext page, final List<RootPanel> rootPanels) {

        if (rootPanels == null) {
            return;
        }
        final List<Image> captchaImages = new ArrayList<Image>();
        for (final RootPanel rootPanel : rootPanels) {
            final String nodeType = DOM.getElementAttribute(rootPanel.getElement(), "nodeType");
            final boolean viewInherited = Boolean.parseBoolean(DOM.getElementAttribute(rootPanel.getElement(), "viewInheritedTypes"));
            final String action = DOM.getElementAttribute(rootPanel.getElement(), "action");
            final String target = DOM.getElementAttribute(rootPanel.getElement(), "target");
            final String captcha = DOM.getElementAttribute(rootPanel.getElement(), "captcha");

            final List<String> excludedItemsList = new ArrayList<String>();
            final List<String> excludedTypesList = new ArrayList<String>();

            ContentDefinitionServiceAsync service = ContentDefinitionService.App.getInstance();
            service.getNodeType(nodeType, new AsyncCallback<GWTJahiaNodeType>() {
                public void onFailure(Throwable caught) {
                    Log.error("error", caught);
                }

                public void onSuccess(GWTJahiaNodeType result) {
                    List<GWTJahiaNodeType> list = new ArrayList();
                    list.add(result);
                    final PropertiesEditor pe = new PropertiesEditor(list,new HashMap<String, GWTJahiaNodeProperty>(),false, viewInherited, excludedItemsList, excludedTypesList);
                    final boolean isCaptcha = captcha != null && captcha.length() > 0;
                    final TextField captchaField = new TextField();
                    if (isCaptcha) {
                        captchaField.setName("j_captcha_response");
                        captchaField.setFieldLabel("Please recopy the code");
                        pe.add(captchaField);
                        Image captchaImage = new Image(captcha);
                        pe.add(captchaImage);
                        captchaImages.add(captchaImage);
                    }
                    final Button save = new Button("Save");
                    save.addSelectionListener(new SelectionListener<ComponentEvent>() {
                        public void componentSelected(ComponentEvent event) {
                            save.disable();
                            if (action.equals("createNode")) {
                                String captchaValue = null;
                                if (isCaptcha) {
                                    captchaValue = (String) captchaField.getValue();
                                    if (captchaValue == null) captchaValue = "";
                                }
                                JahiaNodeService.App.getInstance().createNode(target, "node" + System.currentTimeMillis(), nodeType, pe.getProperties(), captchaValue, new AsyncCallback<GWTJahiaNode>() {
                                    public void onFailure(Throwable caught) {
                                        if (caught.getMessage().equals("Invalid captcha")) {
                                            String captchaUrl = captcha + "?" + System.currentTimeMillis();
                                            for (Image captchaImage : captchaImages) {
                                                captchaImage.setUrl(captchaUrl);
                                            }
//                                            pe.remove(pe.getWidget(pe.getItemCount()-1));
//                                            pe.add(new Image(captcha + "?" + System.currentTimeMillis()));
//                                            rootPanel.remove(pe);
//                                            rootPanel.add(pe);
                                        }
                                        Log.error("error", caught);
                                        save.enable();
                                    }

                                    public void onSuccess(GWTJahiaNode result) {
                                        Window.Location.reload();
                                    }
                                });
                            }
                        }
                    });
                    pe.setButtonAlign(Style.HorizontalAlignment.CENTER);
                    pe.addButton(save);
//                    pe.addButton(new Button("Restore", new SelectionListener<ComponentEvent>() {
//                        public void componentSelected(ComponentEvent event) {
//                            pe.resetForm();
//                        }
//                    }));
                    rootPanel.add(pe);
                }
            });
        }

    }

}
