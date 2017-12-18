/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.ModalPanel;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem.SidePanelLinker;

/**
 * Represents the edit engine panel.
 * User: toto
 * Date: Aug 2, 2010
 * Time: 4:04:33 PM
 */
public class EnginePanel extends ContentPanel implements EngineContainer {

    private EditLinker linker;
    private ModalPanel modalPanel;

    public EnginePanel() {
        setBodyBorder(false);
        setLayout(new FitLayout());
        setId("JahiaGxtEnginePanel");
        addStyleName("engine-panel");
    }

    public ContentPanel getPanel() {
        return this;
    }

    public void setEngine(Component component, String header, ButtonBar buttonsBar, GWTJahiaLanguage language, final Linker linker) {
        this.linker = linker instanceof EditLinker ? (EditLinker) linker : ((SidePanelLinker) linker).getEditLinker();
        String name = component.getClass().getName();
        name = name.substring(name.lastIndexOf('.')+1).toLowerCase();
        addStyleName(name+ "-ctn");
        removeAll();
        add(component);
        head.setStyleAttribute("height", "20px");
        setHeadingHtml(header);
        if (buttonsBar != null) {
            setBottomComponent(buttonsBar);
        }
    }

    public void showEngine() {
        linker.replaceMainAreaComponent(this);
    }

    public void closeEngine() {
        ModalPanel.push(modalPanel);
        modalPanel = null;

        linker.restoreMainArea();

        fireEvent(Events.Close);
    }

    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);

        el().updateZIndex(0);
        modalPanel = ModalPanel.pop();
        modalPanel.setBlink(false);
        modalPanel.show(this);
    }

}
