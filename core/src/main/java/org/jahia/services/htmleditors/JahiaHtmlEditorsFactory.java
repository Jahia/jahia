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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.jahia.exceptions.JahiaException;
import java.util.Collections;

/**
 * Html Editors Factory Default implementation
 *
 * @author Khue Nguyen
 */
class JahiaHtmlEditorsFactory implements HtmlEditorsFactory
{
    private Map htmlEditors;
    private Map htmlEditorCSSs;
    private JahiaHtmlEditorsDigester htmlEditorsDigester;

    /**
     * Xml Configuration file.
     */
    private String configFile;

    /**
     *
     * @param configFile, full path to the configuration file
     */
    public JahiaHtmlEditorsFactory(String configFile) throws JahiaException{
        this.configFile = configFile;
        this.htmlEditors = new LinkedHashMap();
        this.htmlEditorCSSs = new LinkedHashMap();
        this.htmlEditorsDigester = new JahiaHtmlEditorsDigester();

        if ( !loadEditors() ){
            throw new JahiaException("Error load html editors config file",
                                     "Error load html editors config file",
                                     JahiaException.CONFIG_ERROR,
                                     JahiaException.CONFIG_ERROR);
        }
    }

    /**
     * Returns a List of all Html Editors registered in the System
     *
     * @return all Html Editors registered in the system
     * @throws JahiaException
     */
    public List getEditors()
    throws JahiaException
    {
        List v = new ArrayList(this.htmlEditors.values());
        Collections.sort(v);
        return v;
    }

    /**
     * Returns a List of all Html Editor CSS registered in the System
     *
     * @return all Html Editor CSS registered in the system
     * @throws JahiaException
     */
    public List getCSSs()
    throws JahiaException
    {
        List v = new ArrayList(this.htmlEditorCSSs.values());
        return v;
    }

    /**
     * Returns an Editor looking at it id
     *
     * @param id the Editor identifier
     * @return an Editor looking at it id
     * @throws JahiaException
     */
    public HtmlEditor getEditor(String id)
    throws JahiaException {
        if ( id == null ){
            return null;
        }
        return (HtmlEditor)this.htmlEditors.get(id);
    }

    /**
     * Returns an CSS looking at it id
     *
     * @param id the CSS identifier
     * @return an CSS looking at it id
     * @throws JahiaException
     */
    public HtmlEditorCSS getCSS(String id) throws JahiaException {
        if ( id == null ){
            return null;
        }
        return (HtmlEditorCSS)this.htmlEditorCSSs.get(id);
    }

    /**
     * Load Html Editors from persistence, the internal list of
     * Html Editors is cleared before loading again.
     *
     */
    public boolean loadEditors(){

        boolean success = this.htmlEditorsDigester.loadHtmlEditors(this.configFile);
        if ( !success ){
            return false;
        }

        this.htmlEditors = new LinkedHashMap();

        List v = this.htmlEditorsDigester.getHtmlEditors();
        int size = v.size();
        HtmlEditor editor = null;
        for ( int i=0; i<size; i++ ){
            editor = (HtmlEditor)v.get(i);
            this.htmlEditors.put(editor.getId(),editor);
        }

        v = this.htmlEditorsDigester.getCSSList();
        size = v.size();
        HtmlEditorCSS css = null;
        for ( int i=0; i<size; i++ ){
            css = (HtmlEditorCSS)v.get(i);
            this.htmlEditorCSSs.put(css.getId(),css);
        }
        return true;
    }
}

