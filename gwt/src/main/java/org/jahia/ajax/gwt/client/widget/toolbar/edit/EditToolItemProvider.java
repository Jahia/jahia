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
package org.jahia.ajax.gwt.client.widget.toolbar.edit;

import org.jahia.ajax.gwt.client.widget.toolbar.provider.AbstractJahiaToolItemProvider;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.allen_sauer.gwt.log.client.Log;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Sep 7, 2009
 * Time: 2:26:34 PM
 */
public class EditToolItemProvider extends AbstractJahiaToolItemProvider {
    public static final String ACTION = "action";
    private String ACTION_CREATE_PAGE = "createPage";
    private String ACTION_PUBLISH = "publish";
    private String ACTION_UNPUBLISH = "unpublish";
    private String ACTION_VIEWPUBLISHSTATUS = "viewPublishStatus";
    private String ACTION_LOCK = "lock";
    private String ACTION_UNLOCK = "unlock";
    private String ACTION_EDIT = "edit";
    private String ACTION_DELETE = "delete";
    private String ACTION_STATUS = "status";
    private EditLinker editLinker;


    public EditToolItemProvider(EditLinker editLinker) {
        this.editLinker = editLinker;
    }

    public <T extends ComponentEvent> SelectionListener<T> getSelectListener(final GWTJahiaToolbarItem gwtToolbarItem) {
        final SelectionListener<T> listener = new SelectionListener<T>() {
            public void componentSelected(T t) {
                String action = getPropertyValue(gwtToolbarItem, ACTION);
                Log.debug("Edit action: " + action);
                if (action != null) {
                    // create page
                    if (action.equalsIgnoreCase(ACTION_CREATE_PAGE)) {
                        EditActions.createPage(editLinker);
                    }
                    // publish
                    else if (action.equalsIgnoreCase(ACTION_PUBLISH)) {
                        EditActions.publish(editLinker);
                    }
                    // unpublish
                    else if (action.equalsIgnoreCase(ACTION_UNPUBLISH)) {
                        EditActions.unpublish(editLinker);
                    }
                    // view publish status
                    else if (action.equalsIgnoreCase(ACTION_VIEWPUBLISHSTATUS)) {
                        EditActions.viewPublishedStatus(editLinker);
                    }
                    // lock
                    else if (action.equalsIgnoreCase(ACTION_LOCK)) {
                        EditActions.switchLock(editLinker);
                    }
                    // unlock
                    else if (action.equalsIgnoreCase(ACTION_UNLOCK)) {
                        EditActions.switchLock(editLinker);
                    }
                    // edit
                    else if (action.equalsIgnoreCase(ACTION_EDIT)) {
                        EditActions.edit(editLinker);
                    }
                    // delete
                    else if (action.equalsIgnoreCase(ACTION_DELETE)) {
                        EditActions.delete(editLinker);
                    }
                }
            }
        };
        return listener;
    }

    /**
     * Create a new ToolItem
     *
     * @param gwtToolbarItem
     * @return
     */
    public Component createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        String action = getPropertyValue(gwtToolbarItem, ACTION);
        if (action != null) {
            // create page
            if (action.equalsIgnoreCase(ACTION_STATUS)) {
                Status status = new Status();
                status.setText("Not writing");
                status.setWidth(150);
                status.setBox(true);
                status.setBusy("Loading...");
                return new Status();
            }
        }
        return new Button();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
