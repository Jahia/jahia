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
abstract class AbstractMultipleField<T> extends MultiField<List<T>> {
    private Button addFieldButton;

    AbstractMultipleField() {
        super();
        setOrientation(Style.Orientation.VERTICAL);
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        addFieldButton = new Button(Messages.get("label.add"));
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
    }

    private void addField(T value) {
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
}
