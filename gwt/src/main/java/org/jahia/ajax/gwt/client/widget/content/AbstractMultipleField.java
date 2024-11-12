/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract layer for multiple fields {@link MultipleTextField} and {@link MultipleNumberField}
 *
 * Created by kevan
 */
public abstract class AbstractMultipleField<T> extends MultiField<List<T>> {
    private Button addFieldButton;
    private boolean allowBlank;

    AbstractMultipleField() {
        super();
        setOrientation(Style.Orientation.VERTICAL);
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        addFieldButton = new Button(Messages.get("label.add"));
        addFieldButton.addStyleName("button-add");
        addFieldButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                addField(null);
                AbstractMultipleField.this.fireEvent(Events.Change, ce);
            }
        });
        if (!readOnly) {
            lc.add(addFieldButton);
        }

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                lc.setWidth(getWidth());
                for (Field<?> field : fields) {
                    field.setWidth(getWidth());
                }
                lc.layout();
            }
        });
        originalValue = value;

        if (GXT.isAriaEnabled()) {
            if (!allowBlank) {
                setAriaState("aria-required", "true");
            }
        }
    }

    @Override
    public void markInvalid(String msg) {
        super.markInvalid(msg);
        if (errorIcon != null) {
            errorIcon.addStyleName("invalid-icon");
        }
    }

    private void addField(T value) {
        clearInvalid();
        Field field = getNewField();

        field.setWidth(getWidth());
        if (value != null) {
            field.setValue(value);
        }
        field.setReadOnly(readOnly);
        fields.add(field);

        field.addListener(Events.Change, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                AbstractMultipleField.this.fireEvent(Events.Change, be);
            }
        });
        if (afterRender) {
            TableData data = (TableData) ComponentHelper.getLayoutData(field);
            if (data == null) {
                data = new TableData();
            }
            String style = "position: static;";

            if (spacing > 0) {
                style += "paddingTop:" + spacing + "px;";
            }

            data.setStyle(style);
            lc.insert(field, fields.size()-1, data);
            lc.layout();
        }
    }

    abstract Field getNewField();

    @Override
    protected boolean validateValue(String value) {
        if (allowBlank) {
            return super.validateValue(value);
        } else if (!fields.isEmpty()) {
            // check that at least one field is not empty
            for (Field field : fields) {
                if (!"".equals(field.getRawValue())) {
                    return super.validateValue(value);
                }
            }
        }
        markInvalid(GXT.MESSAGES.textField_blankText());
        return false;
    }

    @Override
    public List<T> getValue() {
        List<T> values = new ArrayList<T>();
        for (Field<?> field : getAll()) {
            values.add(((Field<T>)field).getValue());
        }
        return values;
    }

    @Override
    public void setValue(List<T> value) {
        super.setValue(value);
        for (Field<?> field : fields) {
            lc.remove(field);
        }
        fields.clear();
        for (T t : value) {
            addField(t);
        }
    }

    @Override
    public void setRawValue(String value) {
        // Cannot set raw value
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        boolean reRender = false;
        if (rendered) {
            if (this.readOnly && !readOnly) {
                lc.add(addFieldButton);
                reRender = true;
            }
            if (!this.readOnly && readOnly) {
                lc.remove(addFieldButton);
                reRender = true;
            }
            reRender |= !fields.isEmpty();
        }
        this.readOnly = readOnly;
        for (Field<?> field : fields) {
            field.setReadOnly(readOnly);
        }
        if (reRender) {
            lc.layout();
        }
    }

    public void setAllowBlank(boolean allowBlank) {
        this.allowBlank = allowBlank;
    }
}
