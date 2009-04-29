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

import org.jahia.ajax.gwt.client.util.nodes.actions.FileActions;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

/**
 * Folder tree view used in the file path selector component.
 * 
 * @author Sergiy Shyrkov
 */
public class FolderListContextMenu extends Menu {

    public FolderListContextMenu(final BrowserLinker linker) {
        super();

        add(new MenuItem("Create folder", "newfolder",
                new SelectionListener<ComponentEvent>() {
                    public void componentSelected(ComponentEvent ce) {
                        FileActions.createFolder(linker);
                    }
                }));

        add(new MenuItem("Rename", "rename",
                new SelectionListener<ComponentEvent>() {
                    public void componentSelected(ComponentEvent ce) {
                        FileActions.rename(linker);
                    }
                }));

        add(new MenuItem("Copy", "copy",
                new SelectionListener<ComponentEvent>() {
                    public void componentSelected(ComponentEvent ce) {
                        FileActions.copy(linker);
                    }
                }));

        add(new MenuItem("Cut", "cut", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent ce) {
                FileActions.cut(linker);
            }
        }));

        add(new MenuItem("Paste", "paste",
                new SelectionListener<ComponentEvent>() {
                    public void componentSelected(ComponentEvent ce) {
                        FileActions.paste(linker);
                    }
                }));

        if (GXT.isIE) {
            add(new MenuItem("Open web folder", "webfolder",
                    new SelectionListener<ComponentEvent>() {
                        public void componentSelected(ComponentEvent ce) {
                            FileActions.openWebFolder(linker);
                        }
                    }));
        }

        add(new MenuItem("Remove", "remove",
                new SelectionListener<ComponentEvent>() {
                    public void componentSelected(ComponentEvent ce) {
                        FileActions.remove(linker);
                    }
                }));

        add(new MenuItem("Lock", "lock",
                new SelectionListener<ComponentEvent>() {
                    public void componentSelected(ComponentEvent ce) {
                        FileActions.lock(true, linker);
                    }
                }));

        add(new MenuItem("Unlock", "unlock",
                new SelectionListener<ComponentEvent>() {
                    public void componentSelected(ComponentEvent ce) {
                        FileActions.lock(false, linker);
                    }
                }));
    }

}
