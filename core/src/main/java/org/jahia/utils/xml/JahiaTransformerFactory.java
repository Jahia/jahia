/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
        return setDefaultAttributes(TransformerFactory.newInstance());
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
        setOptionalAttribute(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setOptionalAttribute(factory, XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        setOptionalFeature(factory, FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS, false);
        return factory;
    }

    private static void setOptionalAttribute(TransformerFactory factory, String name, Object value) {
        try {
            factory.setAttribute(name, value);
        } catch (IllegalArgumentException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} property not supported by {}", name, factory.getClass().getCanonicalName());
            }
        }
    }
    private static void setOptionalFeature(TransformerFactory factory, String name, boolean value) {
        try {
            factory.setFeature(name, value);
        } catch (IllegalArgumentException | TransformerConfigurationException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} feature not supported by {}", name, factory.getClass().getCanonicalName());
            }
        }
    }
}
