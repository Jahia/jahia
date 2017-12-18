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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.Iterator;
import java.util.Map;

/**
 * User: jahia
 * Date: 4 avr. 2008
 * Time: 10:44:01
 */
public class OpenWindowActionItem extends BaseActionItem {

    public static class OpenWindowForSingleFileActionItem extends OpenWindowActionItem {
        @Override
        public void handleNewLinkerSelection() {
            LinkerSelectionContext lh = linker.getSelectionContext();
            setEnabled(lh.getSingleSelection() != null);
        }
    }

    @Override
    public void onComponentSelection() {
        Map preferences = getGwtToolbarItem().getProperties();
        final GWTJahiaProperty windowUrl = (GWTJahiaProperty) preferences.get(Constants.URL);
        if (Log.isDebugEnabled()) {
            Iterator it = preferences.keySet().iterator();
            while (it.hasNext()) {
                Log.debug("Found property: " + it.next());
            }
        }

        String wOptions = "";

        final GWTJahiaProperty noOptions = (GWTJahiaProperty) preferences.get(Constants.NO_OPTIONS);

        if (noOptions == null) {
            final GWTJahiaProperty windowWidth = (GWTJahiaProperty) preferences.get(Constants.WIDTH);
            String wWidth = "";
            if (windowWidth == null) {
                Log.debug("Warning: width not found - nb. preferences:" + preferences.size());
                wWidth = ",width=900";
            } else {
                wWidth = ",width=" + windowWidth.getValue();
            }

            final GWTJahiaProperty windowHeight = (GWTJahiaProperty) preferences.get(Constants.HEIGHT);
            String wHeight = "";
            if (windowHeight == null) {
                wHeight = ",height=600";
            } else {
                wHeight = ",height=" + windowHeight.getValue();
            }
            wOptions = "scrollbars=yes,resizable=yes,status=no,location=no" + wWidth + wHeight;
        }

        String name = getPropertyValue(getGwtToolbarItem(), "target");
        name = name != null ? name : getGwtToolbarItem().getTitle().replaceAll(" ", "_").replaceAll("-", "_");
        final String jsUrl = getPropertyValue(getGwtToolbarItem(), "js.url");
        if (jsUrl != null) {
            Window.open(JahiaGWTParameters.getParam(jsUrl), name, wOptions);
        } else if (windowUrl != null && windowUrl.getValue() != null) {
            String value = URL.replacePlaceholders(windowUrl.getValue(), linker.getSelectionContext().getSingleSelection());
            Window.open(value, name, wOptions);
        }
    }

    @Override
    public void handleNewLinkerSelection() {
        Map preferences = getGwtToolbarItem().getProperties();
        final GWTJahiaProperty windowUrl = (GWTJahiaProperty) preferences.get(Constants.URL);
        if (!hasPermission(JahiaGWTParameters.getSiteNode())) {
            setEnabled(false);
        } else {
            if (windowUrl != null && windowUrl.getValue() != null) {
                try {
                    URL.replacePlaceholders(windowUrl.getValue(), linker.getSelectionContext().getSingleSelection());
                    setEnabled(true);
                } catch (Exception e) {
                    setEnabled(false);
                }
            }
        }
    }
}
