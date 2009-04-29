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

import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.menu.Item;

/**
 * User: rfelden
 * Date: 7 janv. 2009 - 15:22:50
 */
public interface FileActionItemItf {

    public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) ;

    public void setEnabled(boolean enabled) ;

    public ToolItem getTextToolitem() ;

    public Item getMenuItem() ;

    public Item getContextMenuItem() ;

}
