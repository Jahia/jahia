/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils.xml;

import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * A utility class that provides instances of SAX XML parser.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaSAXParserFactory extends BaseXMLParserFactory {

    private static JahiaSAXParserFactory instance;

    /**
     * Creates a new instance of a {@link SAXParserFactory} using the currently configured factory parameters.
     *
     * @return a new instance of an SAXParserFactory
     *
     * @throws ParserConfigurationException
     *             if a factory cannot be created which satisfies the requested configuration
     * @throws SAXException
     *             for SAX errors
     */
    public static SAXParserFactory newInstance() throws ParserConfigurationException, SAXException {
        if (instance == null) {
            throw new UnsupportedOperationException("This XML parser factory is not initialized yet");
        }
        return instance.create();
    }

    /**
     * Initializes an instance of this class.
     */
    public JahiaSAXParserFactory() {
        super();
        instance = this;
    }

    private SAXParserFactory create()
            throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException, SAXException {
        SAXParserFactory factory = new SAXParserFactoryImpl();

        factory.setNamespaceAware(isNamespaceAware());
        factory.setValidating(isValidating());
        factory.setXIncludeAware(isXIncludeAware());
        for (Map.Entry<String, Boolean> feature : getFeatures().entrySet()) {
            factory.setFeature(feature.getKey(), feature.getValue());
        }

        return factory;
    }
}
