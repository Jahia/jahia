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

import org.quartz.JobExecutionContext;
import org.quartz.JobDataMap;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.StructuralRelationship;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.fields.ContentFileField;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.scheduler.BackgroundJob;

import java.util.*;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * Date: 25 oct. 2005 - 16:34:07
 *
 * @author toto
 * @version $Id$
 */
public class ChangeContentPickerJob extends BackgroundJob {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ChangeContentPickerJob.class);

    private static ThreadLocal isInContentPickJob = new ThreadLocal();

    public synchronized void executeJahiaJob(JobExecutionContext jobExecutionContext, ProcessingContext context) throws Exception {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        ImportExportService ie = ServicesRegistry.getInstance().getImportExportService();

        String eventType = (String) jobDataMap.get("event");

        String language = context.getCurrentLocale().toString();

        JahiaUser oldUser = context.getUser();
        List<ImportAction> actions = new ArrayList<ImportAction>();
        ExtendedImportResult result = new ExtendedImportResult();

        if ("objectChanged".equals(eventType)) {
            ContentObject source = ContentObject.getContentObjectInstance(ObjectKey.getInstance((String) jobDataMap.get("source")));
            if (source == null) {
                return;
            }

            Set pickers = source.getPickerObjects("");

            if (!pickers.isEmpty()) {
                for (Iterator iterator = pickers.iterator(); iterator.hasNext();) {
                    ContentObject picker = (ContentObject) iterator.next();

                    Map<String,String> pathMapping = new HashMap<String,String>();

                    if (picker != null) {
                        JahiaSite destSite = ServicesRegistry.getInstance().getJahiaSitesService().getSite(picker.getSiteID());
                        setUser(picker, destSite, context, oldUser);
                        isInContentPickJob.set(Boolean.TRUE);
                        try {
                            if (source instanceof ContentFileField) {
                                JahiaSite sourceSite = ServicesRegistry.getInstance().getJahiaSitesService().getSite(source.getSiteID());
                                String path = ((ContentFileField)source).getValue(context, EntryLoadRequest.STAGED);
                                JCRNodeWrapper sourceFile = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(path, context.getUser());
                                if (sourceFile.isValid()) {
                                    InputStream inputStream = sourceFile.getFileContent().downloadFile();
                                    String type = sourceFile.getFileContent().getContentType();
                                    ie.ensureFile(sourceFile.getPath(), inputStream, type, context, destSite,pathMapping);
                                }
                            }

                            if ((source instanceof ContentContainer && ((ContentContainer)source).isDeleted(Integer.MAX_VALUE)) || source.isMarkedForDelete()) {
                                Set<String> curLanguageCodes = new HashSet<String>();
                                curLanguageCodes.add(ContentObject.SHARED_LANGUAGE);

                                StateModificationContext stateModifContext =
                                        new StateModificationContext(picker.getObjectKey(), curLanguageCodes, true);
                                stateModifContext.pushAllLanguages(true);

                                picker.markLanguageForDeletion(
                                        context.getUser(),
                                        ContentObject.SHARED_LANGUAGE,
                                        stateModifContext);

                            } else {
                                ImportHandler handler = new ImportHandler(picker,context, language, destSite, actions, result);
                                handler.setUpdateOnly(true);
                                handler.setPathMapping(pathMapping);
                                Set included = new HashSet();
                                included.add(source);

                                Map params = new HashMap();
                                params.put(ImportExportService.LINK, StructuralRelationship.CHANGE_PICKER_LINK);
                                params.put(ImportExportService.TO, EntryLoadRequest.STAGED);
                                params.put(ImportExportService.INCLUDED, included);

                                ie.export(source, language, handler, null, context, params);
                            }
                        } catch (Exception t) {
                            logger.error("Error when content picking content",t);
                        } finally {
                            isInContentPickJob.set(null);
                            context.setTheUser(oldUser);
                        }
                    }
                }
            } else if (!source.isMarkedForDelete()) {
                ContentObject parent = source.getParent(null,null,null);
                if (parent != null) {
                    Set parentPicks = parent.getPickerObjects("");
                    if (!parentPicks.isEmpty()) {
                        for (Iterator iterator = parentPicks.iterator(); iterator.hasNext();) {
                            ContentObject picker = (ContentObject) iterator.next();
                            Map<String,String> pathMapping = new HashMap<String,String>();
                            if (picker != null) {
                                JahiaSite destSite = ServicesRegistry.getInstance().getJahiaSitesService().getSite(picker.getSiteID());
                                setUser(picker, destSite, context, oldUser);
                                isInContentPickJob.set(Boolean.TRUE);
                                try {
                                    checkFilesInChildren(source, context, ie, destSite,pathMapping);
                                    ImportHandler handler = new ImportHandler(picker,context, language, destSite, actions, result);
                                    handler.setCopyUuid(true);

                                    Map params = new HashMap();
                                    params.put(ImportExportService.LINK, StructuralRelationship.CHANGE_PICKER_LINK);
                                    params.put(ImportExportService.TO, EntryLoadRequest.STAGED);

                                    ie.export(source, language, handler, null, context, params);
                                } catch (Exception t) {
                                    logger.error("Error when content picking content",t);
                                } finally {
                                    isInContentPickJob.set(null);
                                    context.setTheUser(oldUser);
                                }
                            }
                        }
                    }
                }
            }
        } else if ("beforeStagingContentIsDeleted".equals(eventType)) {
            String key = (String) jobDataMap.get("todelete");
            StringTokenizer tok = new StringTokenizer(key, ",");

            while (tok.hasMoreTokens()) {
                ContentObject picker = ContentObject.getContentObjectInstance(ObjectKey.getInstance(tok.nextToken()));

                if (picker != null) {
                    JahiaSite destSite = ServicesRegistry.getInstance().getJahiaSitesService().getSite(picker.getSiteID());
                    setUser(picker, destSite, context, oldUser);
                    isInContentPickJob.set(Boolean.TRUE);
                    try {
                        Set<String> curLanguageCodes = new HashSet<String>();
                        if (picker instanceof ContentContainer) {
                            ContentContainer contentContainer = (ContentContainer) picker;
                            curLanguageCodes.add(ContentObject.SHARED_LANGUAGE);

                            StateModificationContext stateModifContext =
                                    new StateModificationContext(picker.getObjectKey(), curLanguageCodes, true);
                            stateModifContext.pushAllLanguages(true);

                            contentContainer.markLanguageForDeletion(
                                    context.getUser(),
                                    ContentObject.SHARED_LANGUAGE,
                                    stateModifContext);

                        } else {
                            curLanguageCodes.add(language);
                            curLanguageCodes.add(ContentObject.SHARED_LANGUAGE);
                            StateModificationContext stateModifContext =
                                    new StateModificationContext(picker.getObjectKey(), curLanguageCodes, true);
                            picker.markLanguageForDeletion(context.getUser(), language, stateModifContext);
                        }
                    } catch (Exception t) {
                        logger.error("Error when content picking content",t);
                    } finally {
                        isInContentPickJob.set(null);
                        context.setTheUser(oldUser);
                    }
                }
            }
        }
        jobDataMap.put(ACTIONS, actions);
        jobDataMap.put(RESULT, result);
    }

    private void checkFilesInChildren(ContentObject source, ProcessingContext context, ImportExportService ie, JahiaSite destSite, Map<String,String> pathMapping) throws JahiaException {
        if (source instanceof ContentFileField) {
            String path = ((ContentFileField)source).getValue(context, EntryLoadRequest.STAGED);
            JCRNodeWrapper sourceFile = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(path, context.getUser());
            if (sourceFile.isValid()) {
                InputStream inputStream = sourceFile.getFileContent().downloadFile();
                String type = sourceFile.getFileContent().getContentType();
                ie.ensureFile(sourceFile.getPath(), inputStream, type, context, destSite, pathMapping);
            }
        }
        List childs = source.getChilds(null,context.getEntryLoadRequest());
        for (Iterator iterator1 = childs.iterator(); iterator1.hasNext();) {
            ContentObject child = (ContentObject) iterator1.next();
            checkFilesInChildren(child, context, ie, destSite,pathMapping);
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

    public static boolean isInContentPickJob() {
        return (isInContentPickJob.get() == Boolean.TRUE);
    }
}
/**
 *$Log$
 *Revision 1.1  2006/01/06 13:11:08  tdraier
 *content picker and workflow updates
 *
 *Revision 1.5  2005/12/21 16:15:19  dpillot
 *added operation type in jobdata
 *
 */