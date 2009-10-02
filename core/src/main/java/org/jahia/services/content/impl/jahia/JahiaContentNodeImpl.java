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

import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.timebasedpublishing.DayInWeekBean;
import org.jahia.services.timebasedpublishing.RangeRetentionRule;
import org.jahia.services.timebasedpublishing.RetentionRule;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.apache.commons.lang.StringUtils;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.*;

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
    private JahiaObjectManager jahiaObjectManager;

    protected JahiaContentNodeImpl(SessionImpl session, ContentObject object) throws ItemNotFoundException {
        super(session);
        this.object = object;
        try {
            if (WorkflowService.LINKED != ServicesRegistry.getInstance().getWorkflowService().getWorkflowMode(object)) {
                initMixin(NodeTypeRegistry.getInstance().getNodeType("jmix:workflowed"));
            }
            if (!object.isAclSameAsParent() && object.getACL().getACL().getHasEntries()==1) {
                initMixin(NodeTypeRegistry.getInstance().getNodeType("jmix:accessControlled"));
            }
            checkTimeBasePublishingState(session, object);
        } catch (NoSuchNodeTypeException e) {
            e.printStackTrace();
        } catch (JahiaException e) {
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

            if (isNodeType("jmix:workflowed")) {
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
                                        state = Math.max(state, states.get(loc));
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
                                        v = "validated";
                                        break;
                                    case '2':
                                        v = "inValidation";
                                        break;
                                    default:
                                        v = "validationStep" + (Integer.valueOf("" + c) - 2) + quickEdit;
                                        break;
                                }
                            }

                            initProperty(new PropertyImpl(getSession(), this, "j:workflowState",
                                                          NodeTypeRegistry.getInstance().getNodeType("jmix:workflowed").getPropertyDefinitionsAsMap().get("j:workflowState"),
                                                          locale, new ValueImpl(v, PropertyType.STRING)));

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // retentionRules
            initTimeBasedPublishingProperties();

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
                    } else {
                        i18nEmptyProperties.add((PropertyImpl) item);
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
            if (!object.isAclSameAsParent() && object.getACL().getACL().getHasEntries()==1) {
                initNode(new JahiaAclNodeImpl(getSession(), object.getAclID(), this));
            }
            initTimeBasedPublishingNodes();
            initFields();
            if (isNodeType("jmix:workflowed")) {
                try {
                    if (WorkflowService.INHERITED != ServicesRegistry.getInstance().getWorkflowService().getWorkflowMode(object)) {
                        initNode(new JahiaWorkflowSettingsNodeImpl(getSession(), this, object));
                    }
                } catch (JahiaException e) {
                    throw new UnsupportedRepositoryOperationException("Could not find the mode of workflow for object : " + object);
                }
            }
            try {
                List<Locale> locales = getProcessingContext().getSite().getLanguageSettingsAsLocales(true);
                for (Locale locale : locales) {
                    TranslationNodeImpl t = new TranslationNodeImpl(getSession(), this, i18nProperties, i18nEmptyProperties, locale);
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
                    try {
                        cal.setTime(new Date(Long.parseLong(value)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
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
                        fields.add(new JahiaPageLinkNodeImpl(getSession(), this, def.getNodeDefinition(), ((ContentPageField)contentField), p));
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
            case FieldTypes.FILE: {
                try {
                    if (value != null) {
                        if (value.startsWith("/")) {
                            try {
                                JCRNodeWrapper file = JCRStoreService.getInstance().getSessionFactory().getCurrentUserSession().getNode(value);
                                Value v = new ValueImpl(file.getUUID(), PropertyType.REFERENCE);
                                fields.add(new JahiaFieldPropertyImpl(getSession(), this, def.getPropertyDefinition(), v, contentField, locale));
                            } catch (PathNotFoundException e) {

                            }
                        } else {
                            try {
                                String providerKey = StringUtils.substringBefore(value,":");
                                String uuid  = StringUtils.substringAfter(value,":");
                                if (!uuid.equals("/")) {
                                    JCRNodeWrapper file = JCRStoreService.getInstance().getSessionFactory().getCurrentUserSession().getNodeByUUID(providerKey, uuid);
                                    Value v = new ValueImpl(file.getUUID(), PropertyType.REFERENCE);
                                    fields.add(new JahiaFieldPropertyImpl(getSession(), this, def.getPropertyDefinition(), v, contentField, locale));
                                }
                            } catch (ItemNotFoundException e) {
                            } catch (UnsupportedRepositoryOperationException e) {
                            }
                        }
                    }
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
                break;
            }
            case FieldTypes.APPLICATION: {
                try {
                    JCRNodeWrapper portlet = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(contentField.getJahiaField(elr).getRawValue());
                    if (portlet != null) {
                        Value v = new ValueImpl(portlet.getUUID(), PropertyType.REFERENCE);
                        fields.add(new JahiaFieldPropertyImpl(getSession(), this, def.getPropertyDefinition(), v, contentField, locale));
                    }
                } catch (RepositoryException e) {
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
                    ExtendedPropertyDefinition propertyDefinition = def.getPropertyDefinition();
                    String[] vcs = propertyDefinition.getValueConstraints();
                    List<String> constraints = Arrays.asList(vcs);
                    if (!propertyDefinition.isMultiple()) {
                        if (value.startsWith("<jahia-resource")) {
                            value = ResourceBundleMarker.parseMarkerValue(value).getResourceKey();
                        }
                        if (constraints.isEmpty() || constraints.contains(value)) {
                            Value v = new ValueImpl(value, PropertyType.STRING);
                            fields.add(new JahiaFieldPropertyImpl(getSession(), this, propertyDefinition, v, contentField, locale));
                        }
                    } else {
                        String[] strings = value.split("\\$\\$\\$");
                        List<Value> values = new ArrayList<Value>();
                        for (int i = 0; i < strings.length; i++) {
                            String string = strings[i];

                            if (string.startsWith("<jahia-resource")) {
                                string = ResourceBundleMarker.parseMarkerValue(string).getResourceKey();
                            }
                            if (constraints.isEmpty() || constraints.contains(value)) {
                                values.add(new ValueImpl(string,propertyDefinition.getRequiredType()));
                            }
                        };
                        fields.add(new JahiaFieldPropertyImpl(getSession(), this, propertyDefinition, values.toArray(new Value[values.size()]), contentField, locale));
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

    private void checkTimeBasePublishingState(SessionImpl session, ContentObject object) throws ItemNotFoundException, NoSuchNodeTypeException {
        jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaObjectManager.class.getName());
        final JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectManager.getJahiaObjectDelegate(object.getObjectKey());
        if(jahiaObjectDelegate!=null) {
            if(session.getWorkspace().getName().equals("live") && jahiaObjectDelegate.isNotValid()) {
                throw new ItemNotFoundException("This item is not visible in live due to some time based publishing rules");
            }
            final RetentionRule retentionRule = jahiaObjectDelegate.getRule();
            if(retentionRule instanceof RangeRetentionRule) {
                if(RetentionRule.RULE_START_AND_END_DATE.equals(retentionRule.getRuleType())) {
                    initMixin(NodeTypeRegistry.getInstance().getNodeType("jmix:simpleTimebasedPublished"));
                } else if (RetentionRule.RULE_XDAYINWEEK.equals(retentionRule.getRuleType())) {
                    initMixin(NodeTypeRegistry.getInstance().getNodeType("jmix:dailyTimebasedPublished"));
                } else if (RetentionRule.RULE_DAILY.equals(retentionRule.getRuleType())){
                    initMixin(NodeTypeRegistry.getInstance().getNodeType("jmix:hourlyTimebasedPublished"));
                }
            }
        }
    }

    private void initTimeBasedPublishingProperties() throws RepositoryException {
        if (isNodeType("jmix:simpleTimebasedPublished")) {
            jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaObjectManager.class.getName());
            final JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectManager.getJahiaObjectDelegate(object.getObjectKey());
            if (jahiaObjectDelegate != null) {
                final RetentionRule retentionRule = jahiaObjectDelegate.getRule();
                if (RetentionRule.RULE_START_AND_END_DATE.equals(retentionRule.getRuleType())) {
                    RangeRetentionRule rule = (RangeRetentionRule) retentionRule;
                    final Long startDate = rule.getStartDate();
                    final Long endDate = rule.getEndDate();
                    if (startDate != null && startDate > 0) {
                        final GregorianCalendar c = new GregorianCalendar();
                        c.setTimeInMillis(startDate);
                        initProperty(new PropertyImpl(getSession(), this, "j:startDate",
                                                      NodeTypeRegistry.getInstance().getNodeType("jmix:simpleTimebasedPublished").getPropertyDefinitionsAsMap().get("j:startDate"),
                                                      null, new ValueImpl(c)));
                    }
                    if (endDate != null && endDate > 0) {
                        final GregorianCalendar c = new GregorianCalendar();
                        c.setTimeInMillis(endDate);
                        initProperty(new PropertyImpl(getSession(), this, "j:endDate",
                                                      NodeTypeRegistry.getInstance().getNodeType("jmix:simpleTimebasedPublished").getPropertyDefinitionsAsMap().get("j:endDate"),
                                                      null, new ValueImpl(c)));
                    }
                }
            }
        } else if (isNodeType("jmix:hourlyTimebasedPublished")) {
            jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaObjectManager.class.getName());
            final JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectManager.getJahiaObjectDelegate(object.getObjectKey());
            if (jahiaObjectDelegate != null) {
                final RetentionRule retentionRule = jahiaObjectDelegate.getRule();
                if (RetentionRule.RULE_DAILY.equals(retentionRule.getRuleType())) {
                    RangeRetentionRule rule = (RangeRetentionRule) retentionRule;
                    final int fromHours = rule.getDailyFromHours();
                    final int fromMinutes = rule.getDailyFromMinutes();
                    final int toHours = rule.getDailyToHours();
                    final int toMinutes = rule.getDailyToMinutes();
                    if (fromHours >= 0) {
                        initProperty(new PropertyImpl(getSession(), this, "j:fromHour",
                                                      NodeTypeRegistry.getInstance().getNodeType("jmix:hourlyTimebasedPublished").getPropertyDefinitionsAsMap().get("j:fromHour"),
                                                      null, new ValueImpl(fromHours)));
                        initProperty(new PropertyImpl(getSession(), this, "j:fromMinutes",
                                                      NodeTypeRegistry.getInstance().getNodeType("jmix:hourlyTimebasedPublished").getPropertyDefinitionsAsMap().get("j:fromMinutes"),
                                                      null, new ValueImpl(fromMinutes)));
                    }
                    if (toHours >= 0) {
                        initProperty(new PropertyImpl(getSession(), this, "j:toHour",
                                                      NodeTypeRegistry.getInstance().getNodeType("jmix:hourlyTimebasedPublished").getPropertyDefinitionsAsMap().get("j:toHour"),
                                                      null, new ValueImpl(toHours)));
                        initProperty(new PropertyImpl(getSession(), this, "j:toMinutes",
                                                      NodeTypeRegistry.getInstance().getNodeType("jmix:hourlyTimebasedPublished").getPropertyDefinitionsAsMap().get("j:toMinutes"),
                                                      null, new ValueImpl(toMinutes)));
                    }
                }
            }
        }
    }

    private void initTimeBasedPublishingNodes() throws RepositoryException {
        if (isNodeType("jmix:dailyTimebasedPublished")) {
            jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaObjectManager.class.getName());
            final JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectManager.getJahiaObjectDelegate(object.getObjectKey());
            if (jahiaObjectDelegate != null) {
                final RetentionRule retentionRule = jahiaObjectDelegate.getRule();
                if (RetentionRule.RULE_XDAYINWEEK.equals(retentionRule.getRuleType())) {
                    RangeRetentionRule rule = (RangeRetentionRule) retentionRule;
                    final List days = rule.getDaysInWeek();
                    for (int i = 0; i < days.size(); i++) {
                        DayInWeekBean dayInWeekBean = (DayInWeekBean) days.get(i);
                        if (dayInWeekBean.isSelected()) {
                            initNode(new DailyTimeBasedPublishingNode(getSession(),dayInWeekBean,this));
                        }
                    }
                }
            }
        }
    }
}
