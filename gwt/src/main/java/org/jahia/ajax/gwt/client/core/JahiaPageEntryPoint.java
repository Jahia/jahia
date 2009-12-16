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
package org.jahia.ajax.gwt.client.core;

import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.util.templates.TemplatesDOMUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.Element;


/**
 * Created by Jahia.
 * User: ktlili
 * Date: 11 juil. 2007
 * Time: 09:57:56
 */
public abstract class JahiaPageEntryPoint extends CommonEntryPoint {
    private JahiaModuleProvider customJahiaModuleProvider;
    private static DockPanel dockPanel = new DockPanel();

    public static JahiaPageEntryPoint instance;

    public void onModuleLoad() {
        super.onModuleLoad();

        // init javascript api
        JavaScriptApi.init();

        initJavaScriptApi();

        loadJahiaModules(RootPanel.getBodyElement());

        instance = this;
    }

    public void loadJahiaModules(Element element) {
        // load jahia config
        GWTJahiaPageContext page = getJahiaGWTPage();

        // get all jahia root panels
        Map<String, List<RootPanel>> jahiaTypedRootPanels = getRootPanels(element);

        // load each jahia type
        for (String jahiaType : jahiaTypedRootPanels.keySet()) {
            try {
                List<RootPanel> rootPanelsForType = jahiaTypedRootPanels.get(jahiaType);
                JahiaModule jahiaModule = getJahiaModuleManagerByType(jahiaType);
                if (jahiaModule != null) {
                    jahiaModule.onModuleLoad(page, rootPanelsForType);
                } else {
                    // TODO handle this better
                    // Window.alert("Jahia Type '" + jahiaType + "' unknown.\nPlease register it first in 'org.jahia.ajax.gwt.global.client.EditModeEntryPoint'\n(getCustomJahiaModuleManagerByType)");
                }
            } catch (Exception e) {
                GWT.log("Unable to load jahia module [" + jahiaType + "].", e);
            }
        }
    }

    /**
     * This method returns a JahiaModule instance depending on the jahiaType.
     * First of all, it's in the Custom JahiaModuleProvider than,if not found,  in the OperationModeJahiaModuleProvider.
     *
     * @param jahiaType the jahia type
     * @return a new instance of the corresponding JahiaModule
     */

    public JahiaModule getJahiaModuleManagerByType(String jahiaType) {
        if (getCustomJahiaModuleProvider() != null) {
            JahiaModule jahiaModule = customJahiaModuleProvider.getJahiaModuleByJahiaType(jahiaType);
            if (jahiaModule != null) {
                return jahiaModule;
            }
        }
        JahiaModuleProvider operationModeJahiaModuleProvider = getOperationModeJahiaModuleProvider();
        if (operationModeJahiaModuleProvider != null) {
            return operationModeJahiaModuleProvider.getJahiaModuleByJahiaType(jahiaType);
        }
        return null;
    }


    /**
     * This method returns an operationMode JahiaModuleProvider.
     *
     * @return an OperationModeJahiaModuleProvider
     */
    public abstract JahiaModuleProvider getOperationModeJahiaModuleProvider();


    /**
     * Retrieve GWTJahiaPage object
     *
     * @return a new instance of a GWTJahiaPage.
     */
    public static GWTJahiaPageContext getJahiaGWTPage() {
        // init panel
        GWTJahiaPageContext page = new GWTJahiaPageContext(URL.getRelativeURL());
        page.setPid(JahiaGWTParameters.getPID());
        page.setMode(JahiaGWTParameters.getOperationMode());
        return page;
    }

    /**
     * If in the module.gwt.xml, there is:
     * <p>
     * <replace-with class="org.jahia.ajax.gwt.base.client.example.ExampleCustomJahiaModuleProvider">
     * <when-type-is class="org.jahia.ajax.gwt.base.client.module.provider.JahiaModuleProvider"/>
     * <when-property-is name="customJahiaProvider" value="custom"/>
     * </replace-with>
     * </p>
     * it returns an instance of org.jahia.ajax.gwt.base.client.example.ExampleCustomJahiaModuleProvider
     *
     * @return a unqiue instance of a "custom" JahiaModulePrider defined in the module.gwt.xml descriptor.
     */
    private JahiaModuleProvider getCustomJahiaModuleProvider() {
        if (customJahiaModuleProvider == null) {
            // customJahiaModuleProvider = (JahiaModuleProvider) GWT.create(JahiaModuleProvider.class);
        }
        return customJahiaModuleProvider;
    }


    /**
     * Retrieve the jahia typed root panel(s)
     *
     * @return a map of RootPanels<String (jahiaType), ArrayList(RootPanel)>
     * @param element
     */
    private Map<String, List<RootPanel>> getRootPanels(Element element) {
        return TemplatesDOMUtil.getAllJahiaTypedRootPanels(element);
    }


    private native void initJavaScriptApi() /*-{
        // define a static JS function with a friendly name
        $wnd.loadRSS = function (elementId, url, numberPage) {@org.jahia.ajax.gwt.client.module.RSSJahiaModule::loadRSS(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(elementId, url, numberPage); };
    }-*/;


}
