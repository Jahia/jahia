/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.commons.client.module;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Label;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;

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
