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
package org.jahia.services.content.impl.jahia;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.JahiaBigTextField;
import org.jahia.data.files.JahiaFileField;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentDefinition;
import org.apache.commons.lang.StringUtils;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;

/**
 * JCR property implementation for Jahia field.
 * @author Thomas Draier
 * Date: Oct 16, 2008
 * Time: 6:47:12 PM
 */
public class JahiaFieldPropertyImpl extends PropertyImpl {
    private ContentField field;
    private ContentObject parent;
    private Locale locale;

    public JahiaFieldPropertyImpl(SessionImpl session, JahiaContentNodeImpl node, ExtendedPropertyDefinition def, Value value, ContentField field, Locale locale) {
        super(session, node, def, locale, value);
        this.field = field;
        this.parent = node.getContentObject();
        this.locale = this.locale;
    }

    public JahiaFieldPropertyImpl(SessionImpl session, JahiaContentNodeImpl node, ExtendedPropertyDefinition def, Value[] values, ContentField field, Locale locale) {
        super(session, node, def, locale, values);
        this.field = field;
        this.parent = node.getContentObject();
    }


    @Override
    public void setValue(Value[] values) throws ValueFormatException,
            VersionException, LockException, RepositoryException {
        this.values = values;
        setModified();
    }

    public void save() throws RepositoryException {
        if (isModified() || isNew()) {
            try {
                if (field != null) {
                    JahiaField jahiaField = field.getJahiaField(getEntryLoadRequest());

                    String v = getValue(values, jahiaField.getType());
                    updateField(v, jahiaField);
                    ServicesRegistry.getInstance().getJahiaFieldService().saveField(jahiaField, parent.getAclID(), getProcessingContext());
                } else {
                    if (def.isMetadataItem()) {

                        // new metadata
                        JahiaFieldDefinition contentDefinition = JahiaFieldDefinitionsRegistry.getInstance().getDefinition(0, StringUtils.substringAfter(def.getName(),":"));

                        String v = getValue(values, contentDefinition.getType());
                        JahiaSaveVersion saveVersion = new JahiaSaveVersion(false, false);

                        JahiaField field = ServicesRegistry.getInstance().getJahiaFieldService().createJahiaField(0, parent.getSiteID(), 0,
                                0, contentDefinition.getID(),contentDefinition.getType(),0, v, 0, 0,saveVersion.getVersionID(),saveVersion.getWorkflowState(),getProcessingContext().getLocale().toString());
                        if (field != null) {
                            field.setIsMetadata(true);
                            field.setMetadataOwnerObjectKey(parent.getObjectKey());
                            updateField(v, field);

                            // save the field
                            ServicesRegistry.getInstance().getJahiaFieldService().saveField(field, parent.getAclID(), getProcessingContext());
                        }

                    } else {
                        ContentDefinition parentdef = (ContentDefinition) ContentDefinition.getInstance(((JahiaContentNodeImpl)getParent()).getContentObject().getDefinitionKey(null));
                        JahiaFieldDefinition contentDefinition = JahiaFieldDefinitionsRegistry.getInstance().getDefinition(parent.getSiteID(), parentdef.getName()+"_"+def.getName());

                        String v = getValue(values, contentDefinition.getType());
                        JahiaSaveVersion saveVersion = new JahiaSaveVersion(false, false);

                        int pid = parent.getPageID();
                        JahiaField field = ServicesRegistry.getInstance().getJahiaFieldService().createJahiaField(0, parent.getSiteID(), pid,
                                parent.getID(), contentDefinition.getID(),contentDefinition.getType(),0, v, 0, 0,saveVersion.getVersionID(),saveVersion.getWorkflowState(),getProcessingContext().getLocale().toString());
                        if (field != null) {
                            updateField(v, field);

                            // save the field
                            ServicesRegistry.getInstance().getJahiaFieldService().saveField(field, parent.getAclID(), getProcessingContext());
                        }

                    }
                }
            } catch (ClassNotFoundException e) {
                //
            } catch (JahiaException e) {
                throw new RepositoryException(e);
            }
        }
        super.save();
    }

    private void updateField(String v, JahiaField field) throws RepositoryException {
        int fieldType = field.getType();
        if (fieldType == FieldTypes.FILE) {
            JCRNodeWrapper object = (JCRNodeWrapper) JCRStoreService.getInstance().getThreadSession(getProcessingContext().getUser()).getNodeByUUID(v);
            JahiaFileField fField = object.getJahiaFileField();
            field.setValue(v);
            field.setObject(fField);
        } else if(fieldType == FieldTypes.BIGTEXT) {
            String rawv = JahiaBigTextField.rewriteURLs(v, getProcessingContext());
            field.setValue(rawv);
            field.setRawValue(rawv);
        } else if(fieldType == FieldTypes.DATE) {
            field.setValue(v);
            field.setObject(v);
        } else {
            field.setValue(v);
        }
    }

    /**
     * Returns a string representation of the JCR property values, considering
     * also multiple value case.
     * 
     * @param values
     *            the property values array
     * @param type
     *            the type of the corresponding Jahia field
     * @return a string representation of the JCR property values, considering
     *         also multiple value case
     * @throws RepositoryException
     *             in case of value conversion problems
     */
    private static String getValue(Value[] values, int type)
            throws RepositoryException {

        List<String> textValues = new LinkedList<String>();
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                Value val = values[i];
                String text = null;
                switch (type) {
                case FieldTypes.DATE:
                    text = val != null ? Long.toString(val.getDate().getTime()
                            .getTime()) : null;
                    break;
                default:
                    text = val.getString();
                }
                if (StringUtils.isNotEmpty(text)) {
                    textValues.add(text);
                }
            }
        }
        return StringUtils.join(textValues, JahiaField.MULTIPLE_VALUES_SEP);
    }

    protected EntryLoadRequest getEntryLoadRequest() throws RepositoryException {
        if (locale != null) {
            EntryLoadRequest elr = new EntryLoadRequest(super.getEntryLoadRequest());
            elr.setFirstLocale(locale.toString());
        }
        return super.getEntryLoadRequest();
    }

    @Override
    public Node getNode() throws ValueFormatException, RepositoryException {
        final int type = getType();
        if(type == PropertyType.REFERENCE || type == ExtendedPropertyType.WEAKREFERENCE) {
            return getSession().getNodeByUUID(values[0].getString());
        } else {
            throw new ValueFormatException("This value is not a reference to a node "+def);
        }
    }
}
