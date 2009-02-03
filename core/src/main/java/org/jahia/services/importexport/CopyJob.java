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

import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.lock.LockKey;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * Date: 25 oct. 2005 - 16:34:07
 *
 * @author toto
 * @version $Id$
 */
public class CopyJob extends BackgroundJob {
    public static final String COPYPASTE_TYPE = "copypaste";
    public static final String PICKERCOPY_TYPE = "pickercopy";

    public static final String SITESOURCE = "sitesource";
    public static final String DEST = "dest";
    public static final String SOURCE = "source";
    public static final String VERSION = "version";
    public static final String VERSION_CURRENT = "current";
    public static final String LINK = "link";
    public static final String VERSION_COMPLETE = "complete";

    public void executeJahiaJob(JobExecutionContext jobExecutionContext, ProcessingContext context) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        ContentObject source = ContentObject.getContentObjectInstance(ObjectKey.getInstance((String) jobDataMap.get(SOURCE)));
        ContentObject dest = ContentObject.getContentObjectInstance(ObjectKey.getInstance((String) jobDataMap.get(DEST)));
        String link = (String) jobDataMap.get(LINK);
        String version = (String) jobDataMap.get(VERSION);

        List<ImportAction> actions = new ArrayList<ImportAction>();
        ExtendedImportResult result = new ExtendedImportResult();

        EntryLoadRequest loadrequest = EntryLoadRequest.STAGED;

        if (VERSION_CURRENT.equals(version)) {
            loadrequest = EntryLoadRequest.CURRENT;
        }

        ContentObject imported = ServicesRegistry.getInstance().getImportExportService().copy(source, dest, context, loadrequest, link, actions, result);

        if (imported != null) {
            LockKey lock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_" + imported.getObjectKey().getType(), imported.getID(), imported.getID());
            ((Set)jobDataMap.get(JOB_LOCKS)).add(lock);
        }

        jobDataMap.put(ACTIONS, actions);
        jobDataMap.put(RESULT, result);
    }

    private Set getSiteLanguages(JahiaSite site) throws JahiaException {
        Set languages = new HashSet();
        List v = site.getLanguageSettings(true);
        for (Iterator iterator = v.iterator(); iterator.hasNext();) {
            SiteLanguageSettings sls = (SiteLanguageSettings) iterator.next();
            languages.add(sls.getCode());
        }

        return languages;
    }


    private void activateAll(ContentObject o, Set languageCodes, boolean versioningActive, JahiaSaveVersion saveVersion, JahiaUser user, ProcessingContext jParams, StateModificationContext stateModifContext) throws JahiaException {
        List l = o.getChilds(null,null);
        for (Iterator iterator = l.iterator(); iterator.hasNext();) {
            ContentObject child = (ContentObject) iterator.next();
            activateAll(child, languageCodes, versioningActive, saveVersion, user, jParams, stateModifContext);
        }
        o.activate(languageCodes, versioningActive, saveVersion, user, jParams, stateModifContext);
    }
}
/**
 *$Log $
 */