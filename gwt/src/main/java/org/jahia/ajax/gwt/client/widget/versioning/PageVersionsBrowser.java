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
package org.jahia.ajax.gwt.client.widget.versioning;

import org.jahia.ajax.gwt.client.widget.tripanel.TriPanelBrowserWindow;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;

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

        saveButton = new Button("Save", new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                onClick(buttonEvent);
            }
        });

        cancelButton = new Button("Cancel", new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                onCancelClick();
            }
        });
        this.addButton(this.saveButton);
        this.addButton(this.cancelButton);
    }

    protected void onClick(ButtonEvent ce){
        Iterator<VersionsBrowserListener> iterator = this.listeners.iterator();
        VersionsBrowserListener listener;
        while (iterator.hasNext()){
            listener = iterator.next();
            if (ce.getComponent() == this.saveButton){
                listener.onSave(getSelectedVersion());
                this.hide();
            } else if (ce.getComponent() == this.cancelButton){
                //
                this.hide();
            }
        }
    }

    protected void onCancelClick(){
        Iterator<VersionsBrowserListener> iterator = this.listeners.iterator();
        VersionsBrowserListener listener;
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
