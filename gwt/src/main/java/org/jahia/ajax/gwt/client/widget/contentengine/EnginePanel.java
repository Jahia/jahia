/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
