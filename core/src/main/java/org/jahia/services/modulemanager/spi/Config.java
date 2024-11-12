/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.spi;

import org.jahia.services.modulemanager.util.PropertiesValues;

import java.io.IOException;
import java.util.Map;

/**
 * Wrapper around OSGi configuration
 * Provides an identifier for service factory configs
 * Allows to manipulate properties in a structured way with a PropertiesValues object
 */
public interface Config {

    /**
     * Get the identifier for this config, if based on a service factory - null otherwise
     * @return
     */
    String getIdentifier();

    /**
     * Get the map of properties as they will be stored in the OSGi configuration
     * @return
     */
    Map<String, String> getRawProperties();

    /**
     * Get structured values
     * @return
     */
    PropertiesValues getValues();

    /**
     * Get config file content
     * @return content as a string
     * @throws IOException exception
     */
    String getContent() throws IOException;

    /**
     * Set config file content from a string
     * @param content content as a string
     * @throws IOException exception
     */
    void setContent(String content) throws IOException;

    /**
     * Get the file format, cfg or yml
     * @return cfg or yml
     */
    String getFormat();

    /**
     * Change the file format
     */
    void setFormat(String format);
}
