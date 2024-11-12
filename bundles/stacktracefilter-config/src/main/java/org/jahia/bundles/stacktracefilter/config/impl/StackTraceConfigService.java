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
package org.jahia.bundles.stacktracefilter.config.impl;

import org.jahia.bin.errors.StackTraceFilter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * This service is responsible for listening to OSGi configuration updates and modifying the StackTrace configuration
 */
@Component(immediate = true,configurationPid = "org.jahia.bundles.stacktracefilter.config")
@Designate(ocd = StackTraceConfigService.Config.class)
public class StackTraceConfigService {

    @ObjectClassDefinition(name = "%stackTraceFilterConfig.name", description = "%stackTraceFilterConfig.description", localization = "OSGI-INF/l10n/stackTraceFilterConfiguration")
    public @interface Config {
        @AttributeDefinition(name = "%filteredPackages.name", defaultValue = StackTraceFilter.DEFAULT_FILTERED_PACKAGES, description = "%filteredPackages.description")
        String filteredPackages() default StackTraceFilter.DEFAULT_FILTERED_PACKAGES;

        @AttributeDefinition(name = "%maxNbOfLines.name", defaultValue = "" + StackTraceFilter.DEFAULT_MAX_NUMBER_OF_LINES, description = "%maxNbOfLines.description")
        int maxNbOfLines() default StackTraceFilter.DEFAULT_MAX_NUMBER_OF_LINES;

        @AttributeDefinition(name = "%log4jAppenders.name", defaultValue = StackTraceFilter.DEFAULT_APPENDERS_TO_MODIFY, description = "%log4jAppenders.description")
        String log4jAppenders() default StackTraceFilter.DEFAULT_APPENDERS_TO_MODIFY;

    }

    @Activate
    public void activate(Config config) {
        modified(config);
    }

    @Modified void modified(Config config) {
        if (config == null) {
            return;
        }
        StackTraceFilter.init(config.filteredPackages(), config.maxNbOfLines(), config.log4jAppenders());
    }

    @Deactivate
    public void deactivate(Config config) {
        modified(config);
    }

}
