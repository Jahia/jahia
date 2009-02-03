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
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Resource Bundle Service.
 * Maintain a registry of resource bundles that can be referred by dynamic content ( JahiaField content ).
 *
 * @author Khue Nguyen
 */
public class ResourceBundleBaseService extends ResourceBundleService {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ResourceBundleBaseService.class);

    static private ResourceBundleBaseService instance = null;
    private String resourceBundleConfigFile = "";
    private Map registry;
    private Map registryByName;
    private ResourceBundlesDigester resBundleDigester;
    private Document xmlDocument;

    protected ResourceBundleBaseService() {
        registry = new HashMap();
        registryByName = new HashMap();
        resBundleDigester = new ResourceBundlesDigester();
    }

     public static ResourceBundleBaseService getInstance() {
         if (instance == null) {
             synchronized ( ResourceBundleBaseService.class ){
                 if (instance == null) {
                     instance = new ResourceBundleBaseService();
                 }
             }
         }
         return instance;
     }

     public void start()
     throws JahiaInitializationException {

         StringBuffer buff = new StringBuffer(settingsBean.getJahiaEtcDiskPath());
         buff.append(File.separator);
         buff.append("config");
         buff.append(File.separator);
         buff.append(this.configFileName);
         this.resourceBundleConfigFile = buff.toString();
         loadAllResourceBundles();
     }

     public void stop() {}

    /**
     * Returns an Iterator of all Resource Bundle Definition.
     *
     * @return all ResourceBundle registered in the system
     * @throws JahiaException
     */
    public Iterator getResourceBundles()
    throws JahiaException
    {
        return this.registry.values().iterator();
    }

    /**
     * Return a resource bundle definition looking at it key.
     *
     * @param key
     * @return ResourceBundleDefinition
     * @throws JahiaException
     */
    public ResourceBundleDefinition getResourceBundle(String key)
    throws JahiaException
    {
        return (ResourceBundleDefinition)this.registry.get(key);
    }

    public ResourceBundleDefinition getResourceBundleFromName(String name) throws JahiaException {
        return (ResourceBundleDefinition) registryByName.get(name);
    }

    /**
     * Add a resource Bundle.
     * If a resource bundle already exists for this key, this is this one that is returned !
     * The existing resource bundle definition won't be overriden.
     *
     * @param key ResourceBundle Key
     * @return an Resource Bundle looking at it key
     * @throws JahiaException
     */
    public synchronized ResourceBundleDefinition
    declareResourceBundleDefinition (String key, String resourceBundleName)
    throws JahiaException {
        ResourceBundleDefinition rbDef = this.getResourceBundle(key);
        if (  rbDef != null ){
            return rbDef;
        }
        rbDef = new ResourceBundleDefinition(key,resourceBundleName);
        this.registry.put(key,rbDef);
        this.registryByName.put(resourceBundleName, rbDef);
        this.storeResourceBundleDefinition(rbDef);
        return rbDef;
    }

    private void loadAllResourceBundles(){

        this.resBundleDigester
                .loadResourceBundleDefinitions(this.resourceBundleConfigFile);
        List v = this.resBundleDigester.getResourceBundleDefinitions();
        int size = v.size();
        for ( int i=0 ; i<size ; i++ )
        {
            ResourceBundleDefinition rbDef = (ResourceBundleDefinition)v.get(i);
            this.registry.put(rbDef.getResourceBundleID(),rbDef);
            this.registryByName.put(rbDef.getResourceBundleFile(),rbDef);
        }
    }

    private synchronized void storeResourceBundleDefinition(ResourceBundleDefinition rbDef){


        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            if ( this.xmlDocument == null ){
                File bundleConfigFile = new File(this.resourceBundleConfigFile);
                this.xmlDocument = builder.parse(bundleConfigFile);
            }
            if ( this.xmlDocument != null ){

                Element root = (Element) this.xmlDocument.getDocumentElement();

                // resource bundle element
                Element rbDefElement = (Element) this.xmlDocument.createElement("resource-bundle");

                // key element
                Element keyElement = this.xmlDocument.createElement("key");
                keyElement.appendChild(this.xmlDocument.createTextNode(rbDef.getResourceBundleID()));
                rbDefElement.appendChild(keyElement);

                // file element
                Element fileElement = this.xmlDocument.createElement("file");
                fileElement.appendChild(this.xmlDocument.createTextNode(rbDef.getResourceBundleFile()));
                rbDefElement.appendChild(fileElement);

                root.appendChild(rbDefElement);

                // write out to the file
                this.xmlDocument.normalize(); // cleanup DOM tree a little

                TransformerFactory tfactory = TransformerFactory.newInstance();

                // This creates a transformer that does a simple identity transform,
                // and thus can be used for all intents and purposes as a serializer.
                Transformer serializer = tfactory.newTransformer();

                serializer.setOutputProperty(OutputKeys.METHOD, "xml");
                serializer.setOutputProperty(OutputKeys.INDENT, "yes");
                FileOutputStream fileStream = new FileOutputStream(this.resourceBundleConfigFile);
                serializer.transform(new DOMSource(this.xmlDocument),
                                     new StreamResult(fileStream));
            }
        } catch (Exception e) {
            logger.error(
                "Error while reading or writing resource bundle configuration file : " +
                this.resourceBundleConfigFile, e);
        }
    }

}

