/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
