/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
            box.setProgressHtml(Messages.get("org_jahia_admin_workInProgressProgressText", "Loading..."));

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
