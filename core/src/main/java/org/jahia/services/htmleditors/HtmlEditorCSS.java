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
//

package org.jahia.services.htmleditors;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

/**
 * <p>Title: HtmlEditor Editor CSS</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia S.A.R.L</p>
 * @author Khue Nguyen
 * @version 1.0
 */

public class HtmlEditorCSS implements Serializable {

    private String id;
    private String name;
    private String url;
    private String stylesDef;
    private boolean shared; // shared to all sites
    private Set allowedSites = new HashSet();
    private Set allowedTemplateSets = new HashSet();

    /**
     *
     * @param id
     * @param name
     * @param url
     * @param shared
     */
    public HtmlEditorCSS(String id, String name, String url, String stylesDef, boolean shared){
        this.id = id;
        this.name = name;
        this.url = url;
        this.shared = shared;
        this.stylesDef = stylesDef;
    }

    /**
     * Returns the unique identifier.
     *
     * @return the unique identifier
     */
    public String getId(){
        return this.id;
    }

    /**
     * Returns the visual Display Name
     *
     * @return the visual Display name
     */
    public String getName(){
        return this.name;
    }

    /**
     * Returns the CSS url
     *
     * @return the CSS url
     */
    public String getURL(){
        return this.url;
    }

    /**
     * Returns the CSS styles def XML filename
     *
     * @return the CSS styles def XML filename
     */
    public String getStylesDef(){
        return this.stylesDef;
    }    
    
    /**
     * Returns true if shared with all sites
     *
     */
    public boolean isShared(){
        return this.shared;
    }

    /**
     * Add a site to the allowed site list
     */
    public void addAllowedSite(String siteKey){
        if ( siteKey != null ){
            this.allowedSites.add(siteKey);
        }
    }

    /**
     * Add a set of allowed site
     *
     * @param sites
     */
    public void addAllowedSites(Collection sites){
        if ( sites != null ){
            this.allowedSites.addAll(sites);
        }
    }
    
    /**
     * Add a site to the allowed site list
     */
    public void addAllowedTemplatesSet(String templateRoot){
        if ( templateRoot != null ){
            this.allowedTemplateSets.add(templateRoot);
        }
    }

    /**
     * Add a set of allowed site
     *
     * @param sites
     */
    public void addAllowedTemplateSets(Collection templateRoots){
        if ( templateRoots != null ){
            this.allowedTemplateSets.addAll(templateRoots);
        }
    }    

    /**
     * Returns true if a site is allowed to use this CSS
     */
    public boolean isSiteAllowed(String siteKey){
        return ( this.isShared()
                 || ( siteKey != null && this.allowedSites.contains(siteKey) ) );
    }
    
    /**
     * Returns true if a site is allowed to use this CSS
     */
    public boolean isTemplateAllowed(String templatePath){
        boolean templateAllowed = false;
        if (allowedTemplateSets.isEmpty() || templatePath == null || templatePath.length() == 0) {
            templateAllowed = true;
        } else {
            for (Iterator it = allowedTemplateSets.iterator(); it.hasNext() && !templateAllowed; ) {
                String templateRootName = (String)it.next();
                if (templatePath.indexOf(templateRootName) != -1) {
                    templateAllowed = true;
                }
            }
            
        }
        return ( templateAllowed );
    }    

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("HtmlEditorCSS - id: ");
        buff.append(id);
        buff.append("\nname: ");
        buff.append(name);
        buff.append("\nurl: ");
        buff.append(url);
        buff.append("\nstylesDef: ");
        buff.append(stylesDef);              
        return buff.toString();
    }
}