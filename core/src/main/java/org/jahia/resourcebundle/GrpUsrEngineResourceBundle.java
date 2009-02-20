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

//
//
// NK 18.02.2002 - added in Jahia
//

package org.jahia.resourcebundle;

import java.security.Principal;
import java.security.acl.Group;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.JahiaConsole;


/**
 * Tools to handles Pages engine resource bundle for a given principal.
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class GrpUsrEngineResourceBundle
{

    private static final String CLASS_NAME =
                GrpUsrEngineResourceBundle.class.getName();

    private static GrpUsrEngineResourceBundle mInstance ;

    private Map usrResources ;
    private Map grpResources ;


    private GrpUsrEngineResourceBundle(){
        usrResources = new HashMap();
        grpResources = new HashMap();
    }

    public static synchronized GrpUsrEngineResourceBundle getInstance(){

        if ( mInstance == null ){
            mInstance = new GrpUsrEngineResourceBundle();
        }
        return mInstance;
    }

    /**
     * Add a resource bundle
     *
     * @param JahiaPage page
     * @param Principal p, a JahiaUser or JahiaGroup
     * @param ResourceBundle res
     */
    public void addResourceBundle(ContentPage contentPage, Principal p, ResourceBundle res){

        if ( contentPage == null || res == null || p == null )
            return;

        boolean isGroup = ( p instanceof Group );
        String key;
        if ( isGroup ){
            key = ((JahiaGroup)p).getGroupKey();
            JahiaConsole.println(CLASS_NAME+".addResourceBundle","Added grp res. bun. : " + key ) ;
            grpResources.put( JahiaResourceBundle.DEFAULT_INTERNAL_RESOURCE_BUNDLE + "_" + contentPage.getID() + "_" + key , res );
        } else {
            key = ((JahiaUser)p).getUserKey();
            JahiaConsole.println(CLASS_NAME+".addResourceBundle","Added user res. bun. : " + key ) ;
            usrResources.put( JahiaResourceBundle.DEFAULT_INTERNAL_RESOURCE_BUNDLE + "_" + contentPage.getID() + "_" + key , res );
        }
    }

    /**
     * Returns a resource bundle for a given page and a given principal
     *
     * @param JahiaPage page
     * @param Principal p, a JahiaUser or JahiaGroup
     * @return ResourceBundle res
     */
    public ResourceBundle getResourceBundle(ContentPage contentPage, Principal p){

        if ( contentPage == null || p == null )
            return null;

        boolean isGroup = ( p instanceof Group );
        String key;
        if ( isGroup ){
            key = ((JahiaGroup)p).getGroupKey();
            //JahiaConsole.println(CLASS_NAME+".getResourceBundle","For group : " + key ) ;
        } else {
            key = ((JahiaUser)p).getUserKey();
            //JahiaConsole.println(CLASS_NAME+".getResourceBundle","For user : " + key ) ;
        }

        ResourceBundle res;
        if ( !isGroup ){
            res = (ResourceBundle)usrResources.get( JahiaResourceBundle.DEFAULT_INTERNAL_RESOURCE_BUNDLE
                              + "_" + contentPage.getID() + "_" + key );
            if ( res == null )
                res = getGrpResourceBundle(contentPage,p);
        } else {
            res = getGrpResourceBundle(contentPage,p);
        }
        return res;
    }

    /**
     * Returns a resource bundle for a given JahiaUser looking at its group membership
     *
     * @param JahiaPage page
     * @param Principal p, a JahiaUser or JahiaGroup
     * @return ResourceBundle res
     */
    private ResourceBundle getGrpResourceBundle(ContentPage contentPage, Principal p){

        //JahiaConsole.println(CLASS_NAME+".getGrpResourceBundle()","Started looking for page :" + page.getID());

        if ( contentPage == null || p == null )
            return null;

        JahiaGroup grp;
        String grpKey;
        String key ;
        String resName = JahiaResourceBundle.DEFAULT_INTERNAL_RESOURCE_BUNDLE + "_" + contentPage.getID() + "_";
        ResourceBundle res = null;
        Iterator keys = grpResources.keySet().iterator();
        while (keys.hasNext()){
            grpKey = "";
            key = (String)keys.next();
            //JahiaConsole.println(CLASS_NAME+".getGrpResourceBundle()","res key found :" + key);

            if ( key.startsWith(resName) ){
                grpKey = key.substring(resName.length(),key.length());
                //JahiaConsole.println(CLASS_NAME+".getGrpResourceBundle()","groupkey :" + grpKey);

                grp = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(grpKey);
                if ( grp != null && grp.isMember(p)){
                    //JahiaConsole.println(CLASS_NAME+".getGrpResourceBundle()","user " + ((JahiaUser)p).getUsername() + " is member of group :" + grpKey);
                    res = (ResourceBundle)grpResources.get(key);
                    if ( res != null )
                        return res;
                }
            }
        }
        return res;
    }

}

