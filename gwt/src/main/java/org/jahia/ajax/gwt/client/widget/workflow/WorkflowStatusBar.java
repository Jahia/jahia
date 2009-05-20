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
