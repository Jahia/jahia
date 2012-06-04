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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * Shows the distant publication action confirmation dialog (with optional start date field) and executes the action.
 * 
 * @author Sergiy Shyrkov
 */
public class ExecuteRemotePublicationActionItem extends ExecuteActionItem {

    private static final long serialVersionUID = 1008895688179692790L;

    private String titleKey;

    private transient CalendarField calendarField;

    private transient Window wnd;

    @Override
    public void onComponentSelection() {
        if (wnd != null) {
            wnd.show();
            return;
        }
        wnd = new Window();
        wnd.setSize(500, 150);
        wnd.setModal(true);
        wnd.setBlinkModal(true);
        wnd.setHeading(titleKey != null ? Messages.get(titleKey) : Messages.get("label.information",
                "Information"));
        wnd.setLayout(new FitLayout());

        final FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setFrame(false);
        form.setLabelWidth(250);

        VerticalPanel vpLabels = new VerticalPanel();
        vpLabels.add(new Label(confirmationMessageKey != null ? Messages.get(confirmationMessageKey,
                "You are about to execute action " + action + ". Do you want to continue?")
                : "You are about to execute action " + action + ". Do you want to continue?"));

        form.add(vpLabels);

        calendarField = new CalendarField("yyyy-MM-dd HH:mm", true, false, "startDate", false, null);
        calendarField.setFieldLabel( Messages.get("label.remotePublication.startDate",
                        "Time of the first replication (optional)"));
        calendarField.setAllowBlank(true);
        form.add(calendarField);

        Button btnSubmit = new Button(Messages.get("label.yes", "Yes"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                wnd.mask(Messages.get("label.executing", "Executing action..."));
                doAction();
            }
        });
        form.addButton(btnSubmit);


        Button btnCancel = new Button(Messages.get("label.no", "No"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                wnd.hide();
            }
        });
        form.addButton(btnCancel);
        form.setButtonAlign(HorizontalAlignment.CENTER);

        wnd.add(form);

        wnd.show();
    }

    @Override
    protected String getRequestData() {
        return calendarField.getValue() != null ? ("start=" + calendarField.getValue()
                .getTime()) : null;
    }

    @Override
    protected void actionExecuted(int statusCode) {
        if (wnd != null) {
            wnd.unmask();
            wnd.hide();
        }
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }
}
