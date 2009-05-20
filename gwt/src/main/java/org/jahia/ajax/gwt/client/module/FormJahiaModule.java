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
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;

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
