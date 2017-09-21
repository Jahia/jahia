/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.util.WindowUtil;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem.SidePanelLinker;

/**
 * The edit engine window widget.
 * User: toto
 * Date: Aug 2, 2010
 * Time: 3:55:13 PM
 */
public class EngineWindow extends Window implements EngineContainer {

    public EngineWindow() {
        setId("JahiaGxtEngineWindow");
        setSize(750, 480);
        setBodyBorder(false);
        setClosable(false);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setLayout(new FitLayout());
    }

    public ContentPanel getPanel() {
        return this;
    }

    public void setEngine(Component component, String header, ButtonBar buttonsBar, GWTJahiaLanguage language, final Linker linker) {
        removeAll();
        add(component);
        setHeadingHtml(header);
        if (buttonsBar != null) {
            setBottomComponent(buttonsBar);
        }

        if (!(linker instanceof ManagerLinker)) {
            EditLinker editLinker = linker instanceof EditLinker ? (EditLinker) linker : ((SidePanelLinker) linker).getEditLinker();
            if (GXT.isIE) {
                // resize to fit main module area
                MainModule main = editLinker.getMainModule();
                setSize(main.getOffsetWidth(), main.getOffsetHeight());
                setPosition(WindowUtil.getAbsoluteLeft(main.getElement()), WindowUtil.getAbsoluteTop(main.getElement()));
                setBorders(false);
            } else if (editLinker.getMainAreaComponent() != null) {
                setContainer(editLinker.getMainAreaComponent().getElement());
            }
        }
    }

    public void showEngine() {
        show();
        if (!GXT.isIE) {
            maximize();
        }
    }

    public void closeEngine() {
        hide();
    }

    protected void doFocus() {
        // windows hack
    }
}
