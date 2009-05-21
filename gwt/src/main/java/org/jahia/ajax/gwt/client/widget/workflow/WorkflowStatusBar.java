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
package org.jahia.ajax.gwt.client.widget.workflow;

import org.jahia.ajax.gwt.client.widget.tripanel.BottomBar;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowManager;
import org.jahia.ajax.gwt.client.util.WindowUtil;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.Style;

/**
 * Bypassed status bar to have another toolbar at the bottom of the workflow manager.
 *
 * @author rfelden
 * @version 16 juil. 2008 - 16:54:09
 */
 public class WorkflowStatusBar extends BottomBar {

    private ButtonBar m_component ;
    private WorkflowExecuteWindow executeWindow ;

    public WorkflowStatusBar() {
        m_component = new ButtonBar() ;

        Button batch = new Button(WorkflowManager.getResource("wf_showBatch")) ;
        batch.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent e) {
                ((WorkflowToolbar) getLinker().getTopObject()).showBatchReport();
            }
        });
        Button ok = new Button(WorkflowManager.getResource("wf_ok")) ;
        ok.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent e) {
                showExecuteWindow();
            }
        });
        Button cancel = new Button(WorkflowManager.getResource("wf_cancel")) ;
        cancel.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent e) {
                WindowUtil.close();
            }
        });
        batch.setIconStyle("wf-button_info");
        ok.setIconStyle("wf-button_ok");
        cancel.setIconStyle("wf-button_cancel");
        m_component.add(batch) ;
        m_component.add(ok) ;
        m_component.add(cancel) ;
        m_component.setButtonAlign(Style.HorizontalAlignment.CENTER);
    }

    public void showExecuteWindow() {
        if (executeWindow == null) {
            executeWindow = new WorkflowExecuteWindow(getLinker()) ;
        }
        executeWindow.showWindow();
    }

    public Component getComponent() {
        return m_component ;
    }

    public void clear() {
        // ..
    }

    public void setIconStyle(String style) {
        // ..
    }

    public void setMessage(String info) {
        // ..
    }

    public void showBusy() {
        // ..
    }

    public void showBusy(String message) {
        // ..
    }
}
