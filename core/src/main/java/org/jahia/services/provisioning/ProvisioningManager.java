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
package org.jahia.services.provisioning;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Service to provision bundles/features/configs/content with script
 */
public interface ProvisioningManager {
    /**
     * Parse script as the specified URL
     * @param url url
     * @return script
     * @exception IOException any io exception
     */
    public List<Map<String, Object>> parseScript(URL url) throws IOException;

    /**
     * Parse script as the specified URL
     * @param content The script content
     * @param format The script format
     * @return script
     * @exception IOException any io exception
     */
    public List<Map<String, Object>> parseScript(String content, String format) throws IOException;

    /**
     * Apply batch operations from a file, yaml or json
     *
     * @param url The url where the script can be found
     * @exception IOException any io exception
     */
    void executeScript(URL url) throws IOException;

    /**
     * Apply batch operations from a file, yaml or json
     *
     * @param content The script content
     * @param format The script format
     * @exception IOException any io exception
     */
    void executeScript(String content, String format) throws IOException;

    /**
     * Apply batch operations
     *
     * @param script The list of operations
     * @exception IOException any io exception
     */
    void executeScript(List<Map<String, Object>> script);

    void executeScript(List<Map<String, Object>> script, Map<String,Object> context);

    /**
     * Apply batch operations from a list of entries
     *
     * @param script The entries to execute
     * @param executionContext The context
     * @exception IOException any io exception
     */
    void executeScript(List<Map<String, Object>> script, ExecutionContext executionContext);
}
