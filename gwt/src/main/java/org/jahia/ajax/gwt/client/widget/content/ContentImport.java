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

package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.form.FileUploadField;

/**
 * User: toto
 * Date: Nov 13, 2008 - 7:31:46 PM
 */
public class ContentImport extends Window {

    private Linker m_linker;
    private final boolean replaceContent;

    public ContentImport(final Linker linker, final GWTJahiaNode n) {
        this(linker, n, false);
    }

    public ContentImport(final Linker linker, final GWTJahiaNode n, boolean replaceContent) {
        super();

        m_linker = linker;
        this.replaceContent = replaceContent;

        setHeading(Messages.get("label.import"));
        setSize(500, 150);
        setResizable(false);
        setModal(true);
        setId("JahiaGxtContentImport");
        ButtonBar buttons = new ButtonBar();

        final FormPanel form = new FormPanel();
        form.setFrame(false);
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setLabelWidth(200);

        final FileUploadField field = new FileUploadField("import");
        field.setFieldLabel(Messages.get("label.import"));
        form.add(field);

        final CheckBox checkbox = new CheckBox();
        checkbox.setFieldLabel(Messages.get("label.scheduleAsBackgroundJob", "Schedule as background job"));
        checkbox.setValue(true);
        form.add(checkbox);

        Button submit = new Button(Messages.get("label.ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                mask();
                doImport(n.getPath(), field.getValue(), checkbox.getValue());
            }
        });

        Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });

        buttons.add(submit);
        buttons.add(cancel);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);
        add(form);
    }

    public void doImport(String path, Object value, Boolean asynchronously) {
        Log.debug(path + " " + value);
        JahiaContentManagementService.App.getInstance().importContent(path, value.toString(), asynchronously, replaceContent, new BaseAsyncCallback() {

            public void onApplicationFailure(Throwable caught) {
                com.google.gwt.user.client.Window.alert(Messages.get("label.error") + "\n" + caught.getLocalizedMessage());
                Log.error(Messages.get("label.error"), caught);
                hide();
            }

            public void onSuccess(Object result) {
                m_linker.refresh(Linker.REFRESH_ALL);
                hide();
            }
        });
    }

}
