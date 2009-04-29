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
package org.jahia.ajax.gwt.client.util.nodes.actions;

import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Item;

/**
 * User: rfelden
 * Date: 7 janv. 2009 - 15:24:18
 */
public class FileActionItemSeparator implements FileActionItemItf {

    public void setEnabled(boolean enabled) {}

    public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {}

    public ToolItem getTextToolitem() {
        return new SeparatorToolItem() ;
    }

    public Item getMenuItem() {
        return new SeparatorMenuItem() ;
    }

    public Item getContextMenuItem() {
        return new SeparatorMenuItem() ;
    }
}
