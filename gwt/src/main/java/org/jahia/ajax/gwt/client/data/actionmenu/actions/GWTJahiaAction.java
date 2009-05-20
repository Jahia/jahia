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
