/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
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
