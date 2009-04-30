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
 package org.jahia.data.containers.containersorterfieldvalueproviderimpl;

import org.jahia.bin.Jahia;
import org.jahia.data.containers.ContainerSorterFieldValueProvider;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.version.EntryLoadRequest;

/**
 * User: pol
 * Date: 26 nov. 2006
 * Time: 16:18:32
 */
public class PageTitleProvider implements ContainerSorterFieldValueProvider {

    private static final long serialVersionUID = -1184905505819430500L;

    /**
     * Returns the page name of a file within a jahiaField
     *
     * @param fieldId
     * @param params
     * @param loadRequest
     * @param defaultValue
     * @return
     */
    public String getFieldValue(int fieldId,
                                ProcessingContext params,
                                EntryLoadRequest loadRequest,
                                String defaultValue) {
        String value = "";
        // use the file name as field value
        try {
            JahiaField jahiaField = (JahiaField) ServicesRegistry.getInstance()
                    .getJahiaFieldService()
                    .loadField(fieldId, LoadFlags.ALL, Jahia.getThreadParamBean());
            if (jahiaField != null) {
                JahiaPage pageField = (JahiaPage)jahiaField.getObject();
                if (pageField != null) {
                    value = pageField.getTitle();
                }
            }
        } catch ( Exception t ){
            // could not retrieve the file name
        }
        return value;
    }

}
