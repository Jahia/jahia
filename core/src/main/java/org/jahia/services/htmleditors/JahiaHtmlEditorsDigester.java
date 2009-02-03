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
package org.jahia.services.htmleditors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Load Html Editors from XML configuration file.
 * Implementation based on Apache Digester.
 *
 * @author Khue Nguyen
 */
class JahiaHtmlEditorsDigester {
    
    private static final transient Logger logger = Logger
            .getLogger(JahiaHtmlEditorsDigester.class);
    
    private Digester digester;
    private List editors;
    private List cssList;

    public JahiaHtmlEditorsDigester(){
        this.initDigester();
        this.editors = new ArrayList();
        this.cssList = new ArrayList();
    }

    private void initDigester(){
        this.digester = new Digester();

        AddHtmlEditorRule rule = new AddHtmlEditorRule();
        digester.addRule("editors/editor", rule);
        digester.addRule("editors/editor/id", rule.setParamRule);
        digester.addRule("editors/editor/name", rule.setParamRule);
        digester.addRule("editors/editor/base-directory", rule.setParamRule);
        digester.addRule("editors/editor/include-file", rule.setParamRule);
        digester.addRule("editors/editor/compatibility-tester", rule.setParamRule);
        digester.addRule("editors/editor/enable-css", rule.setParamRule);
        digester.addRule("editors/editor/rank", rule.setParamRule);

        AddCSSRule addCSSRule = new AddCSSRule();
        digester.addRule("editors/style-sheets/css",addCSSRule);
        digester.addRule("editors/style-sheets/css/id",addCSSRule.setParamRule);
        digester.addRule("editors/style-sheets/css/name",addCSSRule.setParamRule);
        digester.addRule("editors/style-sheets/css/url",addCSSRule.setParamRule);
        digester.addRule("editors/style-sheets/css/stylesdef",addCSSRule.setParamRule);
        digester.addRule("editors/style-sheets/css/shared",addCSSRule.setParamRule);
        digester.addRule("editors/style-sheets/css/allowed-sites/site-key", addCSSRule.addAllowedSiteRule);
        digester.addRule("editors/style-sheets/css/allowed-templatesets/template-root", addCSSRule.addAllowedTemplateSetRule);
    }

    /**
     * Full path to the XML source file, from which to load the Html Editors.
     * The internal list of HtmlEditors will be cleared before loading again.
     *
     * @param sourceFile
     */
    public boolean loadHtmlEditors(String sourceFile){
        boolean success = false;
        try {
            File xmlSourceFile = new File(sourceFile);
            this.digester.parse(xmlSourceFile);
            success = true;
        } catch (SAXException saxe) {
            logger.error(saxe.getMessage(), saxe);
        } catch ( IOException ioe ){
            logger.error(ioe.getMessage(), ioe);
        }
        return success;
    }

    /**
     * Add a new Html Editor to the internal list
     * @param id
     * @param name
     * @param baseDirectory
     * @param includeFile
     * @param enableCSS
     */
    public void addHtmlEditor( String id,
                               String name,
                               String baseDirectory,
                               String includeFile,
                               String compatibilityTester,
                               String enableCSS,
                               int rank ){

        HtmlEditor editor =
                new JahiaHtmlEditor(id,name,baseDirectory,includeFile,
                compatibilityTester,"true".equalsIgnoreCase(enableCSS), rank);
        this.editors.add(editor);
    }

    /**
     * Returns the List of HtmlEditors
     * You must call loadHtmlEditors once to load them first
     *
     * @return
     */
    public List getHtmlEditors(){
        return this.editors;
    }

    /**
     * Returns the List of CSS
     * You must call loadHtmlEditors once to load them first
     *
     * @return
     */
    public List getCSSList(){
        return this.cssList;
    }

    final class AddHtmlEditorRule extends Rule {
        private Map params = new HashMap();
        SetParamRule setParamRule = new SetParamRule();

        public void end(String namespace, String name)
                throws Exception {
            int rank = -1;
            if (params.get("rank") != null) {
                rank = Integer.parseInt((String) params.get("rank"));
            }
            JahiaHtmlEditor editor = new JahiaHtmlEditor((String) params.get("id"),
                    (String) params.get("name"),
                    (String) params.get("base-directory"),
                    (String) params.get("include-file"),
                    (String) params.get("compatibility-tester"),
                    "true".equalsIgnoreCase((String) params.get("enable-css")),
                    rank);
            params.clear();
            editors.add( editor );
        }

        final class SetParamRule extends Rule {
            public void body(String namespace, String name, String text)
                    throws Exception {
                params.put(name,text);
            }
        }
    }

    final class AddCSSRule extends Rule {
        private Map params = new HashMap();
        private List allowedSites = new ArrayList();
        private List allowedTemplateSets = new ArrayList();
        SetParamRule setParamRule = new SetParamRule();
        AddAllowedSiteRule addAllowedSiteRule = new AddAllowedSiteRule();
        AddAllowedTemplateSetRule addAllowedTemplateSetRule = new AddAllowedTemplateSetRule();

        public void end(String namespace, String name)
                throws Exception {
            HtmlEditorCSS css =
                    new HtmlEditorCSS((String) params.get("id"),
                            (String) params.get("name"),
                            (String) params.get("url"),
                            (String) params.get("stylesdef"),
                            "true".equalsIgnoreCase((String) params.get("shared")));

            css.addAllowedSites(this.allowedSites);
            css.addAllowedTemplateSets(this.allowedTemplateSets);
            this.allowedSites = new ArrayList();
            this.allowedTemplateSets = new ArrayList();

            params.clear();
            allowedSites.clear();
            allowedTemplateSets.clear();
            cssList.add( css );
        }

        final class SetParamRule extends Rule {
            public void body(String namespace, String name, String text)
                    throws Exception {
                params.put(name,text);
            }
        }

        final class AddAllowedSiteRule extends Rule {
            public void body(String namespace, String name, String text)
                    throws Exception {
                allowedSites.add(text);
            }
        }
        
        final class AddAllowedTemplateSetRule extends Rule {
            public void body(String namespace, String name, String text)
                    throws Exception {
                allowedTemplateSets.add(text);
            }
        }        
    }
}

