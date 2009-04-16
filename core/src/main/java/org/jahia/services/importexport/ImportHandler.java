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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.content.*;
import org.jahia.data.containers.*;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.*;
import org.jahia.data.files.JahiaFileField;
import org.jahia.data.templates.JahiaTemplateDef;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateNotFoundException;
import org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider;
import org.jahia.hibernate.manager.*;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaACLEntry;
import org.jahia.services.acl.JahiaACLException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.categories.Category;
import org.jahia.services.categories.CategoryService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.fields.*;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockRegistry;
import org.jahia.services.lock.LockService;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.pages.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.timebasedpublishing.*;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.*;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.webdav.JahiaWebdavBaseService;
import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.services.workflow.WorkflowInfo;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.ExternalWorkflow;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.engines.EngineMessage;
import org.jahia.security.license.LicenseActionChecker;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.security.Principal;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 19 avr. 2005
 * Time: 18:31:36
 * To change this template use File | Settings | File Templates.
 */
public class ImportHandler extends DefaultHandler {
    private static Logger logger = Logger.getLogger(ImportHandler.class);

    protected ContentObject currentObject;
    protected ProcessingContext jParams;
    protected Stack<ContentObject> objects;
    protected Map<String,ContentObject> objectMap;
    protected Map<String,String> uuidMapping;
    protected Map<ACLResourceInterface, List<String>> links;
    protected List<Object[]> jahiaLinks;
    protected String language;
    protected Map<ContentObject, Integer> containerIndex;
    protected ContentObject lastObject;
    protected ContentPage lastPage;
    protected boolean updateOnly = false;
    protected EntryLoadRequest elr;
    protected Locale oldLocale;
    protected EntryLoadRequest oldElr;
    protected JahiaSite site;
    protected ContentObject topObjectWithAclChanged;
    protected String topAcl;
    protected boolean restoreAcl = true;
    protected List<ImportAction> actions;
    protected ExtendedImportResult result;
    protected boolean copyUuid = false;
    protected boolean copyReadAccessOnly = false;
    protected int count = 0;
    protected Map<String,String> pathMapping;
    private Locator locator;

    private Map<String, Map<String,String>> typeMappings;
    private Map<String, String> templateMappings;
    private Map<String, String> importedMappings;

    public static final String JAHIA_LINK = "jahiaLink";

    public ImportHandler(ContentObject root, ProcessingContext jParams, String language, List<ImportAction> actions, ExtendedImportResult result) {
        this(root, jParams, language, jParams.getSite(), actions, result);
    }

    public ImportHandler(ContentObject root, ProcessingContext jParams, String language, JahiaSite site, List<ImportAction> actions, ExtendedImportResult result) {
        this.jParams = jParams;
        this.objects = new Stack<ContentObject>();
        this.currentObject = root;
        objectMap = new HashMap<String,ContentObject>();
        uuidMapping = new HashMap<String,String>();
        links = new HashMap<ACLResourceInterface, List<String>>();
        jahiaLinks = new ArrayList<Object[]>();
        containerIndex = new HashMap<ContentObject, Integer>();
        this.language = language;
        elr = new EntryLoadRequest(EntryLoadRequest.STAGED);
        elr.setFirstLocale(language);
        this.site = site;
        this.result = result;
        this.actions = actions;
        importedMappings = new HashMap<String,String>();
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }


    public void startDocument() throws SAXException {
        logger.info("Import starts ("+ language+")");
        oldLocale = jParams.getCurrentLocale();
        jParams.setAttribute("importMode", Boolean.TRUE);
        Locale currentLocale = LanguageCodeConverters.languageCodeToLocale(language);

        try {
            List<Locale> languageSettingsAsLocales = site.getLanguageSettingsAsLocales(false);

            if (!languageSettingsAsLocales.contains(currentLocale) && (currentObject == null || (updateOnly && currentObject.getParent(null)==null))) {
                // create site language
                if (LicenseActionChecker.isAuthorizedByLicense("org.jahia.actions.sites.*.admin.languages.ManageSiteLanguages",0) || languageSettingsAsLocales.isEmpty()) {
                    SiteLanguageSettings newLanguage =
                            new SiteLanguageSettings(site.getID(), currentLocale.toString(), true, languageSettingsAsLocales.size()+1, false);
                    JahiaSiteLanguageListManager listManager = (JahiaSiteLanguageListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageListManager.class.getName());
                    listManager.addSiteLanguageSettings(newLanguage);
                    languageSettingsAsLocales = site.getLanguageSettingsAsLocales(false);
                } else {
                    throw new SAXException("languages");
                }
            } else {
                while (!languageSettingsAsLocales.contains(currentLocale)) {
                    if (!"".equals(currentLocale.getVariant())) {
                        currentLocale = new Locale(currentLocale.getLanguage(), currentLocale.getCountry());
                    } else if (!"".equals(currentLocale.getCountry())) {
                        currentLocale = new Locale(currentLocale.getLanguage());
                    } else {
                        throw new SAXException("languages");
                    }
                }
            }
        } catch (JahiaException e) {
            //??
        }

        language = currentLocale.toString();

        jParams.setCurrentLocale(currentLocale);
        oldElr = jParams.getEntryLoadRequest();
        jParams.setEntryLoadRequest(elr);
    }

    public void endDocument() throws SAXException {
        try {
            // Create links
            JahiaFieldXRefManager fieldLinkManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());

            for (Iterator<ACLResourceInterface> iterator = links.keySet().iterator(); iterator.hasNext();) {
                Object key = iterator.next();
                List<String> refs = links.get(key);
                for (Iterator<String> iterator4 = refs.iterator(); iterator4.hasNext();) {
                    String ref = iterator4.next();
                    String simpleRef = ref;

//                    String origRef = null;
//                    if (ref.indexOf('/')>0) {
//                        origRef = ref.substring(ref.indexOf('/')+1);
//                        simpleRef = ref.substring(0, ref.indexOf('/'));
//                    }

//                    int objectID = 0;
                    String type = ContentPageKey.PAGE_TYPE;
                    if (key instanceof JahiaField && !(key instanceof JahiaBigTextField)) {
                        type = ContentContainerKey.CONTAINER_TYPE;
                    }
                    ContentObject co = getReferenceObject(type,simpleRef);
                    int objectID = co != null ? co.getID() : 0;
                    if (objectID == 0) {
                        if (key instanceof JahiaPage) {
                            int pageId = ((JahiaPage)key).getParentID();
                            result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
                            logger.warn("Page link " + ref + ", in jahia link on page " + pageId + " has not been found in import");
                            final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.linkNotFound", new Object[] {ref, ""+pageId, language, ""+ getLineNumber()});
                            result.appendWarning(new NodeImportResult(((JahiaPage)key).getContentPage().getObjectKey(), language, msg, null,null,null,null,null));
                        } else if (key instanceof JahiaBigTextField) {
                            int pageId = ((JahiaBigTextField)key).getPageID();
                            result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
                            logger.warn("Page link " + ref + ", in bigtext " + ((JahiaBigTextField) key).getID() + " on page " + pageId + " has not been found in import");
                            final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.linkNotFoundInBigText", new Object[] {ref, ""+((JahiaBigTextField) key).getID(), ""+pageId, language, ""+ getLineNumber()});
                            result.appendWarning(new NodeImportResult(((JahiaBigTextField)key).getContentField().getObjectKey(), language, msg, null,null,null,null,null));
                        }
                    }
                    if (key instanceof JahiaPage) {
                        JahiaPage jahiaPage = (JahiaPage) key;
                        try {
                            if (objectID != 0) {
                                jahiaPage.setPageType(JahiaPage.TYPE_LINK);
                                jahiaPage.setPageLinkID(objectID);
                                jahiaPage.commitChanges(true, jParams.getUser());
                            } else {
                                JahiaField jahiaField = ((ContentField)jahiaPage.getContentPage().getParent(EntryLoadRequest.STAGED)).getJahiaField(elr);
                                jahiaField.setValue(null);
                                jahiaField.setObject(null);
                                ServicesRegistry.getInstance ().getJahiaFieldService ().saveField (jahiaField, jahiaField.getACL().getParent().getID(), jParams);

                                Set<String> langs = new HashSet<String>();
                                langs.add(language);
                                langs.add(ContentObject.SHARED_LANGUAGE);
                                StateModificationContext stateModifContext = new StateModificationContext(jahiaPage.getContentPage().getObjectKey(), langs, true);
                                jahiaPage.getContentPage().markLanguageForDeletion(jParams.getUser(), language, stateModifContext);
                            }
                        } catch (Exception e) {
                            logger.warn("Error when trying to create jahia link for "+ref);
                        }
                    } else if (key instanceof JahiaBigTextField) {
                        JahiaBigTextField btf = (JahiaBigTextField) key;
                        if (objectID != 0) {
                            String v = btf.getValue();
                            v = StringUtils.replace(v,"/ref/"+ref,"/pid/"+objectID);
                            btf.setValue(v);
                            btf.setRawValue(v);
                            ServicesRegistry.getInstance ().getJahiaFieldService ().saveField (btf, btf.getAclID (), jParams);
//                            fieldLinkManager.createFieldReference(btf.getID(), site.getID(), language, EntryLoadRequest.STAGING_WORKFLOW_STATE, JahiaFieldXRefManager.PAGE+objectID);
                        } else {
                            fieldLinkManager.createFieldReference(btf.getID(), site.getID(), language, EntryLoadRequest.STAGING_WORKFLOW_STATE, JahiaFieldXRefManager.IMPORTED_PAGE+ref);
                        }
                    } else if (key instanceof JahiaField) {
                        JahiaField f = ((JahiaField) key);
                        String v = f.getValue();
                        if (v.startsWith("<jahia-expression") && v.endsWith("/>")) {
                            Pattern p = Pattern.compile("(.*)getContainerByUUID\\(\"([^\"]*)\"\\)(.*)");
                            Matcher m = p.matcher(v);
                            while (m.matches()) {
                                String replacement = m.group(1) + "getContainerByID(" + objectID + ")" + m.group(3);
                                m.reset();
                                v = m.replaceFirst(replacement);
                                m = p.matcher(v);
                            }

                            f.setValue(v);
                            ServicesRegistry.getInstance ().getJahiaFieldService ().saveField (f, f.getAclID (), jParams);
                        }
                    }
                }
            }

            // restoring old links
            for (Iterator<String> iterator = objectMap.keySet().iterator(); iterator.hasNext();) {
                String originalUuid = iterator.next();
                ContentObject object = objectMap.get(originalUuid);
                if (object instanceof ContentPage) {
                    List<JahiaFieldXRef> refs = fieldLinkManager.getReferencesForTarget(JahiaFieldXRefManager.IMPORTED_PAGE+originalUuid);
                    for (Iterator<JahiaFieldXRef> iterator1 = refs.iterator(); iterator1.hasNext();) {
                        JahiaFieldXRef xref = (JahiaFieldXRef) iterator1.next();
                        ContentField field = ContentField.getField(xref.getComp_id().getFieldId());
                        if (field instanceof ContentBigTextField) {
                            String bigText = ServicesRegistry.getInstance().getJahiaTextFileService().loadBigTextValue(site.getID(), field.getPageID(),
                                    field.getID(), "", 0, xref.getComp_id().getWorkflow(), xref.getComp_id().getLanguage());
                            bigText = bigText.replace("/ref/"+originalUuid, "/pid/"+object.getID());
                            ServicesRegistry.getInstance().getJahiaTextFileService().saveContents(site.getID(), field.getPageID(),
                                    field.getID(), bigText, 0, xref.getComp_id().getWorkflow(), xref.getComp_id().getLanguage());
//                            fieldLinkManager.createFieldReference(field.getID(), site.getID(), xref.getComp_id().getLanguage(), xref.getComp_id().getWorkflow(), JahiaFieldXRefManager.PAGE+object.getID());
                            fieldLinkManager.deleteFieldReference(xref);
                        }
                    }
                }
            }

            for (Iterator<Object[]> iterator = jahiaLinks.iterator(); iterator.hasNext();) {
                Object[] links = iterator.next();
                ObjectKey rightKey = (ObjectKey) links[0];
                String leftRef = (String) links[1];
                ObjectKey leftKey = null;
                String type  = (String) links[2];

                ContentObject referenceObject = getReferenceObject(rightKey.getType(), leftRef);
                if (referenceObject != null) {
                    leftKey = referenceObject.getObjectKey();
                }
                if (leftKey != null) {
                    List<ObjectLink> l = ObjectLink.findByTypeAndLeftAndRightObjectKeys(type, leftKey, rightKey);
                    if (l.isEmpty()) {
                        ObjectLink.createLink(leftKey, rightKey, type,
                                new HashMap<String, String>());
                    }
                } else {
                    try {
                        ContentObject object = ContentObject.getContentObjectInstance(rightKey);
                        String t = object.getProperty("tempLink");
                        if ( t == null ) {
                            object.setProperty("tempLink", type);
                        } else {
                            object.setProperty("tempLink", t + " " + type);
                        }

                        object.setProperty("tempLink_"+type, leftRef);
                    } catch (ClassNotFoundException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            // restoring old links

            try {
                restoreOldJahiaLink(ServicesRegistry.getInstance().getJahiaPageService().getPagePropertiesByName("tempLink"), ContentPageKey.PAGE_TYPE);
                restoreOldJahiaLink(ServicesRegistry.getInstance().getJahiaContainersService().getContainerPropertiesByName("tempLink"), ContentContainerKey.CONTAINER_TYPE);
                restoreOldJahiaLink(ServicesRegistry.getInstance().getJahiaContainersService().getContainerListPropertiesByName("tempLink"), ContentContainerListKey.CONTAINERLIST_TYPE);
                restoreOldJahiaLink(ServicesRegistry.getInstance().getJahiaFieldService().getFieldPropertiesByName("tempLink"), ContentFieldKey.FIELD_TYPE);
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }

            jParams.setCurrentLocale(oldLocale);
            jParams.setEntryLoadRequest(oldElr);
        } catch (JahiaException e) {
            throw new SAXException(e);
        }
        if (count == 0 && result.getStatus() == TreeOperationResult.PARTIAL_OPERATION_STATUS) {
            result.setStatus(TreeOperationResult.FAILED_OPERATION_STATUS);
        }
        logger.info("Imported "+count+ " elements");
    }

    private void restoreOldJahiaLink(List<Object[]> pages, String type) throws ClassNotFoundException, JahiaException {
        for (Iterator<Object[]> iterator = pages.iterator(); iterator.hasNext();) {
            Object[] o = (Object[]) iterator.next();
            int id = (Integer) o[0];
            String value = (String) o[2];

            ObjectKey rightKey = ContentObjectKey.getInstance(type + "_" + id);
            ContentObject obj = (ContentObject) ContentObject.getInstance(rightKey);
            StringTokenizer st = new StringTokenizer(value," ");
            String newValue = "";
            while (st.hasMoreTokens()) {
                String linkType = st.nextToken();
                String ref = obj.getProperty("tempLink_"+linkType);
                if (objectMap.containsKey(ref)) {
                    ObjectKey leftKey = objectMap.get(ref).getObjectKey();
                    List<ObjectLink> l = ObjectLink.findByTypeAndLeftAndRightObjectKeys(linkType, rightKey, leftKey);
                    if (l.isEmpty()) {
                        ObjectLink.createLink(leftKey, rightKey, linkType, new HashMap<String, String>());
                    }
                    obj.removeProperty("tempLink_"+linkType);
                } else {
                    newValue += linkType + " ";
                }
            }
            newValue = newValue.trim();
            if (!newValue.equals(value)) {
                if (newValue.length() ==0) {
                    obj.removeProperty("tempLink");
                } else {
                    obj.setProperty("tempLink", newValue);
                }
            }
        }
    }

    private ContentObject getReferenceObject(String type, String simpleRef) throws JahiaException {
        ContentObject co = null;
        if (objectMap.containsKey(simpleRef)) {
            co = objectMap.get(simpleRef);
        } else {
            JahiaPageService jahiaPageService = ServicesRegistry.getInstance().getJahiaPageService();
            List<? extends ContentObject> withOriginalUuid = getObjectByPropertyNameAndValue(type, "originalUuid", simpleRef);

            if (lastPage != null) {
                boolean ok = false;
                for (Iterator<? extends ContentObject> iterator1 = withOriginalUuid.iterator(); iterator1.hasNext() && !ok;) {
                    co = iterator1.next();

                    if (co.getSiteID() == site.getID()) {
                        // check if this one is in our path
                        List<ContentPage> path = jahiaPageService.getContentPagePath(co.getPageID(), jParams);
                        for (Iterator<ContentPage> iterator2 = path.iterator(); iterator2.hasNext();) {
                            ContentPage current = iterator2.next();
                            if (current.getID() == lastPage.getPageID()) {
                                ok = true;
                                break;
                            }
                        }
                    }
                }
                if (!ok) {
                    co = null;
                }
            }
            if (co == null) {
                List<? extends ContentObject> withUuid = getObjectByPropertyNameAndValue(type, "uuid", simpleRef);

                for (Iterator<? extends ContentObject> iterator1 = withUuid.iterator(); iterator1.hasNext();) {
                    ContentObject cp = (ContentObject) iterator1.next();
                    if (cp.getSiteID() == site.getID()) {
                        co = cp;
                        break;
                    }

                }
                if (co == null && withUuid.size() > 0) {
                    co = (ContentObject) withUuid.iterator().next();
                }
            }

            if (co == null && withOriginalUuid.size() > 0) {
                co = (ContentObject) withOriginalUuid.iterator().next();
            }

        }
        return co;
    }

    private List<? extends ContentObject> getObjectByPropertyNameAndValue(String type, String propertyName, String propertyValue) throws JahiaException {
        List<? extends ContentObject> withOriginalUuid;
        if (type.equals(ContentContainerKey.CONTAINER_TYPE)) {
            JahiaContainersService containerService = ServicesRegistry.getInstance().getJahiaContainersService();
            withOriginalUuid = containerService.findContainersByPropertyNameAndValue(propertyName, propertyValue);
        } else if (type.equals(ContentContainerListKey.CONTAINERLIST_TYPE)) {
            JahiaContainersService containerService = ServicesRegistry.getInstance().getJahiaContainersService();
            withOriginalUuid = containerService.findContainerListsByPropertyNameAndValue(propertyName, propertyValue);
        } else if (type.equals(ContentPageKey.PAGE_TYPE)) {
            JahiaPageService jahiaPageService = ServicesRegistry.getInstance().getJahiaPageService();
            withOriginalUuid = jahiaPageService.findPagesByPropertyNameAndValue(propertyName, propertyValue);
        } else if (type.equals(ContentFieldKey.FIELD_TYPE)) {
            JahiaFieldService fieldService = ServicesRegistry.getInstance().getJahiaFieldService();
            withOriginalUuid = fieldService.findFieldsByPropertyNameAndValue(propertyName, propertyValue);
        } else {
            withOriginalUuid = new ArrayList<ContentObject>();
        }
        return withOriginalUuid;
    }

    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) throws SAXException {
        logger.debug("Importing {" + namespaceURI + "}" + localName + " " + atts);

        int op = VersioningDifferenceStatus.ADDED;

        if (localName.equals("empty") && namespaceURI.equals(ImportExportBaseService.JAHIA_URI)) {
            result.setStatus(TreeOperationResult.FAILED_OPERATION_STATUS);
            final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.empty", new Object[] {language});
            result.appendError(new NodeImportResult(null, language, msg, localName, namespaceURI, qName, atts, null));

            JahiaPageService pageService = ServicesRegistry.getInstance().getJahiaPageService();
            try {
                JahiaPage page = pageService.createPage(site.getID(),
                        0,
                        PageInfoInterface.TYPE_DIRECT,
                        "Welcome to " + site.getServerName(),
                        site.getDefaultTemplateID(),
                        "http://",
                        -1,
                        jParams.getUser().getUserKey(),
                        site.getAclID(),
                        jParams);
                objects.push(page.getContentPage());

                String templateType = atts.getValue("jahia:template-type");
                if (templateType != null) {
                    importedMappings.put(page.getContentPage().getObjectKey().toString(), templateType);
                }
            } catch (JahiaException e) {
                logger.error("Cannot create default home page",e);
                objects.push(null);
            }
            jParams.resetSubstituteEntryLoadRequest();

            return;
        }
        try {
            String uuid = atts.getValue(ImportExportBaseService.JCR_URI, "uuid");
//            String originalUuid = atts.getValue(ImportExportBaseService.JAHIA_URI, "originalUuid");
            String diff = atts.getValue(ImportExportBaseService.JAHIA_URI, "diff");
            if (ImportExportService.UNCHANGED_STATUS.equals(diff)) {
                op = VersioningDifferenceStatus.UNCHANGED;
            } else if (ImportExportService.ADDED_STATUS.equals(diff)) {
                op = VersioningDifferenceStatus.ADDED;
            } else if (ImportExportService.UPDATED_STATUS.equals(diff)) {
                op = VersioningDifferenceStatus.TO_BE_UPDATED;
            } else if (ImportExportService.REMOVED_STATUS.equals(diff)) {
                op = VersioningDifferenceStatus.TO_BE_REMOVED;
            }
            if (updateOnly && op != VersioningDifferenceStatus.UNCHANGED && op != VersioningDifferenceStatus.TO_BE_REMOVED) {
                op = VersioningDifferenceStatus.TO_BE_UPDATED;
            }
            if (diff != null) {
                logger.debug("diff = " + diff);
            }
            if (op == VersioningDifferenceStatus.ADDED) {
                boolean top = false;
                if (objects.empty()) {
                    objects.push(currentObject);
                    top = true;
                }
                ContentObject parent = objects.peek();
                if (parent != null) {
                    currentObject = createObject(parent, namespaceURI, localName, qName, atts);
                } else {
                    if (currentObject == null && site.getHomePageID() == -1) {
                        JahiaBaseACL jAcl = new JahiaBaseACL ();
                        jAcl.create (site.getAclID());

                        JahiaPage jahiaPage = createPage(localName, namespaceURI, qName, atts, 0, jAcl.getID(),null);
                        if (jahiaPage == null) {
                            return;
                        }
                        currentObject = jahiaPage.getContentPage();

                        WorkflowEvent theEvent = new WorkflowEvent (this, currentObject, jParams.getUser(), language, false);
                        ServicesRegistry.getInstance ().getJahiaEventService ().fireObjectChanged(theEvent);

                        setAcl(currentObject, atts);
                        setWF(currentObject, atts);
//                        if (originalUuid != null) {
//                            setOriginalUuid(currentObject, originalUuid);
//                        } else if (uuid != null) {
                            setOriginalUuid(currentObject, uuid);
//                        }
                        setMetadata(currentObject, atts);
                        setJahiaLinks(currentObject, atts);
                                    
                        actions.add(new ImportAction((ContentObjectKey) currentObject.getObjectKey(), language, "updated"));
                    }
                }
                if (top && currentObject != null) {
//                    topObjectWithAclChanged = currentObject;
//                    topAcl = atts.getValue(ImportExportBaseService.JAHIA_URI, "acl");
//                    JahiaBaseACL acl = currentObject.getACL();
//                    acl.setInheritance(1);
//                    acl.removeAllUserEntries();
//                    acl.removeAllGroupEntries();

                    // transfer lock from parent to current
                    LockKey lock;
                    if (parent != null) {
                        lock = LockKey.composeLockKey(LockKey.ADD_ACTION + "_" + parent.getObjectKey().getType(), parent.getID());
                    } else {
                        lock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_SITE", site.getID());
                    }
                                                            
                    // transfer lock from parent to current
                    LockService lockReg = ServicesRegistry.getInstance().getLockService();
                    List<Map<String, Serializable>> linfos = lockReg.getInfo(lock);
                    if (!linfos.isEmpty()) {
                        Map<String, Serializable> infos = linfos.iterator().next();
                        LockKey newlock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_" + currentObject.getObjectKey().getType(), currentObject.getID());
                        JahiaUser owner = (JahiaUser) infos.get(LockRegistry.OWNER);
                        String lockID = (String) infos.get(LockRegistry.ID);
                        lockReg.acquire(newlock, owner, lockID, (int) (((Long) infos.get(LockRegistry.TIMEOUT)).longValue()/1000));
                        lockReg.release(lock, owner, lockID);
                    }
                }
            } else {
                if (currentObject == null) {
                    ContentObject parent = objects.peek();
                    if (parent instanceof ContentPage || parent instanceof ContentContainer) {
                        int containerID = 0;
                        int pageID;
                        if (parent instanceof ContentContainer) {
                            containerID = parent.getID();
                            pageID = ((ContentContainer) parent).getPageID();
                        } else {
                            pageID = parent.getID();
                        }
                        currentObject = getOrCreateContainerListOrField(localName, namespaceURI, qName, atts, parent, pageID, containerID, false);
                    } else if (parent instanceof ContentContainerList) {
                        // Container inside a list
                        List<ContentObject> l = new ArrayList<ContentObject>(parent.getChilds(jParams.getUser(), elr));

                        Integer iIndex = containerIndex.get(parent);
                        if (iIndex == null) {
                            iIndex = new Integer(0);
                            containerIndex.put(parent, iIndex);
                        }

                        //System.out.println("------------------------>" +l);
                        ImportExportUtils.orderContainerList(l, jParams);
                        //System.out.println("------------------ordered------>" +l);

                        if (uuid != null) {
                            List rl = new ArrayList(l);
                            Collections.reverse(rl);
                            for (Iterator<ContentObject> iterator = rl.iterator(); iterator.hasNext();) {
                                ContentContainer contentContainer = (ContentContainer) iterator.next();
                                Map<Object, Object> p = contentContainer.getProperties();
//                                if (uuid.equals(p.get("originalUuid"))
//                                        || (originalUuid != null && (originalUuid.equals(p.get("uuid")) || originalUuid.equals(p.get("originalUuid"))))) {
                                if (uuid.equals(p.get("originalUuid"))) {
                                    currentObject = contentContainer;
                                    break;
                                }
                            }
                        } else {
                            try {
                                currentObject = l.get(iIndex.intValue());
                            } catch (Exception e) {
                                currentObject = null;
                            }
                        }
                        if (updateOnly && currentObject == null && op != VersioningDifferenceStatus.TO_BE_REMOVED) {
                            currentObject = createObject(parent, namespaceURI, localName, qName, atts);
                            l.add(iIndex, currentObject);
                        }

                        if (op != VersioningDifferenceStatus.UNCHANGED && currentObject != null) {
                            l.remove(currentObject);
                            l.add(iIndex, currentObject);
                            for (int i = 0 ; i < l.size() ; i++) {
                                ContentContainer c = (ContentContainer) l.get(i);
                                JahiaContainer jc = c.getJahiaContainer(jParams, elr);
                                if (jc != null && jc.getRank() != -l.size()+i) {
                                    jc.setRank(-l.size()+i);
                                    ServicesRegistry.getInstance ().getJahiaContainersService().saveContainerInfo (jc,-1,-1, jParams);
                                }
                            }
                        }

                        if (op == VersioningDifferenceStatus.TO_BE_REMOVED) {
                            containerIndex.put(parent, iIndex);
                        } else {
                            containerIndex.put(parent, iIndex+1);
                        }
                        
                        if (currentObject == null && op != VersioningDifferenceStatus.TO_BE_REMOVED) {
                            result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
                            final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.ctnNotFound", new Object[] {uuid, localName, language, ""+ getLineNumber()});
                            result.appendWarning(new NodeImportResult(null, language, msg, localName, namespaceURI, qName, atts, null));
                            logger.warn("Container not found, might have been deleted : "+localName + " (" + uuid + ")");
                        }
                    } else if (parent instanceof ContentPageField) {
                        // Page
                        ContentPageField theField = (ContentPageField) parent;
                        currentObject = theField.getContentPage(elr);
                        if (updateOnly && currentObject == null) {
                            currentObject = createObject(parent, namespaceURI, localName, qName, atts);
                        }
                    }
                }

                if (currentObject != null) {
                    if (op == VersioningDifferenceStatus.TO_BE_REMOVED) {
                        if (currentObject.getParent(null, EntryLoadRequest.STAGED, null) == null) {
                            // never remove home page, delete only its children
                            List<? extends ContentObject> c = currentObject.getChilds(null, EntryLoadRequest.STAGED);
                            for (Iterator<? extends ContentObject> iterator = c.iterator(); iterator.hasNext();) {
                                ContentObject contentObject = (ContentObject) iterator.next();
                                removeObject(contentObject);
                            }
                        } else {
                            removeObject(currentObject);
                        }
                        actions.add(new ImportAction((ContentObjectKey) currentObject.getObjectKey(), language, "removed"));

                    } else if (op == VersioningDifferenceStatus.TO_BE_UPDATED) {
                        update(currentObject, localName, atts);
                    }
                }
            }
            if (currentObject != null) {
                objectMap.put(uuid, currentObject);
                uuidMapping.put(uuid, currentObject.getUUID());
            } else {
//                ObjectKey parentKey = null;
//                if (!objects.isEmpty() && objects.peek() != null) {
//                    ContentObject parent = ((ContentObject) objects.peek());
//                }
                if (op != VersioningDifferenceStatus.TO_BE_REMOVED) {
                    ContentObjectKey key = null;
                    if (!objects.isEmpty() && objects.peek() != null) {
                        key = new ContentPageKey(objects.peek().getPageID());
                    }
                    result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
                    final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.notimported", new Object[] {uuid, localName, language, ""+ getLineNumber()});
                    result.appendWarning(new NodeImportResult(key, language, msg, localName, namespaceURI, qName, atts, null));
                    if (uuid != null) {
                        logger.warn("Not imported : "+localName + " (" + uuid + ")");
                    } else {
                        logger.warn("Not imported : "+localName);
                    }
                }
            }

//            String version = atts.getValue(ImportExportBaseService.JAHIA_URI, "lastImportedVersion");
//            if (version == null) {
//                version = atts.getValue(ImportExportBaseService.JAHIA_URI, "version");
//            }
            String version = atts.getValue(ImportExportBaseService.JAHIA_URI, "version");
            if (version != null) {
                if (currentObject != null && !currentObject.getEntryStates().isEmpty()) {
                    if (currentObject.isShared()) {
                        currentObject.setProperty("lastImportedVersion", version);
                    } else {
                        currentObject.setProperty("lastImportedVersion-"+language, version);
                    }
                    if (currentObject.getProperty("originalUuid") == null) {
//                        if (originalUuid != null) {
//                            setOriginalUuid(currentObject, originalUuid);
//                        } else if (uuid != null) {
                            setOriginalUuid(currentObject, uuid);
//                        }
                    }

                }
            }

            String linkkey = atts.getValue(ImportExportBaseService.JAHIA_URI, "linkkey");
            if (currentObject != null && linkkey != null && currentObject.getPickedObject() == null) {
                try {
                    ContentObject source = ContentObject.getContentObjectInstance(ContentObjectKey.getInstance(linkkey));
                    source.addPickerObject(jParams, currentObject, atts.getValue(ImportExportBaseService.JAHIA_URI,"linktype"));

                    ContentObject rootPicker = objects.size() == 1 ? currentObject : objects.get(1).getPickedObject();
                    if (!source.isAclSameAsParent() || rootPicker == currentObject) {
                        JahiaBaseACL jAcl = new JahiaBaseACL ();
                        jAcl.create (currentObject.getAclID(), source.getAclID());
                        currentObject.updateAclForChildren(jAcl.getID());
                    }

                    if (currentObject instanceof ContentContainerList) {
                        Map<Object, Object> p = ((ContentContainerList)source).getProperties();
                        for (Iterator<?> iterator = p.keySet().iterator(); iterator.hasNext();) {
                            String key = (String)iterator.next();
                            if (key.startsWith("view_field_acl_")) {
                                int i = Integer.parseInt((String) p.get(key));
                                if (i != source.getAclID()) {
                                    JahiaBaseACL jAcl = new JahiaBaseACL ();
                                    jAcl.create (currentObject.getAclID(), i);
                                    currentObject.setProperty(key, Integer.toString(jAcl.getID()));
                                }
                            }
                        }
                    }

                    String l = language;
                    if (currentObject.isShared()) {
                        l = ContentField.SHARED_LANGUAGE;
                    }
                    WorkflowEvent theEvent = new WorkflowEvent(this, currentObject, jParams.getUser(), l, false);
                    ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);
                } catch (ClassNotFoundException e) {
                    //...
                }
            }
        } catch (Exception e) {
            logger.error("Error when importing : ",e);
            result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
            final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.error", new Object[] {atts.getValue(ImportExportBaseService.JCR_URI, "uuid"),localName, "", language, ""+ getLineNumber()});
            result.appendError(new NodeImportResult(null, language, msg, localName, namespaceURI, qName, atts, e));
        } finally {
            objects.push(currentObject);
            if (currentObject != null) {
                if (op != VersioningDifferenceStatus.UNCHANGED) {
                    count++;
                }
                currentObject = null;
            }

            ServicesRegistry.getInstance().getCacheService().syncClusterNow();
            JahiaBatchingClusterCacheHibernateProvider.syncClusterNow();
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        lastObject = objects.pop();
        logger.debug("End element {" + namespaceURI + "}" + localName + " = " + lastObject);

        if (lastObject == topObjectWithAclChanged && lastObject != null && restoreAcl) {
            restoreAcl(topAcl, topObjectWithAclChanged);
        }
        
        if (objects.size() == 1) {
            if (lastObject instanceof ContentContainer) {
                try {
                    ContentContainerList theList = (ContentContainerList) lastObject.getParent(null);
                    if (theList != null) {
                        final Set<Integer> containerPageRefs = ContentContainerListsXRefManager.getInstance().
                                getAbsoluteContainerListPageIDs(theList.getID());
                        if (containerPageRefs != null) {
                            final Iterator<Integer> pageRefIDs = containerPageRefs.iterator();
                            while (pageRefIDs.hasNext()) {
                                final Integer curPageID = (Integer) pageRefIDs.next();
                            }
                        } else {
                            logger.debug("Why is cross ref list empty ?");
                        }
                    } else {
                        logger.debug(
                                "Couldn't retrieve parent containerList, why is that ?");
                    }
                } catch (JahiaException e) {
                    logger.error("Cannot flush html cache",e);
                }
            }
        }
    }

    public void restoreAcl(String aclString, ContentObject object) {
//        JahiaBaseACL acl = object.getACL();
//        try {
//            acl.setInheritance(0);
//            if (aclString != null) {
//                fillAcl(acl, aclString, true);
//            }
//        } catch (JahiaACLException e) {
//            logger.error("Cannot set user or group ACL entry !!", e);
//        }
//        try {
//            HtmlCache htmlCache = ServicesRegistry.getInstance().getCacheService().getHtmlCacheInstance();
//            if (htmlCache != null) {
//                htmlCache.invalidatePageEntries(Integer.toString(lastObject.getPageID()), EntryLoadRequest.STAGING_WORKFLOW_STATE);
//            }
//        } catch (JahiaInitializationException e) {
//        }
    }

    protected ContentObject createObject(ContentObject parent, String namespaceURI, String localName, String qName, Attributes atts) throws JahiaException {
        logger.debug("Create {" + namespaceURI + "}" + localName);
        ContentObject object = null;
        int pageID = 0;
        if (parent instanceof ContentPage) {
            int containerID = 0;
            pageID = parent.getID();
            if (ImportExportService.JAHIA_URI.equals(namespaceURI) && "page".equals(localName)) {
                // Wow, might be a new language entry for the root page
                object = parent;
                updatePage(((ContentPage)object), localName, atts);

                actions.add(new ImportAction((ContentObjectKey) object.getObjectKey(), language, "updated"));
            } else {
                object = getOrCreateContainerListOrField(localName, namespaceURI, qName, atts, parent, pageID, containerID, true);
            }
        } else if (parent instanceof ContentContainer) {
            int containerID = parent.getID();
            pageID = ((ContentContainer) parent).getPageID();
            object = getOrCreateContainerListOrField(localName, namespaceURI, qName, atts, parent, pageID, containerID, true);
        } else if (parent instanceof ContentContainerList) {

            boolean isSingleContainer = false;
            try {
                JahiaContainerDefinition jcd = ((JahiaContainerDefinition) JahiaContainerDefinition.getInstance(parent.getDefinitionKey(null)));
                isSingleContainer = ((jcd.getContainerListType() & JahiaContainerDefinition.SINGLE_TYPE) != 0);

                String importedPt = atts.getValue(ImportExportService.JCR_URI, "primaryType");
                if (importedPt != null) {
                    localName = getMappedProperty(importedMappings.get(parent.getParent(null).getObjectKey().toString()), localName);

                    ExtendedNodeType importedListNodeType = null;
                    try {
                        importedListNodeType = NodeTypeRegistry.getInstance().getNodeType(importedPt);
                    } catch (NoSuchNodeTypeException e) {
                        if (localName.endsWith("List")) {
                            ExtendedNodeType listNt = jcd.getContainerListNodeDefinition().getRequiredPrimaryTypes()[0];
                            ExtendedNodeType ctnNt = listNt.getChildNodeDefinitionsAsMap().get("*").getRequiredPrimaryTypes()[0];
                            ExtendedNodeType importedNt = NodeTypeRegistry.getInstance().getNodeType(importedPt.substring(0, importedPt.length()-4));
                            if (ctnNt.isNodeType(importedNt.getName()) || importedNt.isNodeType(ctnNt.getName())) {
                                return parent;
                            }
                        } else {
                            throw e;
                        }
                    }

                    if (!ImportExportBaseService.getInstance().isCompatible(jcd, importedListNodeType, jParams)) {
                        if (importedListNodeType.isNodeType("jnt:containerList")) {
                            ExtendedNodeType listNt = jcd.getContainerListNodeDefinition().getRequiredPrimaryTypes()[0];
                            ExtendedNodeType ctnNt = listNt.getChildNodeDefinitionsAsMap().get("*").getRequiredPrimaryTypes()[0];
                            ExtendedNodeType importedNt = importedListNodeType.getChildNodeDefinitionsAsMap().get("*").getRequiredPrimaryTypes()[0];
                            if (ctnNt.isNodeType(importedNt.getName()) || importedNt.isNodeType(ctnNt.getName())) {
                                return parent;
                            }
                        }

                        result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
                        final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.incompatible", new Object[] {localName, language, ""+ getLineNumber()});
                        result.appendWarning(new NodeImportResult(null, language, msg, localName, namespaceURI, qName , atts, null));
                        return null;
                    }
                } else if (!localName.equals(jcd.getName())) {
                    result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
                    final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.incompatible", new Object[] {localName, language, ""+ getLineNumber()});
                    result.appendWarning(new NodeImportResult(null, language, msg, localName, namespaceURI, qName , atts, null));
                    return null;
                }
            } catch (NoSuchNodeTypeException e) {
                logger.error("Error when lloking nodetype",e);
            } catch (ClassNotFoundException e) {
            }

            if ( isSingleContainer ){
                List<? extends ContentObject> childIds = parent.getChilds(null,elr);
                if ( childIds.size()>0 ){
                    object = (ContentObject) childIds.iterator().next();
                }
            }
            // only create a new container if the container list is not single or if it's actually empty
            if ( object == null ){
                // Container inside a list
                pageID = ((ContentContainerList)parent).getPageID();
                JahiaContentContainerFacade contentContainerFacade =
                        null;
                contentContainerFacade = new JahiaContentContainerFacade (0,
                        site.getID(),
                        pageID,
                        parent.getID(),
                        parent.getDefinitionID(null),
                        0,
                        jParams,
                        site.getLanguageSettingsAsLocales (false));
                int parentAclID = parent.getParent(elr).getAclID();
                int parentId = 0;
                if (parent.getParent(elr) instanceof ContentContainer) {
                    parentId = parent.getParent(elr).getID();
                }
                JahiaContainer container = contentContainerFacade.getContainer(elr,true);
                if (atts.getValue(ImportExportBaseService.JAHIA_URI, "diff")!=null) {
                    Integer iIndex = containerIndex.get(parent);

                    // SET
                    if (iIndex == null) {
                        iIndex = 0;
                    }

                    List<? extends ContentObject> l = parent.getChilds(jParams.getUser(), elr);
                    ImportExportUtils.orderContainerList(l, jParams);
                    int i = -(l.size()+1);
                    int rank;
                    rank = i + iIndex.intValue();
                    containerIndex.put(parent, iIndex+ 1);

                    for (Iterator<? extends ContentObject> iterator = l.iterator(); iterator.hasNext();) {
                        ContentContainer contentObject = (ContentContainer) iterator.next();
                        JahiaContainer jc = contentObject.getJahiaContainer(jParams, elr);
                        if (i == rank) {
                            i++;
                        } else {
                            jc.setRank(i++);
                            ServicesRegistry.getInstance ().getJahiaContainersService ().
                                    saveContainerInfo (jc, parentId, parentAclID, jParams);
                        }
                    }
                    container.setRank(rank);
                }

                ServicesRegistry.getInstance ().getJahiaContainersService ().
                        saveContainerInfo (container, parentId, parentAclID, jParams);
                object = container.getContentContainer();
                String type = atts.getValue(Constants.JCR_PRIMARYTYPE);
                if (type != null) {
                    importedMappings.put(object.getObjectKey().toString(), type);
                }
                actions.add(new ImportAction((ContentObjectKey) object.getObjectKey(), language, "created"));
            }
        } else if (parent instanceof ContentPageField) {
            // Page
            if (!namespaceURI.equals(ImportExportBaseService.JAHIA_URI)) {
                result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
                final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.incompatible", new Object[] {localName, language, ""+ getLineNumber()});
                result.appendWarning(new NodeImportResult(null, language, msg, localName, namespaceURI, qName , atts, null));
                return null;
            }
            ContentPageField theField = (ContentPageField) parent;
            pageID = theField.getPageID();


            JahiaPage jahiaPage = null;
            ContentPage cp;
            List<? extends ContentObject> l = parent.getChilds(null,null);

            if (l.size() == 1) {
                cp = (ContentPage) l.iterator().next();
                updatePage(cp, localName, atts);
                object = cp;
            } else {
                int parentAclID = theField.getAclID();
                jahiaPage = createPage(localName, namespaceURI, qName, atts, pageID, parentAclID,(JahiaPageField) ((ContentPageField) parent).getJahiaField(elr));

//                JahiaField jahiaField = ;
//                jahiaField.setValue(Integer.toString(jahiaPage.getID()));
//                jahiaField.setObject(jahiaPage);
//                ServicesRegistry.getInstance ().getJahiaFieldService ().
//                        saveField (jahiaField, parent.getACL().getID(), jParams);
                object = jahiaPage.getContentPage();

                WorkflowEvent theEvent = new WorkflowEvent (this, object, jParams.getUser(), language, false);
                ServicesRegistry.getInstance ().getJahiaEventService ().fireObjectChanged(theEvent);

                theEvent = new WorkflowEvent (this, ContentField.getField(parent.getID()), jParams.getUser(), language, false);
                ServicesRegistry.getInstance ().getJahiaEventService ().fireObjectChanged(theEvent);
            }
        }

        if (object != null) {
            setAcl(object, atts);
            setWF(object, atts);
            String uuid = atts.getValue(ImportExportService.JCR_URI, "uuid");
//            String originalUuid = atts.getValue(ImportExportService.JAHIA_URI, "originalUuid");
//            if (originalUuid != null) {
//                setOriginalUuid(object, originalUuid);
//            } else
            if (uuid != null) {
                setOriginalUuid(object, uuid);
            }
            setMetadata(object, atts);
            setJahiaLinks(object, atts);

            if (parent instanceof ContentContainerList) {
                // since we have made modifications concerning this page, let's flush
                // the content cache for all the users and browsers as well as all
                // pages that display this containerList...
                Set<Integer> containerPageRefs = ContentContainerListsXRefManager.
                        getInstance().getAbsoluteContainerListPageIDs(parent.getID());
                if (containerPageRefs != null) {
                    Iterator<Integer> pageRefIDs = containerPageRefs.iterator();
                    while (pageRefIDs.hasNext()) {
                        Integer curPageID = (Integer) pageRefIDs.next();
                    }
                } else {
                    logger.debug("Why is cross ref list empty ?");
                }
            }
        }
        return object;
    }

    public String getMappedTemplate(String name) {
        if (templateMappings != null) {
            String tpl = templateMappings.get(name);
            return tpl;
        } else {
            return name;
        }
    }

    public String getMappedProperty(String parentName, String name) {
        if (typeMappings != null) {
            Map<String,  String> m = typeMappings.get(parentName);
            if (m == null) {
                return null;
            }
            name = m.get(name);
            return name;
        } else {
            return name;
        }
    }

    private JahiaPage createPage(String localName, String namespaceURI, String qName, Attributes atts, int pageID, int parentAclID, JahiaPageField parentField) throws JahiaException {
        JahiaPage jahiaPage;
        String title = atts.getValue(ImportExportBaseService.JAHIA_URI, "title");
        if (namespaceURI.equals(ImportExportBaseService.JAHIA_URI)) {
            if (localName.equals("link")) {
                // Link to jahia page (do it later)
                jahiaPage = ServicesRegistry.getInstance().getJahiaPageService().
                        createPage(site.getID(), pageID,
                                JahiaPage.TYPE_URL, title ,
                                site.getDefaultTemplateID(),
                                "http://", -1, jParams.getUser().getUserKey(),
                                parentAclID, jParams, parentField);
                List<String> ls = new ArrayList<String>();
                links.put(jahiaPage, ls);
                ls.add(atts.getValue(ImportExportBaseService.JAHIA_URI, "reference"));
            } else if (localName.equals("url")) {
                // External link
                String value = atts.getValue(ImportExportBaseService.JAHIA_URI, "value");
                jahiaPage = ServicesRegistry.getInstance().getJahiaPageService().
                        createPage(site.getID(), pageID,
                                JahiaPage.TYPE_URL, title ,
                                site.getDefaultTemplateID(),
                                value, -1, jParams.getUser().getUserKey(),
                                parentAclID, jParams, parentField);
            } else if (localName.equals("page")) {
                JahiaPageTemplateService pageTemplateService = ServicesRegistry.getInstance().getJahiaPageTemplateService();
                // Direct page
                String template = atts.getValue(ImportExportBaseService.JAHIA_URI, "template");

                template = getMappedTemplate(template);
                if (template == null) {
                    return null;
                }
                JahiaPageDefinition jpd = null;
                try {
                    jpd = pageTemplateService.lookupPageTemplateByName(template, site.getID());
                } catch (JahiaTemplateNotFoundException ex) {
                    // ignore
                }
                if (jpd == null) {
                    // try to find template by display name --> backward compatibility to Jahia < 5.1 
                    JahiaTemplatesPackage templSet = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(site.getID());
                    if (templSet != null) {
                        for (JahiaTemplateDef templDef : (List<JahiaTemplateDef>)templSet.getTemplates()) {
                            if (template.equals(templDef.getDisplayName())) {
                                jpd = pageTemplateService.lookupPageTemplateByName(templDef.getName(), site.getID());
                                break;
                            }
                        }
                    }
                }
                
                if (jpd == null) {
                    logger.warn("Template with the name '" + template
                            + "' not found for site '" + site.getTitle()
                            + "'. Using default.");
                    jpd = pageTemplateService.lookupPageTemplate(site.getDefaultTemplateID());

                    result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
                    final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.template", new Object[] {atts.getValue(ImportExportBaseService.JCR_URI, "uuid"), template, language, ""+ getLineNumber()});
                    result.appendWarning(new NodeImportResult(null, language, msg, localName, namespaceURI, null,atts,null));
                }
                jahiaPage = ServicesRegistry.getInstance().getJahiaPageService().
                        createPage(site.getID(),
                                pageID,
                                JahiaPage.TYPE_DIRECT, // or create a new page per default
                                title ,
                                jpd.getID(), // Page default template
                                "http://", // URL undefined
                                -1, // Link ID undefined
                                jParams.getUser().getUserKey(),
                                parentAclID,
                                jParams,
                                parentField); // value should be < 0 if new field.

                String templateType = atts.getValue("jahia:template-type");
                if (templateType != null) {
                    importedMappings.put(jahiaPage.getContentPage().getObjectKey().toString(), templateType);
                }

                String pageKey = atts.getValue(ImportExportBaseService.JAHIA_URI, "pageKey");
                setPageKey(pageKey, jahiaPage.getContentPage());

                String oldPid = atts.getValue(ImportExportBaseService.JAHIA_URI, "pid");
                if (oldPid != null) {
                    result.addPidMapping(Integer.parseInt(oldPid), jahiaPage.getID());
                }

                actions.add(new ImportAction((ContentObjectKey) jahiaPage.getContentPage().getObjectKey(), language, "created"));
            } else {
                result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
                final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.incompatible", new Object[] {localName, language, ""+ getLineNumber()});
                result.appendWarning(new NodeImportResult(null, language, msg, localName, namespaceURI, qName , atts, null));
                return null;
            }
        } else {
            result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
            final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.incompatible", new Object[] {localName, language, ""+ getLineNumber()});
            result.appendWarning(new NodeImportResult(null, language, msg, localName, namespaceURI, qName , atts, null));
            return null;
        }
        return jahiaPage;
    }

    private ContentObject getOrCreateContainerListOrField(String localName, String namespaceURI, String qName, Attributes atts, ContentObject parent, int pageID, int containerID, boolean updateIfExists) throws JahiaException {
        ContentObject object = null;

        boolean isContainerList = false;
        boolean isField = false;

        int ctnDefId = 0;
        int pageDefId = 0;
        String defPrefix = "";
        if (parent instanceof ContentContainer) {
            ctnDefId = parent.getDefinitionID(elr);
            pageDefId = ContentPage.getPage(parent.getPageID()).getDefinitionID(elr);
            try {
                defPrefix = ((ContentDefinition)ContentDefinition.getInstance(((ContentContainer)parent).getDefinitionKey())).getName();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            pageDefId = parent.getDefinitionID(elr);
            String pageType = ServicesRegistry.getInstance().getJahiaPageTemplateService().lookupPageTemplate(pageDefId).getPageType();
            defPrefix =  pageType.replace(":","_");
        }


        ContentDefinition def = null;

//        if (typeMappings != null) {
//            if (localName.endsWith("List")) {
//                localName = getMappedProperty(importedMappings.get(parent.getObjectKey().toString()), localName.substring(0, localName.length()-4));
//                if (localName == null) {
//                    return null;
//                }
//                localName += "List";
//            } else {
//                localName = getMappedProperty(importedMappings.get(parent.getObjectKey().toString()), localName);
//                if (localName == null) {
//                    return null;
//                }
//            }
//        }

        if (localName.endsWith("List")) {
            def = JahiaContainerDefinitionsRegistry.getInstance().getDefinition(site.getID(), defPrefix+"_"+localName.substring(0,localName.length()-4));
            if (def != null) {
                isContainerList = true;
            }
        } else {
            def = JahiaFieldDefinitionsRegistry.getInstance().getDefinition(site.getID(), defPrefix+"_"+localName);
            if (def != null) {
                isField = true;
            }
        }

        // Field or containerlist inside a container or page
        if (isContainerList) {
            ContentContainerList cc = null;
            JahiaContainerList containerList = null;
            if (parent instanceof ContentContainer) {
                containerList = ((ContentContainer) parent).getJahiaContainer(jParams, elr).getContainerList((JahiaContainerDefinition) def);
            } else {
                int id = ServicesRegistry.getInstance().getJahiaContainersService().getContainerListID(def.getName(), pageID);
                if (id != -1) {
                    try {
                        containerList = ServicesRegistry.getInstance().getJahiaContainersService().loadContainerList(id, LoadFlags.ALL, jParams);
                    } catch (JahiaException e) {
                    }
                }
            }

            if (containerList != null) {
                cc = containerList.getContentContainerList();
            }

            if (cc == null) {
                // ContainerList definition
                containerList = new JahiaContainerList (0, containerID, pageID, ((JahiaContainerDefinition)def).getID(), 0);
                ServicesRegistry.getInstance ().getJahiaContainersService ().
                        saveContainerListInfo (containerList, parent.getAclID(),jParams);
                cc = containerList.getContentContainerList();
            }
            object = cc;
            containerIndex.put(cc, new Integer(0));
            String type = atts.getValue(Constants.JCR_PRIMARYTYPE);
            if (type != null) {
                importedMappings.put(cc.getObjectKey().toString(), type);
            }
        } else if (isField) {
            JahiaFieldDefinition jfd = ((JahiaFieldDefinition)def);
            ContentField cf = null;
            if (parent instanceof ContentContainer) {
                List<? extends ContentObject> l = parent.getChilds(jParams.getUser(),elr, JahiaContainerStructure.JAHIA_FIELD);
                for (Iterator<? extends ContentObject> iterator = l.iterator(); iterator.hasNext();) {
                    ContentField contentObject = (ContentField) iterator.next();
                    if (contentObject.getDefinitionID(elr)==jfd.getID()) {
                        cf = contentObject;
                        break;
                    }
                }
            } else {
                int id = ServicesRegistry.getInstance().getJahiaFieldService().getFieldID(localName, pageID);
                cf = ContentField.getField(id);
            }

            if (cf == null) {
                if (jfd == null) {
                    result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
                    final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.incompatible", new Object[] {localName, language, ""+ getLineNumber()});
                    result.appendWarning(new NodeImportResult(null, language, msg, localName, namespaceURI, qName , atts, null));
                    return null;
                }
                // Field Definition
                String value = atts.getValue(ImportExportBaseService.JAHIA_URI, "value");
                int fieldType = jfd.getType();
                if (fieldType == FieldTypes.PAGE) {
                    value = "";
                }
                value = parseValue(fieldType, value);

                JahiaField field = ServicesRegistry.getInstance().getJahiaFieldService().createJahiaField(0,site.getID(),pageID,containerID,jfd.getID(),fieldType, 0, "", 0, 0, 0, EntryLoadRequest.STAGING_WORKFLOW_STATE, language);
                if (field.isShared()) {
                    field.setlanguageCode(ContentField.SHARED_LANGUAGE);
                } else {
                    field.setlanguageCode(language);
                }
                updateField(field, fieldType, value, atts, parent.getAclID ());

                ServicesRegistry.getInstance ().getJahiaFieldService ().saveField (field, parent.getAclID (), jParams);

                cf = field.getContentField();
            } else if (updateIfExists) {
                update(cf, localName, atts);
            }
            object = cf;
        } else {
            if (logger.isEnabledFor(Level.WARN)) {
                String tplName = null;
                String ctnName = null;

                try {
                    tplName =  ContentPage.getPage(pageID).getPageTemplate(elr).getName();
                    if (ctnDefId > 0) {
                        ctnName = JahiaContainerDefinitionsRegistry.getInstance().getDefinition(ctnDefId).getName();
                    }
                } catch (Exception e) {
                    logger.error("Cannot get definitions names",e);
                }
                ContentObjectKey key = null;
                if (parent != null) {
                    key = new ContentPageKey(parent.getPageID());
                }
                if (ctnName == null) {
                    logger.warn("Definitions not found : " +localName + " / " + parent.getObjectKey() + " (" + tplName + ")");

                    result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
                    final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.definition", new Object[] {atts.getValue(ImportExportBaseService.JCR_URI, "uuid"), localName, parent.getObjectKey(), tplName , language, ""+ getLineNumber()});
                    result.appendWarning(new NodeImportResult(key, language, msg, localName, namespaceURI, null,atts,null));
                } else {
                    logger.warn("Definitions not found : " +localName + " / " + parent.getObjectKey() + " (" + ctnName + " in " + tplName + ")");

                    result.setStatus(TreeOperationResult.PARTIAL_OPERATION_STATUS);
                    final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.definitionInCtn", new Object[] {atts.getValue(ImportExportBaseService.JCR_URI, "uuid"), localName, parent.getObjectKey(), tplName , ctnName, language, ""+ getLineNumber()});
                    result.appendWarning(new NodeImportResult(key, language, msg, localName, namespaceURI, null,atts,null));
                }
            }
        }

        return object;
    }

    protected void update(ContentObject object, String localName, Attributes atts) throws JahiaException {
        if (object instanceof ContentField) {
            ContentField cf = (ContentField) object;
            List<Locale> locales = new ArrayList<Locale>();
            locales.add(jParams.getCurrentLocale());
            JahiaContentFieldFacade jahiaContentFieldFacade = new JahiaContentFieldFacade (cf.getID(),LoadFlags.ALL,jParams,locales,true);
            JahiaField jahiaField = jahiaContentFieldFacade.getField(elr,true);
            if (jahiaField == null) {
                return;
            }
            int fieldType = cf.getType();
            if (fieldType != FieldTypes.PAGE) {
                String value = atts.getValue(ImportExportBaseService.JAHIA_URI, "value");
                value = parseValue(fieldType, value);
                updateField(jahiaField, fieldType, value, atts, -1);
                ServicesRegistry.getInstance ().getJahiaFieldService ().saveField (jahiaField, object.getAclID (), jParams);
            }
        } else if (object instanceof ContentPage) {
            ContentPage cp = (ContentPage) object;
            updatePage(cp, localName, atts);
            actions.add(new ImportAction((ContentObjectKey) currentObject.getObjectKey(), language, "updated"));
        } else if (object instanceof ContentContainer) {
            actions.add(new ImportAction((ContentObjectKey) currentObject.getObjectKey(), language, "updated"));
        }

        if (object != null) {
            setAcl(object, atts);
            setWF(object, atts);
            setMetadata(object, atts);
            setJahiaLinks(object, atts);

            String l = language;
            if (object.isShared()) {
                l = ContentField.SHARED_LANGUAGE;
            }
            WorkflowEvent theEvent = new WorkflowEvent(this, object, jParams.getUser(), l, false);
            ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);

            JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, object);
            ServicesRegistry.getInstance().getJahiaEventService().fireContentObjectUpdated(objectCreatedEvent);
        }
    }

    private void updateField(JahiaField field, int fieldType, String value, Attributes atts, int parentAclID) throws JahiaException {
        if (value == null) {
            field.setValue(null);
            return;
        }

        if (fieldType == FieldTypes.FILE) {
            if (pathMapping != null) {
                for (String map : pathMapping.keySet()) {
                    if (value.startsWith(map)) {
                        value = pathMapping.get(map) + value.substring(map.length());
                        break;
                    }
                }
            }
            JCRNodeWrapper object = JahiaWebdavBaseService.getInstance().getDAVFileAccess(value, jParams.getUser());
            JahiaFileField fField = object.getJahiaFileField();
            field.setValue(value);
            field.setObject(fField);
        } else if(fieldType == FieldTypes.APPLICATION) {
            if (pathMapping != null) {
                for (String map : pathMapping.keySet()) {
                    if (value.startsWith(map)) {
                        value = pathMapping.get(map) + value.substring(map.length());
                        break;
                    }
                }
            }
            try {
                JCRNodeWrapper object = JCRStoreService.getInstance().getFileNode(value, jParams.getUser());

                field.setValue(object.getUUID());
            } catch (RepositoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else if(fieldType == FieldTypes.BIGTEXT) {
            int i = -1;
            while ((i = value.indexOf(" href=", i+1)) > -1) {
                int j = value.indexOf("/ref/", i);
                int l = value.indexOf(" ", i+2);
                if (j!=-1 && (l==-1 || j<l)) {
                    StringBuffer refb = new StringBuffer();
                    int k = j+5;
//                    for (; i < value.length() && (Character.isLetterOrDigit(value.charAt(k)) || value.charAt(k)=='-' || value.charAt(k)=='/'); k++) {
                    for (; i < value.length() && (Character.isLetterOrDigit(value.charAt(k)) || value.charAt(k)=='-'); k++) {
                        refb.append(value.charAt(k));
                    }
                    List<String> ls = links.get(field);
                    if (ls == null ){
                        ls = new ArrayList<String>();
                        links.put(field, ls);
                    }
                    
                    String reference = refb.toString();
                    if (!ls.contains(reference)) {
                        ls.add(reference);
                    }
                }
            }
            String str = "###/webdav";
            if ((value.indexOf(str, i+1)) > -1) {
                StringBuffer b = new StringBuffer(value);
                i = -1;
                while ((i = b.indexOf(str, i+1)) > -1) {
                    b.insert(i+str.length(), "/site/"+site.getSiteKey());
                }
                value = b.toString();
            }
            String rawValue = JahiaBigTextField.rewriteURLs(value, jParams);
            field.setValue(rawValue);
            field.setRawValue(rawValue);
        } else if(fieldType == FieldTypes.DATE) {
            field.setValue(value);
            field.setObject(value);
        } else {
            if (value.startsWith("<jahia-expression") && value.endsWith("/>")) {
                Pattern p = Pattern.compile("(.*)getContainerByUUID\\(\"([^\"]*)\"\\)(.*)");
                Matcher m = p.matcher(value);
                List<String> ls = new ArrayList<String>();
                if (m.matches()) {
                    links.put(field, ls);
                    ls.add(m.group(2));
                }
            }
            field.setValue(value);
        }
    }

    private int updatePage(ContentPage cp, String localName, Attributes atts) throws JahiaException {
        int pageID;
        String title = atts.getValue(ImportExportBaseService.JAHIA_URI, "title");
        cp.setTitle(language, title, elr);

        if (localName.equals("link")) {
            // Link to jahia page (do it later)
            List<String> ls = new ArrayList<String>();
            links.put(cp.getPage(jParams), ls);
            ls.add(atts.getValue(ImportExportBaseService.JAHIA_URI, "reference"));
        } else if (localName.equals("url")) {
            // External link
            String value = atts.getValue(ImportExportBaseService.JAHIA_URI, "value");
            cp.setRemoteURL(value, elr);
        } else if ("page".equals(localName)) {
            String template = atts.getValue(ImportExportBaseService.JAHIA_URI, "template");
            JahiaPageTemplateService pageTemplateService = ServicesRegistry.getInstance().getJahiaPageTemplateService();
            JahiaPageDefinition jpd = null;
            JahiaException ex = null;
            try {
                jpd = pageTemplateService.lookupPageTemplateByName(template, site.getID());
            } catch (JahiaTemplateNotFoundException e) {
                ex = e;
            }
            if (jpd == null) {
                // try to find template by display name --> backward compatibility to Jahia < 5.1 
                JahiaTemplatesPackage templSet = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(site.getID());
                if (templSet != null) {
                    for (JahiaTemplateDef templDef : (List<JahiaTemplateDef>)templSet.getTemplates()) {
                        if (template.equals(templDef.getDisplayName())) {
                            jpd = pageTemplateService.lookupPageTemplateByName(templDef.getName(), site.getID());
                            break;
                        }
                    }
                }
            }
            
            if (jpd == null) {
                logger.warn("Template with the name '" + template
                        + "' not found for site '" + site.getTitle()
                        + "'. Using default.");
                throw ex != null ? ex : new JahiaTemplateNotFoundException(template);
            }

            cp.setPageTemplateID(jpd.getID(), elr);

            String pageKey = atts.getValue(ImportExportBaseService.JAHIA_URI, "pageKey");
            setPageKey(pageKey, cp);
        }
        if (atts.getValue(ImportExportBaseService.JAHIA_URI, "hideFromNavigationMenu") != null) {
            cp.setProperty(PageProperty.HIDE_FROM_NAVIGATION_MENU, "true");
        }
        cp.commitChanges(true,true,jParams.getUser());
        pageID = cp.getID();
        return pageID;
    }

    private void removeObject(ContentObject currentObject) throws JahiaException {
        Set<String> curLanguageCodes = new HashSet<String>();
        if (currentObject instanceof ContentContainer) {
            ContentContainer contentContainer = (ContentContainer) currentObject;
            curLanguageCodes.add(ContentObject.SHARED_LANGUAGE);

            StateModificationContext stateModifContext =
                    new StateModificationContext(currentObject.getObjectKey(), curLanguageCodes, true);
            stateModifContext.pushAllLanguages(true);

            contentContainer.markLanguageForDeletion(
                    jParams.getUser(),
                    ContentObject.SHARED_LANGUAGE,
                    stateModifContext);

        } else {
            Set<String> langs = new HashSet<String>();
            langs.add(language);
            langs.add(ContentObject.SHARED_LANGUAGE);
            StateModificationContext stateModifContext =
                    new StateModificationContext(currentObject.getObjectKey(), langs, true);
            currentObject.markLanguageForDeletion(jParams.getUser(), language, stateModifContext);
        }
    }

    protected void setPageKey(String pageKey, ContentPage cp) throws JahiaException {
        if (pageKey != null) {
            cp.setPageKey(pageKey);
        }
    }

    protected void setAcl(ContentObject object, Attributes atts) throws JahiaException {
        String acl = atts.getValue(ImportExportBaseService.JAHIA_URI, "acl");
        if (acl != null) {
            JahiaBaseACL jAcl;
            if (object.isAclSameAsParent()) {
                jAcl = new JahiaBaseACL ();
                jAcl.create (object.getAclID());
                object.updateAclForChildren(jAcl.getID());
            } else {
                jAcl = object.getACL();
            }
            fillAcl(jAcl, acl, false);
            JahiaEvent setRightsEvent = new JahiaEvent(this, jParams, jAcl);
            try {
                ServicesRegistry.getInstance ().getJahiaEventService ().fireSetRights(setRightsEvent);
            } catch (JahiaException e) {
                logger.error(e);
            }
        }

        if (object instanceof ContentContainerList) {
            for(int i = 0; i<atts.getLength(); i++) {
                if ( ImportExportBaseService.JAHIA_URI.equals(atts.getURI(i)) && atts.getLocalName(i).startsWith("view_field_acl_") ) {
                    JahiaBaseACL newAcl = null;
                    newAcl = new JahiaBaseACL ();
                    newAcl.create (object.getAclID ());
                    object.setProperty (atts.getLocalName(i), String.valueOf (newAcl.getID ()));
                    fillAcl(newAcl, atts.getValue(i), false);
                    JahiaEvent setRightsEvent = new JahiaEvent(this, jParams, newAcl);
                    try {
                        ServicesRegistry.getInstance().getJahiaEventService().fireSetRights(setRightsEvent);
                    } catch (JahiaException e) {
                        logger.error(e);
                    }
                }
            }
        }
    }

    private void fillAcl(JahiaBaseACL jAcl, String acl, boolean checkParent) {
        StringTokenizer st = new StringTokenizer(acl, "|");
        try {
            jAcl.removeAllGroupEntries();
            jAcl.removeAllUserEntries();

            while (st.hasMoreTokens())  {
                String ace = st.nextToken();
                if (ace.equals("break")) {
                    jAcl.setInheritance(1);
                } else if (ace.equals("none")) {
                    jAcl.setInheritance(0);
                } else {
                    int colonIndex = ace.lastIndexOf(":");
                    String perm = ace.substring(colonIndex+1);

                    JahiaAclEntry permissions = new JahiaAclEntry ();
                    permissions.setPermission (JahiaBaseACL.READ_RIGHTS, perm.charAt (0) == 'r' ?
                            JahiaACLEntry.ACL_YES :
                            JahiaACLEntry.ACL_NO);
                    if (copyReadAccessOnly) {
                        permissions.setPermission (JahiaBaseACL.WRITE_RIGHTS, JahiaACLEntry.ACL_NEUTRAL);
                        permissions.setPermission (JahiaBaseACL.ADMIN_RIGHTS, JahiaACLEntry.ACL_NEUTRAL);
                    } else {
                        permissions.setPermission (JahiaBaseACL.WRITE_RIGHTS, perm.charAt (1) == 'w' ?
                                JahiaACLEntry.ACL_YES :
                                JahiaACLEntry.ACL_NO);
                        permissions.setPermission (JahiaBaseACL.ADMIN_RIGHTS, perm.charAt (2) == 'a' ?
                                JahiaACLEntry.ACL_YES :
                                JahiaACLEntry.ACL_NO);
                    }
                    String principal = ace.substring(0, colonIndex);

                    String userName = principal.substring(2);
                    if (principal.charAt(0) == 'u') {
                        try {
                            JahiaUser user = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMember(site.getID(), userName);
                            if (user == null) {
                                user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userName);
                            }
                            boolean needed = false;
                            if (checkParent) {
                                needed |= (permissions.getPermission(JahiaBaseACL.READ_RIGHTS) != JahiaACLEntry.ACL_NEUTRAL) &&
                                        jAcl.getPermission(user, JahiaBaseACL.READ_RIGHTS) != (permissions.getPermission(JahiaBaseACL.READ_RIGHTS)==JahiaAclEntry.ACL_YES);
                                needed |= (permissions.getPermission(JahiaBaseACL.WRITE_RIGHTS) != JahiaACLEntry.ACL_NEUTRAL) &&
                                        jAcl.getPermission(user, JahiaBaseACL.WRITE_RIGHTS) != (permissions.getPermission(JahiaBaseACL.WRITE_RIGHTS)==JahiaAclEntry.ACL_YES);
                                needed |= (permissions.getPermission(JahiaBaseACL.ADMIN_RIGHTS) != JahiaACLEntry.ACL_NEUTRAL) &&
                                        jAcl.getPermission(user, JahiaBaseACL.ADMIN_RIGHTS) != (permissions.getPermission(JahiaBaseACL.ADMIN_RIGHTS)==JahiaAclEntry.ACL_YES);
                            }
                            if (user != null && (needed || !checkParent)) {
                                jAcl.setUserEntry (user, permissions);
                            }
                        } catch (JahiaException e) {
                        }
                    } else {
                        JahiaGroup group = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(site.getID(), userName);
                        if (group == null) {
                            group = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(userName);
                        }
                        boolean needed = false;
                        if (checkParent) {
                            needed |= (permissions.getPermission(JahiaBaseACL.READ_RIGHTS) != JahiaACLEntry.ACL_NEUTRAL) &&
                                    jAcl.getPermission(group, JahiaBaseACL.READ_RIGHTS) != (permissions.getPermission(JahiaBaseACL.READ_RIGHTS)==JahiaAclEntry.ACL_YES);
                            needed |= (permissions.getPermission(JahiaBaseACL.WRITE_RIGHTS) != JahiaACLEntry.ACL_NEUTRAL) &&
                                    jAcl.getPermission(group, JahiaBaseACL.WRITE_RIGHTS) != (permissions.getPermission(JahiaBaseACL.WRITE_RIGHTS)==JahiaAclEntry.ACL_YES);
                            needed |= (permissions.getPermission(JahiaBaseACL.ADMIN_RIGHTS) != JahiaACLEntry.ACL_NEUTRAL) &&
                                    jAcl.getPermission(group, JahiaBaseACL.ADMIN_RIGHTS) != (permissions.getPermission(JahiaBaseACL.ADMIN_RIGHTS)==JahiaAclEntry.ACL_YES);
                        }
                        if (group != null && (needed || !checkParent)) {
                            jAcl.setGroupEntry (group, permissions);
                        }
                    }
                }
            }
        } catch (JahiaACLException jae) {
            logger.error ("Cannot set user or group ACL entry !!", jae);
        }
    }

    protected void setWF(ContentObject object, Attributes atts) throws JahiaException {
        String wf = atts.getValue(ImportExportBaseService.JAHIA_URI, "workflow");
        if (wf != null) {
            WorkflowService service = ServicesRegistry.getInstance().getWorkflowService();
            if ("inactive".equals(wf)) {
                service.setWorkflowMode(object, WorkflowService.INACTIVE, null,null,jParams);
            } else if ("internal".equals(wf)) {
                WorkflowInfo wfInfo = ServicesRegistry.getInstance()
                        .getWorkflowService().getDefaultWorkflowEntry();
                if (WorkflowService.EXTERNAL == wfInfo.getMode()) {
                    service.setWorkflowMode(object, wfInfo.getMode(), wfInfo
                            .getWorkflowName(), wfInfo.getProcessId(), jParams);
                } else {
                    service
                            .setWorkflowMode(object,
                                    WorkflowService.JAHIA_INTERNAL, null, null,
                                    jParams);
                }
            } else if ("external".equals(wf)) {
                String name = atts.getValue(ImportExportService.JAHIA_URI, "workflowName");
                String process = atts.getValue(ImportExportService.JAHIA_URI, "workflowProcess");
                service.setWorkflowMode(object, WorkflowService.EXTERNAL, name, process, jParams);
                ExternalWorkflow workfow = service.getExternalWorkflow(name);
                Collection<String> roles = workfow.getAllActionRoles(process);
                for (Iterator<String> iterator = roles.iterator(); iterator.hasNext();) {
                    String role = (String) iterator.next();
                    String v = atts.getValue(ImportExportService.JAHIA_URI, "workflowRole"+role);
                    if (v != null )  {                        
                        JahiaGroup g = service.getRoleGroup(object, role, true);
                        StringTokenizer st = new StringTokenizer(v, "|");
                        while (st.hasMoreTokens())  {
                            String principal = st.nextToken();
                            String userName = principal.substring(2);
                            Principal p = null;
                            if (principal.charAt(0) == 'u') {
                                    p = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMember(site.getID(), userName);
                                    if (p == null) {
                                        p = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userName);
                                    }
                            } else {
                                p = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(site.getID(), userName);
                                if (p == null) {
                                    p = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(userName);

                                }
                            }
                            if (p != null ) {
                                g.addMember(p);
                            }
                        }
                    }
                }
            } else if ("inherited".equals(wf)) {
                service.setWorkflowMode(object, WorkflowService.INHERITED, null,null,jParams);
            } else if ("linked".equals(wf)) {
                service.setWorkflowMode(object, WorkflowService.LINKED, null,null,jParams);
            }
        }
    }

    private void setOriginalUuid(ContentObject object, String uuid) throws JahiaException {
        if (copyUuid) {
            object.setProperty("originalUuid",uuid);
        }
    }

    private Map<ObjectKey, Map<String, JahiaFieldDefinition>> metadataCache = new HashMap<ObjectKey, Map<String, JahiaFieldDefinition>>();

    private void setMetadata(ContentObject object, Attributes attr) {
        try {
            ObjectKey definitionKey = object.getDefinitionKey(null);
            Map<String, JahiaFieldDefinition> fieldDefs = metadataCache.get(definitionKey);

            if (fieldDefs == null) {
                ContentDefinition contentDefinition = ContentDefinition.getContentDefinitionInstance(definitionKey);
                List<? extends JahiaObject> metadataDefs = contentDefinition.getMetadataDefinitions();
                fieldDefs = new HashMap<String, JahiaFieldDefinition>();
                for (int i = 0; i < metadataDefs.size(); i++) {
                    JahiaObject jahiaObject = (JahiaObject)metadataDefs.get(i);
                    JahiaFieldDefinition fieldDef = (JahiaFieldDefinition)JahiaFieldDefinition.getContentDefinitionInstance(jahiaObject.getObjectKey());
                    fieldDefs.put(fieldDef.getName(),fieldDef);
                }
                metadataCache.put(definitionKey, fieldDefs);
            }

            Collection<String> md = fieldDefs.keySet();
            ObjectKey objectKey = object.getObjectKey();
            List<ContentField> metadatas = object.getMetadatas();
            Map<Integer, ContentField> metadataMap = new HashMap<Integer, ContentField>();
            for (Iterator<ContentField> iterator = metadatas.iterator(); iterator.hasNext();) {
                ContentField contentField = (ContentField) iterator.next();
                metadataMap.put(new Integer(contentField.getDefinitionID(null)), contentField);
            }
            JahiaSaveVersion saveVersion = new JahiaSaveVersion(false, false);
            for (Iterator<String> iterator = md.iterator(); iterator.hasNext();) {
                String metadataName = iterator.next();
                String value;
                if (CoreMetadataConstant.CREATION_DATE.equals(metadataName)) {
                    value = attr.getValue(ImportExportBaseService.JCR_URI, "created");
                } else if (CoreMetadataConstant.LAST_MODIFICATION_DATE.equals(metadataName)) {
                    value = attr.getValue(ImportExportBaseService.JCR_URI, "lastModified");
                } else if ("lastPublishingDate".equals(metadataName)) {
                    continue;
                } else if ("lastPublisher".equals(metadataName)) {
                    continue;
                } else if (CoreMetadataConstant.PAGE_PATH.equals(metadataName)) {
                    value = null;
                } else {
                    value = attr.getValue(ImportExportService.JAHIA_URI, metadataName);
                }
                if (value != null) {
                    JahiaFieldDefinition fieldDef = fieldDefs.get(metadataName);

                    String fieldValue = parseValue(fieldDef.getType(), value);
                    ContentField metadataContentField = metadataMap.get(new Integer(fieldDef.getID()));
                    if (metadataContentField == null) {
                        JahiaField field = ServicesRegistry.getInstance().getJahiaFieldService().createJahiaField(0, jParams.getSiteID(), 0,
                                0, fieldDef.getID(),fieldDef.getType(),0, fieldValue, 0, 0,saveVersion.getVersionID(),saveVersion.getWorkflowState(),language);
                        if (field != null) {
                            field.setIsMetadata(true);
                            field.setMetadataOwnerObjectKey(objectKey);
                            if (field.getType() == ContentFieldTypes.DATE) {
                                field.setObject(fieldValue);
                            }

                            // save the field
                            ServicesRegistry.getInstance().getJahiaFieldService().
                                saveField(field, object.getAclID(), jParams);
                        }
                    } else {
                        List<Locale> locales = new ArrayList<Locale>();
                        locales.add(jParams.getCurrentLocale());
                        JahiaContentFieldFacade jahiaContentFieldFacade = new JahiaContentFieldFacade (metadataContentField.getID(),LoadFlags.ALL,jParams,locales,true);
                        JahiaField jf = jahiaContentFieldFacade.getField(elr,true);
                        if (jf != null) {
                            updateField(jf,jf.getType(),fieldValue, null, -1);
                            jf.save(jParams);
                        }
                    }
                }
            }
            String cats = attr.getValue(ImportExportService.JAHIA_URI, "categories");
            if (cats == null || cats.length() == 0) {
                cats = attr.getValue(ImportExportService.JAHIA_URI, "defaultCategory");
            }
            CategoryService categoryService = ServicesRegistry.getInstance().getCategoryService();
            Set<Category> objectCats = categoryService.getObjectCategories(objectKey);

            for (Iterator<Category> iterator = objectCats.iterator(); iterator.hasNext();) {
                Category category = (Category) iterator.next();
                categoryService.removeObjectKeyFromCategory(category, objectKey);
            }
            if (cats != null && cats.length() > 0) {
                StringTokenizer st = new StringTokenizer(cats, "$$$");
                while (st.hasMoreTokens()) {
                    String catString = st.nextToken();
                    Category cat = categoryService.getCategory(catString);
                    if (cat != null) {
                        categoryService.addObjectKeyToCategory(cat, objectKey);
                    }
                }
            }

            if (!(object instanceof ContentField) && !(object instanceof ContentContainerList)) {
                String from = attr.getValue(ImportExportBaseService.JAHIA_URI, "validFrom");
                String to = attr.getValue(ImportExportBaseService.JAHIA_URI, "validTo");
                String ruleType = attr.getValue(ImportExportBaseService.JAHIA_URI, "ruleType");
                String ruleSettings = attr.getValue(ImportExportBaseService.JAHIA_URI, "ruleSettings");
                TimeBasedPublishingService timeBasedPublishingService = ServicesRegistry.getInstance().getTimeBasedPublishingService();
                RetentionRule rule = timeBasedPublishingService.getRetentionRule(objectKey);

                String linkkey = attr.getValue(ImportExportBaseService.JAHIA_URI, "linkkey");
                if (linkkey != null) {
                    rule = timeBasedPublishingService.getRetentionRule(ObjectKey.getInstance(linkkey));
                } else {
                    if ( rule == null && (ruleSettings != null || from != null || to != null) ) {
                        RetentionRuleDef baseDef = timeBasedPublishingService.getBaseRetentionRuleDef();
                        rule = baseDef.createRule();
                    }
                    if (from != null || to != null) {
                        if (from != null) {
                            ((RangeRetentionRule)rule).setValidFromDate(new Long(parseValue(ContentFieldTypes.DATE, from)));
                        } else {
                            ((RangeRetentionRule)rule).setValidFromDate(new Long(0));
                        }

                        if (to != null) {
                            ((RangeRetentionRule)rule).setValidToDate(new Long(parseValue(ContentFieldTypes.DATE, to)));
                        } else {
                            ((RangeRetentionRule)rule).setValidToDate(new Long(0));
                        }
                        rule.setInherited(Boolean.FALSE);
                        rule.setShared(Boolean.TRUE);
                    } else {
                        if (rule != null) {
                            rule.setInherited(Boolean.TRUE);
                        }
                    }
                    if ( ruleType != null && !"".equals(ruleType.trim()) ){
                        rule.setRuleType(ruleType);
                    }
                    if ( ruleSettings != null && !"".equals(ruleSettings.trim()) ){
                        BaseRetentionRule baseRule = (BaseRetentionRule)rule;
                        baseRule.loadSettings(ruleSettings);
                    }                        
                }
                try {
                    if (rule != null) {
                        JahiaObjectManager jahiaObjectMgr = (JahiaObjectManager) SpringContextSingleton
                                .getInstance().getContext().getBean(JahiaObjectManager.class.getName());
                        JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectMgr.getJahiaObjectDelegate(objectKey);
                        timeBasedPublishingService.saveRetentionRule(rule);
                        jahiaObjectDelegate.setRule(rule);
                        jahiaObjectDelegate.setObjectKey(objectKey);
                        jahiaObjectMgr.save(jahiaObjectDelegate);

                        try {
                            timeBasedPublishingService.scheduleBackgroundJob(objectKey,
                                    TimeBasedPublishingJob.UPDATE_OPERATION,rule,jParams);
                        } catch (Exception e) {
                            throw new JahiaException("Error scheduling Retention Rule at import",
                                    "Error scheduling Retention Rule at import",
                                    JahiaException.ENGINE_ERROR, JahiaException.ERROR_SEVERITY, e);
                        }
                        RetentionRuleEvent event = new RetentionRuleEvent(this, jParams,
                                rule.getId().intValue(),RetentionRuleEvent.UPDATING_RULE,-1);
                        ServicesRegistry.getInstance().getJahiaEventService().fireTimeBasedPublishingStateChange(event);
                        /*                        
                        rule.startJob();
                        */
                        /*
                        if ( rule.getRuleType() != RetentionRule.RULE_START_AND_END_DATE ){
                            rule.scheduleNextJob(jParams);
                        }*/
                    }
                } catch (Exception e) {
                    // !@!#!#$%
                    logger.error("Cannot set retention rule",e);
                }
            }

            String value = attr.getValue(ImportExportBaseService.JAHIA_URI, "sortHandler");
            if (value != null && !"".equals(value) && object instanceof ContentContainerList) {
                object.setProperty("automatic_sort_handler", value);
            }
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void setJahiaLinks(ContentObject object, Attributes atts) {
        for (int i=0; i<atts.getLength(); i++) {
            String n = atts.getLocalName(i);
            if (n.startsWith(JAHIA_LINK)) {
                String type = StringUtils.uncapitalize(n.substring(JAHIA_LINK.length()));
                String ref = atts.getValue(i);
                jahiaLinks.add(new Object[] {object.getObjectKey(), ref, type});
            }
        }
    }

    public String parseValue(int type, String value) {
        if (value == null) {
            return null;
        }
        switch (type) {
            case ContentFieldTypes.DATE:
                DateFormat df = new SimpleDateFormat(ImportExportService.DATE_FORMAT);
                try {
                    Date d = df.parse(value);
                    return Long.toString(d.getTime());
                } catch (ParseException e) {
                    return null;
                }
            default:
                return value;
        }
    }

    private int getLineNumber() {
        if (locator != null) {
            return locator.getLineNumber();
        } else {
            return -1;
        }
    }

    public void setUpdateOnly(boolean updateOnly) {
        this.updateOnly = updateOnly;
    }

    public void setCopyUuid(boolean copyUuid) {
        this.copyUuid = copyUuid;
    }

    public void setCopyReadAccessOnly(boolean copyReadAccessOnly) {
        this.copyReadAccessOnly = copyReadAccessOnly;
    }

    public void setRestoreAcl(boolean restoreAcl) {
        this.restoreAcl = restoreAcl;
    }

    public void setPathMapping(Map<String, String> pathMapping) {
        this.pathMapping = pathMapping;
    }

    public void setUuidMapping(Map<String, String> uuidMapping) {
        if (uuidMapping != null) {
            this.uuidMapping = uuidMapping;
        }
    }

    public void setTypeMappings(Map<String, Map<String, String>> typeMappings) {
        this.typeMappings = typeMappings;
    }

    public void setTemplateMappings(Map<String, String> templateMappings) {
        this.templateMappings = templateMappings;
    }

    public Map<String, String> getImportedMappings() {
        return importedMappings;
    }

    public void setImportedMappings(Map<String, String> importedMappings) {
        if (importedMappings != null) {
            this.importedMappings = importedMappings;
        }
    }

    public ContentObject getLastObject() {
        return lastObject;
    }

    public String getTopAcl() {
        return topAcl;
    }

    public ExtendedImportResult getResult() {
        return result;
    }
}
