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
