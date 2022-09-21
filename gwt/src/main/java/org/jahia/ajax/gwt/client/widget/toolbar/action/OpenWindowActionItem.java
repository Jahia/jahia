/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
            if (windowUrl != null && windowUrl.getValue() != null && !windowUrl.getValue().isEmpty()) {
                try {
                    URL.replacePlaceholders(windowUrl.getValue(), linker.getSelectionContext().getSingleSelection());
                    setEnabled(true);
                } catch (Exception e) {
                    setEnabled(false);
                }
            } else if (preferences.get("js.url") == null) {
                setEnabled(false);
            }
        }
    }
}
