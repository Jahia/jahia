/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.actions;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.jahia.ajax.gwt.commons.client.ui.EngineOpener;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 14 f�vr. 2008 - 10:30:11
 */
public class GWTJahiaEngineAction extends GWTJahiaAction implements IsSerializable {


    private String url ;

    public GWTJahiaEngineAction() {
        super() ;
    }

    /**
     * Build an engine action item using the command item name and an engine url.
     * @param item the action name
     * @param label the action localized label
     * @param url the url to open the engine
     */
    public GWTJahiaEngineAction(String item, String label, String url) {
        super(item, label) ;
        this.url = url ;
    }

    /**
     * Open a new window using the provided URL and the window parameters.
     */
    public void execute() {
        EngineOpener.openEngine(url);
    }

}
