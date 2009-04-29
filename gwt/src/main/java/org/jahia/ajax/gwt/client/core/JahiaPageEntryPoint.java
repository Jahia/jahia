/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.core;

import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.util.templates.TemplatesDOMUtil;
import org.jahia.ajax.gwt.client.widget.toolbar.ToolbarManager;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Element;


/**
 * Created by Jahia.
 * User: ktlili
 * Date: 11 juil. 2007
 * Time: 09:57:56
 */
public abstract class JahiaPageEntryPoint implements EntryPoint {
    private JahiaModuleProvider customJahiaModuleProvider;
    private static DockPanel dockPanel = new DockPanel();
    private static ToolbarManager toolbarManager;

    public static JahiaPageEntryPoint instance;

    public void onModuleLoad() {
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


    public static ToolbarManager getToolbarManager() {
        return toolbarManager;
    }

    public static void setToolbarManager(ToolbarManager toolbarManager) {
        JahiaPageEntryPoint.toolbarManager = toolbarManager;
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

    public static DockPanel getDockPanel() {
        return dockPanel;
    }

    //target area: center
    public static void addCenter(Widget w) {
        dockPanel.add(w, DockPanel.CENTER);
    }

    // create draggable, target area: north
    public static void addNorth(Widget w) {
        RootPanel.get().add(w, 0, 0);
        /*dockPanel.add(w, DockPanel.NORTH);
        dockPanel.setCellVerticalAlignment(w, VerticalPanel.ALIGN_TOP);*/
    }

    //target area: east
    public static void addEast(Widget w) {
        dockPanel.add(w, DockPanel.EAST);
        dockPanel.setCellHorizontalAlignment(w, HorizontalPanel.ALIGN_RIGHT);

    }

    private native void initJavaScriptApi() /*-{
        // define a static JS function with a friendly name
        $wnd.loadRSS = function (elementId, url, numberPage) {@org.jahia.ajax.gwt.client.module.RSSJahiaModule::loadRSS(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(elementId, url, numberPage); };
    }-*/;


}
