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
package org.jahia.ajax.gwt.client.widget.node;

import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import org.jahia.ajax.gwt.client.widget.node.FilePicker;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 1, 2008
 * Time: 6:37:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilePickerField extends TriggerField<String> {
    private String rootPath;
    private String types;
    private String filters;
    private String mimeTypes;
    private String configuration ;
    private boolean allowThumbs;

    public FilePickerField(String rootPath, String types, String filters, String mimeTypes, String config, boolean allowThumbs) {
        this.rootPath = rootPath;
        this.types = types;
        this.filters = filters;
        this.mimeTypes = mimeTypes;
        this.configuration = config ;
        this.allowThumbs = allowThumbs;
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce) {
        super.onTriggerClick(ce);
        if (disabled || isReadOnly()) {
            return;
        }
        final Window w = new Window();
        w.setLayout(new FitLayout());
        final FilePicker filePicker = new FilePicker(rootPath, getValue()!=null?getValue():"", types, filters, mimeTypes, configuration, allowThumbs, "");

        w.setModal(true);
        w.setSize(600, 400);
        ButtonBar bar = new ButtonBar();
        Button ok = new Button("OK", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                List<GWTJahiaNode> selection = (List<GWTJahiaNode>) filePicker.getLinker().getTableSelection();
                if (selection != null && selection.size() > 0) {
                    StringBuilder conCat = new StringBuilder(selection.get(0).getPath());
                    for (int i = 1; i < selection.size(); i++) {
                        conCat.append(", ").append(selection.get(i).getPath());
                    }
                    setRawValue(conCat.toString());
                }
                w.hide();
            }
        });
        bar.add(ok);
        w.setButtonBar(bar);
        w.add(filePicker);
        w.show();
    }


}
