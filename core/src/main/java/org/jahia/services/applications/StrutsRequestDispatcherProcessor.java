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

 package org.jahia.services.applications;

import org.apache.struts.Globals;

import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;

/**
 * For request forward and include pre/post processing
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class StrutsRequestDispatcherProcessor implements
        RequestDispatcherProcessor {
    
    private boolean preProcessed = false;
    private boolean postProcessed = false;
    
    private static String[] attributeNames;
    private static String[] attributeScopes;
    
    private List attributes;
    
    
    static {
        
        String[] names = {Globals.ACTION_SERVLET_KEY,
                // Globals.APPLICATION_KEY,
                Globals.CANCEL_KEY,
                Globals.DATA_SOURCE_KEY,
                Globals.ERROR_KEY,
                Globals.EXCEPTION_KEY,
                // Globals.FORM_BEANS_KEY,
                // Globals.FORWARDS_KEY,
                Globals.LOCALE_KEY,
                Globals.MAPPING_KEY,
                // Globals.MAPPINGS_KEY,
                Globals.MESSAGE_KEY,
                Globals.MESSAGES_KEY,
                Globals.MODULE_KEY,
                Globals.MULTIPART_KEY,
                Globals.PLUG_INS_KEY,
                Globals.REQUEST_PROCESSOR_KEY,
                Globals.SERVLET_KEY,
                Globals.TRANSACTION_TOKEN_KEY};
        attributeNames = names;
                
        String[] scopes = {StrutsGlobalAttribute.REQUEST_SCOPE,
                        // StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.REQUEST_SCOPE,
                        //  StrutsGlobalAttribute.REQUEST_SCOPE,
                        //  StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.SESSION_SCOPE,
                        StrutsGlobalAttribute.REQUEST_SCOPE,
                        //  StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.REQUEST_SCOPE,
                        StrutsGlobalAttribute.SESSION_SCOPE};
        attributeScopes = scopes;                  
    }
    
    public StrutsRequestDispatcherProcessor() {
        initAttributes();
    }
    
    public void preForward(ServletIncludeRequestWrapper request,
            ServletIncludeResponseWrapper response)
            throws ServletException, java.io.IOException {
        backupGlobalAttributes(request, response);
    }
    
    public void postForward(ServletIncludeRequestWrapper request,
            ServletIncludeResponseWrapper response)
            throws ServletException, java.io.IOException {
        restoreGlobalAttributes(request, response);
    }
    
    public void preInclude(ServletIncludeRequestWrapper request,
            ServletIncludeResponseWrapper response)
            throws ServletException, java.io.IOException {
        backupGlobalAttributes(request, response);
    }
    
    public void postInclude(ServletIncludeRequestWrapper request,
            ServletIncludeResponseWrapper response)
            throws ServletException, java.io.IOException {
        restoreGlobalAttributes(request, response);
    }
    
    private void backupGlobalAttributes(ServletIncludeRequestWrapper request,
            ServletIncludeResponseWrapper response)
            throws ServletException, java.io.IOException {
        if (!preProcessed) {
            int size = this.attributes.size();
            StrutsGlobalAttribute at = null;
            for (int i = 0; i < size; i++) {
                at = (StrutsGlobalAttribute) this.attributes.get(i);
                at.backupAttribute(request, response);
            }
        }
    }
    
    private void restoreGlobalAttributes(ServletIncludeRequestWrapper request,
            ServletIncludeResponseWrapper response)
            throws ServletException, java.io.IOException {
        if (!postProcessed) {
            int size = this.attributes.size();
            StrutsGlobalAttribute at = null;
            for (int i = 0; i < size; i++) {
                at = (StrutsGlobalAttribute) this.attributes.get(i);
                at.restoreAttribute(request, response);
            }
        }
    }
    
    /**
     *
     */
    private void initAttributes() {
        this.attributes = new ArrayList();
        
        int length = attributeNames.length;
        for (int i = 0; i < length; i++) {
            StrutsGlobalAttribute at =
                    new StrutsGlobalAttribute(attributeNames[i], attributeScopes[i]);
            this.attributes.add(at);
        }
    }
}
