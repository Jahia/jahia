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
