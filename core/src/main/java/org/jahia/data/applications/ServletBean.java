/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

 package org.jahia.data.applications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletMode;

import org.jahia.registries.ServicesRegistry;

/**
 * This object contains all the data relative to a servlet, notably the
 * context in which it should run, it's type (servlet or JSP) and additionnal
 * @author Serge Huber
 * @version 1.0
 */
public class ServletBean implements Serializable, EntryPointDefinition {

    public final static int SERVLET_TYPE = 1;
    public final static int JSP_TYPE = 2;
    public final static String SERVLET = "SERVLET";
    public final static String JSP = "JSP";
    private boolean isWelcomeFile = false;    

    private String name; // display name
    private String context; // the servlet context
    private String servletName; // Servlet alias  ex: HelloWorldServlet
    private String servletsrc; // Servlet classe or jsp file ex: /jsp/HelloWorld.jsp ou org.jahia.HelloWorldServlet
    private int webAppType = SERVLET_TYPE; // 1=servlet, 2=jsp
    private String desc; // desc
    private String urlMappingPattern = ""; // the mapping pattern that matched this servlet
    private String applicationID = "";

    /** if loaded in the Aplication registry or not
     that is instantiated
     **/
    private boolean loaded = false;

    /**
     *
     */
    public ServletBean (String applicationID,
                        int webAppType,
                        String name,
                        String servletName,
                        String servletsrc,
                        String context,
                        String desc
                        ) {
        this.applicationID = applicationID;
        this.webAppType = webAppType;
        this.name = name;
        this.context = context;
        this.servletName = servletName;
        this.servletsrc = servletsrc;
        this.desc = desc;

    } // end constructor

    /**
     * accessor methods
     * {
     */
    public String getName () {return name;
    }

    /**
     * Get display name. Locale is not supported. Return the name
     * @param locale
     * @return
     */
    public String getDisplayName(java.util.Locale locale) {
        // name and displayName are same
        return getName();
    }

    /**
     * Get description. Locale is not supported
     * @param locale
     * @return
     */
    public String getDescription(java.util.Locale locale) {
        return desc;
    }

    public String getContext () {return context;
    }

    public String getServletName () {return servletName;
    }

    public String getservletsrc () {return servletsrc;
    }

    public int getWebAppType () {return webAppType;
    }

    public String getdesc () {return desc;
    }

    public String getUrlMappingPattern () {return urlMappingPattern;
    }

    public boolean isLoaded () {return loaded;
    }
    public  boolean isWelcomeFile() {   
        return isWelcomeFile;       
    }    

    public String getApplicationID () {
        return applicationID;
    }

    public String getWebAppTypeLabel () {

        if (webAppType == SERVLET_TYPE) {
            return SERVLET;
        }
        return JSP;
    }

    public void setName (String name) {this.name = name;
    }

    public void setContext (String context) {this.context = context;
    }

    public void setServletName (String servletName) {this.servletName =
        servletName;
    }

    public void setservletsrc (String servletsrc) {this.servletsrc = servletsrc;
    }

    public void setWebAppType (int webAppType) {this.webAppType = webAppType;
    }

    public void setUrlMappingPattern (String pattern) {this.urlMappingPattern =
        pattern;
    }

    public void setdesc (String desc) {this.desc = desc;
    }

    public void setLoaded (boolean loaded) {this.loaded = loaded;
    }
    
    public void setIsWelcomeFile(boolean isWelcomeFile) {
        this.isWelcomeFile = isWelcomeFile;
    }    

    public List getPortletModes() {
        List portletModes = new ArrayList();
        portletModes.add(PortletMode.VIEW);
        return portletModes;
    }

    public List getWindowStates() {
        return ServicesRegistry.getInstance().getApplicationsManagerService().getSupportedWindowStates();
    }

} // end ServletBean
