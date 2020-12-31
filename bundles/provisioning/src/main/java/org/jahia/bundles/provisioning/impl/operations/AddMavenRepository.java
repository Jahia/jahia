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
package org.jahia.bundles.provisioning.impl.operations;

import org.apache.commons.lang.StringUtils;
import org.jahia.bundles.config.OsgiConfigService;
import org.jahia.services.modulemanager.spi.Config;
import org.jahia.services.modulemanager.util.PropertiesValues;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Add maven repository operation
 */
@Component(service = Operation.class, property = "type=addMavenRepository")
public class AddMavenRepository implements Operation {
    private static final Logger logger = LoggerFactory.getLogger(AddMavenRepository.class);
    public static final String ADD_MAVEN_REPOSITORY = "addMavenRepository";
    private OsgiConfigService configService;

    @Reference
    protected void setConfigService(OsgiConfigService configService) {
        this.configService = configService;
    }

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.get(ADD_MAVEN_REPOSITORY) instanceof String;
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        try {
            Config settings = configService.getConfig("org.ops4j.pax.url.mvn");
            PropertiesValues values = settings.getValues();
            Set<String> vals = new LinkedHashSet<>(Arrays.asList(values.getProperty("org.ops4j.pax.url.mvn.repositories").split("[, ]+")));
            if (vals.add((String) entry.get(ADD_MAVEN_REPOSITORY))) {
                values.setProperty("org.ops4j.pax.url.mvn.repositories", StringUtils.join(vals, ", "));
                configService.storeConfig(settings);
            }
        } catch (IOException e) {
            logger.error("Cannot update configurations", e);
        }
    }
}
