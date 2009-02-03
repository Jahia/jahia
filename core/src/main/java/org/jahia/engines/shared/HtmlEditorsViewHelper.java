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

package org.jahia.engines.shared;

import java.util.*;

import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.htmleditors.HtmlEditor;
import org.jahia.services.htmleditors.HtmlEditorCSS;
import org.jahia.services.htmleditors.HtmlEditorsService;
import org.jahia.services.htmleditors.JahiaClientCapabilities;
import org.jahia.params.ProcessingContext;

import java.io.Serializable;

/**
 * <p>Title: HtmlEditorsViewHelper</p>
 * <p>Description: Html Editors Helper</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia S.A.R.L</p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class HtmlEditorsViewHelper implements Serializable {

    private Map editors;
    private List editorList;
    private Map enabledCSSs;
    private String defaultEditor;
    private String defaultCSS;

    public HtmlEditorsViewHelper(){
        this.editors = new HashMap();
        this.editorList = new ArrayList();
        this.enabledCSSs = new LinkedHashMap();
        this.defaultEditor = "";
        this.defaultCSS = "";
    }

    /**
     * Can set a default editor
     *
     * @param defaultEditor
     */
    public HtmlEditorsViewHelper(String defaultEditor){
        this.editors = new HashMap();
        this.editorList = new ArrayList();
        this.enabledCSSs = new LinkedHashMap();
        this.defaultEditor = defaultEditor;
    }

    /**
     * Load the editors for a given site, only those that are Client Capable.
     *
     * @param siteID
     * @param processingContext the current processing context
     * @throws JahiaException
     */
    public void loadHtmlEditors(int siteID, ProcessingContext processingContext)
    throws JahiaException {
        HtmlEditorsService heServ = ServicesRegistry.getInstance()
                                  .getHtmlEditorsService();
        JahiaClientCapabilities clientCapabilities =
                new JahiaClientCapabilities( processingContext.getUserAgent() );
        Iterator editorEnum = heServ.getEditors(siteID);
        HtmlEditor editor = null;
        while ( editorEnum.hasNext() )
        {
            editor = (HtmlEditor)editorEnum.next();
            if ( editor.isClientCapable( clientCapabilities ) ){
                this.editors.put(editor.getId(),editor);
                this.editorList.add(editor);
            }
        }

        // retrieve enabled CSS for this site
        Iterator cssEnum = heServ.getCSSs(siteID);
        HtmlEditorCSS css = null;
        while ( cssEnum.hasNext() )
        {
            css = (HtmlEditorCSS)cssEnum.next();
            this.enabledCSSs.put(css.getId(),css);
        }

    }

    /**
     *
     * @return the default Editor ID.
     */
    public String getDefaultEditorID(){
        return this.defaultEditor;
    }

    /**
     * Set the default editor ID
     *
     * @param id the default Editor ID.
     */
    public void setDefaultEditorID(String id){
        if ( id==null ){
            id = "";
        }
        this.defaultEditor = id;
    }

    /**
     * Set the default CSS
     *
     * @param id the default Editor ID.
     */
    public void setDefaultCSSID(String id){
        if ( id==null ){
            id = "";
        }
        this.defaultCSS = id;
    }

    /**
     *
     * @return the default CSS ID.
     */
    public String getDefaultCSSID(){
        return this.defaultCSS;
    }

    /**
     * Returns the editors as an enumeration.
     *
     * @return the editors as an enumeration
     */
    public Iterator getEditors(){
        return this.editorList.iterator();
    }

    /**
     * Returns the list of enabled CSS.
     *
     * @return the list of enabled CSS.
     */
    public Map getEnabledCSSs(){
        return this.enabledCSSs;
    }
    /**
     * Return the default Editor if any.
     *
     * @return the default editor if any
     */
    public HtmlEditor getDefaultEditor(){
        HtmlEditor editor = (HtmlEditor)this.editors.get(this.defaultEditor);
        return editor;
    }

    /**
     * Return the default Editor if any.If not set, return the first available
     * and set it as the default.
     *
     * @return the default editor if any.
     */
    public HtmlEditor getDefaultEditor(boolean firstAvailableIfAny){
        HtmlEditor editor = getDefaultEditor();
        if ( !firstAvailableIfAny ){
            return editor;
        }
        if ( editor == null && this.editorList.size() > 0 ){
            editor = (HtmlEditor)this.editorList.get(0);
        }
        return editor;
    }

}