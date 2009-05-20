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

import org.jahia.content.ContentObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.acl.JahiaBaseACL;

import java.util.Map;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @author Xavier Lawrence
 * @version 1.1
 */

public abstract class ContentBean extends AbstractJahiaObjectBean {

    protected MetadataLookupBean metadata;
    
    protected ProcessingContext processingContext;
    
    public abstract ContentBean getParent();
    
    public abstract int getID();
    
    public abstract int getDefinitionID();
    
    public abstract int getPageID();
    
    /**
     * Returns the type of the ContentBean
     * @return the type of the ContentBean as a String object
     */
    public abstract String getBeanType();
    
    public abstract Map<String, ActionURIBean> getActionURIBeans();
    
    public abstract boolean isCompletelyLocked();
    
    public abstract boolean isPartiallyLocked();
    
    public abstract boolean isIndependantWorkflow();
    
    public abstract int getGroupWorkflowState();

    public abstract JahiaBaseACL getACL();

    public abstract boolean isPicker();

    public abstract ContentObject getContentObject();

    public abstract String getJCRPath() throws JahiaException;
    
    public String getKey() {
        return getContentObject().getObjectKey().getKey();
    }

    public MetadataLookupBean getMetadata() {
        if (metadata == null) {
            metadata = new MetadataLookupBean(this.getContentObject(),
                    processingContext);
        }

        return metadata;
    }
}