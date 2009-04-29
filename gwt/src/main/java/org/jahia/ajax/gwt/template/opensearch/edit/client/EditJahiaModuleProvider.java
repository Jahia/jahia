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
package org.jahia.ajax.gwt.template.opensearch.edit.client;

import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.module.OpenSearchJahiaModule;

/**
 * User: jahia
 * Date: 17 mars 2008
 * Time: 12:21:09
 */
public class EditJahiaModuleProvider extends org.jahia.ajax.gwt.template.common.edit.client.EditJahiaModuleProvider {
    public JahiaModule getJahiaModuleByJahiaType(String jahiaType) {
        if (jahiaType != null) {
            if (jahiaType.equalsIgnoreCase(JahiaType.OPENSEARCH)) {
                return new OpenSearchJahiaModule();
            }
        }
        return super.getJahiaModuleByJahiaType(jahiaType);
    }
}
