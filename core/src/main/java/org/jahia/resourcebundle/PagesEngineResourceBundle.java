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

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.jahia.params.ProcessingContext;
import org.jahia.services.pages.ContentPage;


/**
 * Tools to handles Pages engine resource bundle.
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class PagesEngineResourceBundle
{

    private static PagesEngineResourceBundle mInstance ;

    private Map resources ;


    private PagesEngineResourceBundle(){
        resources = new HashMap();
    }


    public static synchronized PagesEngineResourceBundle getInstance(){

        if ( mInstance == null ){
            mInstance = new PagesEngineResourceBundle();
        }
        return mInstance;
    }


    /**
     * Add a resource bundle
     *
     * @param JahiaPage page
     * @param ResourceBundle res
     * @param ParamBean jParams
     */
    public void addResourceBundle(ContentPage contentPage, ResourceBundle res, ProcessingContext jParams ){

        if ( contentPage == null || res == null || jParams == null )
            return;
        resources.put( JahiaResourceBundle.ENGINE_DEFAULT_RESOURCE_BUNDLE + "_" + contentPage.getID(), res );
    }

    /**
     * Returns a resource bundle for a given page
     *
     * @param JahiaPage page
     * @return ResourceBundle res
     * @param ParamBean jParams
     */
    public ResourceBundle getResourceBundle(ContentPage contentPage, ProcessingContext jParams ){

        if ( contentPage == null || jParams == null )
            return null;

        ResourceBundle res = (ResourceBundle)resources.get( JahiaResourceBundle.ENGINE_DEFAULT_RESOURCE_BUNDLE
                              + "_" + contentPage.getID() );
        return res;
    }

}

