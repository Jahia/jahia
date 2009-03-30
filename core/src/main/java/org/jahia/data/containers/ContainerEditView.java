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

//
//
//
//
//
// 28.07.2002 NK

package org.jahia.data.containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.JahiaSimpleField;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.JahiaConsole;

/**
 * Holds information used to build the Container Edition Popup.
 * 
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class ContainerEditView implements Serializable {

    private static final long serialVersionUID = 5058456682938269472L;

    public static final String EMPTY_STRING = "";

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(ContainerEditView.class);

    private Map<String, ContainerEditViewFieldGroup> views = new HashMap<String, ContainerEditViewFieldGroup>();

    /** The list of all fields ordered as they are added **/
    private List<String> fields = new ArrayList<String>();

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param String
     *            groupName
     * 
     */
    public ContainerEditView() {
    }

    // --------------------------------------------------------------------------
    /**
     * Add a Field
     * 
     * @param String
     *            groupName, the group name
     * @param String
     *            groupTitle, the group title
     * @param String
     *            groupDescr, the group descr
     * @param String
     *            fielName, the field name
     * @param String
     *            descr, the field descr
     */
    public void addField(String groupName, String groupTitle,
            String groupDescr, String fieldName, String fieldDescr) {
        if (groupName == null || groupName.trim().equals("")) {
            return;
        }
        if (fieldName == null || fieldName.trim().equals("")) {
            return;
        }

        ContainerEditViewFieldGroup fieldGroup = new ContainerEditViewFieldGroup(
                groupName, groupTitle, groupDescr);
        fieldGroup.addField(fieldName, fieldDescr);
        addFieldGroup(fieldGroup);
    }

    // --------------------------------------------------------------------------
    /**
     * Add a group of fields
     * 
     * @param ContainerEditViewFieldGroup
     *            , a group of fields
     */
    public void addFieldGroup(ContainerEditViewFieldGroup fieldGroup) {

        JahiaConsole.println("ContainerEditView.addFieldGroup", "Added Group ["
                + fieldGroup.getName() + "]");

        if (fieldGroup == null || fieldGroup.getName() == null
                || fieldGroup.getName().trim().equals("")) {
            return;
        }
        fieldGroup.setPos(views.size());
        views.put(fieldGroup.getName(), fieldGroup);

        fields.addAll(fieldGroup.getFieldNames());

    }

    // --------------------------------------------------------------------------
    /**
     * Return the Field Group containing a given field name
     * 
     * @param String
     *            fieldName, the field name
     */
    public ContainerEditViewFieldGroup getFieldGroupByFieldName(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        List<ContainerEditViewFieldGroup> v = new ArrayList<ContainerEditViewFieldGroup>(
                views.values());
        int size = v.size();
        ContainerEditViewFieldGroup fieldGroup = null;
        for (int i = 0; i < size; i++) {
            fieldGroup = v.get(i);
            if (fieldGroup.fieldExists(fieldName)) {
                return fieldGroup;
            }
        }
        return null;
    }

    // --------------------------------------------------------------------------
    /**
     * Returns the Map of views
     * 
     */
    public Map<String, ContainerEditViewFieldGroup> getViews() {
        return views;
    }

    // --------------------------------------------------------------------------
    /**
     * Returns the List of field names
     * 
     */
    public List<String> getFields() {
        return this.fields;
    }

    /**
     * Return customized Edit view if any, else return the default
     * 
     * @param theContainer
     *            JahiaContainer
     * @param jParams
     *            ProcessingContext
     * @param visibleFields
     *            Set, if null, all fields are visible
     * @return ContainerEditView
     */
    public static ContainerEditView getInstance(JahiaContainer theContainer,
            ProcessingContext jParams, Set<Integer> visibleFields) throws JahiaException {
        ContainerEditView ctnEditView = null;
        try {
            ctnEditView = getCustomizedInstance(theContainer, jParams,
                    visibleFields);
        } catch (JahiaException je) {
            logger.debug(je);
        }
        if (ctnEditView == null) {
            ctnEditView = getDefaultInstance(theContainer, jParams,
                    visibleFields);
        }
        return ctnEditView;
    }

    /**
     * Build a Default Container Edit View for a given Container. It's based on how fields are declared in the template.
     * 
     * @param theContainer
     *            the container reference
     * @param jParams
     *            the param bean
     * @param visibleFields
     *            the list of visible fields, if null, all fields are visible
     * 
     * @return
     */
    public static ContainerEditView getDefaultInstance(
            JahiaContainer theContainer, ProcessingContext jParams,
            Set<Integer> visibleFields) throws JahiaException {

        ContainerEditView editView = new ContainerEditView();
        ContainerEditViewFieldGroup fieldGroup = null;

        Iterator<JahiaField> fieldsList = theContainer.getFields();
        int nbFields = 0;
        String fieldTitle = EMPTY_STRING;
        while (fieldsList.hasNext()) {
            JahiaField theField = fieldsList.next();
            if (visibleFields == null
                    || visibleFields.contains(new Integer(theField.getID()))) {
                JahiaFieldDefinition fieldDef = theField.getDefinition();
//                if ("true"
//                        .equals(fieldDef
//                                .getProperty(FieldDefinition.HIDDEN_FROM_EDITING_ENGINE))) {
//                    // Do not display field that should be hidden in Editing engine
//                    continue;
//                }
                if (fieldDef.getIsMetadata()
                        && !ServicesRegistry.getInstance().getMetadataService()
                                .isDeclaredMetadata(fieldDef.getName())) {
                    // skip not declared metadata
                    continue;
                }
                if (theField instanceof JahiaSimpleField) {
                    if (nbFields == 0) {
                        // the first Field
                        fieldTitle = theField.getDefinition().getTitle();
                        fieldGroup = new ContainerEditViewFieldGroup(theField
                                .getDefinition().getName(), fieldTitle,
                                EMPTY_STRING);
                    } else {
                        // a consecutive simple field
                        fieldGroup.addField(theField.getDefinition().getName(),
                                EMPTY_STRING);
                        if (!fieldGroup.getTitle().endsWith(";...")) {
                            fieldGroup.setTitle(fieldGroup.getTitle() + ";...");
                        }
                    }
                    fieldGroup.addField(theField.getDefinition().getName(),
                            EMPTY_STRING);
                    nbFields += 1;
                } else {
                    if (nbFields > 0) {
                        // add the previous groups of simple fields in the edit view
                        editView.addFieldGroup(fieldGroup);
                    }
                    // Create a new group with this single field
                    fieldTitle = theField.getDefinition().getTitle();
                    fieldGroup = new ContainerEditViewFieldGroup(theField
                            .getDefinition().getName(), fieldTitle,
                            EMPTY_STRING);
                    fieldGroup.addField(theField.getDefinition().getName(),
                            EMPTY_STRING);
                    editView.addFieldGroup(fieldGroup);
                    // reset nbFields
                    nbFields = 0;
                }
            }
        }
        if (nbFields > 0) {
            editView.addFieldGroup(fieldGroup);
        }
        return editView;
    }

    /**
     * Build a Customized Container Edit View for a given Container and a given User. Use Container Edit vie
     * 
     * @param theContainer
     *            the container on a
     * @param jParams
     *            the param bean
     * @param visibleFields
     *            the list of visible fields, if null, all fields are visible
     */
    public static ContainerEditView getCustomizedInstance(
            JahiaContainer theContainer, ProcessingContext jParams,
            Set<Integer> visibleFields) throws JahiaException {

        if (theContainer.getctndefid() <= 0) {
            return null;
        }
        logger.debug("Started for container ["
                + theContainer.getDefinition().getName() + "]");

        ContainerEditView editView = theContainer.getDefinition()
                .getContainerEditView();
        if (editView == null) {
            return null;
        }
        ContainerEditView newEditView = new ContainerEditView();
        ContainerEditViewFieldGroup fieldGroup = null;

        List<ContainerEditViewFieldGroup> v = new ArrayList<ContainerEditViewFieldGroup>(
                editView.getViews().values());
        ContainerEditViewFieldGroup dummyFieldGroup = new ContainerEditViewFieldGroup(
                EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
        Collections.sort(v, dummyFieldGroup);
        JahiaField theField = null;
        int vSize = v.size();

        for (int i = 0; i < vSize; i++) {
            fieldGroup = v.get(i);
            List<String> fieldNames = fieldGroup.getFieldNames();
            int fieldNamesSize = fieldNames.size();
            ContainerEditViewFieldGroup newFieldGroup = new ContainerEditViewFieldGroup(
                    fieldGroup.getName(), fieldGroup.getTitle(), fieldGroup
                            .getDescr());
            String fieldName = EMPTY_STRING;
            for (int j = 0; j < fieldNamesSize; j++) {
                fieldName = (String) fieldNames.get(j);
                theField = theContainer.getField(fieldName);
                if (visibleFields == null
                        || visibleFields
                                .contains(new Integer(theField.getID()))) {
                    newFieldGroup.addField(fieldGroup.getField(fieldName));
                }
            }
            if (newFieldGroup.getFields().size() > 0) {
                newEditView.addFieldGroup(newFieldGroup);
            }
        }
        return newEditView;
    }

}
