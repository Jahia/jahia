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

import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.StructuralRelationship;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.StateModificationContext;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.io.InputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * Date: 25 oct. 2005 - 16:34:07
 *
 * @author toto
 * @version $Id$
 */
public class ActivationContentPickerJob extends BackgroundJob {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ActivationContentPickerJob.class);

    public static final String PICKED_TYPE = "picked";

    public static final String PICKER = "picker";
    public static final String PICKERSITE = "pickersite";
    public static final String LANGS = "langs";

    public synchronized void executeJahiaJob(JobExecutionContext jobExecutionContext, ProcessingContext jParams) throws Exception {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        ImportExportService ie = ServicesRegistry.getInstance().getImportExportService();

        String pickerKey = (String) jobDataMap.get(PICKER);
        ContentObject picker = ContentObject.getContentObjectInstance(ObjectKey.getInstance(pickerKey));
        Set<String> langs = (Set<String>) jobDataMap.get(LANGS);

//        DataWriter dw = new DataWriter(new OutputStreamWriter(System.out, "UTF-8"));

        ContentObject pickedObject = picker.getPickedObject(StructuralRelationship.ACTIVATION_PICKER_LINK);

        if (!langs.isEmpty()) {
            // change the workflow state of the target to STAGING
            Set<String> langCodes = new HashSet<String>(langs);
            langCodes.add(ContentObject.SHARED_LANGUAGE);
            try {
                StateModificationContext stateModifContext = new StateModificationContext(
                        picker.getObjectKey(), langCodes);
                stateModifContext.setDescendingInSubPages(true);
                ServicesRegistry.getInstance().getWorkflowService()
                        .changeStagingStatus(picker, langCodes,
                                EntryLoadRequest.STAGING_WORKFLOW_STATE,
                                stateModifContext, jParams, true);
            } catch (Exception e) {
                logger.warn(
                        "Error changing the state of the target conteht object to staging. Cause: "
                                + e.getMessage(), e);
            }
        }

        List<ImportAction> actions = new ArrayList<ImportAction>();
        ExtendedImportResult result = new ExtendedImportResult();

        Set<JCRNodeWrapper> files = new HashSet<JCRNodeWrapper>();
        JahiaUser oldUser = jParams.getUser();
        try {
            JahiaSite destSite = ServicesRegistry.getInstance().getJahiaSitesService().getSite(picker.getSiteID());
            setUser(picker, destSite, jParams, oldUser);

            Map<String, String> pathMapping = new HashMap<String, String>();
            checkFilesInChildren(pickedObject, jParams, ie, destSite, langs, pathMapping);

            for (Iterator<String> iterator2 = langs.iterator(); iterator2.hasNext();) {
                String lang = iterator2.next();

                Map<String, EntryLoadRequest> froms = new HashMap<String, EntryLoadRequest>();
                getFroms(froms, lang, picker, jParams);

                long now = System.currentTimeMillis() / 1000;
                EntryLoadRequest toLoadRequest = new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE, new Long(now).intValue(), jParams.getLocales());
                toLoadRequest.setWithDeleted(true);
//                                    System.out.println("----------> "+froms);
//                                    System.out.println("----------> "+toLoadRequest);

                // avoid cache issue
                pickedObject = (ContentObject) ContentObject.getInstance(pickedObject.getObjectKey());

                Map<String, Object> params = new HashMap<String, Object>();
                params.put(ImportExportService.LINK, StructuralRelationship.ACTIVATION_PICKER_LINK);
                params.put(ImportExportService.FROM, froms);
                params.put(ImportExportService.TO, toLoadRequest);
                params.put(ImportExportService.VIEW_PICKERS, Boolean.FALSE);
//                ie.export(pickedObject, lang, dw, files, jParams, params);
                ImportHandler handler = new ImportHandler(picker, jParams, lang, destSite, actions, result);
                handler.setCopyUuid(true);
                handler.setPathMapping(pathMapping);
                ie.export(pickedObject, lang, handler, files, jParams, params);
            }
        } catch (Exception t) {
            logger.error("Error when content picking content", t);
        } finally {
            jParams.setTheUser(oldUser);
        }
        jobDataMap.put(ACTIONS, actions);
        jobDataMap.put(RESULT, result);
    }

    private void checkFilesInChildren(ContentObject source, ProcessingContext context, ImportExportService ie, JahiaSite destSite, Set<String> langs, Map<String, String> pathMapping) throws JahiaException {
        Set<JCRNodeWrapper> f = new HashSet<JCRNodeWrapper>();
        for (Iterator<String> iterator = langs.iterator(); iterator.hasNext();) {
            String lang = iterator.next();
            ie.getFilesForField(source, context, lang, EntryLoadRequest.CURRENT, f);
        }
        for (Iterator<JCRNodeWrapper> iterator = f.iterator(); iterator.hasNext();) {
            JCRNodeWrapper fileNode = (JCRNodeWrapper) iterator.next();
            InputStream inputStream = fileNode.getFileContent().downloadFile();
            String type = fileNode.getFileContent().getContentType();
            ie.ensureFile(fileNode.getPath(), inputStream, type, context, destSite, pathMapping);
        }
        List<? extends ContentObject> childs = source.getChilds(null, context.getEntryLoadRequest());
        for (Iterator<? extends ContentObject> iterator1 = childs.iterator(); iterator1.hasNext();) {
            ContentObject child = (ContentObject) iterator1.next();
            checkFilesInChildren(child, context, ie, destSite, langs, pathMapping);
        }
    }


    private void setUser(ContentObject picker, JahiaSite site, ProcessingContext jParams, JahiaUser oldUser) throws JahiaException {
        ContentObject container = picker;
        while (!(container instanceof ContentContainer) && container != null) {
            container = container.getParent(null);
        }

        JahiaGroup admins = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(site.getID(), JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
        JahiaUser admin = (JahiaUser) admins.members().nextElement();

        if (admin != null) {
            jParams.setTheUser(admin);
        } else {
            jParams.setTheUser(oldUser);
        }
    }


    private void getFroms(Map<String, EntryLoadRequest> m, String lang, ContentObject o, ProcessingContext jParams) throws JahiaException {
        List<? extends ContentObject> l = o.getChilds(null, EntryLoadRequest.STAGED);
        for (Iterator<? extends ContentObject> iterator = l.iterator(); iterator.hasNext();) {
            ContentObject child = (ContentObject) iterator.next();
            getFroms(m, lang, child, jParams);
        }

        String name;
        if (o.isShared()) {
            name = "lastImportedVersion";
        } else {
            name = "lastImportedVersion-" + lang;
        }
        String last = o.getProperty(name);
        if (o.getProperty("originalUuid") != null) {
            if (last != null) {
                m.put(o.getProperty("originalUuid"), new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE, Integer.parseInt(last), jParams.getLocales()));
            } else {
                m.put(o.getProperty("originalUuid"), new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, jParams.getLocales()));
            }
        }
    }

}
/**
 *$Log$
 *Revision 1.2  2006/01/09 11:11:29  tdraier
 *content pick as a job
 *
 *Revision 1.1  2006/01/06 13:11:08  tdraier
 *content picker and workflow updates
 *
 *Revision 1.5  2005/12/21 16:15:19  dpillot
 *added operation type in jobdata
 *
 */