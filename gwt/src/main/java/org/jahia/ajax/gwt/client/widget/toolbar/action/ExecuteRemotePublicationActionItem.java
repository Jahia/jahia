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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

/**
 * Shows the distant publication action confirmation dialog (with optional start date field) and executes the action.
 *
 * @author Sergiy Shyrkov
 */
public class ExecuteRemotePublicationActionItem extends ExecuteActionItem {

    private static final long serialVersionUID = 1008895688179692790L;

    private boolean showOptions = true;

    private String titleKey;

    private transient CalendarField calendarFieldStart;

    private transient CalendarField calendarFieldEnd;

    private transient Window wnd;

    @Override
    public void onComponentSelection() {
        if (wnd != null) {
            wnd.show();
            return;
        }
        wnd = new Window();
        wnd.setWidth(550);
        wnd.setHeight(showOptions ? 220 : 120);
        wnd.setModal(true);
        wnd.setBlinkModal(true);
        wnd.setHeadingHtml(titleKey != null ? Messages.get(titleKey) : Messages.get("label.information",
                "Information"));
        wnd.setLayout(new FitLayout());

        final FormPanel form = new FormPanel();
        form.setHeight(showOptions ? 180 : 80);
        form.setHeaderVisible(false);
        form.setFrame(false);
        form.setLabelWidth(250);

        VerticalPanel vpLabels = new VerticalPanel();
        vpLabels.add(new Label(confirmationMessageKey != null ? Messages.get(confirmationMessageKey,
                "You are about to execute action " + action + ". Do you want to continue?")
                : "You are about to execute action " + action + ". Do you want to continue?"));

        form.add(vpLabels);

        if (showOptions) {
            FieldSet fieldSet = new FieldSet();
            fieldSet.setHeadingHtml(Messages.get("label.options", "Options"));
            FormLayout layout = new FormLayout();
            layout.setLabelWidth(250);
            fieldSet.setLayout(layout);
            fieldSet.setCollapsible(true);
            fieldSet.collapse();
            fieldSet.addListener(Events.Expand, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent componentEvent) {
                    wnd.setHeight(wnd.getHeight() + 70);
                }
            });
            fieldSet.addListener(Events.Collapse, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent componentEvent) {
                    wnd.setHeight(wnd.getHeight() - 70);
                }
            });

            calendarFieldStart = new CalendarField("yyyy-MM-dd HH:mm", true, false, "startDate",
                    false, null);
            calendarFieldStart.setFieldLabel(Messages.get("label.remotePublication.startDate",
                    "Start time of the replication (optional)"));
            calendarFieldStart.setAllowBlank(true);
            fieldSet.add(calendarFieldStart);

            calendarFieldEnd = new CalendarField("yyyy-MM-dd HH:mm", true, false, "endDate", false,
                    null);
            calendarFieldEnd.setFieldLabel(Messages.get("label.remotePublication.endDate",
                    "End time of the replication (optional)"));
            calendarFieldEnd.setAllowBlank(true);
            fieldSet.add(calendarFieldEnd);

            form.add(fieldSet);
        }

        Button btnSubmit = new Button(Messages.get("label.yes", "Yes"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                wnd.mask(Messages.get("label.executing", "Executing action..."));
                doAction();
            }
        });
        btnSubmit.addStyleName("button-yes");

        form.addButton(btnSubmit);


        Button btnCancel = new Button(Messages.get("label.no", "No"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                wnd.hide();
            }
        });
        btnCancel.addStyleName("button-no");

        form.addButton(btnCancel);
        form.setButtonAlign(HorizontalAlignment.CENTER);

        wnd.add(form);
        wnd.layout();

        wnd.show();
    }

    @Override
    protected String getRequestData() {
        if (!showOptions) {
            return null;
        }
        StringBuffer data = new StringBuffer();
        if (calendarFieldStart.getValue() != null) {
            data.append("start=").append(calendarFieldStart.getValue().getTime());
        }
        if (calendarFieldEnd.getValue() != null) {
            if (data.length() > 0) {
                data.append("&");
            }
            data.append("end=").append(calendarFieldEnd.getValue().getTime());
        }
        return data.length() > 0 ? data.toString() : null;
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

    public void setShowOptions(boolean showOptions) {
        this.showOptions = showOptions;
    }
}
