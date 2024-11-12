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
package org.jahia.services.importexport;

import java.io.File;

/**
 * A XMLContentTransformer performs some modifications in a Jahia exported content xml file.
 * It can be used when the content structure does not fit the set of templates used to import it. To use this feature,
 * the first step is to define one or more implementations of this interface, and then to declare it in the
 * WEB-INF/etc/spring/applicationcontext-services.xml configuration file of the Jahia application.
 * <p/>
 * &lt;bean id="ImportExportService" parent="proxyTemplate"&gt;
 * &lt;property name="target"&gt;
 * &lt;bean class="org.jahia.services.importexport.ImportExportBaseService" parent="jahiaServiceTemplate" factory-method="getInstance"&gt;
 * <p/>
 * ...
 * <p/>
 * &lt;property name="xmlContentTransformers"&gt;
 * &lt;list&gt;
 * &lt;bean class="org.myProject.AnImplementation" /&gt;
 * &lt;bean class="org.myProject.AnOtherImplementation" /&gt;
 * &lt;/list&gt;
 * &lt;/property&gt;
 * &lt;/bean&gt;
 * &lt;/property&gt;
 * &lt;/bean&gt;
 * <p/>
 * This interface provides the transform() method that will be called by the import feature. Several implementations
 * can be used to simplify the code, for example focusing on a single modification in the xml file. In this case,
 * the different declared implementations will be processed sequencially. It is very important to keep in mind that
 * the output of the last processed Object has to be a Jahia compliant content export file, as it will be processed
 * by the import feature, instead of the initial file.
 */
public interface XMLContentTransformer {
    /**
     * Performs some custom modifications in an Jahia exported content xml file, and returns the updated file.
     *
     * @param input A Jahia exported content xml file
     * @return The updated file
     */
    public File transform(File input);

    /**
     * Performs some custom modifications in an Jahia exported content xml file, and returns the updated file.
     *
     * @param input A Jahia exported content xml file
     * @param tmpDirectory Temporary directory where the XMLContentTransformer should write some output for troubleshooting purpose
     * @return The updated file
     */
    public File transform(File input, File tmpDirectory);
}
