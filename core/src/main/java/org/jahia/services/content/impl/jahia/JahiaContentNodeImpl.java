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

import java.util.*;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.params.ProcessingContext;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 16, 2008
 * Time: 2:39:58 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class JahiaContentNodeImpl extends NodeImpl {

    protected ContentObject object;

    protected List<Item> fields;
    protected List<Item> emptyFields;

    protected JahiaContentNodeImpl(SessionImpl session, ContentObject object) {
        super(session);
        this.object = object;
        try {
            addMixin(NodeTypeRegistry.getInstance().getNodeType("jmix:workflowed"));
        } catch (NoSuchNodeTypeException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initProperties() throws RepositoryException {
        if (properties == null) {
            super.initProperties();

            // uuid
            ExtendedNodeType ref = NodeTypeRegistry.getInstance().getNodeType("mix:referenceable");
            String uuid = getUUID();
            initProperty(new PropertyImpl(getSession(), this,
                    ref.getDeclaredPropertyDefinitionsAsMap().get("jcr:uuid"), null,
                    new ValueImpl(uuid, PropertyType.STRING)));

            /// workflow
            try {
                List<Locale> locales = getProcessingContext().getSite().getLanguageSettingsAsLocales(true);
                for (Locale locale : locales) {
                    String lang = locale.toString();
                    String v = "";
                    String loc = (object.isShared()) ? "shared" : lang;
                    Map<String, Integer> states = object.getLanguagesStates();
                    if (states.containsKey(loc)) {
                        int state = states.get(loc);
                        if (object instanceof ContentContainer) {
                            List<? extends ContentObject> l = object.getChilds(getProcessingContext().getUser(), getEntryLoadRequest());
                            for (Iterator<? extends ContentObject> contentObjectIterator = l.iterator(); contentObjectIterator.hasNext();) {
                                ContentObject child = contentObjectIterator.next();
                                if (child instanceof ContentField) {
                                    loc = (child.isShared()) ? "shared" : lang;
                                    states = child.getLanguagesStates();
                                    if (!states.containsKey(loc)) {
                                        continue;
                                    }
                                    state = Math.max(state,states.get(loc));
                                }
                            }
                        }
                        if (state == 1) {
                            v = "active";
                        } else {
                            String extState = WorkflowService.getInstance().getExtendedWorkflowState(object, lang);
                            char c = extState.charAt(1);
                            String quickEdit = "";
                            if (extState.charAt(2) == '1') {
                                quickEdit = "-quickEdit";
                            }
                            switch (c) {
                                case '1':
                                    v = "validated"; break;
                                case '2':
                                    v = "inValidation"; break;
                                case '3':
                                    v = "validationStep1"+quickEdit; break;
                                case '4':
                                    v = "validationStep2"+quickEdit; break;
                                case '5':
                                    v = "validationStep3"+quickEdit; break;
                            }
                        }

                        initProperty(new PropertyImpl(getSession(), this, "j:workflowState",
                                NodeTypeRegistry.getInstance().getNodeType("jmix:workflowed").getPropertyDefinitionsAsMap().get("j:workflowState"),
                                locale,new ValueImpl(v, PropertyType.STRING)));

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }



            initFields();
            for (Item item : fields) {
                if (item instanceof PropertyImpl) {
                    initProperty((PropertyImpl) item);
                }
            }
            for (Item item : emptyFields) {
                if (item instanceof PropertyImpl) {
                    if (!((PropertyImpl)item).isI18n()) {
                        emptyProperties.put(item.getName(), (PropertyImpl) item);
                    }
                }
            }
        }
    }

    @Override
    protected void initNodes() throws RepositoryException {
        if (nodes == null) {
            super.initNodes();

            // acl
            if (!object.isAclSameAsParent()) {
                addMixin(NodeTypeRegistry.getInstance().getNodeType("jmix:accessControlled"));
                initNode(new JahiaAclNodeImpl(getSession(), object.getAclID(), this));
            }

            initFields();

            try {
                List<Locale> locales = getProcessingContext().getSite().getLanguageSettingsAsLocales(true);
                for (Locale locale : locales) {
                    TranslationNodeImpl t = new TranslationNodeImpl(getSession(), this, i18nProperties, locale);
                    translationNodes.put(locale.toString(), t);
                    initNode(t);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // jahialinks todo

            for (Item item : fields) {
                if (item instanceof NodeImpl) {
                    initNode((NodeImpl) item);
                }
            }
        }
    }

    protected void initFields() throws RepositoryException {
        if (fields == null) {
            fields = new ArrayList<Item>();
            emptyFields = new ArrayList<Item>();
            try {
                List<ContentField> metadatas = object.getMetadatas();
                for (ContentField contentField : metadatas) {
                    try {
                        initFieldItem(contentField);
                    } catch (ItemNotFoundException e) {
                    }
                }
            } catch (JahiaException e) {
                throw new RepositoryException(e);
            }
        }
    }

    protected void initFieldItem(ContentField contentField) throws RepositoryException {
        try {

            List<Locale> locales = getProcessingContext().getSite().getLanguageSettingsAsLocales(true);
            if (contentField.isShared()) {
                initFieldItem(contentField, null);
            } else {
                for (Locale locale : locales) {
                    initFieldItem(contentField, locale);
                }
            }
        } catch (JahiaException e) {
            throw new RepositoryException(e);
        } catch (ClassNotFoundException e) {
            throw new RepositoryException(e);
        }
    }

    private void initFieldItem(ContentField contentField, Locale locale) throws JahiaException, RepositoryException, ClassNotFoundException {
        final ProcessingContext processingContext = getProcessingContext();
        EntryLoadRequest elr = processingContext.getEntryLoadRequest();
        if (locale != null) {
            elr = new EntryLoadRequest(elr);
            elr.setFirstLocale(locale.toString());
        }
        String value = contentField.getValue(processingContext, elr);
        JahiaFieldDefinition def = (JahiaFieldDefinition) ContentDefinition.getContentDefinitionInstance(contentField.getDefinitionKey(EntryLoadRequest.STAGED));
        switch (contentField.getType()) {
            case FieldTypes.DATE: {
                if (value == null || value.length()==0 || value.equals("<empty>")) {
                    emptyFields.add(new JahiaFieldPropertyImpl(getSession(), this, def.getPropertyDefinition(), new Value[0],contentField, locale));
                } else {
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(new Date(Long.parseLong(value)));
                    fields.add(new JahiaFieldPropertyImpl(getSession(), this, def.getPropertyDefinition(), new ValueImpl(cal),contentField, locale));
                }
                break;
            }
            case FieldTypes.PAGE: {
                if (value == null || value.length()==0 || value.equals("<empty>")) {
                    //emptyFields.add(new JahiaPageLinkNodeImpl(getSession(), this, def.getNodeDefinition(), null));
                } else {
                    ContentPage p = ((ContentPageField)contentField).getContentPage(processingContext);
                    if (p != null) {
                        fields.add(new JahiaPageLinkNodeImpl(getSession(), this, def.getNodeDefinition(), p));
                    }
                }
                break;
            }
            case FieldTypes.BIGTEXT: {
                if (value == null || value.length()==0 || value.equals("<empty>")) {
                    emptyFields.add(new JahiaFieldPropertyImpl(getSession(), this, def.getPropertyDefinition(), new Value[0],contentField, locale));
                } else {
                    Value v = new ValueImpl(value, PropertyType.STRING);
                    fields.add(new JahiaFieldPropertyImpl(getSession(), this, def.getPropertyDefinition(), v, contentField, locale));
                }
                break;
            }
            default : {
                if (def.getCtnType().equals(definition.getName()+" jcr_primaryType")) {
                    return;
                }
                if (value == null || value.length()==0 || value.equals("<empty>")) {
                    emptyFields.add(new JahiaFieldPropertyImpl(getSession(), this, def.getPropertyDefinition(), new Value[0],contentField, locale));
                } else {
                    Value v = new ValueImpl(value, PropertyType.STRING);
                    ExtendedPropertyDefinition propertyDefinition = def.getPropertyDefinition();
                    if (propertyDefinition != null) {
                        fields.add(new JahiaFieldPropertyImpl(getSession(), this, propertyDefinition, v, contentField, locale));
                    }
                }
                break;
            }

        }
    }

    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return getUUID(object);
    }

    public static String getUUID(ContentObject object) throws UnsupportedRepositoryOperationException, RepositoryException {
        try {
            return object.getUUID();
        } catch (JahiaException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    protected PropertyImpl getPropertyForSet(String s) throws RepositoryException {
        PropertyImpl p = super.getPropertyForSet(s);
        if (p == null) {
            ExtendedPropertyDefinition def = nodetype.getPropertyDefinitionsAsMap().get(s);
            if (def != null && !def.isInternationalized()) {
                p = new JahiaFieldPropertyImpl(getSession(), this, def, new Value[0], null, null);
                properties.put(s,p);
                return p;
            }
        }
        return p;
    }

    public ContentObject getContentObject() {
        return object;
    }

    @Override
    public JahiaSite getSite() {
        try {
            return ServicesRegistry.getInstance().getJahiaSitesService().getSite(object.getSiteID());
        } catch (JahiaException e) {
            e.printStackTrace();
            return null;
        }
    }
}
