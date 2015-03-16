/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
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
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Text field with multiple values
 * @param <T>
 */
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
                MultipleTextField.this.fireEvent(Events.Change, ce);
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

    private void addField(T value) {
        ItemField field = new ItemField();
        field.setWidth(getWidth());
        if (value != null) {
            field.setValue(value);
        }
        field.setReadOnly(readOnly);
        fields.add(field);

        field.addListener(Events.Change, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                MultipleTextField.this.fireEvent(Events.Change, be);
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

    /**
     * Field for one value
     */
    class ItemField extends TriggerField<T> {

        ItemField() {
            setEditable(true);
            setTriggerStyle("x-form-clear-trigger");
        }

        @Override
        protected void onTriggerClick(ComponentEvent ce) {
            fields.remove(this);
            this.removeAllListeners();
            MultipleTextField.this.fireEvent(Events.Change, ce);
            removeFromParent();
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            super.setReadOnly(readOnly);
            setHideTrigger(readOnly);
        }
    }

}
