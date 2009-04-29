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
package org.jahia.ajax.gwt.client.util.layoutmanager;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 4 d�c. 2007
 * Time: 12:11:19
 * To change this template use File | Settings | File Templates.
 */
public interface LayoutManagerImageBundle extends ImageBundle {


    /**
     * Would match the file 'open_quickbar_icon.png', 'open_box_icon.gif', or
     * 'open_box_icon.png' located in the same package as this type.
     */
    public AbstractImagePrototype openQuickbarIcon();

    /**
     * Would match the file 'close_box_icon.png', 'open_box_icon.gif', or
     * 'open_box_icon.png' located in the same package as this type.
     */
    public AbstractImagePrototype closeBoxIcon();

    /**
     * Would match the file 'close_quickbar_icon.png', 'open_box_icon.gif', or
     * 'open_box_icon.png' located in the same package as this type.
     */
    public AbstractImagePrototype closeQuickbarIcon();

    /**
     * Would match the file 'miximize_box_icon.png', 'open_box_icon.gif', or
     * 'open_box_icon.png' located in the same package as this type.
     */
    public AbstractImagePrototype minimizeBoxIcon();

     /**
     * Would match the file 'maximize_box_icon.png', 'open_box_icon.gif', or
     * 'open_box_icon.png' located in the same package as this type.
     */
    public AbstractImagePrototype maximizeBoxIcon();

}
