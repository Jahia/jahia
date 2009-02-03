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

 package org.jahia.bin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.xml.sax.SAXException;

/**
 * <p>Title: A manager class that preloads classes into memory for a given
 * configuration loaded from an XML file </p>
 * <p>Description: This class is used to preload classes into the default
 * class loader in order to statisfy static dependencies between Jahia's
 * classes. This allows us to use our factory pattern that is used notably
 * in the ObjectKey and ContentObject class hierarchies, that use static
 * initializers to register class names into the factories.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber
 * @version 1.0
 */

public class ClassesPreloadManager {

    List preloadClassNames = new ArrayList();

    /**
     * Constructor for the manager. This constructor actually does all the work
     * of parsing the configuration file that contains the list of classes to
     * preload into the default class loader, and then actually calls the
     * class loader to load the classes, and therefore also call all the static
     * initializers of those classes.
     *
     * The format of the XML file can be seen in this example :
     * <?xml version="1.0" encoding="ISO-8859-1"?>
     * <preload-classes>
     *   <class-name>org.jahia.content.ObjectKey</class-name>
     *   <class-name>org.jahia.content.ContentFieldKey</class-name>
     *   <class-name>org.jahia.content.ContentContainerListKey</class-name>
     *   <class-name>org.jahia.content.ContentObject</class-name>
     * </preload-classes>
     *
     * @param configurationFileName the name of the XML configuration file that
     * contains a simple list of classes to preload into the default class
     * loader.
     *
     * @throws IOException thrown if there was an IO error while reading or
     * opening the given XML configuration file
     * @throws SAXException thrown if there was an XML error while parsing the
     * configuration file
     * @throws ClassNotFoundException thrown if one of the classes given in the
     * configuration file couldn't be found by the default class loader.
     */
    public ClassesPreloadManager(String configurationFileName)
        throws IOException, SAXException, ClassNotFoundException {
        loadXMLConfiguration(configurationFileName);
        preloadClasses();
    }

    private void loadXMLConfiguration (String configurationFileName)
        throws IOException, SAXException {

        Digester digester = new Digester();

        // This set of rules calls the addClassName method and passes
        // in a single parameter to the method.
        digester.addRule("preload-classes/class-name", new AddClassNameRule());

        // This method starts the parsing of the document.
        File configurationFile = new File(configurationFileName);
        digester.parse(configurationFile);
    }

    private void preloadClasses()
        throws ClassNotFoundException {
        // we can now actually preload the classes into the default class
        // loader.
        Iterator preloadClassNameIter = preloadClassNames.iterator();
        while (preloadClassNameIter.hasNext()) {
            String curClassName = (String) preloadClassNameIter.next();
            Class.forName(curClassName);
        }
    }

    final class AddClassNameRule extends Rule {
        public void body(String namespace, String name, String text)
                throws Exception {
            preloadClassNames.add(text);
        }
    }

}