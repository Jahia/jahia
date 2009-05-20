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
import com.google.gwt.user.client.Window;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 14 f�vr. 2008 - 11:14:30
 */
public class GWTJahiaRedirectAction extends GWTJahiaAction implements Serializable {

    private String url ;

    public GWTJahiaRedirectAction() {
        super() ;
    }

    public GWTJahiaRedirectAction(String item, String label, String url) {
        super(item, label) ;
        this.url = url ;
    }

    public void execute() {
        Window.Location.replace(url);
    }

}
