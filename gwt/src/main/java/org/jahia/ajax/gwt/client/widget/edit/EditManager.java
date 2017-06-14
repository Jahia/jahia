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
package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanel;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbarLayoutContainer;

/**
 * Edit mode manager widget.
 * @author toto
 */
public class EditManager extends ContentPanel {

    private MainModule mainModule;
    private SidePanel sidePanel;
    private ActionToolbarLayoutContainer toolbar;
    private EditLinker editLinker;
    private BorderLayout borderLayout ;

    public EditManager(String path, String template, String nodeTypes, String locale, final GWTEditConfiguration config) {
        long start = System.currentTimeMillis();

        JahiaGWTParameters.setSiteNode(config.getSiteNode());
        JahiaGWTParameters.setSitesMap(config.getSitesMap());
        JahiaGWTParameters.setChannels(config.getChannels());

        borderLayout =  new BorderLayout();
        setLayout(borderLayout);
        setHeaderVisible(false);

        sidePanel = new SidePanel(config);

        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.WEST, 300);
        data.setCollapsible(true);
        data.setSplit(true);
        data.setFloatable(true);

        sidePanel.setStyleAttribute("z-index", "999");
        sidePanel.addStyleName("gwt-only-panel");
        sidePanel.addStyleName("window-side-panel");
        add(sidePanel, data);

        sidePanel.setVisible(!config.getTabs().isEmpty());

        toolbar =  new ActionToolbarLayoutContainer(config.getTopToolbar());
        toolbar.setStyleAttribute("z-index", "999");
        toolbar.setStyleAttribute("position", "relative");
        toolbar.addStyleName("gwt-only-panel");
        setTopComponent(toolbar);

        setScrollMode(Style.Scroll.NONE);
        mainModule = new MainModule(path, template, nodeTypes, config);
        if (mainModule.getHeader() != null) {
            mainModule.getHeader().addStyleName("gwt-only-panel");
        }
        add(mainModule, new BorderLayoutData(Style.LayoutRegion.CENTER));

        editLinker = new EditLinker(mainModule, sidePanel, toolbar, config);
        GWTJahiaLanguage lang = new GWTJahiaLanguage();
        lang.setLanguage(JahiaGWTParameters.getLanguage());
        lang.setDisplayName(JahiaGWTParameters.getLanguageDisplayName());
        editLinker.setLocale(lang);

        addStyleName("app-container");
        Log.debug("Edit manager initiated in " + (System.currentTimeMillis() - start) + " ms");
    }


    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        if (this.toolbar != null) {
            this.toolbar.addStyleName("action-bar-container");
        }
        if (this.body != null) {
            this.body.addStyleName("window-container");
        }
    }

    public MainModule getMainModule() {
        return mainModule;
    }

    public SidePanel getSidePanel() {
        return sidePanel;
    }

    public ActionToolbarLayoutContainer getToolbar() {
        return toolbar;
    }

    public EditLinker getEditLinker() {
        return editLinker;
    }

    


}
