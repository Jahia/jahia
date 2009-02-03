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
package org.jahia.resourcebundle;

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
 * Load Resource Bundle Definitions from XML configuration file.
 * Implementation based on Apache Digester.
 *
 * @author Khue Nguyen
 */
class ResourceBundlesDigester {
    
    private static final transient Logger logger = Logger
            .getLogger(ResourceBundlesDigester.class);
    
    private Digester digester;
    private List registry;

    public ResourceBundlesDigester(){
        this.initDigester();
        this.registry = new ArrayList();
    }

    private void initDigester(){

        this.digester = new Digester();

        AddResourceBundleRule rule = new AddResourceBundleRule();
        digester.addRule("registry/resource-bundle", rule);
        digester.addRule("registry/resource-bundle/file", rule.setParamRule);
        digester.addRule("registry/resource-bundle/key", rule.setParamRule);
    }

    /**
     * Full path to the XML source file, from which to load the Resource Bundle Definition.
     * The internal list of Resoure Bundle Definitions will be cleared before loading again.
     *
     * @param sourceFile
     */
    public boolean loadResourceBundleDefinitions(String sourceFile){
        boolean success = false;
        try {
            File bundleFile = new File(sourceFile);
            this.digester.parse(bundleFile);
            success = true;
        } catch (SAXException saxe) {
            logger.error(saxe.getMessage(), saxe);
        } catch ( IOException ioe ){
            logger.error(ioe.getMessage(), ioe);
        }
        return success;
    }

    /**
     * Returns the List of Definitions
     * You must call loadResourceBundleDefinitions once to load them first
     *
     * @return
     */
    public List getResourceBundleDefinitions(){
        return this.registry;
    }


    final class AddResourceBundleRule extends Rule {
        private Map params = new HashMap();
        SetParamRule setParamRule = new SetParamRule();

        public void end(String namespace, String name)
                throws Exception {
            ResourceBundleDefinition rbDef = new ResourceBundleDefinition((String) params.get("key"), (String) params.get("file"));
            params.clear();
            registry.add(rbDef);
        }

        final class SetParamRule extends Rule {
            public void body(String namespace, String name, String text)
                    throws Exception {
                params.put(name,text);
            }
        }
    }

}

