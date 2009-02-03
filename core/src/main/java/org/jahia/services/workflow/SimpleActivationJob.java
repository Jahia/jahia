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

 package org.jahia.services.workflow;

import java.util.Set;

import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 6 avr. 2006
 * Time: 16:25:16
 * To change this template use File | Settings | File Templates.
 */
public class SimpleActivationJob extends BackgroundJob {
    private static final WorkflowService service = ServicesRegistry.getInstance().getWorkflowService();

    public static final String KEYS = "keys";
    public static final String LANGS = "langs";

    public void executeJahiaJob(JobExecutionContext jobExecutionContext, ProcessingContext processingContext) throws Exception {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();

        ContentObject object;
        Set<ObjectKey> keys = (Set<ObjectKey>) data.get(KEYS);

        Set<String> languages = (Set<String>) data.get(LANGS);
        JahiaSaveVersion saveVersion = ServicesRegistry.getInstance ().getJahiaVersionService ().
                getSiteSaveVersion (processingContext.getSiteID ());

        for (ObjectKey key : keys) {
            try {
                object = (ContentObject) JahiaObject.getInstance(key);
                StateModificationContext stateModifContext = new StateModificationContext(object.getObjectKey(), languages);
                stateModifContext.addModifiedObjects(keys);
                service.activate(object, languages, saveVersion, processingContext, stateModifContext);
            } catch (ClassNotFoundException e) {
                throw new JobExecutionException(e);
            }
        }
    }
}
