/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.ArrayList;
import java.util.List;

public class MultipleTextField<T> extends MultiField<List<T>> {

    protected Button addFieldButton;

    public MultipleTextField() {
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
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);    //To change body of overridden methods use File | Settings | File Templates.
    }

    private void addField(T value) {
        ItemField field = new ItemField();
        field.setWidth(getWidth());
        if (value != null) {
            field.setValue(value);
        }
        field.setReadOnly(readOnly);
        fields.add(field);

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
        //
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

    class ItemField extends TriggerField<T> {

        ItemField() {
            setEditable(true);
            setTriggerStyle("x-form-clear-trigger");
        }

        @Override
        protected void onTriggerClick(ComponentEvent ce) {
            fields.remove(this);
            removeFromParent();
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            super.setReadOnly(readOnly);
            setHideTrigger(readOnly);
        }
    }

}
