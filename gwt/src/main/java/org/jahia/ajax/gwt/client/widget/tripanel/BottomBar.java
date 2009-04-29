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
package org.jahia.ajax.gwt.client.widget.tripanel;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 23 juin 2008 - 11:24:12
 */
public abstract class BottomBar extends LinkableComponent {

    public abstract void clear() ;

    public abstract void setIconStyle(String style) ;

    public abstract void setMessage(String info) ;

    public abstract void showBusy() ;

    public abstract void showBusy(String message) ;

}
