/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.form.FileUploadField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        addStyleName("content-import");
        m_linker = linker;
        this.replaceContent = replaceContent;

        setHeadingHtml(Messages.get("label.import"));
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

//        final CheckBox checkbox = new CheckBox();
//        checkbox.setFieldLabel(Messages.get("label.scheduleAsBackgroundJob", "Schedule as background job"));
//        checkbox.setValue(true);
//        form.add(checkbox);

        Button submit = new Button(Messages.get("label.ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                mask();
                doImport(n.getPath(), field.getValue());
            }
        });
        submit.addStyleName("button-submit");

        Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });
        cancel.addStyleName("button-cancel");

        buttons.add(submit);
        buttons.add(cancel);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);
        add(form);
    }

    public void doImport(String path, Object value) {
        Log.debug(path + " " + value);
        JahiaContentManagementService.App.getInstance().importContent(path, value.toString(), replaceContent, new BaseAsyncCallback<List<GWTJahiaJobDetail>>() {

            public void onApplicationFailure(Throwable caught) {
                com.google.gwt.user.client.Window.alert(Messages.get("label.error") + "\n" + caught.getLocalizedMessage());
                Log.error(Messages.get("label.error"), caught);
                hide();
            }

            public void onSuccess(List<GWTJahiaJobDetail> result) {
                removeAll();
                unmask();
                add(new Label(Messages.get("label.import.wait","Your import will be processed by the system as a background job.")));
                if (result.size() > 0) {
                    add(new Label(Messages.getWithArgs("label.import.waitingprocess","There are {0} jobs to finish before this import will be processed.",new Object[] {Integer.toString(result.size())})));
                }

                ButtonBar buttons = ((ButtonBar)getBottomComponent());

                Button close = new Button(Messages.get("label.close"), new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent event) {
                        hide();
                    }
                });
                close.addStyleName("button-close");

                buttons.removeAll();
                buttons.add(close);

                layout();
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(Linker.REFRESH_ALL, true);
                m_linker.refresh(data);
            }
        });
    }

}
