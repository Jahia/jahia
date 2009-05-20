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
package org.jahia.ajax.gwt.client.data.actionmenu.actions;

import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.module.ActionMenuJahiaModule;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 14 f�vr. 2008 - 10:37:52
 */
public class GWTJahiaClipboardAction extends GWTJahiaAction implements Serializable {

    private String objectKey ;

    public GWTJahiaClipboardAction() {
        super() ;
    }

    public GWTJahiaClipboardAction(String item, String label, String objectKey) {
        super(item, label) ;
        this.objectKey = objectKey ;
    }

    public void execute() {
        if (item.equals(COPY)) {
            ActionMenuJahiaModule.getClipboardTool().copy(objectKey);
        } else if (item.equals(PASTE)) {
            ActionMenuJahiaModule.getClipboardTool().paste(objectKey);
        } else if (item.equals(PASTE_REF)) {
            ActionMenuJahiaModule.getClipboardTool().pasteReference(objectKey);
        } else {
            Window.alert("Unknown action " + item) ;
        }
    }

}
