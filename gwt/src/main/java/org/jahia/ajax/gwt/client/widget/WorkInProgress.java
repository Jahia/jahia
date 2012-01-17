/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget;

import org.jahia.ajax.gwt.client.messages.Messages;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.MessageBox.MessageBoxType;

/**
 * UI component for displaying "loading..." control using modal panel and a
 * progress bar.
 * 
 * @author Sergiy Shyrkov
 */
public class WorkInProgress {

    private static WorkInProgress instance;
    private MessageBox messageBox;
    
    /**
     * Initializes an instance of this class.
     * @param messageBox message box
     */
    private WorkInProgress(MessageBox messageBox) {
        super();
        this.messageBox = messageBox;
        exposeMethods();
    }

    private static WorkInProgress getInstance() {
        if (instance == null) {
            MessageBox box = new MessageBox();
            box.setType(MessageBoxType.WAIT);

            box.setMessage(Messages.get("org_jahia_admin_workInProgressTitle", "Work in progress, please wait..."));
            box.setProgressText(Messages.get("org_jahia_admin_workInProgressProgressText", "Loading..."));

            box.setButtons("");
            box.setClosable(false);
            box.getDialog().setHeaderVisible(false);
            box.getDialog().setOnEsc(false);
            box.getDialog().setDraggable(false);
            instance = new WorkInProgress(box);
        }

        return instance;
    }

    public static void hide() {
        if (instance != null) {
            getInstance().messageBox.close();
        }
    }

    private native void exposeMethods() /*-{
        $wnd.workInProgressOverlay = new Object();
        $wnd.workInProgressOverlay.start = $wnd.workInProgressOverlay.launch = @org.jahia.ajax.gwt.client.widget.WorkInProgress::show();
        $wnd.workInProgressOverlay.stop = @org.jahia.ajax.gwt.client.widget.WorkInProgress::hide();
    }-*/;

    private static native boolean needToStartImmediatelly() /*-{
        return ($wnd.jahia!=undefined  && $wnd.jahia.config!=undefined  && $wnd.jahia.config.startWorkInProgressOnLoad!=undefined && $wnd.jahia.config.startWorkInProgressOnLoad);
    }-*/;

    public static void init() {
        getInstance();
        if (needToStartImmediatelly()) {
            show();
        }
    }

    public static void show() {
        getInstance().messageBox.show();
    }
}
