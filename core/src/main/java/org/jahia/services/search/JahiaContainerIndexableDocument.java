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
import java.util.Map.Entry;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ObjectKey;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.version.EntryLoadRequest;

/**
 *
 */
public class JahiaContainerIndexableDocument extends IndexableDocumentImpl {

    private static final long serialVersionUID = -3251263723722207367L;

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaContainerIndexableDocument.class);

    private volatile Boolean alreadyLoadedValues = Boolean.FALSE;

    private JahiaContainer container;
    private ObjectKey objectKey;

    /**
     * Internally init extended IndexableDocument with
     * key = ContentContainerKey.getKey() and keyFieldName = JahiaSearchConstant.COMP_ID
     *
     * @param container
     */
    public JahiaContainerIndexableDocument (JahiaContainer container) {
        super();
        objectKey = new ContentContainerKey(container.getID());
        this.setKeyFieldName(JahiaSearchConstant.COMP_ID);
        this.setKey(getCompId(container));
        this.container = container;
    }

    public JahiaContainer getContainer() {
        return container;
    }

    public ObjectKey getObjectKey() {
        return objectKey;
    }

    public Map<String, DocumentField> getFields () {
        prepareFieldValue ();
        return super.getFields ();
    }

    public DocumentField getField(String name){
        prepareFieldValue ();
        return super.getField(name);
    }

    /**
     * Used to delay when field content will really be loaded in memory
     */
    public void prepareFieldValue () {

        synchronized ( alreadyLoadedValues ) {
            if (alreadyLoadedValues.booleanValue()) {
                return;
            }
            alreadyLoadedValues = Boolean.TRUE;
            this.fillDocument();
        }
    }

    protected void fillDocument() {

        if ( this.getField(this.getKeyFieldName()) == null ){
            this.setFieldValue(this.getKeyFieldName(),this.getKey());
        }

        // Add comparable meta-data to index.
        this.setFieldValue(JahiaSearchConstant.ID,
                String.valueOf(this.container.getID()));
        this.setFieldValue (JahiaSearchConstant.OBJECT_KEY,
                objectKey.getKey());
        this.setFieldValue (JahiaSearchConstant.JAHIA_ID,
                String.valueOf (this.container.getJahiaID()));
        this.setFieldValue (JahiaSearchConstant.PAGE_ID,
                String.valueOf(this.container.getPageID()));
        this.setFieldValue (JahiaSearchConstant.PARENT_ID,
                String.valueOf(this.container.getListID()));
        this.setFieldValue (JahiaSearchConstant.CONTENT_TYPE,
                String.valueOf (JahiaSearchConstant.CONTAINER_TYPE));
        this.setFieldValue (JahiaSearchConstant.DEFINITION_ID,
                String.valueOf (this.container.getctndefid()));
        try {
            this.setFieldValue (JahiaSearchConstant.DEFINITION_NAME,
                String.valueOf (this.container.getDefinition().getName()));
        } catch ( Exception t ){
            logger.debug("Exception occured retrieving Container Definition Name",t);
        }
        try {
            this.addFieldValues(JahiaSearchConstant.CONTAINER_ALIAS,
                    this.container.getDefinition().getAliasNames());
        } catch ( Exception t ){
            logger.debug("Exception occured retrieving Container Alias Names",t);
        }
        this.setFieldValue (JahiaSearchConstant.ACL_ID,
                String.valueOf (this.container.getAclID ()));
        this.setFieldValue (JahiaSearchConstant.VERSION,
                String.valueOf (this.container.getVersionID()));
        int wf = this.container.getWorkflowState();
        if ( wf > EntryLoadRequest.STAGING_WORKFLOW_STATE ){
            wf = EntryLoadRequest.STAGING_WORKFLOW_STATE;
        }
        this.setFieldValue (JahiaSearchConstant.WORKFLOW_STATE,
                String.valueOf (wf));
        this.setFieldValue (JahiaSearchConstant.LANGUAGE_CODE,
                this.container.getLanguageCode ());

        // add the container's properties
        Properties properties = container.getProperties();
        if ( properties != null ){
            for (Iterator<Entry<Object, Object>> it = properties.entrySet().iterator(); it.hasNext();) {
                Entry<Object, Object> entry = it.next();
                this.setFieldValue(JahiaSearchConstant.OBJECT_PROPERTY_PREFIX
                        + entry.getKey(), (String)entry.getValue());
            }
        }

        // add the container's definition properties
        try {
            properties = container.getDefinition().getProperties();
            this.setFieldValue(
                    JahiaSearchConstant.CONTAINER_DEFINITION_PRIMARYTYPE,
                    container.getDefinition().getPrimaryType());
            if (properties != null) {
                for (Iterator<Entry<Object, Object>> entries = properties.entrySet().iterator(); entries.hasNext();) {
                    Entry<Object, Object> entry = entries.next();
                    this.setFieldValue(JahiaSearchConstant.DEFINITION_PROPERTY_PREFIX
                            + entry.getKey(), (String)entry.getValue());
                }
            }
        } catch ( Exception t ){
            logger.debug(t);
        }

        // add the container's categories
        try {
            Set<Category> categories = ServicesRegistry.getInstance ().getCategoryService ()
                    .getObjectCategories (objectKey);
            for (Category category : categories) {
                this.addFieldValue (JahiaSearchConstant.CATEGORY_ID,
                        category.getKey ());
            }
        } catch (Exception t) {
            logger.debug ("Error accessing container's categories", t);
        }

    }

    public static final String getCompId(JahiaContainer container){
        ObjectKey objectKey = new ContentContainerKey(container.getID());
        StringBuffer buff = new StringBuffer(objectKey.getKey());
        buff.append("_");
        int wf = container.getWorkflowState();
        if ( wf > EntryLoadRequest.STAGING_WORKFLOW_STATE ){
            wf = EntryLoadRequest.STAGING_WORKFLOW_STATE;
        }
        buff.append(wf);
        buff.append("_");
        buff.append(container.getLanguageCode());
        return buff.toString();
    }
}
