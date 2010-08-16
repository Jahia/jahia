/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.form;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.allen_sauer.gwt.log.client.Log;

/**
 * RSS mashup creating form.
 * User: jahia
 * Date: 27 f�vr. 2009
 * Time: 16:20:39
 */
public abstract class FormQuickRSS extends FormQuickMashup {


    public FormQuickRSS(String folderPath) {
        super(folderPath);
        createUI();
    }

    protected void createUI() {
        setBodyBorder(false);
        setFrame(false);
        setAutoHeight(true);
        setHeaderVisible(false);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setStyleAttribute("padding", "4");


        final TextField<String> nameField = new TextField<String>();
        nameField.setName("name");
        nameField.setFieldLabel(Messages.get("label.name", "Name"));
        nameField.setAllowBlank(false);
        nameField.setMaxLength(200);
        add(nameField);

        final TextField<String> urlField = new TextField<String>();
        urlField.setName("url");
        urlField.setFieldLabel(Messages.get("label.url", "Url"));
        urlField.setAllowBlank(false);
        urlField.setMaxLength(200);
        add(urlField);


        // save properties button
        Button saveButton = new Button(Messages.get("label.save"));
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                if (nameField.getValue() == null || nameField.getValue().trim().length() == 0
                        || urlField.getValue() == null || urlField.getValue().trim().length() == 0) {
                    return;
                }
                JahiaContentManagementService.App.getInstance().createRSSPortletInstance(getFolderPath(), nameField.getValue(), urlField.getValue(), new BaseAsyncCallback<GWTJahiaNode>() {
                    public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                        onMashupCreated();                        
                        if(getParent() instanceof Window){
                            ((Window)getParent()).hide();
                        }
                    }

                    public void onApplicationFailure(Throwable throwable) {
                        Log.error("Unable to create rss portlet", throwable);
                        if(getParent() instanceof Window){
                            ((Window)getParent()).hide();
                        }
                    }
                });

            }
        });
        addButton(saveButton);

        layout();
    }
}
