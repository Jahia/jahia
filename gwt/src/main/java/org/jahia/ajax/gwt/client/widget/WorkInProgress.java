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

            box.setMessage(Messages.getResource("workInProgressTitle"));
            box.setProgressText(Messages.getResource("workInProgressProgressText"));

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
