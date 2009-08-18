package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.WorkInProgress;

/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 * <p/>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * <p/>
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * <p/>
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
public abstract class FormDeployPortletDefinition extends FormPanel {

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


        final com.extjs.gxt.ui.client.widget.form.FileUploadField portletDefinitionField = new com.extjs.gxt.ui.client.widget.form.FileUploadField();
        portletDefinitionField.setAllowBlank(false);
        portletDefinitionField.setName("portletDefinition");
        portletDefinitionField.setFieldLabel(Messages.getNotEmptyResource("portlet_definition", "Portlet Definition"));
        add(portletDefinitionField);

        final HiddenField preparePortlet = new HiddenField();
        preparePortlet.setName("doPrepare");
        preparePortlet.setValue(false);
        add(preparePortlet);


        final HiddenField deployPortlet = new HiddenField();
        deployPortlet.setName("doDeploy");
        deployPortlet.setValue(false);
        add(deployPortlet);


        Button deployButton = new Button(Messages.getNotEmptyResource("fm_portlet_deploy", "Deploy"));
        deployButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                deployPortlet.setValue(true);
                submit();
            }
        });
        addButton(deployButton);

        Button prepareButton = new Button(Messages.getNotEmptyResource("fm_portlet_preparewar", "Prepare"));
        prepareButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                preparePortlet.setValue(true);
                submit();
            }
        });
        addButton(prepareButton);

        Button prepareAndDeployButton = new Button(Messages.getNotEmptyResource("fm_portlet_prepareAndDeploywar", "Prepare and deploy"));
        prepareAndDeployButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                deployPortlet.setValue(true);
                preparePortlet.setValue(true);
                submit();

            }
        });
        addButton(prepareAndDeployButton);
        addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent formEvent) {
                WorkInProgress.show();
            }
        });

        addListener(Events.Submit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent formEvent) {
                afterSubmit();
                WorkInProgress.hide();
            }
        });




        layout();
    }

     public static String getPortletDeploymentParam(String key) {
        try {
            //Log.debug("Dictionary name: " + jahiaModuleType + "_rb_" + elementId);
            Dictionary dictionary = Dictionary.getDictionary("portletDeployment");
            return dictionary.get(key);
        } catch (Exception e) {
            Log.error("Can't retrieve [" + key + "]", e);
            return key;
        }
    }

    public abstract void afterSubmit();




}

