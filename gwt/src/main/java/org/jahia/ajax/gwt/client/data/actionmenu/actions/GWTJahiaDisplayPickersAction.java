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
import java.util.List;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 14 f�vr. 2008 - 11:24:58
 */
public class GWTJahiaDisplayPickersAction extends GWTJahiaAction implements Serializable {

    private List<GWTJahiaRedirectAction> pickersRedirect ;

    public GWTJahiaDisplayPickersAction() {
        super() ;
    }

    public GWTJahiaDisplayPickersAction(String item, String label, List<GWTJahiaRedirectAction> pickersRedirect) {
        super(item, label) ;
        this.pickersRedirect = pickersRedirect ;
    }

    public void execute() {
        // nothing here, needs submenu
    }

    public List<GWTJahiaRedirectAction> getPickers() {
        return pickersRedirect ;
    }
    
}
