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

