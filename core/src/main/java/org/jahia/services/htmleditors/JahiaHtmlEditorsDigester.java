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

