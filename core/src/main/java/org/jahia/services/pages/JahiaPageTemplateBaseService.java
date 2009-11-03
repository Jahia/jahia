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
package org.jahia.services.pages;

import org.jahia.data.JahiaDOMObject;
import org.jahia.data.events.JahiaEvent;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaTemplateNotFoundException;
import org.jahia.hibernate.manager.JahiaTemplateManager;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLNotFoundException;
import org.jahia.services.acl.ACLResource;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.events.JahiaEventGeneratorBaseService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.bin.Jahia;

import java.util.*;


/**
 * @author
 * @version
 */
public class JahiaPageTemplateBaseService extends JahiaPageTemplateService{

    /** logging */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaPageTemplateBaseService.class);

    /** the unique instance of this class */
    private static JahiaPageTemplateBaseService instance = null;

    // the Page Templates cache name.
    public static final String PAGE_TEMPLATE_CACHE = "PageTemplateCache";
    /** the template cache */
    private static Cache<Integer, JahiaPageDefinition> templateCache = null;

    private JahiaTemplateManager templateManager;

    private CacheService cacheService;

    private JahiaTemplateManagerService templateManagerService;
    
    private JahiaGroupManagerService groupManagerService;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setTemplateManager(JahiaTemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }
    
    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    /**
     * Default constructor, creates a new <code>JahiaPageTemplateBaseService</code> instance.
     */
    public JahiaPageTemplateBaseService () {
    }


    /**
     * Create a new page template.
     *
     * @param siteID      The jahia site ID.
     * @param name        The page template name.
     * @param sourcePath  The page template source path.
     * @param isAvailable <code>true</code> is the page template is Available in edition
     *                    mode or <code>false</code> if it should be hidden.
     * @param pageType
     *@param image       Image path.
     * @param parentAclID The parent acl id
 * @return Return a new page template instanciation.
     *
     * @throws org.jahia.exceptions.JahiaException
     *          when any error occured in the page template creation process.
     */
    public JahiaPageDefinition createPageTemplate(
            int siteID,
            String name,
            String sourcePath,
            boolean isAvailable,
            String pageType,
            String description,
            String image,
            int parentAclID) throws JahiaException {
        // We need the Group Manager Service, before instanciating anything, check
        // if the service is available.

        if (groupManagerService == null) {
            logger.warn (
                    "Could not access to the Group Manager Service instance. Stopping page template creation.");
            throw new JahiaException ("Could not access the Group Manager Service",
                    "JahiaGroupManagerService.getInstance() returned null!!!",
                    JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY);
        }

        // get the next available page template ID
        int newID = 0;

        // Create a new ACL.
        JahiaBaseACL acl = new JahiaBaseACL ();
        try {
            acl.create (parentAclID);
        } catch (ACLNotFoundException ex) {
            throw new JahiaException ("Could not create the page def.",
                    "The parent ACL ID [" + parentAclID + "] could not be found," +
                            " while trying to create a new page def.",
                    JahiaException.TEMPLATE_ERROR, JahiaException.ERROR_SEVERITY, ex);
        }

        // instanciate the new page template
        JahiaPageDefinition template = new JahiaPageDefinition (newID, siteID,
                name, sourcePath, isAvailable, image, pageType);

        if (template == null) {
            throw new JahiaException ("Could not create page template",
                    "Could not instanciate a new JahiaPageDefinition object.",
                    JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY);
        }

        // Add the page template in the database.
        if (!templateManager.insertPageTemplate (template)) {
            throw new JahiaException ("Could not create page template",
                    "Could not insert the page template into the database.",
                    JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY);
        }

        populatePageTemplateData(template);

        template.setACL (acl.getID ());
        template.setDescription(description);
        templateManager.updatePageTemplate(template);

        // set the template default permissions
        // enable guest users to view the template
        JahiaAclEntry aclEntry = new JahiaAclEntry (1, 0);
        JahiaGroup guestGroup = groupManagerService.getGuestGroup (siteID);
        acl.setGroupEntry (guestGroup, aclEntry);

        // Finally add it into the cache and return the template
        templateCache.put (new Integer (template.getID ()), template);

        // Nicolás Charczewski - Neoris Argentina - added 28/03/2006 - Begin
        JahiaEvent je = new JahiaEvent(this,  Jahia.getThreadParamBean() != null ? Jahia.getThreadParamBean() : new ProcessingContext(), template);
        JahiaEventGeneratorBaseService.getInstance().fireAddTemplate(je);
        // Nicolás Charczewski - Neoris Argentina - added 28/03/2006 - End

        return template;
    }


    /**
     * Deletes the specified page template.
     *
     * @param defID the page template identification number
     *
     * @throws JahiaException when the service is not available, or a general failure occured while
     *                        removing the page template.
     */
    public synchronized void deletePageTemplate (int defID)
            throws JahiaException {

        JahiaPageDefinition pageTemplate = lookupPageTemplate (defID);

        JahiaBaseACL pageTemplateACL = pageTemplate.getACL ();
        pageTemplateACL.delete ();

        templateManager.deletePageTemplate (defID);

        // Finally remove it from the cache
        templateCache.remove (new Integer (defID));

        // NicolÃ¡s Charczewski - Neoris Argentina - added 28/03/2006 - Begin
        JahiaEvent je = new JahiaEvent(this, Jahia.getThreadParamBean() != null ? Jahia.getThreadParamBean() : new ProcessingContext(), pageTemplate);
        JahiaEventGeneratorBaseService.getInstance().fireDeleteTemplate(je);
        // NicolÃ¡s Charczewski - Neoris Argentina - added 28/03/2006 - End

    }


    /**
     * Retrieve all the page template identification numbers.
     *
     * @return the page template identification numbers.
     *
     * @throws JahiaException when a database access could not be preformed.
     */
    public List<Integer> getAllPageTemplateIDs ()
            throws JahiaException {
        return templateManager.getAllPageTemplateIDs ();
    }


    /**
     * Returns the unique instance of the page template service
     */
    public static synchronized JahiaPageTemplateBaseService getInstance () {
        if (instance == null) {
            instance = new JahiaPageTemplateBaseService ();
        }
        return instance;
    }


    public JahiaPageDefinition getPageTemplateBySourcePath (int siteID, String path)
            throws JahiaException, JahiaTemplateNotFoundException {

        JahiaPageDefinition template = null;
        int templateID = templateManager.getPageTemplateIDMatchingSourcePath (siteID, path);

        if (templateID != -1) {
            template = lookupPageTemplate (templateID);
        }
        return template;
    }


    /**
     * Retrieve the requested page template related to the <code>user</code> and the
     * jahia <code>siteID</code> site identification number.
     *
     * @param user          the user reference
     * @param siteID        the site identification number
     * @param availableOnly <code>true</code> to retrieve only the current available
     *                      page templates. <code>false</code> to retrieve all the page
     *                      templates the user has access to.
     *
     * @return an Iterator of page templates
     *
     * @throws JahiaException when a general failure occured
     */
    public Iterator<JahiaPageDefinition> getPageTemplates (JahiaUser user, int siteID, boolean availableOnly)
            throws JahiaException {

        TreeMap<String, JahiaPageDefinition> tm = new TreeMap<String, JahiaPageDefinition> ();
        List<Integer> templateIDs = templateManager.getPageTemplateIDs(siteID, availableOnly);
        for (Integer entryKey : templateIDs) {
            JahiaPageDefinition template = templateCache.get (entryKey);
            if(template == null) {
                template = lookupPageTemplate(entryKey.intValue());
            }
            if (ACLResource.checkReadAccess (null, template, user)) {
                tm.put (template.getName (), template);
            }
        }
        List<JahiaPageDefinition> theList = new ArrayList<JahiaPageDefinition>(tm.values ());
        return theList.iterator();
    }


    /**
     * Retireve all the page templates related to the site identification number
     * <code>siteID</code>.
     *
     * @param siteID        the site identification number
     * @param availableOnly <code>true</code> to retrieve only the current available
     *                      page templates. <code>false</code> to retrieve all the page
     *                      templates the user has access to.
     *
     * @return an Iterator of page templates
     *
     * @throws JahiaException when a general failure occured
     */
    public Iterator<JahiaPageDefinition> getPageTemplates (int siteID, boolean availableOnly)
            throws JahiaException {

        TreeMap<String, JahiaPageDefinition> tm = new TreeMap<String, JahiaPageDefinition> ();
        List<JahiaPageDefinition> theList = new ArrayList<JahiaPageDefinition>(tm.values());
        
        return theList.iterator();
    }


    /**
     * Initialize the service.
     *
     * @throws JahiaInitializationException when the service could not be initialized
     */
    public synchronized void start()
            throws JahiaInitializationException {
        logger.debug ("** Initializing the Page Template Service ...");
        // do not allow initialization when the service is still running

        // Initialize the page template cache
        logger.debug ("  - Instanciate the page template cache ...");
        templateCache = cacheService.createCacheInstance(PAGE_TEMPLATE_CACHE);
//            if (templateCache != null)
//                templateCache.registerListener (this);



        // Verify if the needed classes could be successfully instanciated.
        if ((templateCache != null)) {

            logger.debug ("** Page Template Service successfully initialized!");

        } else {
            // invalidate the previous initializations
            templateCache = null;

            // and raise an exception :(
            throw new JahiaInitializationException (
                    "Page Template Service could not be initialized successfully.");
        }

//        try {
//            loadAllPageTemplates ();
//        } catch (JahiaException je) {
//            throw new JahiaInitializationException (
//                    "Error while loading all page templates from database", je);
//        }
    }


    private synchronized void loadAllPageTemplates ()
            throws JahiaException {
        List<Integer> templateIDs = templateManager.getAllPageTemplateIDs ();
        for (int templateID : templateIDs) {

            // the template which could not be found in the database are not
            // loaded.
            try {
                JahiaPageDefinition template = lookupPageTemplate (templateID);
                if (template != null) {
                    templateCache.put (new Integer (template.getID ()), template);
                }

            } catch (JahiaTemplateNotFoundException ex) {
                logger.warn(ex);
                // the page template could not be found, don't add it into the
                // database.

                // This exception should theoreticaly not happen, if it does, the DB
                // is unconsistent.
            }
        }
    }


    public JahiaPageDefinition lookupPageTemplate (int templateID)
            throws JahiaException, JahiaTemplateNotFoundException {
        return null;
    }


    public JahiaPageDefinition lookupPageTemplateByName (String name, int siteID)
            throws JahiaException,
            JahiaTemplateNotFoundException {
        return null;
    }


    public synchronized void stop () {
        //////////////////////////////////////////////////////////////////////////////////////
        // FIXME -Fulco- :
        //   before shutting down the service, a check should be done to know
        //   if a page has an update-lock active. If any active update-lock is
        //   active, the system can not be shutdown !
        //   If the shutdown process can be forced, a message should indicate the editing
        //   user the service is in shutdown process, as soon as he does an action.
        //////////////////////////////////////////////////////////////////////////////////////

        templateCache.flush ();

    }


    // FH   2 May 2001
    // javadocs automaticaly imported.
    //
    public int getNbPageTemplates ()
            throws JahiaException {
        return templateManager.getNbPageTemplates();
    }


    // NK   17 May 2001
    // javadocs automaticaly imported.
    //
    public int getNbPageTemplates (int siteID)
            throws JahiaException {
        return templateManager.getNbPageTemplates (siteID);
    }


    /**
     * returns a DOM representation of all page def of a site
     *
     * @param siteID
     */
    public JahiaDOMObject getPageDefsAsDOM (int siteID) throws JahiaException {

        return null;

    }

    /**
     * returns a DOM representation of all page def props of a site
     *
     * @param siteID
     */
    public JahiaDOMObject getPageDefPropsAsDOM (int siteID) throws JahiaException {

        return null;

    }

    /**
     * Returns a List of all page templates' Acl ID of this site
     * Need this for site extraction
     *
     * @param siteID
     */
    public List<Integer> getAclIDs (int siteID)
            throws JahiaException {
        return templateManager.getAllAclId(siteID);
    }

    // Patch ---------------------------------------------------
    // 30.01.2002 : NK patch for old databases containing templates without ACL
    // 				Do create ACL for them.
    public final void patchTemplateWithoutACL () throws JahiaException {
        /* OUT OF DATE NOW
        JahiaSite site = null;
        JahiaPageDefinition template = null;

        Object[] ids = templateCache.keys ();
        Integer I = null;
        for (int i = 0; i < ids.length; i++) {
            I = (Integer) ids[i];
            template = (JahiaPageDefinition) templateCache.get (I);

            if (template.getAclID () == -1) {
                site =
                        ServicesRegistry.getInstance ().getJahiaSitesService ().getSite (
                                template.getJahiaID ());
                if (site != null) {
                    // Create a new ACL.
                    JahiaBaseACL acl = new JahiaBaseACL ();
                    if (acl != null) {
                        try {
                            acl.create (site.getAclID ());

                            template.setACL (acl.getID ());

                            // set the template default permissions
                            // enable guest users to view the template
                            JahiaACLEntry aclEntry = new JahiaACLEntry (1, 0);
                            JahiaGroup guestGroup = ServicesRegistry.getInstance ()
                                    .getJahiaGroupManagerService ()
                                    .getGuestGroup (site.getID ());
                            acl.setGroupEntry (guestGroup, aclEntry);

                            template.commitChanges ();
                            logger.debug ("Patch : ACL [" + template.getAclID () +
                                    "] has been created for the template :" +
                                    template.getName ());

                        } catch (ACLNotFoundException ex) {
                            logger.warn (ex);
                            throw new JahiaException ("Could not patch ACL for the page def.",
                                    "The parent ACL ID [" + site.getAclID () +
                                    "] could not be found," +
                                    " while trying to patch ACL ( create a new one ) for page def [" +
                                    template.getID () + "]",
                                    JahiaException.TEMPLATE_ERROR,
                                    JahiaException.ERROR_SEVERITY);
                        }
                    } else {
                        logger.debug ("Could not patch ACL for page def.");
                    }
                }
            }
        }*/
    }

    // End Patch -----------------------------------------------


    /**
     * This method is called each time the cache flushes its items.
     *
     * @param cacheName the name of the cache which flushed its items.
     */
    public void onCacheFlush(String cacheName) {
        try {
            if (PAGE_TEMPLATE_CACHE.equals(cacheName)) {
                logger.debug("Page template cache has been flushed, reload the page templates!");
                templateCache.flush(false);
                loadAllPageTemplates();
            }

        } catch (JahiaException ex) {
            logger.warn (ex);
        }
    }

    public void onCachePut(String cacheName, Object entryKey, Object entryValue) {
        // do nothing;
    }

    /**
     * Update page template in database and cache.
     * @param thePageTemplate JahiaPageDefinition
     * @throws JahiaException
     */
    public void updatePageTemplate(JahiaPageDefinition thePageTemplate)
        throws JahiaException {
        templateManager.updatePageTemplate(thePageTemplate);
        templateCache.put(new Integer(thePageTemplate.getID()), thePageTemplate);
    }

    /**
     * Save the page template and create a new Acl if the current is null
     * @param thePageTemplate
     * @throws JahiaException
     */
    public synchronized void createPageTemplateAcl(JahiaPageDefinition thePageTemplate)
        throws JahiaException {

        if ( thePageTemplate.getACL() == null ) {
            // Create a new ACL.
            JahiaBaseACL acl = new JahiaBaseACL ();
            JahiaSite site = null;
            try {
                site = ServicesRegistry.getInstance().getJahiaSitesService()
                        .getSite(thePageTemplate.getJahiaID());
                acl.create (site.getAclID());
                // set the template default permissions
                // enable guest users to view the template
                JahiaAclEntry aclEntry = new JahiaAclEntry (1, 0);
                JahiaGroup guestGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService()
                        .getGuestGroup (site.getID());
                acl.setGroupEntry (guestGroup, aclEntry);
                thePageTemplate.setACL(acl.getID());
                this.updatePageTemplate(thePageTemplate);

                // Nicolás Charczewski - Neoris Argentina - added 28/03/2006 - Begin
                JahiaEvent je = new JahiaEvent(this,Jahia.getThreadParamBean() != null ? Jahia.getThreadParamBean() : new ProcessingContext(), thePageTemplate);
                JahiaEventGeneratorBaseService.getInstance().fireAddTemplate(je);
                // Nicolás Charczewski - Neoris Argentina - added 28/03/2006 - End

            } catch (ACLNotFoundException ex) {
                throw new JahiaException ("Could not create the page def.",
                        "The parent ACL ID [" + site.getAclID() + "] could not be found," +
                                " while trying to create a new page def.",
                        JahiaException.TEMPLATE_ERROR, JahiaException.ERROR_SEVERITY, ex);
            }
        }
    }

    private void populatePageTemplateData(JahiaPageDefinition template) {
        template.setSourcePath(templateManagerService.getTemplateSourcePath(template
                .getName(), template.getJahiaID()));
        template.setDisplayName(templateManagerService.getTemplateDisplayName(template
                .getName(), template.getJahiaID()));
        template.setDescription(templateManagerService.getTemplateDescription(template
                .getName(), template.getJahiaID()));
    }

}