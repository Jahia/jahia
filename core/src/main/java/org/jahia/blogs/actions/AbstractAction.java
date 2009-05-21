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
package org.jahia.blogs.actions;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jahia.blogs.ServletResources;
import org.jahia.blogs.model.MetaPostInfo;
import org.jahia.blogs.model.PostInfo;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentContainerListsXRefManager;
import org.jahia.content.ContentObject;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ProcessingContextFactory;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.fields.ContentField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaSiteUserManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.springframework.beans.factory.BeanFactory;

/**
 * Base Action implementing common methods and setting common resources. All
 * Concrete Actions should subclass it and implement the execute method.
 *
 * @author Xavier Lawrence
 */
public abstract class AbstractAction {
    
    protected String appKey;
    protected transient String userName;
    protected transient String password;
    
    protected ServicesRegistry servicesRegistry;
    protected JahiaContainersService containerService;
    protected ProcessingContext jParams;
    
    protected BlogDefinitionNames containerNames;
    
    // log4j logger
    static Logger log = Logger.getLogger(AbstractAction.class);
    
    /**
     * Execute the method in Jahia.
     * @return The return value depends on the method specification. See
     *         Blogger API spec and MetaWeblog API spec.
     *
     * @throws JahiaException If something goes wrong
     */
    public abstract Object execute() throws JahiaException;
    
    /**
     * Initializes common resources needed to execute the Action
     *
     * @throws JahiaException If something goes wrong
     */
    protected void init() throws JahiaException {
        // Get ServicesRegistry Singleton object to retreive services
        servicesRegistry = ServicesRegistry.getInstance();
        
        // ContainerService to get the content
        containerService = servicesRegistry.getJahiaContainersService();
        
        // Create the ProcessingContext
        BeanFactory bf = SpringContextSingleton.getInstance().getContext();
        ProcessingContextFactory pcf = (ProcessingContextFactory) bf.getBean(ProcessingContextFactory.class.getName());
        jParams = pcf.getContext(ServletResources.getCurrentRequest(), ServletResources.getCurrentResponse(), ServletResources.getCurrentConfig().getServletContext());

        /*
        jParams = new ParamBean(
                ServletResources.getCurrentRequest(),
                ServletResources.getCurrentResponse(),
                ServletResources.getCurrentConfig().getServletContext(),
                org.jahia.settings.SettingsBean.getInstance(),
                System.currentTimeMillis(),
                ProcessingContext.GET_METHOD);
        */

        // Load the container definition names from the Porpeties file
        containerNames = new BlogDefinitionNames(jParams);
        log.debug("Init sucessfull");
    }

    /**
     * Checks the login information of the user sending the blog request.
     *
     * @return The autenticated JahiaUser
     * @throws JahiaException If the user provided wrong login info
     */
    protected JahiaUser checkLogin() throws JahiaException {
        
        // UserManagerService to authenticate the user
        final JahiaSiteUserManagerService userManagerService = servicesRegistry.
                getJahiaSiteUserManagerService();
        
        // Check if the user has site access
        final JahiaUser theUser = userManagerService.getMember(
                jParams.getSiteID(), userName);
        if (theUser != null) {
            if (!theUser.verifyPassword(password)) {
                log.warn("Couldn't validate password for user " +
                        theUser.getUserKey() + "!");                
                throw new JahiaException("Login error",
                        "User " + userName + " entered bad password",
                        JahiaException.SECURITY_ERROR,
                        JahiaException.WARNING_SEVERITY);
                
            }
            jParams.purgeSession();
            jParams.setUser(theUser);
            
        } else {
            throw new JahiaException("Login error",
                    "Login error: Unknown User "+userName,
                    JahiaException.SECURITY_ERROR,
                    JahiaException.WARNING_SEVERITY);
        }
        
        log.debug("Login sucessfull");
        return theUser;
    }
    
    /**
     * Loads a specific container, checks its field structure and sets its
     * language. Basically used when editting or creating the content of a 
     * container is needed.
     * @param id The Container ID
     * @param languageCode The Language Code to set the loaded container
     * 
     * @return The loaded JahiaContainer
     * @throws JahiaException If the container does not exist
     */
    protected JahiaContainer getContainer(int id, String languageCode)
    throws JahiaException {
        JahiaContainer postContainer = containerService.loadContainer(
                id, LoadFlags.ALL, jParams, EntryLoadRequest.STAGED);
        
        if (postContainer == null) {
            throw new JahiaException("Post: "+id+
                    " does not exist", "Container: "+id+ " does not exist",
                    JahiaException.ENTRY_NOT_FOUND,
                    JahiaException.WARNING_SEVERITY);
        }
        
        if (languageCode != null) {
            postContainer.setLanguageCode(languageCode);
            postContainer.fieldsStructureCheck(jParams);
        }
        return postContainer;
    }

    /**
     * Loads a specific container without any field structure check. Basically
     * used to read the content of a container without making changes to it.
     * @param id The Container ID
     * 
     * @return The loaded JahiaContainer or null if it does not exist
     * @throws JahiaException If something goes wrong
     */
    protected JahiaContainer getContainer(int id)
    throws JahiaException {
        EntryLoadRequest elr = new EntryLoadRequest(EntryLoadRequest.
                STAGING_WORKFLOW_STATE, 
                0, 
                jParams.getEntryLoadRequest().getLocales());
        EntryLoadRequest savedEntryLoadRequest = 
            jParams.getSubstituteEntryLoadRequest();
        jParams.setSubstituteEntryLoadRequest(elr);
        JahiaContainer postContainer = containerService.loadContainer(
                id, LoadFlags.ALL, jParams, elr);
        jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
        return postContainer;
    }
    
    /**
     * Flushes the cache of a Jahia page.
     * @param theContainer The Container of the page to be flushed
     * 
     * @throws JahiaException If something goes wrong
     */
    protected void flushPageCacheThatDisplayContainer(JahiaContainer
            theContainer) throws JahiaException {
        
        log.debug("Flushing cache...");
        
        EntryLoadRequest loadVersion = EntryLoadRequest.CURRENT;
        if (servicesRegistry.getJahiaVersionService().
                isStagingEnabled(theContainer.getJahiaID())) {
            loadVersion = EntryLoadRequest.STAGED;
        }
        
        JahiaContainerList theList = containerService.
                loadContainerListInfo(theContainer.
                getListID(), loadVersion);
        
        // since we have made modifications concerning this page, let's flush
        // the content cache for all the users and browsers as well as all
        // pages that display this containerList...
        if (theList != null) {
            Set containerPageRefs = ContentContainerListsXRefManager.
                    getInstance().
                    getAbsoluteContainerListPageIDs(
                    theList.getID());
            if (containerPageRefs != null) {
                Iterator pageRefIDs = containerPageRefs.iterator();
                while (pageRefIDs.hasNext()) {
                    Integer curPageID = (Integer) pageRefIDs.next();
                }
            } else {
                log.debug("Why is cross ref list empty ?");
            }
        } else {
            log.debug("Couldn't retrieve parent containerList, why is that ?");
        }
    }
    
    /**
     * Changes the page of the ProcessingContext Object
     * @param pageID The ID of the new page
     * 
     * @return The new content page object
     * @throws JahiaException If the new page does not exist
     */
    protected ContentPage changePage(int pageID) throws JahiaException {
        ContentPage blogContentPage = ContentPage.getPage(pageID);
        
        if (blogContentPage == null) {
            throw new JahiaException(
                    "Blog: "+pageID+ " does not exist",
                    "Page: "+pageID+ " does not exist",
                    JahiaException.ENTRY_NOT_FOUND,
                    JahiaException.WARNING_SEVERITY);
        }
        jParams.setSite(blogContentPage.getSite());
        jParams.setSiteID(blogContentPage.getSite().getID());
        jParams.setSiteKey(blogContentPage.getSite().getSiteKey());
        jParams.changePage(blogContentPage);
        return blogContentPage;
    }
    
    /**
     * Activates a specified container. Note that the user needs write and
     * administration access on that container.
     * @param containerID The ID of the container to activate
     * @param user The JahiaUser requesting the activation
     *
     * @return The ActivationTestResults of the activation request
     * @throws JahiaException If something goes wrong
     */
    protected ActivationTestResults activateContainer(int containerID, 
            JahiaUser user) 
    throws JahiaException {
        log.debug("activating container: "+containerID);
        
        Set<String> languageCodes = new HashSet<String>();
        languageCodes.add(ContentObject.SHARED_LANGUAGE);
        languageCodes.add(jParams.getLocale().toString());
        
        JahiaSaveVersion saveVersion = servicesRegistry.getJahiaVersionService().
                getSiteSaveVersion(jParams.getSiteID());
        
        StateModificationContext smc = new StateModificationContext(
                new ContentContainerKey(containerID), languageCodes);
        smc.setDescendingInSubPages(false);
        
        JahiaContainer container = this.getContainer(containerID); 
        Iterator<JahiaField> childs = container.getFields();
           
        while (childs.hasNext()) {
            JahiaField child = childs.next();       
            ContentField field = child.getContentField();
            
            field.activate(languageCodes, saveVersion.getVersionID(),
                    jParams, smc);
        }  
              
        ActivationTestResults res = containerService.activateStagedContainer(
                languageCodes, containerID, user, saveVersion, jParams, smc); 
        
        log.debug(res);      
        return res;
    }
    
    /**
     * Activates a specified containerList. Note that the user needs write and
     * administration access on that containerList.
     * @param containerListID The ID of the containerList to activate
     * @param user The JahiaUser requesting the activation
     *
     * @return The ActivationTestResults of the activation request
     * @throws JahiaException If something goes wrong
     */
    protected ActivationTestResults activateContainerList(int containerListID,
            JahiaUser user, int pageID) throws JahiaException {
        log.debug("activating containerList: "+containerListID);
        
        Set<String> languageCodes = new HashSet<String>();
        languageCodes.add(ContentObject.SHARED_LANGUAGE);
        languageCodes.add(jParams.getLocale().toString());
        
        JahiaSaveVersion saveVersion = servicesRegistry.getJahiaVersionService().
                getSiteSaveVersion(jParams.getSiteID());
        
        StateModificationContext smc = new StateModificationContext(
                new ContentContainerListKey(containerListID), languageCodes);
        smc.setDescendingInSubPages(false);
        
        ActivationTestResults res = containerService.activateStagedContainerLists(
                languageCodes, pageID, user, saveVersion, smc);
        
        log.debug(res);
        return res;
    }
    
    /**
     * Creates a Map containing the information about a post according
     * to the Blogger API
     * @param postContainer The Container representing the post
     * 
     * @return The post information in a Map object
     * @throws JahiaException If something goes wrong
     */
    protected Map<String, Comparable> createPostInfo(JahiaContainer postContainer)
    throws JahiaException {
        Map<String, Comparable> postInfo = new HashMap<String, Comparable>(4);
        String fieldName = containerNames.getValue(BlogDefinitionNames.POST_BODY);
        
        StringBuffer buffer = new StringBuffer();
        buffer.append("<"); 
        buffer.append(containerNames.getValue(BlogDefinitionNames.POST_TITLE));
        buffer.append(">");
        buffer.append(postContainer.getFieldValue(containerNames.getValue(
                BlogDefinitionNames.POST_TITLE), "n/a"));
        buffer.append("</"); 
        buffer.append(containerNames.getValue(BlogDefinitionNames.POST_TITLE));
        buffer.append(">");
        
        buffer.append(this.getValue(postContainer.getField(fieldName)));
        postInfo.put(PostInfo.CONTENT, buffer.toString());
        
        fieldName = containerNames.getValue(BlogDefinitionNames.POST_AUTHOR);
        JahiaUser author = servicesRegistry.getJahiaSiteUserManagerService().
                getMember(jParams.getSiteID(), postContainer.
                getFieldValue(fieldName, "guest"));
        
        String userName;
        if (author == null) {
            userName = "guest";
            
        } else {
            userName = author.getUsername();
        }
        
        postInfo.put(MetaPostInfo.USER_ID, userName);
        
        postInfo.put(PostInfo.POST_ID, Integer.toString(postContainer.getID()));
        
        fieldName = containerNames.getValue(BlogDefinitionNames.POST_DATE);
        String dateValue = (String)postContainer.getField(fieldName).getObject();
        Date date = new Date(Long.parseLong(dateValue));
        postInfo.put(PostInfo.DATE_CREATED, date);
        
        return postInfo;
    }
    
    /**
     * Creates a Map containing the information about a post according
     * to the MetaWeblog API
     * @param postContainer The Container representing the post
     * @param categories The Set of categories of the post
     * 
     * @return The post information in a Map object
     * @throws JahiaException If something goes wrong
     */
    protected Map<String, Object> createMetaPostInfo(JahiaContainer postContainer, 
            Set categories) throws JahiaException {
        
        Map<String, Object> postInfo = new HashMap<String, Object>(8);
        
        String fieldName = containerNames.getValue(BlogDefinitionNames.POST_TITLE);
        postInfo.put(MetaPostInfo.TITLE,
                postContainer.getFieldValue(fieldName, "n/a"));

        
        fieldName = containerNames.getValue(BlogDefinitionNames.POST_EXCERPT);
        JahiaField f = postContainer.getField(fieldName);
        if (f != null) {
            postInfo.put(MetaPostInfo.MT_EXCERPT, 
                    postContainer.getFieldValue(fieldName, "n/a"));
        }
        
        
        fieldName = containerNames.getValue(BlogDefinitionNames.POST_KEYWORDS);
        f = postContainer.getField(fieldName);
        if (f != null) {
            postInfo.put(MetaPostInfo.MT_EXCERPT,
                    postContainer.getFieldValue(fieldName, "n/a"));
        }
        
        fieldName = containerNames.getValue(BlogDefinitionNames.POST_BODY);
        postInfo.put(MetaPostInfo.DESCRIPTION,
                this.getValue(postContainer.getField(fieldName)));

        fieldName = containerNames.getValue(BlogDefinitionNames.POST_AUTHOR);
        JahiaUser author = servicesRegistry.getJahiaSiteUserManagerService().
                getMember(jParams.getSiteID(), postContainer.
                getFieldValue(fieldName, "guest"));
        
        String userName;
        if (author == null) {
            userName = "guest";
            
        } else {
            userName = author.getUsername();
        }
        
        postInfo.put(MetaPostInfo.USER_ID, userName);
        
        postInfo.put(MetaPostInfo.POST_ID, Integer.toString(postContainer.
                getID()));
        
        fieldName = containerNames.getValue(BlogDefinitionNames.POST_DATE);
        String dateValue = (String)postContainer.getField(fieldName).getObject();
        
        if (dateValue == null || dateValue.length() < 2) {
            log.warn("No date for Container: "+ postContainer.getID());
            dateValue = "0";
        }
        
        final Date date = new Date(Long.parseLong(dateValue));
        postInfo.put(MetaPostInfo.DATE_CREATED, date);
        
        final JahiaPage blogPage = ContentPage.getPage(postContainer.getPageID()).
                getPage(jParams);
        
        final String url =  getContainerURL(blogPage, postContainer);
        postInfo.put(MetaPostInfo.LINK, url);
        
        postInfo.put(MetaPostInfo.PERMANENT_LINK, url);
        
        if (categories != null && categories.size() > 0) {
            postInfo.put(MetaPostInfo.CATEGORIES, fromSet(categories));
        }
        
        return postInfo;
    }
    
    /**
     * Transforms a Set of category IDs into a List of IDs
     */
    protected List<String> fromSet(Set categories) {
        
        if (categories == null) return new ArrayList<String>(0);
        
        List<String> cats = new ArrayList<String>(categories.size());
        Iterator ite = categories.iterator();
        while (ite.hasNext()) {
            Category cat = (Category)ite.next();
            String catName = cat.getTitle(jParams.getLocale());
            
            if (catName == null || catName.length() < 1) {
                catName = cat.getKey();
            }
            cats.add(catName);
        }
        return cats;
    }
    
    /**
     * Creates a Map containing the information about a post according
     * to the MovableType API
     * @param postContainer The Container representing the post
     * 
     * @return The post information in a Map object
     * @throws JahiaException If something goes wrong
     */
    protected Map<String, Comparable> createMovableTypePostInfo(JahiaContainer postContainer) 
    throws JahiaException {
        
        Map<String, Comparable> postInfo = new HashMap<String, Comparable>(4);
        
        String fieldName = containerNames.getValue(BlogDefinitionNames.POST_TITLE);
        postInfo.put(MetaPostInfo.TITLE, postContainer.getField(fieldName).
                getValue());
        
        fieldName = containerNames.getValue(BlogDefinitionNames.POST_AUTHOR);
        JahiaUser author = servicesRegistry.getJahiaSiteUserManagerService().
                getMember(jParams.getSiteID(), postContainer.getField(fieldName).
                getValue());
        
        String userName;
        if (author == null) {
            userName = "guest";
            
        } else {
            userName = author.getUsername();
        }
        
        postInfo.put(MetaPostInfo.USER_ID, userName);
        
        fieldName = containerNames.getValue(BlogDefinitionNames.POST_DATE);
        String dateValue = (String)postContainer.getField(fieldName).getObject();
        
        if (dateValue == null || dateValue.length() < 2) {
            log.warn("No date for Container: "+ postContainer.getID());
            dateValue = "0";
        }
        
        final Date date = new Date(Long.parseLong(dateValue));
        postInfo.put(MetaPostInfo.DATE_CREATED, date);
        
        postInfo.put(MetaPostInfo.POST_ID, String.valueOf(postContainer.getID()));
        
        return postInfo;
    }
    
    /**
     * Constructs the pageURL of a JahiaPage
     * @param page The page to construct the url
     * 
     * @return A String representing the URL
     * @throws JahiaException If something goes wrong
     */
    protected String getPageURL(JahiaPage page) throws JahiaException {
        StringBuffer buffer = new StringBuffer();
        buffer.append(ServletResources.getCurrentRequest().
                getScheme());
        buffer.append("://");
        buffer.append(ServletResources.getCurrentRequest().
                getServerName());
        buffer.append(":");
        buffer.append(ServletResources.getCurrentRequest().
                getServerPort());
        buffer.append(page.getURL(jParams));
        return buffer.toString();
    }
    
    /**
     * Constructs the URL of a JahiaContainer
     * @param page The parent page of the Container
     * @param container The Container to construct the URL
     * 
     * @return A String representing the URL
     * @throws JahiaException If something goes wrong
     */
    protected String getContainerURL(JahiaPage page, JahiaContainer container) 
    throws JahiaException {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getPageURL(page));
        buffer.append("?entryId=");
        buffer.append(container.getID());
        return buffer.toString();
    } 
    
    /**
     * Sets the categories for a JahiaContainer (ie a post)
     * @param categories The List containing the category keys
     * @param postContainer The container to add the categories to
     *
     * @throws JahiaException If something goes wrong
     */
     protected void setCategories(List categories, 
             JahiaContainer postContainer) throws JahiaException {
         
         if (categories == null) return;
         
         Set oldCats = Category.getObjectCategories(postContainer.
                 getContentContainer().getObjectKey());
         
         if (categories.size() == 0) {
            clearCategories(postContainer);
            return;
         }
         
         Iterator ite = categories.iterator();
         int i = 0;
         while (ite.hasNext()) {
             String catKey = (String)ite.next();
             
             // the category is allready set for that post
             if (oldCats.contains(catKey)) continue;
             
             Category cat = Category.getCategory(catKey, jParams.getUser());
             
             if (cat != null) {               
                 log.debug("Adding category: "+catKey);
                 cat.addChildObjectKey(postContainer.
                         getContentContainer().getObjectKey());
                 i++;
             }
         } 
         
         // Check if we need to remove some categories
         if ((oldCats.size() + i) > categories.size()) {
             Iterator ite2 = oldCats.iterator();
             
             while (ite2.hasNext()) {
                 Category cat = (Category)ite2.next();
                 
                 // Don't need to remove that one
                 if (categories.contains(cat.getKey())) continue;
                 
                 log.debug("Removing category: "+cat.getKey());
                 cat.removeChildObjectKey(postContainer.
                         getContentContainer().getObjectKey());
             }
         }
     }
     
     /**
      * Removes all the categories of a given Container (ie post)
      */
     private void clearCategories(JahiaContainer postContainer)
     throws JahiaException {
        Set oldCats = Category.getObjectCategories(postContainer.
                 getContentContainer().getObjectKey());
        
        Iterator ite = oldCats.iterator();
        while(ite.hasNext()) {
             String catKey = (String)ite.next();
             Category cat = Category.getCategory(catKey, jParams.getUser());
             
             if (cat != null) {               
                 cat.removeChildObjectKey(postContainer.
                         getContentContainer().getObjectKey());
             }
        }
     }
     
     /**
      * Sets the value og a BigText field. The value stored will be the same
      * as the one passed as argument but wrapped between html tags.
      * @param field The BigText field to set the value
      * @param value The value of the field
      *
      * @throws JahiaException If something goes wrong
      */
     protected void setValue(JahiaField field, String value)
     throws JahiaException {
         StringBuffer buffer = new StringBuffer();
         buffer.append("<html>");
         buffer.append(value);
         buffer.append("</html>");
         field.setValue(buffer.toString());
     }
     
     /**
      * Gets the value og a BigText field. The value returned will be the same
      * as the one stored but minus the wrapping html tags.
      * @param field The BigText field to get the value of
      *
      * @return The value of the field 
      * @throws JahiaException If something goes wrong
      */
     protected String getValue(JahiaField field)
     throws JahiaException {
         if (field == null) return "n/a";
         String value = field.getValue();
         if (value == null || value.length() == 0) return "n/a";
         if (value.indexOf("<html>") != -1) {
             String tag = "<html>";
             String endTag = "</html>";
             // contains the value without the html tags
             value = value.substring(value.indexOf(tag) + tag.length(),
                     value.indexOf(endTag));
         }   
         return value;    
     }
}
