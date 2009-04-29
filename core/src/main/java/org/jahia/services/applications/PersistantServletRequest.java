/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.applications;

import org.apache.log4j.Logger;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.WebAppContext;
import org.jahia.data.applications.ServletBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;

import java.util.*;
import java.io.Serializable;

/**
 * This class is used by the dispatching system to store in this information
 * in a Map. Was not made an innerclass because these can cause problems,
 * and this information might be interesting to store elsewhere.
 */
public class PersistantServletRequest extends ServletBean implements Serializable {
    
    private static final transient Logger logger = Logger
            .getLogger(PersistantServletRequest.class);

    private String appUniqueIDStr;
    private String appURL;
    private String appMethod;
    private String appContextID;
    private String servletMappingPattern;
    private ApplicationBean appBean;


    public PersistantServletRequest (ApplicationBean stdAppBean,
                                     ServletBean servletBean,
                                     String appUniqueIDStr,
                                     String applicationURL,
                                     String applicationMethod,
                                     String applicationContextID) {

        super (
                stdAppBean.getID(),
                servletBean.getWebAppType (),
                servletBean.getName (),
                servletBean.getServletName (),
                servletBean.getservletsrc (),
                servletBean.getContext (),
                servletBean.getdesc ()
        );

        super.setLoaded (servletBean.isLoaded ());
        super.setUrlMappingPattern (servletBean.getUrlMappingPattern ());
        super.setWebAppType (servletBean.getWebAppType ());

        this.appUniqueIDStr = appUniqueIDStr;
        this.appURL = applicationURL;
        this.appMethod = applicationMethod;
        this.appContextID = applicationContextID;
        this.appBean = stdAppBean;
        this.servletMappingPattern = servletBean.getUrlMappingPattern ();
    }

    public String getUniqueIDStr () {
        return appUniqueIDStr;
    }

    public String getURL () {
        return appURL;
    }

    public void setURL (String value) {
        appURL = value;
    }

    public String getMethod () {
        return appMethod;
    }

    public String getContextID () {
        return appContextID;
    }

    public String getServletMappingPattern () {
        return servletMappingPattern;
    }

    public int getVisibleStatus () {
        return appBean.getVisibleStatus ();
    }

    public ApplicationBean getApplicationBean () {
        return appBean;
    }

    /**
     * return a list of user names having a role with this application
     * only on this context
     *
     * @author NK
     * @todo FIXME it would be best if we returned a Set rather than a
     * List, but unfortunately existing applications use this method's
     * result as a List...
     */
    public List getAppContextUsers () {

        List users = new ArrayList();
        Set addedUsers = new HashSet();

        if (appBean == null) {
            return users;
        }

        Iterator appRoles = null;
        try {
            WebAppContext appContext = ServicesRegistry.getInstance ()
                    .getApplicationsManagerService ()
                    .getApplicationContext (appBean.getID ());

            appRoles = appContext.getRoles ().iterator();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return users;
        }

        if (appRoles == null) {
            return users;
        }

        JahiaGroup roleGroup = null;
        String role = null;
        while (appRoles.hasNext ()) {

            role = (String) appRoles.next ();

            // Roles are mapped to Jahia groups.
            StringBuffer buff = new StringBuffer (Integer.toString (appBean.getID ()));
            buff.append ("_");
            buff.append (this.appContextID);
            buff.append ("_");
            buff.append (role);

            roleGroup = ServicesRegistry.getInstance ()
                    .getJahiaGroupManagerService ()
                    .lookupGroup (0, buff.toString ()); // Hollis all app group roles are in site 0
            if (roleGroup != null) {

                Set userMembers = roleGroup.getRecursiveUserMembers ();
                Iterator userIterator = userMembers.iterator ();
                while (userIterator.hasNext ()) {
                    JahiaUser curUser = (JahiaUser) userIterator.next ();
                    if (!addedUsers.contains (curUser.getUserKey ())) {
                        addedUsers.add (curUser.getUserKey ());
                        users.add (curUser.getUsername ());
                    }
                }
            }
        }

        return users;
    }

}
