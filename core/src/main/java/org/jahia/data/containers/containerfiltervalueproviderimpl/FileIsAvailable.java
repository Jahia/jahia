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
package org.jahia.data.containers.containerfiltervalueproviderimpl;

import org.jahia.data.containers.ContainerFilterFieldValueProvider;
import org.jahia.data.fields.JahiaFileFieldWrapper;
import org.jahia.data.fields.LoadFlags;
import org.jahia.data.files.JahiaFileField;
import org.jahia.params.ProcessingContext;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.registries.ServicesRegistry;
import org.jahia.bin.Jahia;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 2 mai 2007
 * Time: 10:25:28
 * To change this template use File | Settings | File Templates.
 */
public class FileIsAvailable implements ContainerFilterFieldValueProvider {

    /**
     * Return "true" if the underlying file is not null and accessible.
     * Otherwise return "false"
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
        String value = "true";
        try {
            JahiaFileFieldWrapper jahiaField = (JahiaFileFieldWrapper) ServicesRegistry.getInstance()
                    .getJahiaFieldService()
                    .loadField(fieldId, LoadFlags.ALL, Jahia.getThreadParamBean());
            if ( jahiaField == null ){
                return "false";
            }
            JahiaFileField fileField = (JahiaFileField)jahiaField.getObject();
            if ( fileField == null || fileField.getID()==-1 ){
                return "false";
            }
        } catch ( Exception t ){
            value = "false";
        }
        return value;
    }

}
