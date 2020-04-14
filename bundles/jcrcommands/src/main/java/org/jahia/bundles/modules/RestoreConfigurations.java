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
package org.jahia.bundles.modules;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.services.modulemanager.spi.ConfigService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Command to store karaf configuration into JCR
 */
@Command(
        scope = "config",
        name = "restore",
        description = "Store confgiuration into JCR"
)
@Service
public class RestoreConfigurations implements Action {

    @Reference
    private ConfigService configService;

    @Argument(description = "Types", multiValued = true)
    private List<ConfigService.ConfigType> types = Arrays.asList(ConfigService.ConfigType.MODULE_DEFAULT, ConfigService.ConfigType.USER);

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public Object execute() throws Exception {
        Map<String, ConfigService.ConfigType> configs = configService.getAllConfigurations();
        Map<String, ConfigService.ConfigType> filteredConfigs = configs.keySet().stream().filter(k -> types.contains(configs.get(k))).collect(Collectors.toMap(Function.identity(), configs::get));
        System.out.println("Restoring config : " + types);

        Collection<String> restored = configService.restoreConfigurations(types);

        // Fill the table to output result.
        ShellTable table = new ShellTable();
        table.column(new Col("Filename"))
                .column(new Col("Type"))
                .column(new Col("Restored"));
        for (Map.Entry<String, ConfigService.ConfigType> entry : filteredConfigs.entrySet()) {
            table.addRow().addContent(entry.getKey(), entry.getValue().toString(), restored.contains(entry.getKey()));
        }

        table.print(System.out, true);

        return null;
    }
}
