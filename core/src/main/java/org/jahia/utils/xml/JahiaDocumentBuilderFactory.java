/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A utility class that provides instances of DocumentBuilder XML parser.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaDocumentBuilderFactory extends BaseXMLParserFactory {

    private static JahiaDocumentBuilderFactory instance;

    /**
     * Creates a new instance of a {@link DocumentBuilderFactory} using the currently configured parameters.
     *
     * @return a new instance of a {@link DocumentBuilderFactory}
     *
     * @throws ParserConfigurationException
     *             if a DocumentBuilderFactory cannot be created which satisfies the configuration requested.
     */
    public static DocumentBuilderFactory newInstance() throws ParserConfigurationException {
        if (instance == null) {
            throw new UnsupportedOperationException("This XML parser factory is not initialized yet");
        }
        return instance.create();
    }

    private boolean expandEntityRef = false;

    public JahiaDocumentBuilderFactory() {
        super();
        instance = this;
    }

    private DocumentBuilderFactory create() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setExpandEntityReferences(expandEntityRef);
        factory.setNamespaceAware(isNamespaceAware());
        factory.setValidating(isValidating());
        factory.setXIncludeAware(isXIncludeAware());

        for (Map.Entry<String, Boolean> feature : getFeatures().entrySet()) {
            factory.setFeature(feature.getKey(), feature.getValue());
        }

        return factory;
    }

    /**
     * Specifies that the parser produced by this code will expand entity reference nodes. By default the value of this is set to
     * <code>false</code>.
     *
     * @param expandEntityRef
     *            <code>true</code> if the parser produced will expand entity reference nodes; <code>false</code> otherwise.
     */

    public void setExpandEntityReferences(boolean expandEntityRef) {
        this.expandEntityRef = expandEntityRef;
    }
}
