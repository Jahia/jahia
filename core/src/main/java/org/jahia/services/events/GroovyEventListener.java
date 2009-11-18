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
package org.jahia.services.events;

import groovy.lang.Binding;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.content.events.ContentObjectDeleteEvent;
import org.jahia.content.events.ContentObjectRestoreVersionEvent;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.data.events.JahiaErrorEvent;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.settings.SettingsBean;
import org.springframework.beans.factory.InitializingBean;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

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

public class GroovyEventListener extends JahiaEventListener implements InitializingBean {

    private static Logger logger = Logger.getLogger(GroovyEventListener.class);

    private static final String DEFAULT_PATH_TO_GROOVY = "events/EventListener.groovy";
    private static final String GROOVY_FILE_NAME = "EventListener.groovy";

    private JahiaGroovyEngine jahiaScriptEngine;
    private long lastCall = 0;// used for performance mode

    private Map<String, String> siteEventFile = new HashMap<String, String>();

    private String defaultListenerFilePath = null;
    private String listenerFileName = null;
    
    public void setGroovyScriptEngine (JahiaGroovyEngine jahiaScriptEngine) {
       this.jahiaScriptEngine = jahiaScriptEngine;
    }

    private void dispatchToGroovyScript(String eventName, JahiaEvent je) {
        
        ProcessingContext ctx = je.getProcessingContext();
        SettingsBean settings = SettingsBean.getInstance();
        
        if (!settings.isDevelopmentMode() && ctx != null && ctx.getSiteKey() != null && siteEventFile.containsKey(ctx.getSiteKey()) && !needToHandleEvent(eventName)) {
            return;
        }
        
            try {
                String groovyFileName = resolveGroovyFullFileName(ctx);

                // Development mode flag
                boolean checkDependances = settings.isDevelopmentMode();
                long startTime = System.currentTimeMillis();

                // performance mode
                if (startTime - lastCall < 1500 && checkDependances
                        && enabledPerformanceMode) {
                    // under heavy load we avoid the checking bottleneck repeatedly, assuming script unchanged and dependencies too
                    checkDependances = false;
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("lapse between 2 dependency checks too close: no checks");
                    }
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
                if (logger.isDebugEnabled()) {
                    logger.debug("Groovy script event listener=" + groovyFileName
                            + " event=" + eventName + " execution time="
                            + (System.currentTimeMillis() - startTime) + "ms");
                }

            } catch (Exception t) {
                logger.error("Error while dispatching to Groovy script : "
                        + listenerFileName, t);
            }
    }

    private String resolveGroovyFullFileName(ProcessingContext ctx) {
        String groovyFullFileName = null;

        if (ctx != null && ctx.getSiteKey() != null
                && siteEventFile.containsKey(ctx.getSiteKey())) {
            groovyFullFileName = siteEventFile.get(ctx.getSiteKey());
        }
        String checkedPath = null;
        if (groovyFullFileName == null && ctx != null && ctx.getSite() != null) {
            ContentPage homePage = ctx.getSite().getHomeContentPage();
            if (homePage != null) {
                JahiaPageDefinition pageTemplate = homePage
                        .getPageTemplate(ctx);
                if (pageTemplate != null
                        && pageTemplate.getSourcePath() != null) {
                    checkedPath = pageTemplate.getSourcePath().substring(0,
                            pageTemplate.getSourcePath().lastIndexOf("/") + 1)
                            + listenerFileName;
                    checkedPath = StringUtils.substringAfter(checkedPath, ctx.settings().getTemplatesContext());
                    groovyFullFileName = checkPath(checkedPath, ctx);
                }
            }
        }
        if (groovyFullFileName == null && ctx != null && ctx.getSite() != null && ctx.getSite().getTemplatePackageName() != null) {
            String templateSetSpecificPath = ctx.getSite().getTemplateFolder()
                    + "/" + listenerFileName;
            if (checkedPath == null
                    || !templateSetSpecificPath.equals(checkedPath)) {
                groovyFullFileName = checkPath(templateSetSpecificPath, ctx);
            }
        }
        if (groovyFullFileName == null) {
            groovyFullFileName = defaultListenerFilePath;
        }

        if (ctx != null && ctx.getSiteKey() != null) {
            siteEventFile.put(ctx.getSiteKey(), groovyFullFileName);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("resolveGroovyFullFileName: " + groovyFullFileName);
        }

        return groovyFullFileName;
    }

    private String checkPath(String path, ProcessingContext ctx) {
        String listenerPath = null;
        try {
            listenerPath = Jahia.getStaticServletConfig().getServletContext().getResource(ctx.settings().getTemplatesContext() + path) != null ? path : null;
        } catch (MalformedURLException e) {
            logger.warn(e.getMessage(), e);
        }
    return listenerPath;
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

    @Override
    public void pageLoadedFromCache(JahiaEvent je) {
        dispatchToGroovyScript("pageLoadedFromCache", je);
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
    
    @Override
    public void errorOccurred(JahiaErrorEvent je) {
        dispatchToGroovyScript("errorOccurred", je);
    }

    private static GroovyEventListener getInstance() {
        return (GroovyEventListener) SpringContextSingleton.getInstance()
                .getContext().getBean("GroovyEventListener");
    }

    private void addEvent(String event) {
        if (skipEvents != null && skipEvents.contains(event)) {
            skipEvents.remove(event);
        }
        if (handleEvents != null && !handleEvents.contains(event)) {
            handleEvents.add(event);
        }
    }
    
    /**
     * Allows to register events to be handled. The method can only add events, but not remove already registered events.
     * For default list of handled/skipped events, see <code>applicationcontext-listeners.xml</code> file. 
     * @param eventsToHandleList comma-separated list of events to handle
     */
    public static void registerEvents(String eventsToHandleList) {
        if (eventsToHandleList != null && eventsToHandleList.length() > 0) {
            String eventsToHandle[] = eventsToHandleList.split(",");
            for (String evt : eventsToHandle) {
                getInstance().addEvent(evt.trim());
            }
        }
    }

    // this flag could be use to control the repeated dependencies check from groovy script
    public static boolean enabledPerformanceMode = true;

    public void afterPropertiesSet() {
        if (listenerFileName == null || listenerFileName.length() == 0) {
            listenerFileName = GROOVY_FILE_NAME;
        }
        if (defaultListenerFilePath == null
                || defaultListenerFilePath.length() == 0) {
            defaultListenerFilePath = DEFAULT_PATH_TO_GROOVY;
        }
        logger.info("Listener initialized with listener file name '"
                + listenerFileName + "' and default listener file path '"
                + defaultListenerFilePath + "'");
    }

    public void setDefaultListenerFilePath(String defaultListenerFilePath) {
        this.defaultListenerFilePath = defaultListenerFilePath;
    }

    public void setListenerFileName(String listenerFileName) {
        this.listenerFileName = listenerFileName;
    }
}
