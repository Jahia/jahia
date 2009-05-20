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
package org.jahia.ajax.gwt.template.general.live.client;

import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.client.core.JahiaModuleProvider;

/**
 * User: jahia
 * Date: 28 mars 2008
 * Time: 15:02:52
 */
public class LiveModeEntryPoint extends JahiaPageEntryPoint {
    private JahiaModuleProvider operationModeJahiaModuleProvider;

    public void onModuleLoad() {
        super.onModuleLoad();
    }


    /**
     * This is the method that dispatches jahia types into corresponding module managers.
     * It must contain only Jahia-provided modules.
     * NO FORK HERE PLEASE
     *
     * @return the corresponding module manager
     */
    public JahiaModuleProvider getOperationModeJahiaModuleProvider() {
        if (operationModeJahiaModuleProvider == null) {
            operationModeJahiaModuleProvider = new LiveJahiaModuleProvider();
        }
        return operationModeJahiaModuleProvider;
    }
}
