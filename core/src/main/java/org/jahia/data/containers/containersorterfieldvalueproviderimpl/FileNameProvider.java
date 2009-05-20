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
public class FileNameProvider implements ContainerSorterFieldValueProvider {

    private static final long serialVersionUID = -1528971944790506208L;
    private boolean withFullPath;

    /**
     * Returns the file name of a file within a JahiaFieldFieldWrapper
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
        // use the file name as field value
        try {
            JahiaFileFieldWrapper jahiaField = (JahiaFileFieldWrapper) ServicesRegistry.getInstance()
                    .getJahiaFieldService()
                    .loadField(fieldId, LoadFlags.ALL, Jahia.getThreadParamBean());
            JahiaFileField fileField = (JahiaFileField)jahiaField.getObject();
            value = fileField.getRealName();
            if ( !withFullPath && value != null && value.indexOf("/") != -1 ) {
                value = value.substring(value.lastIndexOf("/")+1,value.length());
            }
        } catch ( Exception t ){
            // could not retrieve the file name
            value = "";
        }
        return value;
    }

    public boolean isWithFullPath() {
        return withFullPath;
    }

    public void setWithFullPath(boolean withFullPath) {
        this.withFullPath = withFullPath;
    }

}
