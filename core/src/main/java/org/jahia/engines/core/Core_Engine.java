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
//  AK   14.12.2000
//  AK   19.12.2000  use the EngineRenderer class
//  AK   04.01.2001  change EngineRenderer call

package org.jahia.engines.core;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentContainerListsXRefManager;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ContentPageKey;
import org.jahia.content.CrossReferenceManager;
import org.jahia.content.ObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.data.events.JahiaEvent;
import org.jahia.engines.EngineRenderer;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.exceptions.JahiaTemplateServiceException;
import org.jahia.operations.valves.UserAgentViewSwitcherValve;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.JahiaPageDefinition;


public class Core_Engine implements JahiaEngine {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (Core_Engine.class);

    /** The engine's name */
    public static final String ENGINE_NAME = "core";

    public Core_Engine () {
    }

    /**
     * authoriseRender AK    14.12.2000
     */
    public boolean authoriseRender (ProcessingContext jParams) {
        return (jParams.getOperationMode () == ProcessingContext.EDIT);
    }


    /**
     * needsJahiaData AK    14.12.2000
     */
    public boolean needsJahiaData (ProcessingContext jParams) {
        return true;
    }


    /**
     * renderLink AK    14.12.2000
     */
    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        return jParams.composeEngineUrl(ENGINE_NAME, new StringBuffer().append(
                EMPTY_STRING + "/op/").append(jParams.getOperationMode())
                .append("/pid/").append(jParams.getPageID()).toString());
    }


    /**
     * handleActions AK    14.12.2000 AK    04.01.2001  use processCore()...
     */
    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException {
        if (logger.isDebugEnabled()) {
            logger.debug("Generating content for " + jParams.getRemoteAddr()
                    + "...");
        }
        processCore (jData);
        postProcessCore (jData);
        return null;
    }

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName () {
        return ENGINE_NAME;
    }


    /**
     * Does the actual dispatching to the template, displaying all the objects that are
     * accessible through the JahiaData facade object.
     *
     * @param jData the JahiaData object containing all the context data for the current
     *              request.
     *
     * @throws JahiaException can mean a lot of things, from errors in communicating with the
     *                        database, to errors in the templates, etc...
     */
    private void processCore (JahiaData jData)
            throws JahiaException {
        if (jData.getProcessingContext ().getPage () == null
                ||  jData.getProcessingContext ().getPage ().getPageTemplate () == null) {
                // this can happen in the page doesn't exist in this operation mode
                // in a certain language.
                throw new JahiaPageNotFoundException (jData.getProcessingContext ().getPageID (),
                        jData.getProcessingContext ().getLocale ().toString (),
                        jData.getProcessingContext ().getOperationMode ());

        }
        
        if (jData.getProcessingContext ().getPage () == null) {
            if (!jData.getProcessingContext().getContentPage().checkReadAccess(jData.getProcessingContext().
                getUser())) {
                throw new JahiaForbiddenAccessException();
            }
        }

        // compose the fileName...
        String fileName = resolveJSPFullFileName(jData);
        
        JahiaEvent theEvent = new JahiaEvent(this, jData.getProcessingContext(), jData.getProcessingContext ().getPage ());
        ServicesRegistry.getInstance().getJahiaEventService().
            fireLoadPage(theEvent);

        // compose a new hashmap with engine properties...
        Map<String, Object> engineHashMap = new HashMap<String, Object>();
        engineHashMap.put (ENGINE_NAME_PARAM, ENGINE_NAME);
        engineHashMap.put (ENGINE_OUTPUT_FILE_PARAM, fileName);
        engineHashMap.put (RENDER_TYPE_PARAM, Integer.valueOf(JahiaEngine.RENDERTYPE_FORWARD));

        EngineRenderer.getInstance ().render (jData, engineHashMap);
    }

    private String resolveJSPFullFileName (JahiaData jData) throws JahiaTemplateServiceException {
        ProcessingContext ctx = jData.getProcessingContext();
        String jspFullFileName = null;

        JahiaPageDefinition template = ctx.getPage ().getPageTemplate ();
        if (template != null) {
            jspFullFileName = template.getSourcePath ();
        }


        // now let's test if we have a user_agent extension, such as templatename_useragentkey.jsp file that exists,
        // if yes we will use that instead of the normal template.
        String userAgentViewKey = (String) ctx.getAttribute(UserAgentViewSwitcherValve.VIEW_SWITCHING_VALVE_KEY_REQUEST_ATTRIBUTE_NAME);
        if (userAgentViewKey != null) {
            String justFileName = FilenameUtils.getName(jspFullFileName);
            String withoutExtension = justFileName.substring(0, justFileName.lastIndexOf("."));
            String justPath = FilenameUtils.getPrefix(jspFullFileName) + FilenameUtils.getPath(jspFullFileName);
            String extension = FilenameUtils.getExtension(justFileName);
            String newJspFullFileName = justPath + withoutExtension + "_" + userAgentViewKey + "."  + extension;
            File jspFile = new File (ctx.settings().getPathResolver().resolvePath(newJspFullFileName));
            if (jspFile.exists ()) {
                jspFullFileName = newJspFullFileName;
            }
        }

        // now let's check for template override parameter
        String pathToAlternateTemplate = ctx.getParameter (ProcessingContext.TEMPLATE_PARAMETER);
        if (StringUtils.isNotEmpty(pathToAlternateTemplate) && pathToAlternateTemplate.indexOf("..") == -1) {
                if (logger.isDebugEnabled()) {
                    logger.debug ("template source path :" + jspFullFileName);
                }

                // the code below is quite powerful, it allows us to set an
                // extension on the name of the template that will specify
                // the mime type we want to use. For example simple.xml will
                // use the mime type (text/xml) (if mapped properly in the
                // web.xml configuration file or in the server config), and
                // then the actual dispatching will be done to simple.jsp
                String justFileName = FilenameUtils.getName(pathToAlternateTemplate);
                String justPath = FilenameUtils.getPrefix(pathToAlternateTemplate) + FilenameUtils.getPath(pathToAlternateTemplate);
                String mimeType = null;
                if (ctx instanceof ParamBean) {
                    mimeType = ((ParamBean) ctx).getContext().getMimeType(justFileName);
                }
                if (mimeType != null) {
                    // we found a mime type, let's use it.
                    logger.debug("Using mime type " + mimeType);
                    ctx.setResponseMimeType(mimeType);
                }
                String extension = FilenameUtils.getExtension(justFileName);
                if (!"jsp".equals(extension)) {
                    justFileName = justFileName + ".jsp";
                }
                pathToAlternateTemplate = justPath + justFileName;
                
                // check if we have related path
                if (!pathToAlternateTemplate.startsWith("/") && jspFullFileName != null) {
                    jspFullFileName = "/" + FilenameUtils.getPath(jspFullFileName) + pathToAlternateTemplate;
                } else {
                    // we've got an absolute path --> use it
                    jspFullFileName = pathToAlternateTemplate;
                }

                // check physical file existance only in development mode
                if (org.jahia.settings.SettingsBean.getInstance().isDevelopmentMode()) {
                    File jspFile = new File (ctx.settings().getPathResolver().resolvePath(jspFullFileName));
                    if (!jspFile.exists ()) {
                        jspFullFileName = "/jsp/test/" + ctx.getParameter (ProcessingContext.TEMPLATE_PARAMETER);
                    }
                }
                
                if (logger.isDebugEnabled()) {
                    logger.debug ("Overriding page template with URL template: " + jspFullFileName);
                }

                // since we overrided the template, we deactivate the cache for
                // the page.
                // expires the cache immediately, not even storing it.
                ctx.setCacheExpirationDate(new Date());
        }
        
        if (jspFullFileName == null) {
            throw new JahiaTemplateServiceException(
                    "Unable to find the template for page ID="
                            + ctx.getPageID()
                            + ", page definition ID="
                            + (template != null ? template.getID() : "undefined")
                            + " and name '"
                            + (template != null ? template.getName()
                                    : "undefined") + "'");
        }

        if (logger.isDebugEnabled()) {
            logger.debug ("resolved JSP path: " + jspFullFileName);
        }

            return jspFullFileName;
        }


    /**
     * General post-process method. Add here any code that should be executed after dispatching
     * to the template
     *
     * @param jData the current request JahiaData object
     *
     * @throws JahiaException thrown in case there was a problem communicating with the
     *                        database.
     */
    private void postProcessCore (JahiaData jData)
            throws JahiaException {
    }

    /**
     * This method collects all the references stored during the request in the JahiaFieldSet
     * and JahiaContainerSet objects and handles the differences with the references that are
     * stored in the database. If new references are added they are also added to the database
     * and if references have been removed, they are removed (duh !:))
     *
     * @param jData the JahiaData object containing the JahiaFieldSet and JahiaContainerSet
     *              objects to use for the difference, as well as the reference to the ProcessingContext
     *              object used to know the current page.
     *
     * @throws JahiaException thrown if there were problems while communicating with the
     *                        database.
     */
    private void handleAbsoluteReferencesChanges (JahiaData jData)
            throws JahiaException {
        // first let's retrieve the accesses maps from the JahiaData structure
//        Set absoluteFieldAccesses = jData.fields ().getAbsoluteFieldAccesses ();
        Set<Integer> absoluteContainerListAccesses = jData.containers ()
                .getAbsoluteContainerListAccesses ();

        // now we must compare it to the database store to see if there are
        // differences, and if so update the database...

        Set<Object> fieldKeys = new TreeSet<Object>();
        Set<Object> containerListKeys = new TreeSet<Object>();

        ContentPageKey pageKey = new ContentPageKey( jData.getProcessingContext ().getPageID() );
        Set<ObjectKey> objectRefs = CrossReferenceManager.getInstance().getReverseObjectXRefs(pageKey);
        if (objectRefs != null) {
            Iterator<ObjectKey> objectRefIter = objectRefs.iterator();
            while (objectRefIter.hasNext()) {
                ObjectKey source = objectRefIter.next();
                if (source instanceof ContentContainerListKey) {
                    containerListKeys.add(source);
                } else if (source instanceof ContentFieldKey) {
                    fieldKeys.add(source);
                } else {
                    logger.debug("Invalid key object in cross reference list, ignoring... ");
                }
            }
        }

        // first let's convert the keys into IDs.
        Set<Integer> databaseAbsoluteFieldIDs = new HashSet<Integer>();
        Iterator<Object> fieldKeyIter = fieldKeys.iterator ();
        while (fieldKeyIter.hasNext ()) {
            ObjectKey curKey = (ObjectKey) fieldKeyIter.next ();
            logger.debug ("Converting databaseAbsoluteFieldKey " + curKey);
            databaseAbsoluteFieldIDs.add (new Integer (curKey.getIDInType ()));
        }
        Set<Integer> databaseAbsoluteContainerListIDs = new HashSet<Integer>();
        Iterator<Object> containerListKeyIter = containerListKeys.iterator ();
        while (containerListKeyIter.hasNext ()) {
            ObjectKey curKey = (ObjectKey) containerListKeyIter.next ();
            logger.debug ("Converting databaseAbsoluteContainerListKey " + curKey);
            databaseAbsoluteContainerListIDs.add (new Integer (curKey.getIDInType ()));
        }

        // okay now we have two sets of IDs that we can compare.
        Set<Integer> addedContainerListIDs = new HashSet<Integer>(absoluteContainerListAccesses);
        addedContainerListIDs.removeAll (databaseAbsoluteContainerListIDs);
        Set<Integer> removedContainerListIDs = new HashSet<Integer>(databaseAbsoluteContainerListIDs);
        removedContainerListIDs.removeAll (absoluteContainerListAccesses);

        // we know have the differences, we can process them...
        Iterator<Integer> addedContainerListIDIter = addedContainerListIDs.iterator ();
        while (addedContainerListIDIter.hasNext ()) {
            Integer curContainerListID = (Integer) addedContainerListIDIter.next ();
            logger.debug (
                    "Adding reference from page ID " + jData.getProcessingContext ().getPageID () +
                    " to container list " +
                    curContainerListID);
            ContentContainerListsXRefManager.getInstance ().setAbsoluteContainerListPageID (
                    curContainerListID.intValue (), jData.getProcessingContext ().getPageID ());
        }
        Iterator<Integer> removedContainerListIDIter = removedContainerListIDs.iterator ();
        while (removedContainerListIDIter.hasNext ()) {
            Integer curContainerListID = removedContainerListIDIter.next ();
            logger.debug (
                    "Removing reference from page ID " + jData.getProcessingContext ().getPageID () +
                    " to container list " +
                    curContainerListID);
            ContentContainerListsXRefManager.getInstance ().removeAbsoluteContainerListPageID (
                    curContainerListID.intValue (), jData.getProcessingContext ().getPageID ());
        }
    }

}
