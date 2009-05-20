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

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Label;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;

import java.util.List;


/**
 * User: jahia
 * Date: 2 avr. 2008
 * Time: 11:45:52
 */
public class ExampleCustomJahiaModuleProvider extends JahiaModuleProvider {
    public static String JAHIA_TYPE_EXAMPLE = "com.mycorp.jahiatype.example";

    public JahiaModule getJahiaModuleByJahiaType(String jahiaType) {
        if (jahiaType != null) {
            if (jahiaType.equalsIgnoreCase(JAHIA_TYPE_EXAMPLE)) {
                JahiaModule jahiModule = new JahiaModule() {
                    /**
                     * This method is called at "onModuleLoad" on the page entrypoint.
                     *
                     * @param page       the GWTJahiaPage page wrapper
                     * @param rootPanels list of all com.google.gwt.user.client.ui.RootPanel that correspond to this JahiaModule
                     */
                    public void onModuleLoad(GWTJahiaPageContext page, List rootPanels) {
                        for (int i = 0; i < rootPanels.size(); i++) {
                            RootPanel rootPanel = (RootPanel) rootPanels.get(i);
                            rootPanel.add(new Label("Example of custom JahiaModule"));
                        }
                    }

                    public String getJahiaModuleType() {
                        return JAHIA_TYPE_EXAMPLE;
                    }
                };
                return jahiModule;
            }
        }
        // jahiaType is null it doesn't correspond to a jahiaModule
        return null;
    }
}
