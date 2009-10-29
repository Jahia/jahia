/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.form.PropertyEditor;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.Style;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 1, 2008
 * Time: 6:37:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContentPickerField extends TriggerField<List<GWTJahiaNode>> {
    private List<GWTJahiaNode> value;
    private String rootPath;
    private String types;
    private String filters;
    private String mimeTypes;
    private String configuration;
    private boolean multiple;
    private boolean allowThumbs;

    public ContentPickerField(String rootPath, String types, String filters, String mimeTypes, String configuration, boolean multiple, boolean allowThumbs) {
        super();
        setPropertyEditor(new PropertyEditor<List<GWTJahiaNode>>() {
            public String getStringValue(List<GWTJahiaNode> value) {
                String s = "";
                for (Iterator<GWTJahiaNode> it = value.iterator(); it.hasNext();) {
                    s += it.next().getName();
                    if (it.hasNext()) {
                        s += ", ";
                    }
                }
                return value.toString();
            }

            public List<GWTJahiaNode> convertStringValue(String value) {
                return new ArrayList<GWTJahiaNode>();
            }
        });
        this.rootPath = rootPath;
        this.types = types;
        this.filters = filters;
        this.mimeTypes = mimeTypes;
        this.configuration = configuration;
        this.multiple = multiple;
        this.allowThumbs = allowThumbs;
        setValue(new ArrayList<GWTJahiaNode>());
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce) {
        super.onTriggerClick(ce);
        if (disabled || isReadOnly()) {
            return;
        }
        final Window w = new Window();
        w.setLayout(new FitLayout());
        final ContentPicker contentPicker = new ContentPicker(rootPath, getValue(), types, filters, mimeTypes, configuration, multiple, allowThumbs, "");

        w.setModal(true);
        w.setSize(800, 600);
        w.setResizable(true);
        w.setMaximizable(true);
        w.setBodyBorder(false);

        final ButtonBar bar = new ButtonBar();
        bar.setAlignment(Style.HorizontalAlignment.CENTER);

        final Button ok = new Button(Messages.getResource("fm_save"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                List<GWTJahiaNode> selection = contentPicker.getSelectedNodes();
                setValue(selection);
                w.hide();
            }
        });
        ok.setIconStyle("gwt-icons-save");
        bar.add(ok);

        final Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                w.hide();
            }
        });
        cancel.setIconStyle("gwt-icons-cancel");

        bar.add(cancel);
        w.add(contentPicker);
        w.setBottomComponent(bar);
        w.show();
    }

    @Override
    public List<GWTJahiaNode> getValue() {
        return value;
    }

    @Override
    public void setValue(List<GWTJahiaNode> value) {
        super.setValue(value);
        this.value = value;
    }
}

