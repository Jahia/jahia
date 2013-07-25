/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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