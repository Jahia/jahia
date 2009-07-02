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
package org.jahia.ajax.gwt.client.widget.content;

import org.jahia.ajax.gwt.client.util.content.actions.FileActions;
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
