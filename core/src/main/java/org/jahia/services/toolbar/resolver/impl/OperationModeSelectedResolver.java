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
package org.jahia.services.toolbar.resolver.impl;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.services.toolbar.resolver.SelectedResolver;

/**
 * User: jahia
 * Date: 4 avr. 2008
 * Time: 17:38:07
 */
public class OperationModeSelectedResolver implements SelectedResolver {
    public static final String ORG_JAHIA_TOOLBAR_ITEM_LIVE = "live";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_COMPARE = "compare";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_PREVIEW = "preview";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_EDIT = "edit";

    public boolean isSelected(JahiaData jahiaData, String type) {
        if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_LIVE)) {
            return jahiaData.getProcessingContext().getOperationMode().equalsIgnoreCase(ProcessingContext.NORMAL);
        }
        if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_COMPARE)) {
            return jahiaData.getProcessingContext().getOperationMode().equalsIgnoreCase(ProcessingContext.COMPARE);
        }
        if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_PREVIEW)) {
            return jahiaData.getProcessingContext().getOperationMode().equalsIgnoreCase(ProcessingContext.PREVIEW);
        }
        if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_EDIT)) {
            return jahiaData.getProcessingContext().getOperationMode().equalsIgnoreCase(ProcessingContext.EDIT);
        }

        return false;
    }
}
