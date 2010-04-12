package org.jahia.ajax.gwt.module.edit.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.widget.edit.EditPanelViewport;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 5:53:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditEntryPoint extends CommonEntryPoint {
    public void afterPermissionsLoad() {
        super.afterPermissionsLoad();
        RootPanel panel = RootPanel.get("editmode");
        if (panel != null) {
            GWTEditConfiguration config = new GWTEditConfiguration();

            config.setName(DOM.getElementAttribute(panel.getElement(), "config"));
            if (config.getName().equals("studiomode")) {
                config.setTabs(Arrays.asList(new GWTSidePanelTab("templates"), new GWTSidePanelTab("createContent"),
                        new GWTSidePanelTab("content"), new GWTSidePanelTab("images"), new GWTSidePanelTab("files"),
                        new GWTSidePanelTab("mashups"), new GWTSidePanelTab("search"), new GWTSidePanelTab("workflow")));
            } else {
                config.setTabs(Arrays.asList(new GWTSidePanelTab("pages"), new GWTSidePanelTab("createContent"),
                        new GWTSidePanelTab("content"), new GWTSidePanelTab("images"), new GWTSidePanelTab("files"),
                        new GWTSidePanelTab("mashups"), new GWTSidePanelTab("search"), new GWTSidePanelTab("workflow")));
            }
            panel.add(new EditPanelViewport(DOM.getInnerHTML(panel.getElement()),
                    DOM.getElementAttribute(panel.getElement(), "path"),
                    DOM.getElementAttribute(panel.getElement(), "template"),
                    DOM.getElementAttribute(panel.getElement(), "locale"), config));
        }
    }
}
