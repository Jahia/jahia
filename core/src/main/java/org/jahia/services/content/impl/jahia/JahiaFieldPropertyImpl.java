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

package org.jahia.services.content.impl.jahia;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTypes;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.webdav.JahiaWebdavBaseService;
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
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 16, 2008
 * Time: 6:47:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaFieldPropertyImpl extends PropertyImpl {
    private ContentField field;
    private ContentObject parent;

    public JahiaFieldPropertyImpl(SessionImpl session, JahiaContentNodeImpl node, ExtendedPropertyDefinition def, Value value, ContentField field) {
        super(session, node, def, value);
        this.field = field;
        this.parent = node.getContentObject();
    }

    public JahiaFieldPropertyImpl(SessionImpl session, JahiaContentNodeImpl node, ExtendedPropertyDefinition def, Value[] values, ContentField field) {
        super(session, node, def, values);
        this.field = field;
        this.parent = node.getContentObject();
    }


    @Override
    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, RepositoryException {
        values = new Value[] { value };
        setModified();
    }

    public void save() throws RepositoryException {
        if (isModified() || isNew()) {
            Value value = values[0];
            try {
                if (field != null) {
                    JahiaField jahiaField = field.getJahiaField(getProcessingContext().getEntryLoadRequest());

                    String v = getValue(value, jahiaField.getType());
                    updateField(v, jahiaField);
                    ServicesRegistry.getInstance().getJahiaFieldService().saveField(jahiaField, parent.getAclID(), getProcessingContext());
                } else {
                    if (def.getDeclaringNodeType().isMixin() && (def.getDeclaringNodeType().isNodeType("jmix:contentmetadata") || def.getDeclaringNodeType().isNodeType("mix:created") ||
                            def.getDeclaringNodeType().isNodeType("mix:createdBy") || def.getDeclaringNodeType().isNodeType("jmix:lastPublished") || def.getDeclaringNodeType().isNodeType("jmix:categorized") || def.getDeclaringNodeType().isNodeType("mix:lastModified"))) {

                        // new metadata
                        JahiaFieldDefinition contentDefinition = JahiaFieldDefinitionsRegistry.getInstance().getDefinition(0, StringUtils.substringAfter(def.getName(),":"));

                        String v = getValue(value, contentDefinition.getType());
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

                        String v = getValue(value, contentDefinition.getType());
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
            JCRNodeWrapper object = JCRStoreService.getInstance().getFileNode(v, getProcessingContext().getUser());
            JahiaFileField fField = object.getJahiaFileField();
            field.setValue(v);
            field.setObject(fField);
        } else if(fieldType == FieldTypes.BIGTEXT) {
            int i = -1;
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

    private String getValue(Value value, int type) throws RepositoryException {
        switch (type) {
            case FieldTypes.DATE:
                return Long.toString(value.getDate().getTime().getTime());
            default:
                return value.getString();
        }
    }


}
