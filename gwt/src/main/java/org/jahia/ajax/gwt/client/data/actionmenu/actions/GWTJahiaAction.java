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
package org.jahia.ajax.gwt.client.data.actionmenu.actions;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 22 janv. 2008 - 14:49:03
 */
public abstract class GWTJahiaAction implements Serializable {

    public static final String ADD = "add" ;
    public static final String UPDATE = "update" ;
    public static final String PASTE = "paste" ;
    public static final String PASTE_REF = "pasteref" ;
    public static final String COPY = "copy" ;
    public static final String RESTORE = "restore" ;
    public static final String DELETE = "delete" ;
    public static final String PICKED = "picked" ;
    public static final String PICKER = "picker" ;
    public static final String PICKER_LIST = "pickerlist" ;
    public static final String SOURCE = "source" ;

    public static final String CLIPBOARD_CONTENT = "clipboard_content" ;

    protected String item ;
    protected String label ;
    protected boolean locked = false ;

    public GWTJahiaAction() {
        item = "" ;
        label = "" ;
    }

    /**
     * Construct an action with an item name and a target url to open an engine.
     *
     * @param item the item name
     * @param label the item label
     */
    public GWTJahiaAction(String item, String label) {
        this.item = item ;
        this.label = label ;
    }

    /**
     * This is the method to override to trigger the action
     */
    public abstract void execute() ;

    public String getLabel() {
        return label;
    }

    public String getItem() {
        return item ;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
