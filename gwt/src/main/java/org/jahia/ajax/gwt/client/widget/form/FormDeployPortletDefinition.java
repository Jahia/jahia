/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.messages.Messages;

public abstract class FormDeployPortletDefinition extends FormPanel {
    private boolean doCloseParent = true;

    public FormDeployPortletDefinition() {
        super();
        createUI();
    }

    protected void createUI() {
        setAction(getPortletDeploymentParam("formActionUrl"));
        setEncoding(Encoding.MULTIPART);
        setMethod(Method.POST);
        setBodyBorder(false);
        setFrame(false);
        setAutoHeight(true);
        setHeaderVisible(false);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setStyleAttribute("padding", "4");
        setLabelWidth(200);
        setFieldWidth(300);


        final com.extjs.gxt.ui.client.widget.form.FileUploadField portletDefinitionField = new com.extjs.gxt.ui.client.widget.form.FileUploadField();
        portletDefinitionField.setAllowBlank(false);
        portletDefinitionField.setName("portletDefinition");
        portletDefinitionField.setWidth(290);
        portletDefinitionField.setFieldLabel(Messages.get("org.jahia.engines.PortletsManager.wizard.upload.label", "Portlets WAR file"));
        add(portletDefinitionField);

        final HiddenField<Boolean> preparePortlet = new HiddenField<Boolean>();
        preparePortlet.setName("doPrepare");
        preparePortlet.setValue(false);
        add(preparePortlet);


        final HiddenField<Boolean> deployPortlet = new HiddenField<Boolean>();
        deployPortlet.setName("doDeploy");
        deployPortlet.setValue(false);
        add(deployPortlet);

        Button prepareButton = new Button(Messages.get("label.portletPrepareWar", "Prepare"));
        prepareButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                preparePortlet.setValue(true);
                doCloseParent = true;
                submitAfterValidation(portletDefinitionField);
            }
        });
        addButton(prepareButton);

        final boolean autoDeploySupported = autoDeploySupported();
        Button deployButton = new Button(Messages.get("label.deployNewPortlet", "Deploy"));
        deployButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (autoDeploySupported) {
                    deployPortlet.setValue(true);
                    doCloseParent = true;
                    submitAfterValidation(portletDefinitionField);
                } else {
                    doCloseParent = false;
                    Window.open(getAppserverDeployerUrl(), "_blank", "");
                }
            }
        });
        addButton(deployButton);

        if (autoDeploySupported) {
            Button prepareAndDeployButton = new Button(Messages.get("label.prepareAndDeployWar", "Prepare and deploy"));
            prepareAndDeployButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    deployPortlet.setValue(true);
                    preparePortlet.setValue(true);
                    doCloseParent = true;
                    submitAfterValidation(portletDefinitionField);

                }
            });
            addButton(prepareAndDeployButton);

        }

        Button helpButton = new Button("?");
        helpButton.setWidth(30);
        helpButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent buttonEvent) {
                deployPortlet.setValue(false);
                preparePortlet.setValue(false);
                doCloseParent = false;
                submit();
            }
        });
        addButton(helpButton);
        
        final FormPanel form = this;

        addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent formEvent) {
                form.mask(Messages.get("label.loading", "Loading..."));
            }
        });
        addListener(Events.Submit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent formEvent) {
                if (doCloseParent) {
                    closeParent();
                }
                String html = formEvent.getResultHtml();
                if (html != null) {
                    MessageBox.info(Messages.get("label.deployNewPortlet", "Deploy new portlets"), html, new Listener<MessageBoxEvent>() {
                        public void handleEvent(MessageBoxEvent be) {
                            refreshParent();
                        }
                    });
                }
                form.unmask();
            }
        });


        layout();
    }

    private void submitAfterValidation(com.extjs.gxt.ui.client.widget.form.FileUploadField portletDefinitionField) {
        if (portletDefinitionField.getValue() != null && portletDefinitionField.getValue().length() > 0) {
            submit();
        }else{
             MessageBox.alert(Messages.get("label.deployNewPortlet", "Deploy new portlets"), Messages.get("message.selectFileForUpload", "Please select a *.war file"), null);
        }
    }

    public static String getPortletDeploymentParam(String key) {
        try {
            //Log.debug("Dictionary name: " + jahiaModuleType + "_rb_" + elementId);
            Dictionary dictionary = Dictionary.getDictionary("portletDeployment");
            return dictionary.get(key.replace('.','_'));
        } catch (Exception e) {
            Log.error("Can't retrieve [" + key + "]", e);
            return key;
        }
    }

    public static boolean autoDeploySupported() {
        try {
            return Boolean.valueOf(getPortletDeploymentParam("autoDeploySupported"));
        } catch (Exception e) {
            return false;
        }
    }

    public static String getAppserverDeployerUrl() {
        try {
            return getPortletDeploymentParam("appserverDeployerUrl");
        } catch (Exception e) {
            return "";
        }
    }

    public abstract void closeParent();

    public abstract void refreshParent();
}

