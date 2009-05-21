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
//
//  DtdEntityResolver
//
//  NK    08.03.2001
//

package org.jahia.utils.xml;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import org.apache.commons.collections.FastHashMap;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;


/**
 * Substitute external DTD files with local DTD files.
 *
 * @author Khue
 * @version 1.0
 */
public class DtdEntityResolver implements EntityResolver {
    
    private static Logger logger = Logger.getLogger(DtdEntityResolver.class);

	/**
     * Registered DTD, the key is the public id
     */
    private Map m_DTDs = new FastHashMap();
   	
    private Map schemata = new FastHashMap();
    

   	/**
	 * Replace external dtd with local dtd.
	 * Return an InputSource to a local DTD file or null if not found
	 * in the registered DTD
	 * 
	 */
	public InputSource resolveEntity(String publicID, String systemID) {

        if (publicID != null && m_DTDs.containsKey(publicID)) {
            try {
                return new InputSource(new FileInputStream((File) m_DTDs
                        .get(publicID)));
            } catch (FileNotFoundException e) {
                logger.warn("Unable to find the DTD file: "
                        + m_DTDs.get(publicID));
            }
        } else if (systemID != null && schemata.containsKey(systemID)) {
            try {
                return new InputSource(new FileInputStream((File) schemata
                        .get(systemID)));
            } catch (FileNotFoundException e) {
                logger.warn("Unable to find the XML schema file: "
                        + schemata.get(systemID));
            }
        }

        return null;
    }


    /**
     * @param publicID
     *            the key to identify the requested dtd
     * @param dtd
     *            the dtd resource file
     */
    public void registerDTD(String publicID, File dtd) {
        if (!dtd.exists() || !dtd.isFile() || !dtd.canRead()) {
            throw new IllegalArgumentException("Unable to read the DTD file: "
                    + dtd);
        }
		m_DTDs.put(publicID, dtd);
    }

   /**
     * Adds XML schema to the registry.
     * 
     * @param path
     *            schema path (system ID)
     * @param schema
     *            the XSD file
     */
    public void registerSchema(String path, File schema) {
        if (!schema.exists() || !schema.isFile() || !schema.canRead()) {
            throw new IllegalArgumentException(
                    "Unable to read the XML schema file: " + schema);
        }
        schemata.put(path, schema);
    }
}