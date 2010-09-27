/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.contentengine;

import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem.SidePanelLinker;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * The edit engine window widget.
 * User: toto
 * Date: Aug 2, 2010
 * Time: 3:55:13 PM
 */
public class EngineWindow extends Window implements EngineContainer {

    public EngineWindow() {
        setSize(750, 480);
        setBodyBorder(false);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setIcon(StandardIconsProvider.STANDARD_ICONS.engineLogoJahia());
        setLayout(new FitLayout());
    }

    public ContentPanel getPanel() {
        return this;
    }

    public void setEngine(AbstractContentEngine engine) {
        add(engine);
		if (!(engine.getLinker() instanceof ManagerLinker) && (GXT.isIE7 || GXT.isIE6)) {
			EditLinker editLinker = engine.getLinker() instanceof EditLinker ? (EditLinker) engine
			        .getLinker() : ((SidePanelLinker) engine.getLinker()).getEditLinker();
			// resize to fit main module area
			MainModule main = editLinker.getMainModule();
			setSize(main.getOffsetWidth(), main.getOffsetHeight());
			setPosition(main.getAbsoluteLeft(), main.getAbsoluteTop());
			setBorders(false);
		}
    }

    public void showEngine() {
        show();
    }

    public void closeEngine() {
        hide();
    }
}
