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
package org.jahia.ajax.gwt.client.widget.versioning;

import org.jahia.ajax.gwt.client.widget.tripanel.TriPanelBrowserWindow;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.widget.versioning.VersionsBrowserListener;
import org.jahia.ajax.gwt.client.widget.versioning.VersionsBrowser;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 ao�t 2008
 * Time: 17:04:09
 * To change this template use File | Settings | File Templates.
 */
public class PageVersionsBrowser extends TriPanelBrowserWindow implements VersionsBrowser {

    private int pid;
    private VersionsTable versionsTable;
    private Button saveButton;
    private Button cancelButton;
    private List<VersionsBrowserListener> listeners = new ArrayList<VersionsBrowserListener>();

    public PageVersionsBrowser(int pid) {
        super();

        this.pid = pid;
        GWTJahiaPageContext page = new GWTJahiaPageContext();
        page.setPid(JahiaGWTParameters.getPID());
        page.setMode(JahiaGWTParameters.getOperationMode());
        //this.setAutoHeight(true);
        //this.setAutoWidth(true);
        this.setWidth(500);
        this.setHeight(400);
        //setLayout(new FitLayout());
        createUI();
    }

    private void createUI() {

        // table
        versionsTable = new VersionsTable("ContentPage_" + this.pid);

        // important: create ui after creating linker
        versionsTable.createUI();
        versionsTable.getComponent().setWidth("714px");

        // setup widgets in layout
        //initWidgets(filterPanel.getComponent(), tablePanel.getComponent(), messagePanel.getComponent(), processJobTopBar.getComponent(), null);
        initWidgets(null, versionsTable.getComponent(), null, null, null);

        // linker initializations
        versionsTable.initContextMenu();

        saveButton = new Button("Save");
        saveButton.addListener(Events.OnClick, new Listener<ComponentEvent>() {
          public void handleEvent(ComponentEvent ce) {
            onClick(ce);
          }
        });

        cancelButton = new Button("Cancel");
        cancelButton.addListener(Events.OnClick, new Listener<ComponentEvent>() {
          public void handleEvent(ComponentEvent ce) {
            onClick(ce);
          }
        });
        this.addButton(this.saveButton);
        this.addButton(this.cancelButton);
    }

    protected void onClick(ComponentEvent ce){
        Iterator<VersionsBrowserListener> iterator = this.listeners.iterator();
        VersionsBrowserListener listener = null;
        while (iterator.hasNext()){
            listener = iterator.next();
            if (ce.component == this.saveButton){
                listener.onSave(getSelectedVersion());
                this.hide();
            } else if (ce.component == this.cancelButton){
                //
                this.hide();
            }
        }
    }

    protected void onCancelClick(ComponentEvent ce){
        Iterator<VersionsBrowserListener> iterator = this.listeners.iterator();
        VersionsBrowserListener listener = null;
        while (iterator.hasNext()){
            listener = iterator.next();
            listener.onSave(getSelectedVersion());
        }
    }

    public GWTJahiaVersion getSelectedVersion(){
        if (this.versionsTable == null){
            return null;
        }
        return (GWTJahiaVersion)this.versionsTable.getSelection();
    }

    public void addPageVersionsBrowserListener (VersionsBrowserListener listener){
        if (listener == null){
            return;
        }
        this.listeners.add(listener);
    }
}
