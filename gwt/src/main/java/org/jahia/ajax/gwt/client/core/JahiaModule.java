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

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * User: jahia
 * Date: 22 f�vr. 2008
 * Time: 14:29:00
 */
public abstract class JahiaModule {
    /**
     * This method is called at "onModuleLoad" on the page entrypoint.
     *
     * @param page       the GWTJahiaPage page wrapper
     * @param rootPanels list of all com.google.gwt.user.client.ui.RootPanel that correspond to this JahiaModule
     */
    public abstract void onModuleLoad(final GWTJahiaPageContext page, final List<RootPanel> rootPanels);

    /**
     * Get ressource
     *
     * @param key
     * @return
     */
    public String getResource(String rootId, String key) {
        return Messages.getResource(key);
    }

    public String getResource(String key) {
        return getResource(getJahiaModuleType(), key);
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getResourceWithDefaultValue(String key, String defaultValue) {
        String result = getResource(getJahiaModuleType(), key);
        if (result == null || "".equals(result.trim())){
            return defaultValue;
        }
        return result;
    }

    /**
     * Gwt jahia module type
     *
     * @return
     */
    public abstract String getJahiaModuleType();
}
