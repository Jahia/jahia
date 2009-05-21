/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.search;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentFieldKey;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.fields.JahiaField;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.version.EntryLoadRequest;

/**
 *
 */
public class JahiaFieldIndexableDocument extends IndexableDocumentImpl {

    private static final long serialVersionUID = -7098422534417050471L;

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaFieldIndexableDocument.class);

    private volatile Boolean alreadyLoadedValues = Boolean.FALSE;

    private JahiaField field;

    /**
     * A Field instance to store in the search index
     *
     * @param theField
     */

    public JahiaFieldIndexableDocument(JahiaField theField) {

        super();
        this.field = theField;
        this.setKeyFieldName(JahiaSearchConstant.COMP_ID);
        this.setKey(getCompId(field));
    }

    public Map<String, DocumentField> getFields() {
        prepareFieldValue();
        return super.getFields();
    }

    public DocumentField getField(String name) {
        prepareFieldValue();
        return super.getField(name);
    }

    /**
     * Used to delay when field content will really be loaded in memory
     */
    public void prepareFieldValue() {

        synchronized (alreadyLoadedValues) {
            if (alreadyLoadedValues.booleanValue()) {
                return;
            }
            alreadyLoadedValues = Boolean.TRUE;
            this.fillDocument();
        }
    }

    protected void fillDocument() {

        if (this.getField(this.getKeyFieldName()) == null) {
            this.setFieldValue(this.getKeyFieldName(), this.getKey());
        }
        fillDocumentWithParentContainer();

        this.setFieldValue(JahiaSearchConstant.FIELD_FIELDID,
                String.valueOf(this.field.getID()));

        this.setFieldValue(JahiaSearchConstant.FIELD_DEFINITION_ID,
                String.valueOf(this.field.getFieldDefID()));
        try {
            this.setFieldValue(JahiaSearchConstant.FIELD_DEFINITION_NAME,
                    String.valueOf(this.field.getDefinition().getName()));
        } catch (Exception t) {
            logger.debug("Exception occured retrieving Field Definition Name", t);
        }

    }

    protected void fillDocumentWithParentContainer() {

        ContentContainer container = null;
        if (this.field.getctnid() > 0) {
            try {
                container = ContentContainer.getContainer(this.field.getctnid());
            } catch (Exception t) {
                logger.debug("Cannot retrieve container " + this.field.getctnid(), t);
                return;
            }
        } else {
            return;
        }
        // Add comparable meta-data to index.
        this.setFieldValue(JahiaSearchConstant.ID,
                String.valueOf(container.getID()));
        this.setFieldValue(JahiaSearchConstant.OBJECT_KEY,
                ContentContainerKey.toObjectKeyString(container.getID()));
        this.setFieldValue(JahiaSearchConstant.JAHIA_ID,
                String.valueOf(container.getSiteID()));
        this.setFieldValue(JahiaSearchConstant.PAGE_ID,
                String.valueOf(container.getPageID()));
        this.setFieldValue(JahiaSearchConstant.PARENT_ID,
                String.valueOf(container.getParentContainerListID()));
        this.setFieldValue(JahiaSearchConstant.CONTENT_TYPE,
                String.valueOf(JahiaSearchConstant.FIELD_TYPE));
        this.setFieldValue(JahiaSearchConstant.DEFINITION_ID,
                String.valueOf(container.getDefinitionID(EntryLoadRequest.STAGED)));
        try {
            ContentDefinition def = JahiaContainerDefinition
                    .getContentDefinitionInstance(container.getDefinitionKey(EntryLoadRequest.STAGED));
            this.setFieldValue(JahiaSearchConstant.DEFINITION_NAME,
                    String.valueOf(def.getName()));
        } catch (Exception t) {
            logger.debug("Exception occured retrieving Container Definition Name", t);
        }
        this.setFieldValue(JahiaSearchConstant.ACL_ID,
                String.valueOf(container.getAclID()));
        this.setFieldValue(JahiaSearchConstant.VERSION,
                String.valueOf(this.field.getVersionID()));
        this.setFieldValue(JahiaSearchConstant.WORKFLOW_STATE,
                String.valueOf(this.field.getWorkflowState()));
        this.setFieldValue(JahiaSearchConstant.LANGUAGE_CODE,
                this.field.getLanguageCode());

        EntryLoadRequest loadRequest = EntryLoadRequest.STAGED;
        try {
            Map<Object, Object> properties = ServicesRegistry.getInstance()
                    .getJahiaContainersService().getContainerProperties(container.getID());
            if ( properties != null ){
                // add the container's properties
                if (properties != null) {
                    for (Map.Entry<Object, Object> key : properties.entrySet()) {
                        this.setFieldValue(JahiaSearchConstant.OBJECT_PROPERTY_PREFIX
                                + key.getKey(), (String)key.getValue());
                    }
                }
            }
            // add the container's definition properties
            try {
                JahiaContainerDefinition def = (JahiaContainerDefinition) ContentDefinition
                        .getContentDefinitionInstance(container.getDefinitionKey(loadRequest));
                this.setFieldValue(
                        JahiaSearchConstant.CONTAINER_DEFINITION_PRIMARYTYPE,
                        def.getPrimaryType());
                Properties props = def.getProperties();
                if (props != null) {
                    for (Map.Entry<?, ?> key : props.entrySet()) {
                        this.setFieldValue(JahiaSearchConstant.DEFINITION_PROPERTY_PREFIX
                                + key.getKey(),
                                (String) key.getValue());
                    }
                }
            } catch (Exception t) {
                logger.debug(t);
            }
        } catch (Exception t) {
            logger.debug("Exception occured loading JahiaContainer " + container.getID(), t);
        }

        // add the container's categories
        try {
            Set<Category> categories = ServicesRegistry.getInstance().getCategoryService()
                    .getObjectCategories(container.getObjectKey());
            for (Category category : categories) {
                this.addFieldValue(JahiaSearchConstant.CATEGORY_ID,
                        category.getKey());
            }
        } catch (Exception t) {
            logger.debug("Error accessing field's categories", t);
        }

    }

    public static final String getCompId(JahiaField field) {
        StringBuffer buff = new StringBuffer(ContentFieldKey.toObjectKeyString(field.getID()));
        buff.append("_");
        int wf = field.getWorkflowState();
        if ( wf > EntryLoadRequest.STAGING_WORKFLOW_STATE ){
            wf = EntryLoadRequest.STAGING_WORKFLOW_STATE;
        }
        buff.append(wf);
        buff.append("_");
        buff.append(field.getLanguageCode());
        return buff.toString();
    }

}
