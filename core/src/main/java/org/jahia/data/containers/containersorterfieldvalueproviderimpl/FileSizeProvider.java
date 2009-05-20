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

import org.jahia.services.version.EntryLoadRequest;
import org.jahia.params.ProcessingContext;
import org.jahia.data.containers.ContainerSorterFieldValueProvider;
import org.jahia.data.fields.JahiaFileFieldWrapper;
import org.jahia.data.fields.LoadFlags;
import org.jahia.data.files.JahiaFileField;
import org.jahia.registries.ServicesRegistry;
import org.jahia.bin.Jahia;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 nov. 2005
 * Time: 13:36:54
 * To change this template use File | Settings | File Templates.
 */
public class FileSizeProvider implements ContainerSorterFieldValueProvider {

    private static final long serialVersionUID = 4481815135117154813L;

    /**
     * Returns the file size of a file within a JahiaFieldFieldWrapper
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
        String value;
        // use the file size as field value
        try {
            JahiaFileFieldWrapper jahiaField = (JahiaFileFieldWrapper) ServicesRegistry.getInstance()
                    .getJahiaFieldService()
                    .loadField(fieldId, LoadFlags.ALL, Jahia.getThreadParamBean());
            JahiaFileField fileField = (JahiaFileField)jahiaField.getObject();
            value = String.valueOf(fileField.getSize());
        } catch ( Exception t ){
            // could not retrieve the file size
            value = "0";
        }
        return value;
    }

}
