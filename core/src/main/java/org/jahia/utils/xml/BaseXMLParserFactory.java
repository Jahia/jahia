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

import java.util.HashMap;
import java.util.Map;

/**
 * Base utility class that provides instances of XML parsers.
 *
 * @author Sergiy Shyrkov
 */
abstract class BaseXMLParserFactory {

    private Map<String, Boolean> features = new HashMap<>();

    private boolean isXIncludeAware = false;

    private boolean namespaceAware = false;

    private boolean validating = false;

    /**
     * Returns a map of XML parser features.
     *
     * @return a map of XML parser features
     */
    protected Map<String, Boolean> getFeatures() {
        return features;
    }

    /**
     * Indicates whether or not the factory is configured to produce parsers which are namespace aware.
     *
     * @return true if the factory is configured to produce parsers which are namespace aware; false otherwise.
     */
    protected boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * Indicates whether or not the factory is configured to produce parsers which validate the XML content during parse.
     *
     * @return true if the factory is configured to produce parsers which validate the XML content during parse; false otherwise.
     */
    protected boolean isValidating() {
        return validating;
    }

    /**
     * <p>
     * Get state of XInclude processing.
     * </p>
     *
     * @return current state of XInclude processing
     *
     * @throws UnsupportedOperationException
     *             When implementation does not override this method.
     */
    protected boolean isXIncludeAware() {
        return isXIncludeAware;
    }

    /**
     * Sets the features for this parser factory.
     *
     * @param features
     *            the map of features
     */
    public void setFeatures(Map<String, Boolean> features) {
        this.features = features;
    }

    /**
     * Specifies that the parser produced by this code will provide support for XML namespaces. By default the value of this is set to
     * <code>false</code>
     *
     * @param awareness
     *            true if the parser produced will provide support for XML namespaces; false otherwise.
     */
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    /**
     * Specifies that the parser produced by this code will validate documents as they are parsed. By default the value of this is set to
     * <code>false</code>.
     *
     * @param validating
     *            true if the parser produced will validate documents as they are parsed; false otherwise.
     */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    /**
     * <p>
     * Set state of XInclude processing.
     * </p>
     *
     * <p>
     * If XInclude markup is found in the document instance, should it be processed as specified in
     * <a href="http://www.w3.org/TR/xinclude/"> XML Inclusions (XInclude) Version 1.0</a>.
     * </p>
     *
     * <p>
     * XInclude processing defaults to <code>false</code>.
     * </p>
     *
     * @param state
     *            Set XInclude processing to <code>true</code> or <code>false</code>
     */
    public void setXIncludeAware(boolean isXIncludeAware) {
        this.isXIncludeAware = isXIncludeAware;
    }

}
