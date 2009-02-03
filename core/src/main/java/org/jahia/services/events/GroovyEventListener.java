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

package org.jahia.services.events;

import groovy.lang.Binding;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.content.events.ContentObjectDeleteEvent;
import org.jahia.content.events.ContentObjectRestoreVersionEvent;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.params.ProcessingContext;
import org.jahia.services.timebasedpublishing.RetentionRuleEvent;
import org.jahia.services.workflow.WorkflowEvent;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Title: A Jahia Event Listener that dispatches to Groovy script files
 * </p>
 * <p>
 * Description: This event listeners allows the use of Groovy script files as event listeners, allowing for customizations in a more
 * script-like way than having to compile actual code.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: Jahia Ltd
 * </p>
 * 
 * @author Serge Huber
 * @version 1.0
 */

public class GroovyEventListener extends JahiaEventListener {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(GroovyEventListener.class);

    private boolean configLoaded = false;
    private String defaultPathToGroovy = null;
    private String groovyFileName = null;

    private static final String DEFAULT_PATH_TO_GROOVY = "events/EventListener.groovy";
    private static final String GROOVY_FILE_NAME = "EventListener.groovy";

    private JahiaGroovyEngine jahiaScriptEngine;
    private static long lastCall = 0;// used for performance mode

    private boolean checkConfig(ProcessingContext processingContext) {
        if (configLoaded)
            return configLoaded;

        loadConfig(processingContext);

        return configLoaded;
    }

    private static Map eventsHandled = null;
    private static Map siteEventFile = new ConcurrentHashMap(64);
    private static String staticeventsToHandleList = "";

    private void loadConfig(ProcessingContext processingContext) {

        /**
         * @todo for the moment this stuff is hardcoded, but we might want to make this configurable through an XML file.
         */

        defaultPathToGroovy = GroovyEventListener.DEFAULT_PATH_TO_GROOVY;
        groovyFileName = GroovyEventListener.GROOVY_FILE_NAME;

        configLoaded = true;
    }

    public GroovyEventListener() {
    }
    
    public void setGroovyScriptEngine (JahiaGroovyEngine jahiaScriptEngine) {
       this.jahiaScriptEngine = jahiaScriptEngine;
    }

    private void dispatchToGroovyScript(String eventName, JahiaEvent je) {
        ProcessingContext processingContext = je.getProcessingContext();
        if (checkConfig(processingContext)
                && (eventsHandled == null || eventsHandled
                        .containsKey(eventName))) {
            try {
                String groovyFileName = resolveGroovyFullFileName(processingContext);

                // Developement mode flag
                boolean checkDependances = org.jahia.settings.SettingsBean.getInstance()
                        .isDevelopmentMode();
                long startTime = System.currentTimeMillis();

                // performance mode
                if (startTime - lastCall < 1500 && checkDependances
                        && enabledPerformanceMode) {
                    // under heavy load we avoid the checking bottleneck repeatedly, assuming script unchanged and dependencies too
                    checkDependances = false;
                    logger
                            .debug("lapse between 2 dependency checks too close: no checks");
                }
                if (lastCall == 0)
                    checkDependances = true;// only the first time
                lastCall = startTime;
                synchronized (jahiaScriptEngine) {
                    Binding binding = new Binding();
                    binding.setVariable("eventName", eventName);
                    binding.setVariable("jahiaEvent", je);
                    jahiaScriptEngine.run(groovyFileName, binding,
                            checkDependances);
                }
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                logger.debug("Groovy script event listener=" + groovyFileName
                        + " event=" + eventName + " execution time="
                        + executionTime + "ms");

            } catch (Exception t) {
                logger.error("Error while dispatching to Groovy script : "
                        + groovyFileName, t);
            }
        }
    }

    private String resolveGroovyFullFileName(ProcessingContext processingContext) {
        String groovyFullFileName;
        StringBuffer nameBuilder = new StringBuffer("/jsp/jahia/");

        if (processingContext != null && processingContext.getSiteKey() != null
                && siteEventFile.containsKey(processingContext.getSiteKey()))
            return (String) siteEventFile.get(processingContext.getSiteKey());
        else if ((processingContext != null)
                && (processingContext.getSite() != null)
                && (processingContext.getSite().getHomeContentPage() != null)
                && (processingContext.getSite().getHomeContentPage()
                        .getPageTemplate(processingContext) != null)
                && (processingContext.getSite().getHomeContentPage()
                        .getPageTemplate(processingContext).getSourcePath() != null)) {
            groovyFullFileName = processingContext.getSite()
                    .getHomeContentPage().getPageTemplate(processingContext)
                    .getSourcePath();
            logger.debug("template source path :" + groovyFullFileName);

            groovyFullFileName = groovyFullFileName.substring(0,
                    groovyFullFileName.lastIndexOf("/") + 1)
                    + groovyFileName;

            groovyFullFileName = groovyFullFileName.substring(nameBuilder
                    .length());

            logger.debug("resolvedGroovyFullFileName :" + groovyFullFileName);

            File groovyScriptFile = new File(processingContext.settings()
                    .getPathResolver().resolvePath(groovyFullFileName));
            if (!groovyScriptFile.exists()) {
                groovyFullFileName = defaultPathToGroovy;
            }
            siteEventFile.put(processingContext.getSiteKey(),
                    groovyFullFileName);
        } else {
            groovyFullFileName = defaultPathToGroovy;
        }
        return groovyFullFileName;
    }

    public void beforeServicesLoad(JahiaEvent je) {
        dispatchToGroovyScript("beforeServicesLoad", je);
    }

    public void afterServicesLoad(JahiaEvent je) {
        dispatchToGroovyScript("afterServicesLoad", je);
    }

    public void siteAdded(JahiaEvent je) {
        dispatchToGroovyScript("siteAdded", je);
    }

    public void siteDeleted(JahiaEvent je) {
        dispatchToGroovyScript("siteDeleted", je);
    }

    public void beforeFieldActivation(JahiaEvent je) {
        dispatchToGroovyScript("beforeFieldActivation", je);
    }

    public void fieldAdded(JahiaEvent je) {
        dispatchToGroovyScript("fieldAdded", je);
    }

    public void fieldUpdated(JahiaEvent je) {
        dispatchToGroovyScript("fieldUpdated", je);
    }

    public void fieldDeleted(JahiaEvent je) {
        dispatchToGroovyScript("fieldDeleted", je);
    }

    public void beforeContainerActivation(JahiaEvent je) {
        dispatchToGroovyScript("beforeContainerActivation", je);
    }

    public void containerValidation(JahiaEvent je) {
        dispatchToGroovyScript("containerValidation", je);
    }

    public void addContainerEngineAfterSave(JahiaEvent je) {
        dispatchToGroovyScript("addContainerEngineAfterSave", je);
    }

    public void addContainerEngineBeforeSave(JahiaEvent je) {
        dispatchToGroovyScript("addContainerEngineBeforeSave", je);
    }

    public void addContainerEngineAfterInit(JahiaEvent je) {
        dispatchToGroovyScript("addContainerEngineAfterInit", je);
    }

    public void updateContainerEngineBeforeSave(JahiaEvent je) {
        dispatchToGroovyScript("updateContainerEngineBeforeSave", je);
    }

    public void updateContainerEngineAfterInit(JahiaEvent je) {
        dispatchToGroovyScript("updateContainerEngineAfterInit", je);
    }

    public void containerAdded(JahiaEvent je) {
        dispatchToGroovyScript("containerAdded", je);
    }

    public void containerUpdated(JahiaEvent je) {
        dispatchToGroovyScript("containerUpdated", je);
    }

    public void containerDeleted(JahiaEvent je) {
        dispatchToGroovyScript("containerDeleted", je);
    }

    public void pageAdded(JahiaEvent je) {
        dispatchToGroovyScript("pageAdded", je);
    }

    public void pageLoaded(JahiaEvent je) {
        dispatchToGroovyScript("pageLoaded", je);
    }

    public void pagePropertiesSet(JahiaEvent je) {
        dispatchToGroovyScript("pagePropertiesSet", je);
    }

    public void containerListPropertiesSet(JahiaEvent je) {
        dispatchToGroovyScript("containerListPropertiesSet", je);
    }

    public void userPropertiesSet(JahiaEvent je) {
        dispatchToGroovyScript("userPropertiesSet", je);
    }

    public void templateUpdated(JahiaEvent theEvent) {
        dispatchToGroovyScript("templateUpdated", theEvent);
    }

    public void categoryUpdated(JahiaEvent theEvent) {
        dispatchToGroovyScript("categoryUpdated", theEvent);
    }

    public void rightsSet(JahiaEvent je) {
        dispatchToGroovyScript("rightsSet", je);
    }

    public void userLoggedIn(JahiaEvent je) {
        dispatchToGroovyScript("userLoggedIn", je);
    }

    public void userLoggedOut(JahiaEvent je) {
        dispatchToGroovyScript("userLoggedOut", je);
    }

    public void objectChanged(WorkflowEvent we) {
        dispatchToGroovyScript("objectChanged", we);
    }

    public void aggregatedObjectChanged(JahiaEvent je) {
        dispatchToGroovyScript("aggregatedObjectChanged", je);
    }

    public void beforeStagingContentIsDeleted(JahiaEvent je) {
        dispatchToGroovyScript("beforeStagingContentIsDeleted", je);
    }

    public void metadataEngineAfterInit(JahiaEvent theEvent) {
        dispatchToGroovyScript("metadataEngineAfterInit", theEvent);
    }

    public void metadataEngineBeforeSave(JahiaEvent theEvent) {
        dispatchToGroovyScript("metadataEngineBeforeSave", theEvent);
    }

    public void metadataEngineAfterSave(JahiaEvent theEvent) {
        dispatchToGroovyScript("metadataEngineAfterSave", theEvent);
    }

    public void afterGroupActivation(ContentActivationEvent theEvent) {
        dispatchToGroovyScript("afterGroupActivation", theEvent);
    }

    public void contentActivation(ContentActivationEvent theEvent) {
        dispatchToGroovyScript("contentActivation", theEvent);
    }

    public void aggregatedContentActivation(JahiaEvent theEvent) {
        dispatchToGroovyScript("aggregatedContentActivation", theEvent);
    }

    public void contentObjectCreated(JahiaEvent theEvent) {
        dispatchToGroovyScript("contentObjectCreated", theEvent);
    }

    public void aggregatedContentObjectCreated(JahiaEvent theEvent) {
        dispatchToGroovyScript("aggregatedContentObjectCreated", theEvent);
    }

    public void contentObjectUpdated(JahiaEvent theEvent) {
        dispatchToGroovyScript("contentObjectUpdated", theEvent);
    }

    /**
     * Event fired once a content object has been updated ( changes stored in persistence )
     * 
     * @param theEvent
     *                JahiaEvent
     */
    public void contentObjectUndoStaging(ContentUndoStagingEvent theEvent) {
        dispatchToGroovyScript("contentObjectUndoStaging", theEvent);
    }

    /**
     * Event fired after a call to contentObjectonce a content object has been updated ( changes stored in persistence )
     * 
     * @param theEvent
     *                JahiaEvent
     */
    public void contentObjectDelete(ContentObjectDeleteEvent theEvent) {
        dispatchToGroovyScript("contentObjectDelete", theEvent);
    }

    /**
     * Event fired on content object restore version
     * 
     * @param theEvent
     *                JahiaEvent
     */
    public void contentObjectRestoreVersion(
            ContentObjectRestoreVersionEvent theEvent) {
        dispatchToGroovyScript("contentObjectRestoreVersion", theEvent);
    }

    public void fileManagerAclChanged(JahiaEvent theEvent) {
        dispatchToGroovyScript("fileManagerAclChanged", theEvent);
    }

    public void timeBasedPublishingEvent(RetentionRuleEvent theEvent) {
        dispatchToGroovyScript("timeBasedPublishingEvent", theEvent);
    }

    public void aggregatedEventsFlush(JahiaEvent theEvent) {
        dispatchToGroovyScript("aggregatedEventsFlush", theEvent);
    }

    public void flushEsiCacheEvent(JahiaEvent theEvent) {
        dispatchToGroovyScript("flushEsiCacheEvent", theEvent);
    }

    // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - Begin
    public void pageDeleted(JahiaEvent theEvent) {
        dispatchToGroovyScript("pageDeleted", theEvent);
    }

    public void pageAccepted(JahiaEvent theEvent) {
        dispatchToGroovyScript("pageAccepted", theEvent);
    }

    public void pageRejected(JahiaEvent theEvent) {
        dispatchToGroovyScript("pageRejected", theEvent);
    }

    public void templateAdded(JahiaEvent theEvent) {
        dispatchToGroovyScript("templateAdded", theEvent);
    }

    public void templateDeleted(JahiaEvent theEvent) {
        dispatchToGroovyScript("templateDeleted", theEvent);
    }

    public void userAdded(JahiaEvent theEvent) {
        dispatchToGroovyScript("userAdded", theEvent);
    }

    public void userDeleted(JahiaEvent theEvent) {
        dispatchToGroovyScript("userDeleted", theEvent);
    }

    public void userUpdated(JahiaEvent theEvent) {
        dispatchToGroovyScript("userUpdated", theEvent);
    }

    public void groupAdded(JahiaEvent theEvent) {
        dispatchToGroovyScript("groupAdded", theEvent);
    }

    public void groupDeleted(JahiaEvent theEvent) {
        dispatchToGroovyScript("groupDeleted", theEvent);
    }

    public void groupUpdated(JahiaEvent theEvent) {
        dispatchToGroovyScript("groupUpdated", theEvent);
        super.groupUpdated(theEvent);
    }

    public static void registerEvents(String eventsToHandleList) {
        if (!staticeventsToHandleList.equals(eventsToHandleList)) {
            staticeventsToHandleList = eventsToHandleList;
            String eventsToHandle[] = eventsToHandleList.split(",");
            eventsHandled = new ConcurrentHashMap(64);
            for (int i = 0; i < eventsToHandle.length; i++) {
                String s = eventsToHandle[i];
                eventsHandled.put(s, s);
            }
        }
    }

    // this flag could be use to control the repeated dependencies check from groovy script
    public static boolean enabledPerformanceMode = true;
}
