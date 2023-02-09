/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.jcrcommands.modules;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.services.modulemanager.spi.ConfigService;

import java.util.Collection;
import java.util.Map;

/**
 * Command to store karaf configuration into JCR
 */
@Command(
        scope = "config",
        name = "store",
        description = "Store confgiuration into JCR"
)
@Service
@SuppressWarnings({"java:S106","java:S1166"})
public class StoreConfigurations implements Action {

    @Reference
    private ConfigService configService;

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public Object execute() throws Exception {
        Map<String, ConfigService.ConfigType> configs = configService.getAllConfigurationTypes();

        Collection<String> saved = configService.storeAllConfigurationsToJCR();

        // Fill the table to output result.
        ShellTable table = new ShellTable();
        table.column(new Col("Filename"))
                .column(new Col("Type"))
                .column(new Col("Updated"));
        for (Map.Entry<String, ConfigService.ConfigType> entry : configs.entrySet()) {
            table.addRow().addContent(entry.getKey(), entry.getValue().toString(), saved.contains(entry.getKey()));
        }

        table.print(System.out, true);

        return null;
    }
}
