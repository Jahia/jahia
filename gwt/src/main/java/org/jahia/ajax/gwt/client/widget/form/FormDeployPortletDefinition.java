/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.form;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.messages.Messages;

public abstract class FormDeployPortletDefinition extends FormPanel {
    private boolean doCloseParent = true;

    public FormDeployPortletDefinition() {
        super();
        createUI();
    }

    public static String getPortletDeploymentParam(String key) {
        try {
            //Log.debug("Dictionary name: " + jahiaModuleType + "_rb_" + elementId);
            Dictionary dictionary = Dictionary.getDictionary("portletDeployment");
            return dictionary.get(key.replace('.', '_'));
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

    protected void createUI() {
        setAction(getPortletDeploymentParam("formActionUrl"));
        setId("gwt-portlet-upload");
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

        final HiddenField<String> jcrReturnContentType = new HiddenField<String>();
        jcrReturnContentType.setName("jcrReturnContentType");
        jcrReturnContentType.setValue("json");
        add(jcrReturnContentType);

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

        layout();
    }

    private void onBeforeSubmit() {
        mask(Messages.get("label.loading", "Loading..."));
    }

    private void onSubmit(String resultHtml) {
        if (doCloseParent) {
            closeParent();
        }
        HTML responseHTML = new HTML(resultHtml);
        String response = responseHTML.getText();

        if (!response.trim().isEmpty()) {
            JSONValue rspValue = JSONParser.parseStrict(response);
            if (rspValue != null && rspValue.isObject() != null && rspValue.isObject().containsKey("dspMsg")) {
                String dspMsg = rspValue.isObject().get("dspMsg").isString().stringValue();
                MessageBox.info(Messages.get("label.deployNewPortlet", "Deploy new portlets"), dspMsg, new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        refreshParent();
                    }
                });
            }
        }
        unmask();
    }

    private void submitAfterValidation(com.extjs.gxt.ui.client.widget.form.FileUploadField portletDefinitionField) {
        if (portletDefinitionField.getValue() != null && portletDefinitionField.getValue().length() > 0) {
            submit();
        } else {
            MessageBox.alert(Messages.get("label.deployNewPortlet", "Deploy new portlets"), Messages.get("message.selectFileForUpload", "Please select a *.war file"), null);
        }
    }

    @Override
    public void submit() {
        submitForm(getAction(), getId(), this);
    }

    public static native void submitForm(String url, String id, FormDeployPortletDefinition instance) /*-{
        instance.@FormDeployPortletDefinition::onBeforeSubmit()();

        var xhr = new $wnd.parent.XMLHttpRequest();
        xhr.open('POST', url);
        xhr.addEventListener("load", function (event) {
            instance.@FormDeployPortletDefinition::onSubmit(*)(xhr.response);
        });

        xhr.send(new FormData($doc.querySelector("#gwt-portlet-upload form")));
    }-*/;

    public abstract void closeParent();

    public abstract void refreshParent();
}

