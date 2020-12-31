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
}
