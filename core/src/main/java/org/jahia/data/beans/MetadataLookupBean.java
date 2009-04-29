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
package org.jahia.data.beans;

import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;

/**
 * Lookup facade to retrieve content object's metadata fields.
 * 
 * @author Sergiy Shyrkov
 */
public class MetadataLookupBean extends LookupBaseBean<String, ContentBean> {

    private static final transient Logger logger = Logger
            .getLogger(MetadataLookupBean.class);

    private ContentObject contentObject;

    private ProcessingContext ctx;

    public MetadataLookupBean(ContentObject contentObject, ProcessingContext ctx) {
        super();
        this.contentObject = contentObject;
        this.ctx = ctx;
    }

    @Override
    public ContentBean get(Object key) {
        if (key == null) {
            throw new IllegalArgumentException(
                    "Metadata field name cannot be null");
        }
        JahiaField fld = null;
        try {
            fld = contentObject.getMetadataAsJahiaField(key.toString(), ctx);
        } catch (JahiaException e) {
            logger.error("Unable to retrieve metadata field with the name '"
                    + key + "' for content object " + contentObject, e);
        }

        return fld != null ? new FieldBean(fld, ctx) : null;
    }

}
