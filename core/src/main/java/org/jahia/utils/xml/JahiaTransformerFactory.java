/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.BasicTransformerFactory;
import net.sf.saxon.lib.FeatureKeys;

/**
 * A utility class that provides instances of a {@link TransformerFactory}.
 * 
 * @author Benjamin Papež
 */
public class JahiaTransformerFactory {
    private static final Logger logger = LoggerFactory.getLogger(JahiaTransformerFactory.class);
    
    private JahiaTransformerFactory() {
        throw new IllegalStateException("Utility class");
      }
    
    /**
     * Build a new {@link TransformerFactory} using the default constructor and prevent external entities from accessing.
     */
    public static TransformerFactory newInstance() {
        return newInstance(BasicTransformerFactory.class);
    }

    /**
     * Build a {@link TransformerFactory} and prevent external entities from accessing.
     *
     * @see TransformerFactory#newInstance()
     */
    public static TransformerFactory newInstance(Class<? extends TransformerFactory> transformerFactoryClass) {
        try {
            return setDefaultAttributes(transformerFactoryClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new TransformerFactoryConfigurationError(e, "Could not instantiate TransformerFactory [" + transformerFactoryClass + "]");
        }
    }

    /**
     * Prevent external entities from accessing.
     */
    private static TransformerFactory setDefaultAttributes(TransformerFactory factory) {
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        setOptionalFeature(factory, FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS, false);
        return factory;
    }
    
    private static void setOptionalFeature(TransformerFactory factory, String name, boolean value) {
        try {
            factory.setFeature(name, value);
        } catch (IllegalArgumentException | TransformerConfigurationException e) {
            logger.debug("{} feature not supported by {}", name, factory.getClass().getCanonicalName());
        }
    }
}
