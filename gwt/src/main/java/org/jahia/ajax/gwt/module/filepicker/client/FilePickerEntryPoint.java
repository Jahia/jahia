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
package org.jahia.ajax.gwt.module.filepicker.client;

import org.jahia.ajax.gwt.client.util.JahiaGWT;
import org.jahia.ajax.gwt.client.widget.node.FilePicker;
import org.jahia.ajax.gwt.client.widget.node.FilePickerViewport;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
import com.google.gwt.core.client.EntryPoint;

/**
 * File picker to embed in container edition engine or anywhere else you want,
 * provided there is correct initialization.
 *
 * @author rfelden
 *         Date: 27 ao�t 2008
 *         Time: 17:59:59
 */
public class FilePickerEntryPoint implements EntryPoint {
    public void onModuleLoad() {
        JahiaGWT.init();
        RootPanel panel = RootPanel.get("filemanager");
        if (panel != null) {
            String callback = DOM.getElementAttribute(panel.getElement(), "callback") ;
            if (callback == null || callback.length() == 0) {
                panel.add(new FilePicker(
                        DOM.getElementAttribute(panel.getElement(), "rootPath"),
                        DOM.getElementAttribute(panel.getElement(), "startPath"),
                        DOM.getElementAttribute(panel.getElement(), "nodeTypes"),
                        DOM.getElementAttribute(panel.getElement(), "filters"),
                        DOM.getElementAttribute(panel.getElement(), "mimeTypes"),
                        DOM.getElementAttribute(panel.getElement(), "config"),
                        Boolean.parseBoolean(DOM.getElementAttribute(panel.getElement(), "allowThumbs")),
                        callback));
            } else {
                panel.add(new FilePickerViewport(
                        DOM.getElementAttribute(panel.getElement(), "rootPath"),
                        DOM.getElementAttribute(panel.getElement(), "startPath"),
                        DOM.getElementAttribute(panel.getElement(), "nodeTypes"),
                        DOM.getElementAttribute(panel.getElement(), "filters"),
                        DOM.getElementAttribute(panel.getElement(), "mimeTypes"),
                        DOM.getElementAttribute(panel.getElement(), "config"),
                        Boolean.parseBoolean(DOM.getElementAttribute(panel.getElement(), "allowThumbs")),
                        callback));
            }
        }
    }

}
