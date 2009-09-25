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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.service.JahiaServiceAsync;
import org.jahia.ajax.gwt.client.widget.language.LanguageSelectedListener;
import org.jahia.ajax.gwt.client.widget.language.LanguageSwitcher;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 27 nov. 2008
 * Time: 11:35:12
 */
public class LanguageSwitcherActionItem implements ActionItem {


    public Component getTextToolitem() {
        Button item = new Button("loading...") ;
        item.setEnabled(false);
        new LanguageSwitcher(true, true, false,false, JahiaGWTParameters.getLanguage(), false, new LanguageSelectedListener() {
            private final JahiaServiceAsync jahiaServiceAsync = JahiaService.App.getInstance();
            public void onLanguageSelected (String languageSelected) {
                jahiaServiceAsync.getLanguageURL(languageSelected,new AsyncCallback<String>() {
                    public void onFailure (Throwable throwable) {}

                    public void onSuccess (String s) {
                        com.google.gwt.user.client.Window.Location.assign(s);
                    }
                });
            }
        }).init(item);
        return item ;
    }

    public Item createMenuItem () {
        return null;
    }

    public void setEnabled(boolean enabled) {

    }

    public Item getMenuItem() {
        return null;
    }

    public Item getContextMenuItem() {
        return null;
    }

    public GWTJahiaToolbarItem getGwtToolbarItem() {
        return null;
    }

    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
    }
}
