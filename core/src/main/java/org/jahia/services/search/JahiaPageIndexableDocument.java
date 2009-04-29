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
 package org.jahia.services.search;

import java.util.*;

import org.jahia.content.ObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.pages.JahiaPage;

/**
 *
 */
public class JahiaPageIndexableDocument extends IndexableDocumentImpl {

    private static final long serialVersionUID = -2252306891267522024L;

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaPageIndexableDocument.class);

    private ObjectKey objectKey;

    /**
     * Internally init extended IndexableDocument with
     * key = ContentPageKey.getKey() and keyFieldName = JahiaSearchConstant.COMP_ID
     *
     *
     * @param page
     * @param loadRequest
     */
    public JahiaPageIndexableDocument (JahiaPage page, EntryLoadRequest loadRequest) {
        super();
        objectKey = new ContentPageKey(page.getID());
        this.setKeyFieldName(JahiaSearchConstant.COMP_ID);
        this.setKey(getCompId(page,loadRequest));
        fillDocument(page, loadRequest);
    }

    public ObjectKey getObjectKey() {
        return objectKey;
    }


    protected void fillDocument(JahiaPage page, EntryLoadRequest loadRequest) {

        if ( this.getField(this.getKeyFieldName()) == null ){
            this.setFieldValue(this.getKeyFieldName(),this.getKey());
        }

        // Add comparable meta-data to index.
        this.setFieldValue(JahiaSearchConstant.ID,
                this.objectKey.getIDInType());
        this.setFieldValue (JahiaSearchConstant.OBJECT_KEY,
                objectKey.getKey());
        this.setFieldValue (JahiaSearchConstant.JAHIA_ID,
                String.valueOf (page.getJahiaID()));
        this.setFieldValue (JahiaSearchConstant.PARENT_ID,
                String.valueOf(page.getParentID()));
        this.setFieldValue (JahiaSearchConstant.CONTENT_TYPE,
                String.valueOf (JahiaSearchConstant.PAGE_TYPE));
        this.setFieldValue (JahiaSearchConstant.DEFINITION_ID,
                String.valueOf (page.getPageTemplateID()));
        try {
            this.setFieldValue (JahiaSearchConstant.DEFINITION_NAME,
                String.valueOf (page.getPageTemplate().getName()));
        } catch ( Exception t ){
            logger.debug("Exception occured retrieving page Definition Name",t);
        }
        this.setFieldValue (JahiaSearchConstant.ACL_ID,
                String.valueOf (page.getAclID ()));
        this.setFieldValue (JahiaSearchConstant.VERSION,
                String.valueOf (loadRequest.getVersionID()));
        this.setFieldValue (JahiaSearchConstant.WORKFLOW_STATE,
                String.valueOf (loadRequest.getWorkflowState()));
        this.setFieldValue (JahiaSearchConstant.LANGUAGE_CODE,
                loadRequest.getFirstLocale(true).toString());

        // add the page's definition properties
        Map<String, String> properties = page.getPageTemplate().getProperties();
        try {
            if (properties != null) {
                for (Map.Entry<String, String> key : properties.entrySet()) {
                    this.setFieldValue(JahiaSearchConstant.DEFINITION_PROPERTY_PREFIX
                            + key.getKey(),
                                  key.getValue());
                }
            }
        } catch ( Exception t ){
            logger.debug(t);
        }

        // add the page's categories
        try {
            Set<Category> categories = ServicesRegistry.getInstance ().getCategoryService ()
                    .getObjectCategories (objectKey);
            for (Category category : categories) {
                this.addFieldValue (JahiaSearchConstant.CATEGORY_ID,
                        category.getKey ());
            }
        } catch (Exception t) {
            logger.debug ("Error accessing page's categories", t);
        }

    }

    public static final String getCompId(JahiaPage page,
                                         EntryLoadRequest loadRequest){
        StringBuffer buff = new StringBuffer(ContentPageKey.toObjectKeyString(page.getID()));
        buff.append("_");
        buff.append(loadRequest.getWorkflowState());
        buff.append("_");
        buff.append(loadRequest.getFirstLocale(true).toString());
        return buff.toString();
    }
}
