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

package org.jahia.services.importexport;

import org.jahia.content.*;
import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
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
import org.jahia.services.fields.ContentFileField;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.timebasedpublishing.RetentionRule;
import org.jahia.services.timebasedpublishing.RangeRetentionRule;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.ExternalWorkflow;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.registries.ServicesRegistry;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.manager.JahiaLinkManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.utils.LanguageCodeConverters;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.jcr.nodetype.NodeType;
import javax.jcr.RepositoryException;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 21, 2008
 * Time: 5:09:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaLegacyExporter extends Exporter{
    private static Logger logger = Logger
            .getLogger(JahiaLegacyExporter.class);

    public void export(ContentObject object, String languageCode, ContentHandler h, Set files, ProcessingContext jParams, Map params) throws JahiaException, SAXException {
        if (params == null) {
            params = new HashMap();
        }
        EntryLoadRequest toLoadRequest = (EntryLoadRequest) params.get(ImportExportService.TO);
        List<Locale> locales = new ArrayList<Locale>();
        locales.add(EntryLoadRequest.SHARED_LANG_LOCALE);
        locales.add(LanguageCodeConverters.languageCodeToLocale(languageCode));        
        if (toLoadRequest == null) {
            toLoadRequest = new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0, locales);
        } else {
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
        String templatePackageName = jParams.getSite().getTemplatePackageName().replace(' ','_');
        h.startPrefixMapping("tpl", ImportExportService.JAHIA_URI + templatePackageName);
        h.endPrefixMapping("tpl");
        h.startPrefixMapping("tplnt", ImportExportService.JAHIA_URI + templatePackageName + "/nt");
        h.endPrefixMapping("tplnt");

        Stack elementStack = new Stack();
        elementStack.add(new Siblings());
        export(object, languageCode, h, files, jParams, params, true, false, elementStack);

        h.endDocument();

        params.put(ImportExportService.FROM, oldFrom);
    }

    public void export(ContentObject object, String languageCode, ContentHandler h, Set files, ProcessingContext jParams, Map params, boolean top, boolean parentAdded, Stack elementStack) throws JahiaException, SAXException {
        try {
            AttributesImpl attr = new AttributesImpl();
            String templatePackageName = jParams.getSite().getTemplatePackageName().replace(' ','_');
            if (top) {
                attr.addAttribute(ImportExportService.NS_URI, "jahia", "xmlns:jahia", CDATA, ImportExportService.JAHIA_URI);
                attr.addAttribute(ImportExportService.NS_URI, "jcr", "xmlns:jcr", CDATA, ImportExportService.JCR_URI);
            }

            String uri = ImportExportService.JAHIA_URI + templatePackageName;
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
            boolean viewPickers = !Boolean.FALSE.equals(params.get(ImportExportService.VIEW_PICKERS));
            Set ghost = (Set) params.get(GHOST);

            String link = (String) params.get(ImportExportService.LINK);

            if (link != null) {
                viewAcl = false;
                viewVersionNumbers = true;
            }

            EntryLoadRequest toLoadRequest = (EntryLoadRequest) params.get(ImportExportService.TO);
            Map froms = (Map) params.get(ImportExportService.FROM);
            Map<String, Date> filesDates = (Map<String, Date>) params.get(ImportExportService.FILES_DATE);
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
                            } else if (fromEntryState.getVersionID() > 0 && object.getProperty("unversionedChange") != null &&
                                    Integer.parseInt(object.getProperty("unversionedChange")) > fromEntryState.getVersionID()) {
                                stat = VersioningDifferenceStatus.TO_BE_UPDATED;
                                toEntryState = new ContentObjectEntryState(toEntryState.getWorkflowState(), Integer.parseInt(object.getProperty("unversionedChange")), toEntryState.getLanguageCode());
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
                            changedStatus = ImportExportService.REMOVED_STATUS;
                            access = true;
                        } else {
                            changedStatus = ImportExportService.UNCHANGED_STATUS;
                        }
                        view = false;
                        if (object instanceof ContentPage && getEntryState(object, languageCode, fromLoadRequest) == null) {
                            return;
                        }
                        break;
                    case VersioningDifferenceStatus.ADDED:
                        changedStatus = ImportExportService.ADDED_STATUS;
                        break;
                    case VersioningDifferenceStatus.TO_BE_UPDATED:
                        if (object instanceof ContentPage && ((ContentPage)object).getParentID(fromLoadRequest) != ((ContentPage)object).getParentID(toLoadRequest)) {
                            parentAdded = true;
                            changedStatus = ImportExportService.ADDED_STATUS;
                        } else {
                            if (!access && froms.containsKey(uuidToCompare)) {
                                changedStatus = ImportExportService.REMOVED_STATUS;
                                access = true;
                            } else {
                                changedStatus = ImportExportService.UPDATED_STATUS;
                            }
                        }
                        break;
                    case VersioningDifferenceStatus.TO_BE_REMOVED:
                        changedStatus = ImportExportService.REMOVED_STATUS;
                        view = false;
                        break;
                }
            } else if (toEntryState == null || toEntryState.getWorkflowState() == EntryLoadRequest.DELETED_WORKFLOW_STATE || toEntryState.getVersionID() == EntryLoadRequest.DELETED_WORKFLOW_STATE) {
                if (object instanceof ContentPage && ((ContentPage)object).getPageType(toLoadRequest) == JahiaPage.TYPE_DIRECT) {
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
                changedStatus = ImportExportService.ADDED_STATUS;
            }

            if (!access) {
                checkEmptiness(top, h, attr);
                return;
            }

            ContentPage currentPage = null;

            if (object instanceof ContentPage) {
                uri = ImportExportService.JAHIA_URI;
                prefix = "jahia";
                elementName = "page";

                ContentPage cp = ((ContentPage) object);
                String title = cp.getTitle(toLoadRequest, false);
                switch (cp.getPageType(toLoadRequest)) {
                    case JahiaPage.TYPE_DIRECT:
                        currentPage = cp;
                        if (view) {
                            JahiaPageDefinition template = ((ContentPage) object).getPageTemplate(toLoadRequest);
                            attr.addAttribute(ImportExportService.JAHIA_URI, "template", "jahia:template", CDATA, template.getName());
                            if (template.getPageType() != null) {
                                attr.addAttribute(ImportExportService.JCR_URI, "primaryType", "jcr:primaryType", CDATA, template.getPageType());
                            }

                            PageProperty prop = ((ContentPage) object).getPageLocalProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
                            if (prop != null) {
                                attr.addAttribute(ImportExportService.JAHIA_URI, "pageKey", "jahia:pageKey", CDATA, prop.getValue());
                            }

                            if (title != null) {
                                attr.addAttribute(ImportExportService.JAHIA_URI, "title", "jahia:title", CDATA, title);
                            }
                        }
                        break;
                    case JahiaPage.TYPE_LINK:
                        currentPage = ContentPage.getPage(cp.getParentID(toLoadRequest));
                        elementName = "link";
                        String refuuid = null;
                        try {
                            refuuid = ServicesRegistry.getInstance().getImportExportService().getUuid(ContentPage.getPage(cp.getPageLinkID(toLoadRequest)));
                        } catch (JahiaPageNotFoundException e) {
                            refuuid = "";
                        }
                        attr.addAttribute(ImportExportService.JAHIA_URI, "reference", "jahia:reference", CDATA, refuuid);
                        if (view) {
                            if (title != null) {
                                attr.addAttribute(ImportExportService.JAHIA_URI, "title", "jahia:title", CDATA, title);
                            }
                        }
                        break;
                    case JahiaPage.TYPE_URL:
                        currentPage = ContentPage.getPage(cp.getParentID(toLoadRequest));
                        elementName = "url";
                        if (view) {
                            String s = cp.getURL(jParams, languageCode);
                            if (s != null) {
                                attr.addAttribute(ImportExportService.JAHIA_URI, "value", "jahia:value", CDATA, s);
                            }
                            if (title != null) {
                                attr.addAttribute(ImportExportService.JAHIA_URI, "title", "jahia:title", CDATA, title);
                            }
                        }
                        break;
                }
            } else if (object instanceof ContentContainerList) {
                ContentContainerList ccl = (ContentContainerList) object;
                ContainerDefinitionKey cdk = (ContainerDefinitionKey) ccl.getDefinitionKey(toLoadRequest);
                JahiaContainerDefinition jcd = (JahiaContainerDefinition) ContentObject.getInstance(cdk);
                currentPage = ccl.getPage();
                if (jcd.getContainerType() != null) {
                    attr.addAttribute(ImportExportService.JCR_URI, "primaryType", "jcr:primaryType", CDATA, jcd.getContainerType()+"List");
                } else {
                }
                ContentDefinition parentDef = (ContentDefinition) ContentDefinition.getInstance(ccl.getParent(toLoadRequest).getDefinitionKey(toLoadRequest));
                int tplNameLength;
                if (parentDef instanceof JahiaPageDefinition) {
                    tplNameLength = ((JahiaPageDefinition)parentDef).getPageType().length();
                } else {
                    tplNameLength = parentDef.getName().length();
                }
                elementName = jcd.getName().substring(tplNameLength+1) + "List";

                Properties p = ccl.getJahiaContainerList(jParams, toLoadRequest).getProperties();
                Iterator en = p.keySet().iterator();
                if (viewAcl) {
                    while (en.hasNext()) {
                        String s = (String) en.next();
                        if (s.startsWith("view_field_acl_")) {
                            String prop = p.getProperty(s);
                            if (prop != null) {
                                try {
                                    int aclID = Integer.parseInt(prop);
                                    if (aclID != ccl.getAclID()) {
                                        JahiaBaseACL theACL = new JahiaBaseACL(aclID);
                                        exportAcl(theACL, s, attr, top, ImportExportService.UPDATED_STATUS.equals(changedStatus));
                                    }
                                } catch (Exception t) {
                                    logger.error("error when getting acl",t);
                                }
                            }
                        }
                    }
                }
                if (p.containsKey("automatic_sort_handler")) {
                    attr.addAttribute(ImportExportService.JAHIA_URI, "sortHandler", "jahia:sortHandler", CDATA, p.getProperty("automatic_sort_handler"));
                }
            } else if (object instanceof ContentContainer) {
                ContentContainer cc = (ContentContainer) object;
                ContainerDefinitionKey cdk = (ContainerDefinitionKey) cc.getDefinitionKey(toLoadRequest);
                JahiaContainerDefinition jcd = (JahiaContainerDefinition) ContentObject.getInstance(cdk);
                currentPage = cc.getPage();
                if (jcd.getContainerType() != null) {
                    String type = jcd.getContainerType();
                    try {
                        type = cc.getJCRNode(jParams).getPrimaryNodeType().getName();
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                    attr.addAttribute(ImportExportService.JCR_URI, "primaryType", "jcr:primaryType", CDATA, type);
                } else {
                }
                ContentDefinition parentDef = (ContentDefinition) ContentDefinition.getInstance(cc.getParent(toLoadRequest).getParent(toLoadRequest).getDefinitionKey(toLoadRequest));
                int tplNameLength = 0;
                if (parentDef instanceof JahiaPageDefinition) {
                    tplNameLength = ((JahiaPageDefinition)parentDef).getPageType().length();
                } else {
                    tplNameLength = parentDef.getName().length();
                }

                elementName = jcd.getName().substring(tplNameLength + 1);
            } else if (object instanceof ContentField) {
                ContentField cf = (ContentField) object;
                FieldDefinitionKey fdk = (FieldDefinitionKey) cf.getDefinitionKey(toLoadRequest);
                JahiaFieldDefinition jfd = (JahiaFieldDefinition) ContentObject.getInstance(fdk);
                currentPage = cf.getPage();

                ContentDefinition parentDef = (ContentDefinition) ContentDefinition.getInstance(cf.getParent(toLoadRequest).getDefinitionKey(toLoadRequest));
                int tplNameLength = parentDef.getName().length();

                elementName = jfd.getName().substring(tplNameLength+1);

                if (view) {
                    String fieldValue = getFieldValue(cf, files, jParams, toEntryState, attr);
                    if (fieldValue != null && !(object instanceof ContentPageField)) {
                        attr.addAttribute(ImportExportService.JAHIA_URI, "value", "jahia:value", CDATA, fieldValue);
                    }
                } else if (ImportExportService.UNCHANGED_STATUS.equals(changedStatus) && cf instanceof ContentFileField && filesDates != null) {
                    Set f = new HashSet();
                    getFieldValue(cf, f, jParams, toEntryState, null);
                    for (Iterator iterator = f.iterator(); iterator.hasNext();) {
                        JCRNodeWrapper davFileAccess = (JCRNodeWrapper) iterator.next();
                        Date targetFileDate = filesDates.get(davFileAccess.getPath());
                        Date sourceFileDate = davFileAccess.getLastModifiedAsDate();
                        if (targetFileDate == null || sourceFileDate.compareTo(targetFileDate) > 0) {
                            files.add(davFileAccess);
                        }
                    }
                }
            }
            attr.addAttribute(ImportExportService.JCR_URI, "uuid", "jcr:uuid", CDATA, uuid);
            if (originalUuid != null) {
                attr.addAttribute(ImportExportService.JAHIA_URI, "originalUuid", "jahia:originalUuid", CDATA, originalUuid);
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
                                attr.addAttribute(ImportExportService.JAHIA_URI, metadataName, "jahia:" + metadataName, CDATA, fieldValue);
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
                                attr.addAttribute(ImportExportService.JAHIA_URI, "validFrom", "jahia:validFrom", CDATA, df.format(new Date(from)));
                            }
                            long to = ((RangeRetentionRule) r).getValidToDate().longValue();
                            if (to > 0) {
                                attr.addAttribute(ImportExportService.JAHIA_URI, "validTo"  , "jahia:validTo", CDATA, df.format(new Date(to)));
                            }
                            attr.addAttribute(ImportExportService.JAHIA_URI, "ruleType"  , "jahia:ruleType", CDATA, r.getRuleType());
                            attr.addAttribute(ImportExportService.JAHIA_URI, "ruleSettings"  , "jahia:ruleSettings", CDATA, r.getSettings());
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
                        if (!structRelations.contains(objectLink.getType()) && (viewPickers || !pickerRelations.contains(objectLink.getType()))) {
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

                                if (pickerRelations.contains(objectLink.getType())) {
                                    String key = "lastImportedVersion";
                                    if (!object.isShared()) {
                                        key += "-" +languageCode;
                                    }
                                    String v = object.getProperty(key);
                                    if (v != null ) {
                                        attr.addAttribute(ImportExportService.JAHIA_URI, "pickerLastImportedVersion", "jahia:pickerLastImportedVersion", "CDATA", v);
                                    }
                                }
                            }
                        }
                    }
                    for (Iterator iterator = linkAttributes.keySet().iterator(); iterator.hasNext();) {
                        String s = (String) iterator.next();
                        attr.addAttribute(ImportExportService.JAHIA_URI, s, "jahia:"+s, CDATA, (String) linkAttributes.get(s));
                    }
                }
            }

            if (changedStatus != null) {
                attr.addAttribute(ImportExportService.JAHIA_URI, "diff", "jahia:diff", CDATA, changedStatus);
            }

            if (viewAcl && !object.isAclSameAsParent()) {
                JahiaBaseACL acl = object.getACL();
                exportAcl(acl, "acl", attr, top, ImportExportService.UPDATED_STATUS.equals(changedStatus));
            }

            if (viewWF) {
                exportWF(object, attr);
            }

            if (viewPid && object instanceof ContentPage) {
                attr.addAttribute(ImportExportService.JAHIA_URI, "pid", "jahia:pid", CDATA, String.valueOf(object.getID()));
            }

            if (link != null) {
                attr.addAttribute(ImportExportService.JAHIA_URI, "linkkey", "jahia:linkkey", CDATA, object.getObjectKey().toString());
                attr.addAttribute(ImportExportService.JAHIA_URI, "linktype", "jahia:linktype", CDATA, link);
            }

            if (viewVersionNumbers) {
                if (toEntryState != null && toEntryState.getVersionID() > 0) {
                    attr.addAttribute(ImportExportService.JAHIA_URI, "version", "jahia:version", CDATA, Integer.toString(toEntryState.getVersionID()));
                }
                String name;
                if (object.isShared()) {
                    name = "lastImportedVersion";
                } else {
                    name = "lastImportedVersion-" + languageCode;
                }
                String last = object.getProperty(name);
                if (last != null) {
                    attr.addAttribute(ImportExportService.JAHIA_URI, "lastImportedVersion", "jahia:lastImportedVersion", CDATA, last);
                }
            }

            if (prefix.equals("")) {
                currentElement = new Element("", elementName, elementName, attr);
            } else {
                currentElement = new Element(uri, elementName, prefix + ":" + elementName, attr);
            }
            if (toEntryState == null || toEntryState.getWorkflowState() != -1 || !ImportExportService.UNCHANGED_STATUS.equals(changedStatus)) {
                mySiblings.elements.add(currentElement);
            }

            if (!ImportExportService.UNCHANGED_STATUS.equals(changedStatus) && !ghost.contains(currentPage.getObjectKey().toString())|| top) {
                for (Iterator iterator = elementStack.iterator(); iterator.hasNext();) {
                    Siblings siblings = (Siblings) iterator.next();
                    if (siblings != mySiblings || !(object instanceof ContentContainer)) {
                        // only print last element (current ancestor)
                        Element element = (Element) (siblings.elements.get(siblings.elements.size() - 1));
                        if (!element.started) {
                            h.startElement(element.uri, element.elementName, element.qName, element.attr);
                            element.started = true;
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

            // Recurse on children
            if (!ImportExportService.REMOVED_STATUS.equals(changedStatus) && (toEntryState == null || toEntryState.getWorkflowState() != -1 || !ImportExportService.UNCHANGED_STATUS.equals(changedStatus))) {
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

                    if (included == null || included.contains(child.getObjectKey())) {
                        export(child, languageCode, h, files, jParams, params, false, parentAdded, elementStack);
                    } else if (object instanceof ContentContainerList && fromLoadRequest != null) {
                        if (((ContentContainer) child).getJahiaContainer(jParams, fromLoadRequest) != null) {
                            export(child, languageCode, h, files, jParams, params, false, parentAdded, elementStack);
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

            if (currentElement != null && currentElement.started) {
                if (prefix.equals("")) {
                    h.endElement(uri, elementName, elementName);
                } else {
                    h.endElement(uri, elementName, prefix + ":" + elementName);
                }
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

    private void checkEmptiness(boolean top, ContentHandler h, AttributesImpl attr) throws SAXException {
        if (top) {
            h.startElement(ImportExportService.JAHIA_URI,"empty", "jahia:empty", attr);
            h.endElement(ImportExportService.JAHIA_URI,"empty", "jahia:empty");
        }
    }

    private void exportWF(ContentObject obj, AttributesImpl attr) throws JahiaException {
        WorkflowService service = ServicesRegistry.getInstance().getWorkflowService();

        if (service.isModeDifferentFromDefault((ContentObjectKey) obj.getObjectKey())) {
            int mode = service.getWorkflowMode(obj);
            switch(mode) {
                case WorkflowService.INACTIVE:
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflow", "jahia:workflow", CDATA, "inactive");
                    return;
                case WorkflowService.JAHIA_INTERNAL:
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflow", "jahia:workflow", CDATA, "internal");
                    return;
                case WorkflowService.EXTERNAL:
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflow", "jahia:workflow", CDATA, "external");
                    String name = service.getExternalWorkflowName(obj);
                    String processId = service.getExternalWorkflowProcessId(obj);
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflowName", "jahia:workflowName", CDATA, name);
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflowProcess", "jahia:workflowProcess", CDATA, processId);
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
                                        v += "|u:"+user.getUsername();
                                    } else if (curMember instanceof JahiaGroup) {
                                        JahiaGroup group = (JahiaGroup) curMember;
                                        v += "|g:"+group.getGroupname();
                                    }
                                }
                                if (v.length()> 0) {
                                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflowRole"+role, "jahia:workflowRole"+role, CDATA, v.substring(1));
                                }
                            }
                        }
                    }
                    return;
                case WorkflowService.INHERITED:
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflow", "jahia:workflow", CDATA, "inherited");
                    return;
                case WorkflowService.LINKED:
                    attr.addAttribute(ImportExportService.JAHIA_URI, "workflow", "jahia:workflow", CDATA, "linked");
                    return;
            }
        }
    }


    public void exportAcl(JahiaBaseACL acl, String attrName, AttributesImpl attr, boolean top, boolean isUpdate) throws JahiaACLException {
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
            attr.addAttribute(ImportExportService.JAHIA_URI, attrName, "jahia:" + attrName, CDATA, perms);
        } else if (isUpdate) {
            attr.addAttribute(ImportExportService.JAHIA_URI, attrName, "jahia:" + attrName, "CDATA", "none");
        }
    }


}
