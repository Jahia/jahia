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
/*
 * Copyright 2000-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jahia.utils.xml.betwixt;

import java.io.InputStream;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.strategy.DecapitalizeNameMapper;
import org.apache.commons.betwixt.strategy.DefaultPluralStemmer;

/**
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @version $Id$
 */
public class XmlReader
{
    /** Betwixt BeanReader. */
    private BeanReader beanReader;

    /** Class type we are reading. */
    private Class clazz;

    /**
     * Constructor
     */
    public XmlReader(Class clazz)
    {
        this.clazz = clazz;
    }

    /**
     * Parse the given InputStream into an Object.
     *
     * @param is InputSteam to parse.
     * @return Object
     */
    public Object parse(InputStream is)
        throws Exception
    {
        if (beanReader == null)
        {
            beanReader = createBeanReader(clazz);
        }

        return beanReader.parse(is);
    }

    /**
     * Create the Betwixt BeanReader for processing this class.
     *
     * @param clazz Type of Class to parse.
     * @return BeanReader capable of parsing the type of clazz.
     */
    private BeanReader createBeanReader(Class clazz)
        throws Exception
    {
        BeanReader reader = new BeanReader();
        reader.setXMLIntrospector(createXMLIntrospector());
        reader.registerBeanClass(clazz);
        return reader;
    }

    /**
     * Create Betwixt XMLIntrospector.
     *
     * @return XMLIntrospector.
     */
    private XMLIntrospector createXMLIntrospector()
    {
        XMLIntrospector introspector = new XMLIntrospector();

        introspector.setAttributesForPrimitives(false);
        introspector.setNameMapper(new DecapitalizeNameMapper());
        introspector.setPluralStemmer(new DefaultPluralStemmer());

        return introspector;
    }
}
