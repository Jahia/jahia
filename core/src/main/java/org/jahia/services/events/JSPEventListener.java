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

import org.jahia.content.events.ContentActivationEvent;
import org.jahia.content.events.ContentObjectDeleteEvent;
import org.jahia.content.events.ContentObjectRestoreVersionEvent;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.data.events.JahiaErrorEvent;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.timebasedpublishing.RetentionRuleEvent;
import org.jahia.services.workflow.WorkflowEvent;

import java.io.File;

/**
 * <p>Title: A Jahia Event Listener that dispatches to JSP files</p>
 * <p>Description: This event listeners allows the use of JSP files as
 * event listeners, allowing for customizations in a more script-like way
 * than having to compile actual code. The JSPs here are not used as a
 * template language, meaning that nothing will be done with the output of
 * the JSP, but simply as a script extensions functionality.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class JSPEventListener extends JahiaEventListener {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JSPEventListener.class);

    private boolean configLoaded = false;
    private String defaultPathToJSP = null;
    private String jspFileName = null;

    private static final String DEFAULT_PATH_TO_JSP =
            "/events/eventlistener.jsp";
    private static final String JSP_FILE_NAME = "eventlistener.jsp";

    private boolean checkConfig (ProcessingContext processingContext) {
        if (configLoaded)
            return configLoaded;

        loadConfig (processingContext);

        return configLoaded;
    }

    private void loadConfig (ProcessingContext processingContext) {

        /**
         * @todo for the moment this stuff is hardcoded, but we might want to
         * make this configurable through an XML file.
         */

        defaultPathToJSP = DEFAULT_PATH_TO_JSP;
        jspFileName = JSP_FILE_NAME;

        configLoaded = true;
    }

    public JSPEventListener () {
    }

    private void dispatchToJSP (String eventName, JahiaEvent je) {
        if (!checkConfig (je.getProcessingContext ())) {
            return;
        }

        ProcessingContext processingContext = je.getProcessingContext ();
        if ((processingContext == null) ||
            !(processingContext instanceof ParamBean)) {
        	if (logger.isDebugEnabled())
        		logger.debug ("Cannot dispatch to JSP because we need request/response pair to do so for event " +
                    eventName);
            return;
        }
        ParamBean paramBean = (ParamBean) processingContext;
        try {
            String jspFileName = resolveJSPFullFileName (processingContext);

            if (logger.isDebugEnabled())
				logger.debug("Dispatching to JSP " + jspFileName
						+ " for processing of event " + eventName);
            if (paramBean != null && paramBean.getRequest()!=null) {
                paramBean.getRequest ().setAttribute ("eventName", eventName);
                paramBean.getRequest ().setAttribute ("jahiaEvent", je);
                long startTime = System.currentTimeMillis();
                ServicesRegistry.getInstance ().getJahiaFetcherService ()
                        .fetchServlet (paramBean, jspFileName);
                if (logger.isDebugEnabled())
					logger.debug("JSP Event Listener " + jspFileName
							+ " event=" + eventName + " execution time="
							+ (System.currentTimeMillis() - startTime) + " ms");
            } else {
                logger.warn("ParamBean is null, why ?");
            }
            /*
            paramBean.getRequest().getRequestDispatcher(jspFileName).include(
                paramBean.getRequest(), paramBean.getResponse());
            */
        } catch (Exception t) {
            logger.error ("Error while dispatching to JSP : " + jspFileName, t);
        }
    }

    private String resolveJSPFullFileName (ProcessingContext processingContext) {
        String jspFullFileName;
        if ((processingContext.getPage () != null) &&
                (processingContext.getPage ().getPageTemplate () != null) &&
                (processingContext.getPage ().getPageTemplate ().getSourcePath () != null)) {
            jspFullFileName = processingContext.getPage ().getPageTemplate ().
                    getSourcePath ();
            if (logger.isDebugEnabled())
            	logger.debug ("template source path :" + jspFullFileName);

            jspFullFileName = jspFullFileName.substring (0,
                    jspFullFileName.lastIndexOf ("/") + 1) + jspFileName;

            if (logger.isDebugEnabled())
            	logger.debug ("resolvedJSPFullFileName :" + jspFullFileName);

            File jspFile = new File (processingContext.settings().getPathResolver().resolvePath(jspFullFileName));
            if (!jspFile.exists ()) {
                jspFullFileName = defaultPathToJSP;
            }
        } else {
            jspFullFileName = defaultPathToJSP;
        }
        return jspFullFileName;
    }

    public void beforeServicesLoad (JahiaEvent je) {
        dispatchToJSP ("beforeServicesLoad", je);
    }

    public void afterServicesLoad (JahiaEvent je) {
        dispatchToJSP ("afterServicesLoad", je);
    }

    public void siteAdded(JahiaEvent je) {
        dispatchToJSP("siteAdded", je);
    }

    public void siteDeleted(JahiaEvent je) {
        dispatchToJSP("siteDeleted", je);
    }    

    public void beforeFieldActivation (JahiaEvent je) {
        dispatchToJSP ("beforeFieldActivation", je);
    }
    
    public void fieldAdded (JahiaEvent je) {
        dispatchToJSP ("fieldAdded", je);
    }

    public void fieldUpdated (JahiaEvent je) {
        dispatchToJSP ("fieldUpdated", je);
    }

    public void fieldDeleted (JahiaEvent je) {
        dispatchToJSP ("fieldDeleted", je);
    }

    public void beforeContainerActivation (JahiaEvent je) {
        dispatchToJSP ("beforeContainerActivation", je);
    }
    
    public void containerValidation(JahiaEvent je) {
        dispatchToJSP("containerValidation", je);
    }

    public void addContainerEngineAfterSave(JahiaEvent je) {
        dispatchToJSP("addContainerEngineAfterSave", je);
    }

    public void addContainerEngineBeforeSave (JahiaEvent je) {
        dispatchToJSP ("addContainerEngineBeforeSave", je);
    }    
    
    public void addContainerEngineAfterInit (JahiaEvent je) {
        dispatchToJSP ("addContainerEngineAfterInit", je);
    }

    public void updateContainerEngineBeforeSave (JahiaEvent je) {
        dispatchToJSP ("updateContainerEngineBeforeSave", je);
    }

    public void updateContainerEngineAfterInit (JahiaEvent je) {
        dispatchToJSP ("updateContainerEngineAfterInit", je);
    }

    public void containerAdded (JahiaEvent je) {
        dispatchToJSP ("containerAdded", je);
    }

    public void containerUpdated (JahiaEvent je) {
        dispatchToJSP ("containerUpdated", je);
    }

    public void containerDeleted (JahiaEvent je) {
        dispatchToJSP ("containerDeleted", je);
    }

    public void pageAdded (JahiaEvent je) {
        dispatchToJSP ("pageAdded", je);
    }

    public void pageLoaded (JahiaEvent je) {
        dispatchToJSP ("pageLoaded", je);
    }

    @Override
    public void pageLoadedFromCache(JahiaEvent je) {
        dispatchToJSP ("pageLoadedFromCache", je);
    }

    public void pagePropertiesSet (JahiaEvent je) {
        dispatchToJSP ("pagePropertiesSet", je);
    }

    public void containerListPropertiesSet (JahiaEvent je) {
        dispatchToJSP ("containerListPropertiesSet", je);
    }    
    
    public void userPropertiesSet (JahiaEvent je) {
        dispatchToJSP ("userPropertiesSet", je);
    }
    
    public void templateUpdated(JahiaEvent theEvent) {
        dispatchToJSP ("templateUpdated", theEvent);
    }
 
    public void categoryUpdated(JahiaEvent theEvent) {
        dispatchToJSP ("categoryUpdated", theEvent);
    }
    
    public void rightsSet (JahiaEvent je) {
        dispatchToJSP ("rightsSet", je);
    }

    public void userLoggedIn (JahiaEvent je) {
        dispatchToJSP ("userLoggedIn", je);
    }

    public void userLoggedOut (JahiaEvent je) {
        dispatchToJSP ("userLoggedOut", je);
    }

    public void objectChanged (WorkflowEvent we) {
        dispatchToJSP ("objectChanged", we);
    }
    
    public void aggregatedObjectChanged(JahiaEvent je) {
        dispatchToJSP("aggregatedObjectChanged", je);
    }    

    public void beforeStagingContentIsDeleted(JahiaEvent je) {
        dispatchToJSP ("beforeStagingContentIsDeleted", je);
    }

    public void metadataEngineAfterInit (JahiaEvent theEvent) {
        dispatchToJSP ("metadataEngineAfterInit", theEvent);
    }

    public void metadataEngineBeforeSave (JahiaEvent theEvent) {
        dispatchToJSP ("metadataEngineBeforeSave", theEvent);
    }

    public void metadataEngineAfterSave (JahiaEvent theEvent) {
        dispatchToJSP ("metadataEngineAfterSave", theEvent);
    }

    public void afterGroupActivation (ContentActivationEvent theEvent) {
        dispatchToJSP ("afterGroupActivation", theEvent);
    }
    
    public void contentActivation (ContentActivationEvent theEvent) {
        dispatchToJSP ("contentActivation", theEvent);
    }

    public void aggregatedContentActivation(JahiaEvent theEvent) {
        dispatchToJSP("aggregatedContentActivation", theEvent);
    }    
    
    public void contentObjectCreated (JahiaEvent theEvent) {
        dispatchToJSP ("contentObjectCreated", theEvent);
    }
    
    public void aggregatedContentObjectCreated(JahiaEvent theEvent) {
        dispatchToJSP("aggregatedContentObjectCreated", theEvent);
    }    

    public void contentObjectUpdated (JahiaEvent theEvent) {
        dispatchToJSP ("contentObjectUpdated", theEvent);
    }

    /**
     * Event fired once a content object has been updated ( changes stored in persistence )
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectUndoStaging (ContentUndoStagingEvent theEvent) {
        dispatchToJSP ("contentObjectUndoStaging", theEvent);
    }

    /**
     * Event fired after a call to contentObjectonce a content object has been updated ( changes stored in persistence )
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectDelete (ContentObjectDeleteEvent theEvent) {
        dispatchToJSP ("contentObjectDelete", theEvent);
    }

    /**
     * Event fired on content object restore version
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectRestoreVersion (ContentObjectRestoreVersionEvent theEvent) {
        dispatchToJSP ("contentObjectRestoreVersion", theEvent);
    }

    public void fileManagerAclChanged(JahiaEvent theEvent) {
        dispatchToJSP ("fileManagerAclChanged", theEvent);
    }

    public void timeBasedPublishingEvent( RetentionRuleEvent theEvent ) {
        dispatchToJSP ("timeBasedPublishingEvent", theEvent);
    }

    public void aggregatedEventsFlush(JahiaEvent theEvent) {
        dispatchToJSP("aggregatedEventsFlush", theEvent);
    }

    public void flushEsiCacheEvent(JahiaEvent theEvent) {
        dispatchToJSP("flushEsiCacheEvent", theEvent);
    }

    // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - Begin
    public void pageDeleted(JahiaEvent theEvent) {
        dispatchToJSP("pageDeleted", theEvent);
    }

    public void pageAccepted(JahiaEvent theEvent) {
        dispatchToJSP("pageAccepted", theEvent);
    }

    public void pageRejected(JahiaEvent theEvent) {
        dispatchToJSP("pageRejected", theEvent);
    }

    public void templateAdded(JahiaEvent theEvent) {
        dispatchToJSP("templateAdded", theEvent);
    }

    public void templateDeleted(JahiaEvent theEvent) {
        dispatchToJSP("templateDeleted", theEvent);
    }

    public void userAdded(JahiaEvent theEvent) {
        dispatchToJSP("userAdded", theEvent);
    }

    public void userDeleted(JahiaEvent theEvent) {
        dispatchToJSP("userDeleted", theEvent);
    }

    public void userUpdated(JahiaEvent theEvent) {
        dispatchToJSP("userUpdated", theEvent);
    }

    public void groupAdded(JahiaEvent theEvent) {
        dispatchToJSP("groupAdded", theEvent);
    }

    public void groupDeleted(JahiaEvent theEvent) {
        dispatchToJSP("groupDeleted", theEvent);
    }

    public void groupUpdated(JahiaEvent theEvent) {
        dispatchToJSP("groupUpdated", theEvent);
        super.groupUpdated(theEvent);
    }

    @Override
    public void errorOccurred(JahiaErrorEvent je) {
        dispatchToJSP("errorOccurred", je);
    }
}
