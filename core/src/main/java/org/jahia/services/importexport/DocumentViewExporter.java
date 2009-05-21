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
package org.jahia.services.importexport;

import org.jahia.api.Constants;
import org.jahia.content.*;
import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.VersioningDifferenceStatus;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.PageProperty;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.acl.JahiaACLException;
import org.jahia.services.acl.JahiaACLEntry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.fields.ContentFieldTypes;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.timebasedpublishing.RetentionRule;
import org.jahia.services.timebasedpublishing.RangeRetentionRule;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.ExternalWorkflow;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.registries.ServicesRegistry;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.hibernate.manager.JahiaLinkManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.utils.LanguageCodeConverters;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 21, 2008
 * Time: 5:10:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentViewExporter extends Exporter {
    private static Logger logger = Logger
            .getLogger(DocumentViewExporter.class);

    public void export(ContentObject object, String languageCode, ContentHandler h, Set files, ProcessingContext jParams, Map params) throws JahiaException, SAXException {
        if (params == null) {
            params = new HashMap();
        }
        EntryLoadRequest toLoadRequest = (EntryLoadRequest) params.get(ImportExportService.TO);
        if (toLoadRequest == null) {
            List locales = new ArrayList();
            locales.add(EntryLoadRequest.SHARED_LANG_LOCALE);
            locales.add(LanguageCodeConverters.languageCodeToLocale(languageCode));
            toLoadRequest = new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0, locales);
        } else {
            List locales = new ArrayList();
            locales.add(LanguageCodeConverters.languageCodeToLocale(ContentField.SHARED_LANGUAGE));
            locales.add(LanguageCodeConverters.languageCodeToLocale(languageCode));
            toLoadRequest = new EntryLoadRequest(toLoadRequest.getWorkflowState(), toLoadRequest.getVersionID(),
                    locales, toLoadRequest.isWithMarkedForDeletion());
        }

        params.put(ImportExportService.TO, toLoadRequest);

        Object oldFrom = params.get(ImportExportService.FROM);
        Map froms;
        if (oldFrom instanceof Map) {
            froms = (Map) oldFrom;
            if (froms.containsKey(languageCode)) {
                froms = (Map) froms.get(languageCode);
            }
        } else {
            froms = new HashMap();
            if (oldFrom != null) {
                froms.put(null, oldFrom);
            }
        }

        for (Iterator iterator = new HashSet(froms.keySet()).iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            EntryLoadRequest fromLoadRequest = (EntryLoadRequest) froms.get(key);
            if (fromLoadRequest != null) {
                fromLoadRequest = new EntryLoadRequest(fromLoadRequest);
                fromLoadRequest.setFirstLocale(languageCode);
                froms.put(key, fromLoadRequest);
            }
        }
        params.put(ImportExportService.FROM, froms);
        params.put(GHOST, new HashSet());

        h.startDocument();

        h.startPrefixMapping("jahia", ImportExportService.JAHIA_URI);
        h.endPrefixMapping("jahia");
        h.startPrefixMapping("jcr", ImportExportService.JCR_URI);
        h.endPrefixMapping("jcr");
        String templatePackageName = jParams.getSite().getTemplatePackageName().replace(' ', '_');
        h.startPrefixMapping("tpl", ImportExportService.JAHIA_URI + templatePackageName);
        h.endPrefixMapping("tpl");
        h.startPrefixMapping("tplnt", ImportExportService.JAHIA_URI + templatePackageName + "/nt");
        h.endPrefixMapping("tplnt");
        h.startPrefixMapping("", "");
        h.endPrefixMapping("");

        Stack elementStack = new Stack();
        elementStack.add(new Siblings());
        export(object, languageCode, h, files, jParams, params, true, false, elementStack);

        h.endDocument();

        params.put(ImportExportService.FROM, oldFrom);
    }

    public void export(ContentObject object, String languageCode, ContentHandler h, Set files, ProcessingContext jParams, Map params, boolean top, boolean parentAdded, Stack elementStack) throws JahiaException, SAXException {
        try {
            AttributesImpl attr = new AttributesImpl();
            String templatePackageName = jParams.getSite().getTemplatePackageName().replace(' ', '_');
            if (top) {
                attr.addAttribute(ImportExportService.NS_URI, "j", "xmlns:j", CDATA, ImportExportService.JAHIA_URI);
                attr.addAttribute(ImportExportService.NS_URI, "jcr", "xmlns:jcr", CDATA, ImportExportService.JCR_URI);
            }

            String uri = "";
            String prefix = "";
            String elementName = null;
            Element currentElement = null;
            Siblings mySiblings = (Siblings) elementStack.peek();

            String changedStatus = null;

            boolean view = !Boolean.FALSE.equals(params.get(ImportExportService.VIEW_CONTENT));
            boolean viewVersionNumbers = Boolean.TRUE.equals(params.get(ImportExportService.VIEW_VERSION));
            boolean viewAcl = !Boolean.FALSE.equals(params.get(ImportExportService.VIEW_ACL));
            boolean viewMetadata = !Boolean.FALSE.equals(params.get(ImportExportService.VIEW_METADATA));
            boolean viewLinks = !Boolean.FALSE.equals(params.get(ImportExportService.VIEW_JAHIALINKS));
            boolean viewWF = !Boolean.FALSE.equals(params.get(ImportExportService.VIEW_WORKFLOW));
            boolean viewPid = !Boolean.FALSE.equals(params.get(ImportExportService.VIEW_PID));
            Set ghost = (Set) params.get(GHOST);

            String link = (String) params.get(ImportExportService.LINK);

            if (link != null) {
                viewAcl = false;
                viewVersionNumbers = true;
            }

            EntryLoadRequest toLoadRequest = (EntryLoadRequest) params.get(ImportExportService.TO);
            Map froms = (Map) params.get(ImportExportService.FROM);

            if (toLoadRequest.getWorkflowState() > 1 && !object.checkWriteAccess(jParams.getUser())) {
                checkEmptiness(top, h, attr);
                return;
            }

            ContentObjectEntryState toEntryState = getEntryState(object, languageCode, toLoadRequest);

            String uuid = ServicesRegistry.getInstance().getImportExportService().getUuid(object);
            String originalUuid = object.getProperty("originalUuid");
            String uuidToCompare = uuid;
//            if (originalUuid != null) {
//                uuidToCompare = originalUuid;
//            }
            EntryLoadRequest fromLoadRequest = (EntryLoadRequest) froms.get(uuidToCompare);

            boolean access = object.checkReadAccess(jParams.getUser());

            if (fromLoadRequest == null) {
                fromLoadRequest = (EntryLoadRequest) froms.get(null);
            }

            if (fromLoadRequest != null) {
                int stat = VersioningDifferenceStatus.UNCHANGED;

                // resolve active entry state
                ContentObjectEntryState fromEntryState = getEntryState(object, languageCode, fromLoadRequest);

                if (toEntryState != null) {
                    if (toEntryState.getVersionID() == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED ||
                            toEntryState.getWorkflowState() == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
                        if (fromEntryState != null && !fromEntryState.equals(toEntryState))
                            stat = VersioningDifferenceStatus.TO_BE_REMOVED;
                    } else {
                        if (fromEntryState == null) {
                            stat = VersioningDifferenceStatus.ADDED;
                        } else {

                            if (fromEntryState.compareTo(toEntryState) != 0) {
                                stat = VersioningDifferenceStatus.TO_BE_UPDATED;
                            } else if (!(object instanceof ContentField)) {
                                ContentObject md = object.getMetadata(CoreMetadataConstant.LAST_MODIFICATION_DATE);
                                if (md != null) {
                                    ContentObjectEntryState toEntryState2 = getEntryState(md, languageCode, toLoadRequest);
                                    if (toEntryState2 != null) {
                                        fromEntryState = getEntryState(md, languageCode, fromLoadRequest);
                                        if (fromEntryState == null || fromEntryState.compareTo(toEntryState2)!=0) {
                                            stat = VersioningDifferenceStatus.TO_BE_UPDATED;
                                        }
                                        if (toEntryState2.compareTo(toEntryState)>0) {
                                            toEntryState = toEntryState2;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (fromLoadRequest.getWorkflowState() == EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                    stat = VersioningDifferenceStatus.TO_BE_UPDATED; 
                }

                if (parentAdded) {
                    if (stat == VersioningDifferenceStatus.TO_BE_REMOVED) {
                        checkEmptiness(top, h, attr);
                        return;
                    } else {
                        stat = VersioningDifferenceStatus.ADDED;
                    }
                }
                switch (stat) {
                    case VersioningDifferenceStatus.UNCHANGED:
                        if (!access && froms.containsKey(uuidToCompare)) {
                            changedStatus = "removed";
                            access = true;
                        } else {
                            changedStatus = "unchanged";
                        }
                        view = false;
                        if (object instanceof ContentPage && getEntryState(object, languageCode, fromLoadRequest) == null) {
                            return;
                        }
                        break;
                    case VersioningDifferenceStatus.ADDED:
                        changedStatus = "added";
                        break;
                    case VersioningDifferenceStatus.TO_BE_UPDATED:
                        if (object instanceof ContentPage && ((ContentPage) object).getParentID(fromLoadRequest) != ((ContentPage) object).getParentID(toLoadRequest)) {
                            parentAdded = true;
                            changedStatus = "added";
                        } else {
                            if (!access && froms.containsKey(uuidToCompare)) {
                                changedStatus = "removed";
                                access = true;
                            } else {
                                changedStatus = "updated";
                            }
                        }
                        break;
                    case VersioningDifferenceStatus.TO_BE_REMOVED:
                        changedStatus = "removed";
                        view = false;
                        break;
                }
            } else
            if (toEntryState == null || toEntryState.getWorkflowState() == EntryLoadRequest.DELETED_WORKFLOW_STATE || toEntryState.getVersionID() == EntryLoadRequest.DELETED_WORKFLOW_STATE) {
                if (object instanceof ContentPage && ((ContentPage) object).getPageType(toLoadRequest) == JahiaPage.TYPE_DIRECT) {
                    boolean found = false;
                    List locs = jParams.getSite().getLanguageSettingsAsLocales(true);
                    for (Iterator iterator = locs.iterator(); iterator.hasNext() && !found;) {
                        Locale locale = (Locale) iterator.next();
                        if (getEntryState(object, locale.toString(), toLoadRequest) != null) {
                            ghost.add(object.getObjectKey().toString());
                            found = true;
                        }
                    }
                    if (!found) {
                        checkEmptiness(top, h, attr);
                        return;
                    }
                } else {
                    checkEmptiness(top, h, attr);
                    return;
                }
            } else if (!froms.isEmpty()) {
                changedStatus = "added";
            }

            if (!access) {
                checkEmptiness(top, h, attr);
                return;
            }

            ContentPage currentPage = null;

            if (object instanceof ContentPage) {
                uri = ImportExportService.JAHIA_URI;
                prefix = "j";
                elementName = "page";

                ContentPage cp = ((ContentPage) object);
                String title = cp.getTitle(toLoadRequest, false);
                if (cp.getPageType(toLoadRequest) == JahiaPage.TYPE_DIRECT) {
                    currentPage = cp;
                    if (view) {
                        JahiaPageDefinition definition = ((ContentPage) object).getPageTemplate(toLoadRequest);
                        if (definition.getPageType() != null) {
                            attr.addAttribute(ImportExportService.JCR_URI, "primaryType", Constants.JCR_PRIMARYTYPE, CDATA, definition.getPageType());
                        }

                        attr.addAttribute(ImportExportService.JAHIA_URI, "template", "j:template", CDATA, definition.getName());

                        PageProperty prop = ((ContentPage) object).getPageLocalProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
                        if (prop != null) {
                            attr.addAttribute(ImportExportService.JAHIA_URI, "pageKey", "j:pageKey", CDATA, prop.getValue());
                        }

                        if (title != null) {
                            attr.addAttribute(ImportExportService.JAHIA_URI, "title", "j:title", CDATA, title);
                        }
                    }
                }
            } else if (object instanceof ContentContainerList) {
                ContentContainerList cc = (ContentContainerList) object;
//                ContainerDefinitionKey cdk = (ContainerDefinitionKey) cc.getDefinitionKey(toLoadRequest);
//                JahiaContainerDefinition jcd = (JahiaContainerDefinition) ContentObject.getInstance(cdk);
                currentPage = cc.getPage();
//                String[] aliases = jcd.getAliasName();
//                if (aliases.length > 0) {
//                    String pt = aliases[0];
//                    attr.addAttribute(ImportExportService.JCR_URI, "primaryType", Constants.JCR_PRIMARYTYPE, CDATA, "tplnt:" + pt + "List");
//                }
//                elementName = jcd.getName() + "List";

//                Properties p = cc.getJahiaContainerList(jParams, toLoadRequest).getProperties();
//                Iterator en = p.keys();
//                if (viewAcl) {
//                    while (en.hasNext()) {
//                        String s = (String) en.next();
//                        if (s.startsWith("view_field_acl_")) {
//                            String prop = p.getProperty(s);
//                            if (prop != null) {
//                                try {
//                                    int aclID = Integer.parseInt(prop);
//                                    if (aclID != cc.getAclID()) {
//                                        JahiaBaseACL theACL = new JahiaBaseACL(aclID);
//                                        exportAcl(theACL, s, attr, top);
//                                    }
//                                } catch (Exception t) {
//                                    logger.error("error when getting acl",t);
//                                }
//                            }
//                        }
//                    }
//                }
//                if (p.containsKey("automatic_sort_handler")) {
//                    attr.addAttribute(ImportExportService.JAHIA_URI, "sortHandler", "j:sortHandler", CDATA, p.getProperty("automatic_sort_handler"));
//                }
            } else if (object instanceof ContentContainer) {
                ContentContainer cc = (ContentContainer) object;
                ContainerDefinitionKey cdk = (ContainerDefinitionKey) cc.getDefinitionKey(toLoadRequest);
                JahiaContainerDefinition jcd = (JahiaContainerDefinition) ContentObject.getInstance(cdk);
                currentPage = cc.getPage();
                if (jcd.getContainerType()!=null) {
                    attr.addAttribute(ImportExportService.JCR_URI, "primaryType", Constants.JCR_PRIMARYTYPE, CDATA, jcd.getContainerType());
                }
                elementName = jcd.getName();
            }
            attr.addAttribute(ImportExportService.JCR_URI, "uuid", "jcr:uuid", CDATA, uuid);
            if (originalUuid != null) {
                attr.addAttribute(ImportExportService.JAHIA_URI, "originalUuid", "j:originalUuid", CDATA, originalUuid);
            }

            if (view && viewMetadata) {
                // add meta data as properties or nodes ?
                List md = object.getMetadatas();
                for (Iterator iterator = md.iterator(); iterator.hasNext();) {
                    ContentField metadataContentField = (ContentField) iterator.next();
                    FieldDefinitionKey fdk = (FieldDefinitionKey) metadataContentField.getDefinitionKey(toLoadRequest);
                    JahiaFieldDefinition jcd = (JahiaFieldDefinition) ContentObject.getInstance(fdk);
                    String metadataName = jcd.getName();
                    if (CoreMetadataConstant.PAGE_PATH.equals(metadataName)) {
                        continue;
                    }

                    ContentObjectEntryState mdEntryState = getEntryState(metadataContentField, languageCode, toLoadRequest);
                    if (mdEntryState != null) {
                        String fieldValue = getFieldValue(metadataContentField, files, jParams, mdEntryState, attr);
                        if (fieldValue != null && !fieldValue.equals("")) {
                            if (CoreMetadataConstant.CREATION_DATE.equals(metadataName)) {
                                attr.addAttribute(ImportExportService.JCR_URI, "created", "jcr:created", CDATA, fieldValue);
                            } else if (CoreMetadataConstant.LAST_MODIFICATION_DATE.equals(metadataName)) {
                                attr.addAttribute(ImportExportService.JCR_URI, "lastModified", "jcr:lastModified", CDATA, fieldValue);
                            } else {
                                attr.addAttribute(ImportExportService.JAHIA_URI, metadataName, "j:" + metadataName, CDATA, fieldValue);
                            }
                        }
                    }
                }

                if (!(object instanceof ContentField)) {
                    try {
                        RetentionRule r = ServicesRegistry.getInstance().getTimeBasedPublishingService().getRetentionRule(object.getObjectKey());
                        if (r instanceof RangeRetentionRule && !r.getInherited().booleanValue()) {
                            DateFormat df = new SimpleDateFormat(ImportExportService.DATE_FORMAT);
                            long from = ((RangeRetentionRule) r).getValidFromDate().longValue();
                            if (from > 0) {
                                attr.addAttribute(ImportExportService.JAHIA_URI, "validFrom", "j:validFrom", CDATA, df.format(new Date(from)));
                            }
                            long to = ((RangeRetentionRule) r).getValidToDate().longValue();
                            if (to > 0) {
                                attr.addAttribute(ImportExportService.JAHIA_URI, "validTo", "j:validTo", CDATA, df.format(new Date(to)));
                            }
                            attr.addAttribute(ImportExportService.JAHIA_URI, "ruleType", "j:ruleType", CDATA, r.getRuleType());
                            attr.addAttribute(ImportExportService.JAHIA_URI, "ruleSettings", "j:ruleSettings", CDATA, r.getSettings());
                        }
                    } catch (Exception e) {
                    }
                }
                if (viewLinks) {
                    JahiaLinkManager linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean("org.jahia.hibernate.manager.JahiaLinkManager");
                    List links = linkManager.findByRightObjectKey(object.getObjectKey());
                    Map linkAttributes = new HashMap();
                    for (Iterator iterator = links.iterator(); iterator.hasNext();) {
                        ObjectLink objectLink = (ObjectLink) iterator.next();
                        if (!structRelations.contains(objectLink.getType())) {
                            String localName = "jahiaLink" + StringUtils.capitalize(objectLink.getType());
                            ObjectKey k = objectLink.getLeftObjectKey();
                            JahiaObject o = JahiaObject.getInstance(k);
                            if (o instanceof ContentObject) {
                                String s = (String) linkAttributes.get(localName);
                                ContentObject sourceObject = (ContentObject) o;
                                if (s == null) {
                                    s = ServicesRegistry.getInstance().getImportExportService().getUuid(sourceObject);
                                } else {
                                    s += "," + ServicesRegistry.getInstance().getImportExportService().getUuid(sourceObject);
                                }
                                linkAttributes.put(localName, s);
                            }
                        }
                    }
                    for (Iterator iterator = linkAttributes.keySet().iterator(); iterator.hasNext();) {
                        String s = (String) iterator.next();
                        attr.addAttribute(ImportExportService.JAHIA_URI, s, "j:" + s, CDATA, (String) linkAttributes.get(s));
                    }
                }
            }

            if (changedStatus != null) {
                attr.addAttribute(ImportExportService.JAHIA_URI, "diff", "j:diff", CDATA, changedStatus);
            }

            if (viewAcl && !object.isAclSameAsParent()) {
                JahiaBaseACL acl = object.getACL();
                exportAcl(acl, "acl", attr, top);
            }

            if (viewWF) {
                exportWF(object, attr);
            }

            if (viewPid && object instanceof ContentPage) {
                attr.addAttribute(ImportExportService.JAHIA_URI, "pid", "j:pid", CDATA, String.valueOf(object.getID()));
            }

            if (link != null) {
                attr.addAttribute(ImportExportService.JAHIA_URI, "linkkey", "j:linkkey", CDATA, object.getObjectKey().toString());
                attr.addAttribute(ImportExportService.JAHIA_URI, "linktype", "j:linktype", CDATA, link);
            }

            if (viewVersionNumbers) {
                if (toEntryState != null && toEntryState.getVersionID() > 0) {
                    attr.addAttribute(ImportExportService.JAHIA_URI, "version", "j:version", CDATA, Integer.toString(toEntryState.getVersionID()));
                }
                String name;
                if (object.isShared()) {
                    name = "lastImportedVersion";
                } else {
                    name = "lastImportedVersion-" + languageCode;
                }
                String last = object.getProperty(name);
                if (last != null) {
                    attr.addAttribute(ImportExportService.JAHIA_URI, "lastImportedVersion", "j:lastImportedVersion", CDATA, last);
                }
            }

            if (elementName != null) {
                currentElement = new Element(uri, elementName, (prefix != null ? prefix + ":" : "") + elementName, attr);
                mySiblings.elements.add(currentElement);
            }

            // Recurse on children
            if (!"removed".equals(changedStatus)) {
                EntryLoadRequest withDeleted = new EntryLoadRequest(toLoadRequest);
                withDeleted.setWithMarkedForDeletion(true);
                List l = object.getChilds(jParams.getUser(), withDeleted);

                if (object instanceof ContentContainerList) {
                    ImportExportUtils.orderContainerList(l, jParams);
                }
                Siblings siblings = new Siblings();
                elementStack.add(siblings);
                Set included = (Set) params.get(ImportExportService.INCLUDED);
                for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                    ContentObject child = (ContentObject) iterator.next();
                    if (child instanceof ContentField) {
                        exportField((ContentField) child, languageCode, files, jParams, params, parentAdded, attr);
                    } else {
                        if (included == null || included.contains(child.getObjectKey())) {
                            export(child, languageCode, h, files, jParams, params, false, parentAdded, elementStack);
                        } else if (object instanceof ContentContainerList && fromLoadRequest != null) {
                            if (((ContentContainer) child).getJahiaContainer(jParams, fromLoadRequest) != null) {
                                export(child, languageCode, h, files, jParams, params, false, parentAdded, elementStack);
                            }
                        }
                    }
                }

                if (object instanceof ContentPage) {
                    List<JahiaPage> list = ((ContentPage)object).getChildPages(jParams);
                    for (JahiaPage child : list) {
                        if (child.getPageType() == JahiaPage.TYPE_DIRECT) {
                            export(child.getContentPage(), languageCode, h, files, jParams, params, false, parentAdded, elementStack);
                        }
                    }
                }


                elementStack.pop();

                if (siblings.printall) {
                    for (Iterator iterator1 = siblings.elements.iterator(); iterator1.hasNext();) {
                        Element element = (Element) iterator1.next();
                        if (!element.started) {
                            h.startElement(element.uri, element.elementName, element.qName, element.attr);
                            element.started = true;
                            h.endElement(element.uri, element.elementName, element.qName);
                        }
                    }
                }
            }

            if (!"unchanged".equals(changedStatus) && !ghost.contains(currentPage.getObjectKey().toString()) || top) {
                for (Iterator iterator = elementStack.iterator(); iterator.hasNext();) {
                    Siblings siblings = (Siblings) iterator.next();
                    if (siblings != mySiblings || !(object instanceof ContentContainer)) {
                        // only print last element (current ancestor)
                        if (siblings.elements.size() > 0) {
                            Element element = (Element) (siblings.elements.get(siblings.elements.size() - 1));
                            if (!element.started) {
                                h.startElement(element.uri, element.elementName, element.qName, element.attr);
                                element.started = true;
                            }
                        }
                    } else {
                        // print all previous siblings
                        siblings.printall = true;
                        for (Iterator iterator1 = siblings.elements.iterator(); iterator1.hasNext();) {
                            Element element = (Element) iterator1.next();
                            if (!element.started) {
                                h.startElement(element.uri, element.elementName, element.qName, element.attr);
                                element.started = true;
                                if (element != currentElement) {
                                    h.endElement(element.uri, element.elementName, element.qName);
                                }
                            }
                        }
                    }                                                                                                        
                }
            }

            if (currentElement != null && currentElement.started) {
                h.endElement(uri, elementName, (prefix != null ? prefix + ":" : "")+elementName);
            }
//            elementStack.remove(currentElement);
        } catch (ClassNotFoundException e) {
            throw new JahiaException("Invalid objectKey",
                    "Invalid objectKey",
                    JahiaException.SERVICE_ERROR,
                    JahiaException.ERROR_SEVERITY,
                    e);
        }
    }

    public void exportField(ContentField field, String languageCode, Set files, ProcessingContext jParams, Map params, boolean parentAdded, AttributesImpl attr) throws JahiaException, SAXException {
        try {
            String elementName;

            String changedStatus = null;

            boolean view = !Boolean.FALSE.equals(params.get(ImportExportService.VIEW_CONTENT));
            boolean viewVersionNumbers = Boolean.TRUE.equals(params.get(ImportExportService.VIEW_VERSION));

            Set ghost = (Set) params.get(GHOST);

            String link = (String) params.get(ImportExportService.LINK);

            if (link != null) {
                viewVersionNumbers = true;
            }

            EntryLoadRequest toLoadRequest = (EntryLoadRequest) params.get(ImportExportService.TO);
            Map froms = (Map) params.get(ImportExportService.FROM);

            if (toLoadRequest.getWorkflowState() > 1 && !field.checkWriteAccess(jParams.getUser())) {
                return;
            }

            ContentObjectEntryState toEntryState = getEntryState(field, languageCode, toLoadRequest);

            String uuid = ServicesRegistry.getInstance().getImportExportService().getUuid(field);
            String uuidToCompare = uuid;
            EntryLoadRequest fromLoadRequest = (EntryLoadRequest) froms.get(uuidToCompare);

            boolean access = field.checkReadAccess(jParams.getUser());

            if (fromLoadRequest == null) {
                fromLoadRequest = (EntryLoadRequest) froms.get(null);
            }

            if (fromLoadRequest != null) {
                int stat = VersioningDifferenceStatus.UNCHANGED;

                // resolve active entry state
                ContentObjectEntryState fromEntryState = getEntryState(field, languageCode, fromLoadRequest);

                if (toEntryState != null) {
                    if (toEntryState.getVersionID() == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED ||
                            toEntryState.getWorkflowState() == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
                        if (fromEntryState != null && !fromEntryState.equals(toEntryState))
                            stat = VersioningDifferenceStatus.TO_BE_REMOVED;
                    } else {
                        if (fromEntryState == null) {
                            stat = VersioningDifferenceStatus.ADDED;
                        } else {
                            if (fromEntryState.compareTo(toEntryState) != 0) {
                                stat = VersioningDifferenceStatus.TO_BE_UPDATED;
                            }
                        }
                    }
                }

                if (parentAdded) {
                    if (stat == VersioningDifferenceStatus.TO_BE_REMOVED) {
                        return;
                    } else {
                        stat = VersioningDifferenceStatus.ADDED;
                    }
                }
                switch (stat) {
                    case VersioningDifferenceStatus.UNCHANGED:
                        if (!access && froms.containsKey(uuidToCompare)) {
                            changedStatus = "removed";
                            access = true;
                        } else {
                            changedStatus = "unchanged";
                        }
                        view = false;
                        break;
                    case VersioningDifferenceStatus.ADDED:
                        changedStatus = "added";
                        break;
                    case VersioningDifferenceStatus.TO_BE_UPDATED:
                        if (!access && froms.containsKey(uuidToCompare)) {
                            changedStatus = "removed";
                            access = true;
                        } else {
                            changedStatus = "updated";
                        }
                        break;
                    case VersioningDifferenceStatus.TO_BE_REMOVED:
                        changedStatus = "removed";
                        view = false;
                        break;
                }
            } else
            if (toEntryState == null || toEntryState.getWorkflowState() == EntryLoadRequest.DELETED_WORKFLOW_STATE || toEntryState.getVersionID() == EntryLoadRequest.DELETED_WORKFLOW_STATE) {
                return;
            } else if (!froms.isEmpty()) {
                changedStatus = "added";
            }

            if (!access) {
                return;
            }

            ContentPage currentPage = null;


            FieldDefinitionKey fdk = (FieldDefinitionKey) field.getDefinitionKey(toLoadRequest);
            JahiaFieldDefinition jfd = (JahiaFieldDefinition) ContentObject.getInstance(fdk);
            JahiaContainerDefinition jcd = (JahiaContainerDefinition) ContentObject.getInstance(field.getParent(null).getDefinitionKey(null));
            currentPage = field.getPage();

            elementName = jfd.getName();
            elementName = elementName.substring(jcd.getName().length() + 1);
            if (view) {
                String fieldValue = null;
                if (field.getType() == ContentFieldTypes.PAGE) {
                    JahiaPage p = ((ContentPageField)field).getPage(jParams, jParams.getEntryLoadRequest());
                    if (p != null) {
                        attr.addAttribute(ImportExportService.JAHIA_URI, elementName+"Title", "j:" + elementName+"Title", CDATA, p.getTitle());
                        switch (p.getPageType()) {
                            case JahiaPage.TYPE_DIRECT:
                            case JahiaPage.TYPE_LINK:
                                String uuidref = ServicesRegistry.getInstance().getImportExportService().getUuid(p.getContentPage());
                                attr.addAttribute(ImportExportService.JAHIA_URI, elementName+"Reference", "j:" + elementName+"Title", CDATA, uuidref);
                                break;
                            case JahiaPage.TYPE_URL:
                                attr.addAttribute(ImportExportService.JAHIA_URI, elementName+"URL", "j:" + elementName + "URL", CDATA, p.getURL(jParams));
                                break;
                        }
                    }
                } else {
                    fieldValue = getFieldValue(field, files, jParams, toEntryState, attr);
                }

                if (fieldValue != null) {
                    attr.addAttribute(ImportExportService.JAHIA_URI, elementName, "j:" + elementName, CDATA, fieldValue);
                }
            }

//            attr.addAttribute(ImportExportService.JCR_URI, "uuid", "jcr:uuid", CDATA, uuid);
//            if (originalUuid != null) {
//                attr.addAttribute(ImportExportService.JAHIA_URI, "originalUuid", "j:originalUuid", CDATA, originalUuid);
//            }

            if (changedStatus != null) {
                attr.addAttribute(ImportExportService.JAHIA_URI, "diff", "j:diff", CDATA, changedStatus);
            }

            if (viewVersionNumbers) {
                if (toEntryState != null && toEntryState.getVersionID() > 0) {
                    attr.addAttribute(ImportExportService.JAHIA_URI, "version", "j:version", CDATA, Integer.toString(toEntryState.getVersionID()));
                }
                String name;
                if (field.isShared()) {
                    name = "lastImportedVersion";
                } else {
                    name = "lastImportedVersion-" + languageCode;
                }
                String last = field.getProperty(name);
//            if (last != null) {
//                attr.addAttribute(ImportExportService.JAHIA_URI, "lastImportedVersion", "j:lastImportedVersion", CDATA, last);
//            }
            }


            if (!"unchanged".equals(changedStatus) && !ghost.contains(currentPage.getObjectKey().toString())) {

            }

        } catch (ClassNotFoundException e) {
            throw new JahiaException("Invalid objectKey",
                    "Invalid objectKey",
                    JahiaException.SERVICE_ERROR,
                    JahiaException.ERROR_SEVERITY,
                    e);
        }
    }

    private void checkEmptiness(boolean top, ContentHandler h, AttributesImpl attr) throws SAXException {
        if (top) {
            h.startElement(ImportExportService.JAHIA_URI, "empty", "j:empty", attr);
            h.endElement(ImportExportService.JAHIA_URI, "empty", "j:empty");
        }
    }

    private void exportWF(ContentObject obj, AttributesImpl attr) throws JahiaException {
        WorkflowService service = ServicesRegistry.getInstance().getWorkflowService();

        if (service.isModeDifferentFromDefault((ContentObjectKey) obj.getObjectKey())) {
            int mode = service.getWorkflowMode(obj);
            switch (mode) {
                case WorkflowService.INACTIVE:
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflow", "j:workflow", CDATA, "inactive");
                    return;
                case WorkflowService.JAHIA_INTERNAL:
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflow", "j:workflow", CDATA, "internal");
                    return;
                case WorkflowService.EXTERNAL:
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflow", "j:workflow", CDATA, "external");
                    String name = service.getExternalWorkflowName(obj);
                    String processId = service.getExternalWorkflowProcessId(obj);
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflowName", "j:workflowName", CDATA, name);
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflowProcess", "j:workflowProcess", CDATA, processId);
                    ExternalWorkflow workflow = service.getExternalWorkflow(name);
                    if (workflow != null) {
                        final Collection rolesList = workflow.getAllActionRoles(processId);
                        for (Iterator iterator = rolesList.iterator(); iterator.hasNext();) {
                            String role = (String) iterator.next();
                            JahiaGroup grp = service.getRoleGroup(obj, role, false);
                            if (grp != null) {
                                Iterator en = new EnumerationIterator(grp.members());
                                String v = "";
                                while (en.hasNext()) {
                                    Object curMember = en.next();
                                    if (curMember instanceof JahiaUser) {
                                        JahiaUser user = (JahiaUser) curMember;
                                        v += "|u:" + user.getUsername();
                                    } else if (curMember instanceof JahiaGroup) {
                                        JahiaGroup group = (JahiaGroup) curMember;
                                        v += "|g:" + group.getGroupname();
                                    }
                                }
                                if (v.length() > 0) {
                                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflowRole" + role, "j:workflowRole" + role, CDATA, v.substring(1));
                                }
                            }
                        }
                    }
                    return;
                case WorkflowService.INHERITED:
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflow", "j:workflow", CDATA, "inherited");
                    return;
                case WorkflowService.LINKED:
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflow", "j:workflow", CDATA, "linked");
                    return;
            }
        }
    }


    public void exportAcl(JahiaBaseACL acl, String attrName, AttributesImpl attr, boolean top) throws JahiaACLException {
        String perms = "";
        List users = acl.getUsernameList(null);
        for (Iterator iterator = users.iterator(); iterator.hasNext();) {
            String username = (String) iterator.next();
            JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(username);
            if (user != null) {
                JahiaAclEntry ace = acl.getUserEntry(user);

                String inheritedRead = ((acl.getPermission(user, JahiaBaseACL.READ_RIGHTS)) ? "r" : "-");
                String inheritedWrite = ((acl.getPermission(user, JahiaBaseACL.WRITE_RIGHTS)) ? "w" : "-");
                String inheritedAdmin = ((acl.getPermission(user, JahiaBaseACL.ADMIN_RIGHTS)) ? "a" : "-");

                if (ace != null) {
                    String yesRead = (ace.getPermission(JahiaBaseACL.READ_RIGHTS) == JahiaACLEntry.ACL_YES ? "r" : "-");
                    String yesWrite = (ace.getPermission(JahiaBaseACL.WRITE_RIGHTS) == JahiaACLEntry.ACL_YES ? "w" : "-");
                    String yesAdmin = (ace.getPermission(JahiaBaseACL.ADMIN_RIGHTS) == JahiaACLEntry.ACL_YES ? "a" : "-");

                    perms += "|u:" + user.getUsername() + ":" + (ace.getPermission(JahiaBaseACL.READ_RIGHTS) == JahiaAclEntry.ACL_NEUTRAL ? inheritedRead : yesRead) +
                            (ace.getPermission(JahiaBaseACL.WRITE_RIGHTS) == JahiaAclEntry.ACL_NEUTRAL ? inheritedWrite : yesWrite) +
                            (ace.getPermission(JahiaBaseACL.ADMIN_RIGHTS) == JahiaAclEntry.ACL_NEUTRAL ? inheritedAdmin : yesAdmin);
                } else if (top) {
                    perms += "|u:" + user.getUsername() + ":" + inheritedRead + inheritedWrite + inheritedAdmin;

                }
            }
        }
        List groups = acl.getGroupnameListNoAdmin(null);
        for (Iterator iterator = groups.iterator(); iterator.hasNext();) {
            String groupname = (String) iterator.next();
            JahiaGroup group = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(groupname);
            if (group != null) {
                JahiaAclEntry ace = acl.getGroupEntry(group);

                String inheritedRead = ((acl.getPermission(group, JahiaBaseACL.READ_RIGHTS)) ? "r" : "-");
                String inheritedWrite = ((acl.getPermission(group, JahiaBaseACL.WRITE_RIGHTS)) ? "w" : "-");
                String inheritedAdmin = ((acl.getPermission(group, JahiaBaseACL.ADMIN_RIGHTS)) ? "a" : "-");

                if (ace != null) {
                    String yesRead = (ace.getPermission(JahiaBaseACL.READ_RIGHTS) == JahiaACLEntry.ACL_YES ? "r" : "-");
                    String yesWrite = (ace.getPermission(JahiaBaseACL.WRITE_RIGHTS) == JahiaACLEntry.ACL_YES ? "w" : "-");
                    String yesAdmin = (ace.getPermission(JahiaBaseACL.ADMIN_RIGHTS) == JahiaACLEntry.ACL_YES ? "a" : "-");

                    perms += "|g:" + group.getGroupname() + ":" + (ace.getPermission(JahiaBaseACL.READ_RIGHTS) == JahiaAclEntry.ACL_NEUTRAL ? inheritedRead : yesRead) +
                            (ace.getPermission(JahiaBaseACL.WRITE_RIGHTS) == JahiaAclEntry.ACL_NEUTRAL ? inheritedWrite : yesWrite) +
                            (ace.getPermission(JahiaBaseACL.ADMIN_RIGHTS) == JahiaAclEntry.ACL_NEUTRAL ? inheritedAdmin : yesAdmin);
                } else if (top) {
                    perms += "|g:" + group.getGroupname() + ":" + inheritedRead + inheritedWrite + inheritedAdmin;
                }
            }
        }
        if (acl.getInheritance() == 1) {
            perms += "|break";
        }
        if (perms.length() > 0) {
            perms = perms.substring(1);
            attr.addAttribute(ImportExportService.JAHIA_URI, attrName, "j:" + attrName, CDATA, perms);
        }
    }

}
