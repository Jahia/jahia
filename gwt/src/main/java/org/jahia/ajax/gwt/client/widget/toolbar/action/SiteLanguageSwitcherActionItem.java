/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.Arrays;
import java.util.Collections;

/**
 * Language switcher toolbar item for languages of the current site.
 * 
 * @author Sergiy Shyrkov
 */
public class SiteLanguageSwitcherActionItem extends LanguageSwitcherActionItem {
	private static final long serialVersionUID = 7137296084440099716L;
	private String siteKey;

	@Override
	public void handleNewLinkerSelection() {
		super.handleNewLinkerSelection();
        final GWTJahiaNode node = linker.getSelectionContext().getMainNode();
        if (node != null
		        && !node.getSiteUUID().equalsIgnoreCase(siteKey)) {
			siteKey = node.getSiteUUID();
            events = false;
            mainComponent.getStore().removeAll();
            mainComponent.reset();
            mainComponent.getStore().add(JahiaGWTParameters.getSiteLanguages());
            mainComponent.getListView().getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
            if (mainComponent.getSelection().isEmpty() || !JahiaGWTParameters.getLanguage().equals(mainComponent.getSelection().get(0).getLanguage())) {
                for (GWTJahiaLanguage language : JahiaGWTParameters.getSiteLanguages()) {
                    if (language.getLanguage().equals(JahiaGWTParameters.getLanguage())) {
                        mainComponent.setSelection(Arrays.asList(language));
                    }
                }
            }
            events = true;
        }

	}

	@Override
	public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
		siteKey = JahiaGWTParameters.getSiteUUID();
		super.init(gwtToolbarItem, linker);
	}
}