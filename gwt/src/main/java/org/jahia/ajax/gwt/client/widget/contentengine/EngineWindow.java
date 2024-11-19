/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
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
        addStyleName("engine-window");
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

        if (linker instanceof EditLinker || linker instanceof SidePanelLinker) {
            EditLinker editLinker = linker instanceof EditLinker ? (EditLinker) linker : ((SidePanelLinker) linker).getEditLinker();
            if (GXT.isIE) {
                // resize to fit main module area
                MainModule main = editLinker.getMainModule();
                setSize(main.getOffsetWidth(), main.getOffsetHeight());
                Element element = main.getElement();
                setPosition(WindowUtil.getAbsoluteLeft(element), WindowUtil.getAbsoluteTop(element));
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
