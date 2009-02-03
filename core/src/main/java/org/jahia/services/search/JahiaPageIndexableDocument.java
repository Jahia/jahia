/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
