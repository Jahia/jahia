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
package org.jahia.services.opensearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.log4j.Logger;
import org.jahia.utils.JahiaTools;
import org.xml.sax.SAXException;

/**
 * Load Open Search Engine Descriptor from XML configuration file.
 * Implementation based on Apache Digester.
 *
 * @author Khue Nguyen
 */
class SearchEngineDigester {

    private static final transient Logger logger = Logger
            .getLogger(SearchEngineDigester.class);

    private Digester digester;
    private List<SearchEngineBean> searchEngineBeans;
    private List<SearchEngineGroupBean> searchEngineGroupBeans;
    private OpenSearchElement openSearchElement;

    public SearchEngineDigester(){
        this.searchEngineBeans = new ArrayList<SearchEngineBean>();
        this.searchEngineGroupBeans = new ArrayList<SearchEngineGroupBean>();
    }

    public List<SearchEngineBean> getSearchEngineBeans() {
        return searchEngineBeans;
    }

    public List<SearchEngineGroupBean> getSearchEngineGroupBeans() {
        return searchEngineGroupBeans;
    }

    public void setSearchEngineGroupBeans(List<SearchEngineGroupBean> searchEngineGroupBeans) {
        this.searchEngineGroupBeans = searchEngineGroupBeans;
    }

    private void initDigester(){

        openSearchElement = new OpenSearchElement();

        digester = new Digester();
        digester.setValidating(false);

        AddSearchEngineElementRule addSearchEngineRule = new AddSearchEngineElementRule(this.openSearchElement);
        digester.addRule("opensearch/searchEngine", addSearchEngineRule);
        digester.addRule("opensearch/searchEngine/descriptor", addSearchEngineRule.setParamRule);

        AddSearchEngineGroupElementRule addSearchEngineGroupRule =
                new AddSearchEngineGroupElementRule(this.openSearchElement);
        digester.addRule("opensearch/searchGroup", addSearchEngineGroupRule);

        AddDescriptorElementRule addDescriptorRule =
                new AddDescriptorElementRule(this.openSearchElement);
        digester.addRule("opensearch/searchEngine/descriptor", addDescriptorRule);
    }

    /**
     * Full path to the XML source file, from which to load the Html Editors.
     * The internal list of HtmlEditors will be cleared before loading again.
     *
     * @param sourceFile
     */
    public boolean loadSearchEngines(String sourceFile){

        this.initDigester();
        boolean success = false;
        try {
            File xmlSourceFile = new File(sourceFile);
            this.searchEngineBeans.clear();
            this.searchEngineGroupBeans.clear();
            this.digester.parse(xmlSourceFile);

            Iterator<SearchEngineElement> it = openSearchElement.getSearchEngineElements().iterator();
            SearchEngineElement sEl = null;
            while (it.hasNext()) {
                sEl = it.next();
                SearchEngineBean searchEngineBean = new SearchEngineBean();
                searchEngineBean.setName(sEl.getName());
                searchEngineBean.setUrlType(sEl.getUrlType());
                DescriptorElement descriptor = sEl.getDescriptorElement();
                if (descriptor != null){
                    searchEngineBean.setDescriptorType(descriptor.getType());
                    searchEngineBean.setDescriptorFile(descriptor.getFilename());
                }
                this.searchEngineBeans.add(searchEngineBean);
            }

            Iterator<SearchEngineGroupElement> groupIt = openSearchElement.getSearchEngineGroupElements().iterator();
            SearchEngineGroupElement sgEl = null;
            SearchEngineGroupBean searchEngineGroupBean = null;
            while (groupIt.hasNext()) {
                sgEl = groupIt.next();
                searchEngineGroupBean = new SearchEngineGroupBean();
                searchEngineGroupBean.setName(sgEl.getName());
                String engineNames = sgEl.getEngineNames();
                String[] tokens = JahiaTools.getTokens(engineNames, " *+, *+");
                for (String name : tokens) {
                    searchEngineGroupBean.addEngineName(name.trim());
                }
                this.searchEngineGroupBeans.add(searchEngineGroupBean);
            }
            success = true;
        } catch (SAXException saxe) {
            logger.error(saxe.getMessage(), saxe);
        } catch ( IOException ioe ){
            logger.error(ioe.getMessage(), ioe);
        }
        return success;
    }

    final class AddSearchEngineElementRule extends Rule {
        private OpenSearchElement openSearchElement;
        private Map<String, String> params = new HashMap<String, String>();
        private Properties properties;
        SetParamRule setParamRule = new SetParamRule();

        public AddSearchEngineElementRule(OpenSearchElement openSearchElement) {
            this.openSearchElement = openSearchElement;
        }

        public void begin(
            String namespace,
            String name,
            org.xml.sax.Attributes attributes)
            throws Exception {
            properties = new Properties();
            for (int i = 0; i < attributes.getLength(); i++) {
                properties.setProperty(
                    attributes.getQName(i),
                    attributes.getValue(i));
            }
        }

        public void end(String namespace, String name)
                throws Exception {
            SearchEngineElement searchEngineElement = openSearchElement.getSearchEngineElements()
                    .get(openSearchElement.getSearchEngineElements().size()-1);
            searchEngineElement.setName(properties.getProperty ("name",""));
            searchEngineElement.setUrlType(properties.getProperty ("urlType",""));
            searchEngineElement.getDescriptorElement().setFilename(params.get("descriptor"));
            params.clear();
        }

        final class SetParamRule extends Rule {
            public void body(String namespace, String name, String text)
                    throws Exception {
                params.put(name,text);
            }
        }
    }

    final class AddSearchEngineGroupElementRule extends Rule {
        private OpenSearchElement openSearchElement;
        private Map<String, String> params = new HashMap<String, String>();
        private Properties properties;
        SetParamRule setParamRule = new SetParamRule();

        public AddSearchEngineGroupElementRule(OpenSearchElement openSearchElement) {
            this.openSearchElement = openSearchElement;
        }

        public void begin(
            String namespace,
            String name,
            org.xml.sax.Attributes attributes)
            throws Exception {
            properties = new Properties();
            for (int i = 0; i < attributes.getLength(); i++) {
                properties.setProperty(
                    attributes.getQName(i),
                    attributes.getValue(i));
            }
        }

        public void end(String namespace, String name)
                throws Exception {
            SearchEngineGroupElement searchEngineGroupElement = new SearchEngineGroupElement();
            searchEngineGroupElement.setName(properties.getProperty("name",""));
            searchEngineGroupElement.setEngineNames(properties.getProperty("searchEngineNames",""));
            params.clear();
            openSearchElement.addSearchEngineGroupElement(searchEngineGroupElement);
        }

        final class SetParamRule extends Rule {
            public void body(String namespace, String name, String text)
                    throws Exception {
                params.put(name,text);
            }
        }
    }

    final class AddDescriptorElementRule extends Rule {
        private OpenSearchElement openSearchElement;
        private Map<String, String> params = new HashMap<String, String>();
        private Properties properties;
        SetParamRule setParamRule = new SetParamRule();

        public AddDescriptorElementRule(OpenSearchElement openSearchElement) {
            this.openSearchElement = openSearchElement;
        }

        public void begin(
            String namespace,
            String name,
            org.xml.sax.Attributes attributes)
            throws Exception {
            properties = new Properties();
            for (int i = 0; i < attributes.getLength(); i++) {
                properties.setProperty(
                    attributes.getQName(i),
                    attributes.getValue(i));
            }
        }

        public void end(String namespace, String name)
                throws Exception {
            DescriptorElement descriptor = new DescriptorElement();
            descriptor.setType(properties.getProperty("type",""));
            params.clear();
            SearchEngineElement searchEngineElement = new SearchEngineElement();
            searchEngineElement.setDescriptorElement(descriptor);
            openSearchElement.addSearchEngineElement(searchEngineElement);
        }

        final class SetParamRule extends Rule {
            public void body(String namespace, String name, String text)
                    throws Exception {
                params.put(name,text);
            }
        }
    }

    public class OpenSearchElement {

        private List<SearchEngineElement> searchEngineElements;
        private List<SearchEngineGroupElement> searchEngineGroupElements;

        public OpenSearchElement() {
            searchEngineElements = new ArrayList<SearchEngineElement>();
            searchEngineGroupElements = new ArrayList<SearchEngineGroupElement>();
        }

        public List<SearchEngineElement> getSearchEngineElements() {
            return searchEngineElements;
        }

        public void setSearchEngineElements(List<SearchEngineElement> searchEngineElements) {
            this.searchEngineElements = searchEngineElements;
        }

        public List<SearchEngineGroupElement> getSearchEngineGroupElements() {
            return searchEngineGroupElements;
        }

        public void setSearchEngineGroupElements(List<SearchEngineGroupElement> searchEngineGroupElements) {
            this.searchEngineGroupElements = searchEngineGroupElements;
        }

        public void addSearchEngineElement(SearchEngineElement searchEngineElement){
            this.searchEngineElements.add(searchEngineElement);
        }

        public void addSearchEngineGroupElement(SearchEngineGroupElement searchEngineGroupElement){
            this.searchEngineGroupElements.add(searchEngineGroupElement);
        }
    }

    public class SearchEngineElement {
        private String name = "";
        private String urlType = "";
        private DescriptorElement descriptorElement;

        public SearchEngineElement() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrlType() {
            return urlType;
        }

        public void setUrlType(String urlType) {
            this.urlType = urlType;
        }

        public DescriptorElement getDescriptorElement() {
            return descriptorElement;
        }

        public void setDescriptorElement(DescriptorElement descriptorElement) {
            this.descriptorElement = descriptorElement;
        }
    }

    public class DescriptorElement {
        private String type = "";
        private String filename = "";

        public DescriptorElement() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
            if (this.filename != null){
                this.filename = this.filename.trim();
            }
        }
    }

    public class SearchEngineGroupElement {
        private String name = "";
        private String engineNames = "";

        public SearchEngineGroupElement() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEngineNames() {
            return engineNames;
        }

        public void setEngineNames(String engineNames) {
            this.engineNames = engineNames;
        }
    }


}