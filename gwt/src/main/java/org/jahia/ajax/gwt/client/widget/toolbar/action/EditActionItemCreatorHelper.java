package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.Module;
import org.jahia.ajax.gwt.client.widget.toolbar.handler.ModuleSelectionHandler; /**
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
 **/

/**
 * User: ktlili
 * Date: Sep 15, 2009
 * Time: 4:56:15 PM
 */
public class EditActionItemCreatorHelper {
    /**
     * Create item "create content"
     *
     * @param gwtToolbarItem
     * @param linker
     * @return
     */
    public static ActionItemItf createEditCreateActionItem(final GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        ActionItemItf actionItem = new CreatePageActionItem(gwtToolbarItem, linker);
        return actionItem;
    }

    /**
     * Create item "publish content"
     *
     * @param gwtToolbarItem
     * @param linker
     * @return
     */
    public static ActionItemItf createEditPublishActionItem(final GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        ActionItemItf actionItem = new PublishActionItem(gwtToolbarItem, linker);
        return actionItem;
    }

    /**
     * Create item "unpublish content"
     *
     * @param gwtToolbarItem
     * @param linker
     * @return
     */
    public static ActionItemItf createEditUnpublishActionItem(final GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        ActionItemItf actionItem = new UnpublishActionItem(gwtToolbarItem, linker);
        return actionItem;
    }

    /**
     * Create item "view publish status"
     *
     * @param gwtToolbarItem
     * @param linker
     * @return
     */
    public static ActionItemItf createEditViewPublishStatusActionItem(final GWTJahiaToolbarItem gwtToolbarItem, final EditLinker linker) {
        ActionItemItf actionItem = new ViewPublishStatusActionItem(gwtToolbarItem, linker);
        return actionItem;
    }

    /**
     * Create item "lock content"
     *
     * @param gwtToolbarItem
     * @param linker
     * @return
     */
    public static ActionItemItf createEditLockActionItem(final GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        ActionItemItf actionItem = new LockActionItem(gwtToolbarItem, linker);
        return actionItem;
    }

    /**
     * Create item "unlock item"
     *
     * @param gwtToolbarItem
     * @param linker
     * @return
     */
    public static ActionItemItf createEditUnlockActionItem(final GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        ActionItemItf actionItem = new UnlockActionItem(gwtToolbarItem, linker);
        return actionItem;
    }

    /**
     * Create item "edit content"
     *
     * @param gwtToolbarItem
     * @param linker
     * @return
     */
    public static ActionItemItf createEditEditActionItem(final GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        ActionItemItf actionItem = new EditContentActionItem(gwtToolbarItem, linker);
        return actionItem;
    }

    /**
     * Create item "delete content"
     *
     * @param gwtToolbarItem
     * @param linker
     * @return
     */
    public static ActionItemItf createEditDeleteActionItem(final GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        ActionItemItf actionItem = new DeleteActionItem(gwtToolbarItem, linker);
        return actionItem;
    }

    private static class CreatePageActionItem extends BaseActionItem {
        private final Linker linker;

        public CreatePageActionItem(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
            super(gwtToolbarItem);
            this.linker = linker;
        }

        public void onSelection() {
            EditActions.createPage(linker);
        }
    }

    private static class PublishActionItem extends BaseActionItem implements ModuleSelectionHandler {
        private final Linker linker;

        public PublishActionItem(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
            super(gwtToolbarItem);
            this.linker = linker;
            setEnabled(false);
        }

        public void onSelection() {
            EditActions.publish(linker);
        }

        public void handleNewModuleSelection(Module selectedModule) {
            if (selectedModule != null) {
                GWTJahiaPublicationInfo info = selectedModule.getNode().getPublicationInfo();
                setEnabled(info.isCanPublish() && (info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED || info.getStatus() == GWTJahiaPublicationInfo.MODIFIED));
            }
        }
    }

    private static class UnpublishActionItem extends BaseActionItem implements ModuleSelectionHandler {
        private final Linker linker;

        public UnpublishActionItem(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
            super(gwtToolbarItem);
            this.linker = linker;
            setEnabled(false);
        }

        public void onSelection() {
            EditActions.unpublish(linker);
        }

        public void handleNewModuleSelection(Module selectedModule) {
            if (selectedModule != null) {
                GWTJahiaPublicationInfo info = selectedModule.getNode().getPublicationInfo();
                setEnabled(info.isCanPublish() && (info.getStatus() == GWTJahiaPublicationInfo.PUBLISHED));
            }
        }
    }

    private static class ViewPublishStatusActionItem extends BaseActionItem implements ModuleSelectionHandler {
        private final EditLinker linker;

        public ViewPublishStatusActionItem(GWTJahiaToolbarItem gwtToolbarItem, EditLinker linker) {
            super(gwtToolbarItem);
            this.linker = linker;
        }

        public void onSelection() {
            EditActions.viewPublishedStatus(linker);
        }

        public void handleNewModuleSelection(Module selectedModule) {
        }
    }

    private static class LockActionItem extends BaseActionItem implements ModuleSelectionHandler {
        private final Linker linker;

        public LockActionItem(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
            super(gwtToolbarItem);
            this.linker = linker;
            setEnabled(false);
        }

        public void onSelection() {
            EditActions.switchLock(linker);
        }

        public void handleNewModuleSelection(Module selectedModule) {
            if (selectedModule != null) {
                setEnabled(selectedModule.getNode().isLockable() && !selectedModule.getNode().isLocked());
            }
        }
    }

    private static class UnlockActionItem extends BaseActionItem implements ModuleSelectionHandler {
        private final Linker linker;

        public UnlockActionItem(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
            super(gwtToolbarItem);
            this.linker = linker;
            setEnabled(false);
        }

        public void onSelection() {
            EditActions.switchLock(linker);
        }

        public void handleNewModuleSelection(Module selectedModule) {
            if (selectedModule != null) {
                setEnabled(selectedModule.getNode().isLockable() && selectedModule.getNode().isLocked());
            }
        }
    }

    private static class EditContentActionItem extends BaseActionItem implements ModuleSelectionHandler {
        private final Linker linker;

        public EditContentActionItem(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
            super(gwtToolbarItem);
            this.linker = linker;
            setEnabled(false);
        }

        public void onSelection() {
            EditActions.edit(linker);
        }

        public void handleNewModuleSelection(Module selectedModule) {
            if (selectedModule != null) {
                setEnabled(selectedModule.getNode().isWriteable());
            }
        }
    }

    private static class DeleteActionItem extends BaseActionItem implements ModuleSelectionHandler {
        private final Linker linker;

        public DeleteActionItem(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
            super(gwtToolbarItem);
            this.linker = linker;
            setEnabled(false);
        }

        public void onSelection() {
            EditActions.delete(linker);
        }

        public void handleNewModuleSelection(Module selectedModule) {
            if (selectedModule != null) {
                setEnabled(selectedModule.getNode().isWriteable());
            }
        }
    }
}
