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
//
//  JahiaTemplateDef
//
//  NK      16.01.2001
//
//

package org.jahia.data.templates;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Holds Informations about a Template Definition
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaTemplateDef {

    /**
     * Name of the template *
     */
    private String m_Name;
    /**
     * The filename of the template *
     */
    private String m_Filename;
    /**
     * The display Name of the template *
     */
    private String m_DisplayName;
    /**
     * The description of the template *
     */
    private String description;    
    /**
     * If the template is visible *
     */
    private boolean m_Visible = true;
    /**
     * Is a Home page template *
     */
    private boolean m_IsHomePage;
    /**
     * Is a default page template *
     */
    private boolean m_IsDefault;

    /**
     * The file path, starting from the templates folder (computable).
     */
    private String m_FilePath;

    private String pageType;

    private JahiaTemplatesPackage parent;


    /**
     * Initializes an instance of this class.
     */
    public JahiaTemplateDef() {
        super();
    }

    /**
     * Constructor
     */
    public JahiaTemplateDef(
            String name,
            String filename,
            String filePath,
            String displayName,
            String pageType,
            String description,
            boolean visible,
            boolean isHomePage,
            boolean isDefault) {
        m_Name = name;
        m_Filename = filename;
        m_FilePath = filePath;
        m_DisplayName = displayName;
        m_Visible = visible;
        m_IsHomePage = isHomePage;
        m_IsDefault = isDefault;
        this.pageType = pageType;
        this.description = description;        
    }


    /**
     * Return the template name
     *
     * @return (String) the name of the template
     */
    public String getName() {

        return m_Name;
    }


    /**
     * Set the name
     *
     * @param (String) the name of the template
     */
    public void setName(String name) {

        m_Name = name;
    }


    /**
     * Return the template filename
     *
     * @return (String) the name of the template filename
     */
    public String getFileName() {

        return m_Filename;
    }


    /**
     * Set the filename
     *
     * @param (String) the template filename
     */
    public void setFilename(String filename) {

        m_Filename = filename;
    }


    /**
     * Return the template dislay name
     *
     * @return (String) the name of the template display name
     */
    public String getDisplayName() {

        return m_DisplayName;
    }


    /**
     * Set the display name
     *
     * @param (String) the template display name
     */
    public void setDisplayName(String displayName) {

        m_DisplayName = displayName;
    }

    /**
     * is the template visible to user ?
     *
     * @return (boolean) visible
     */
    public boolean isVisible() {

        return m_Visible;

    }


    /**
     * Set template visible to user ?
     *
     * @param (boolean) visible status
     */
    public void setVisible(boolean visible) {

        m_Visible = visible;

    }


    /**
     * is the template a home page like template ?
     *
     * @return (boolean) is a home page like template
     */
    public boolean isHomePage() {

        return m_IsHomePage;

    }


    /**
     * Set this template as a Home Page template like
     *
     * @param (boolean) home page like properties
     */
    public void setIsHomePage(boolean isHomePage) {

        m_IsHomePage = isHomePage;

    }


    public boolean isDefault() {
        return m_IsDefault;
    }

    public void setIsDefault(boolean m_IsDefault) {
        this.m_IsDefault = m_IsDefault;
    }

    public String toString() {
        return new ToStringBuilder(this).append("name", getName()).append(
                "displayName", getDisplayName()).append("isVisible",
                isVisible()).append("isDefault", isDefault()).append(
                "isHomePage", isHomePage()).toString();
    }


    /**
     * Returns the file path, starting from the templates folder (computable).
     *
     * @return the file path, starting from the templates folder (computable)
     */
    public String getFilePath() {
        return m_FilePath;
    }

    public void setFilePath(String filePath) {
        m_FilePath = filePath;
    }

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the original template package that own this template.
     *
     * @return the original template package that own this template
     */
    public JahiaTemplatesPackage getParent() {
        return parent;
    }

    public void setParent(JahiaTemplatesPackage parent) {
        this.parent = parent;
    }
}
