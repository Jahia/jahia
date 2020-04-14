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
package org.jahia.tools.patches;

import org.jahia.utils.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

import static org.jahia.tools.patches.Patcher.FAILED;
import static org.jahia.tools.patches.Patcher.INSTALLED;
import static org.jahia.tools.patches.Patcher.SKIPPED;

/**
 * Utility class for applying SQL-based patches on Jahia startup.
 *
 * @author Sergiy Shyrkov
 */
public final class SqlPatcher implements PatchExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SqlPatcher.class);

    @Override
    public boolean canExecute(String name, String lifecyclePhase) {
        return name.endsWith(".sql") && lifecyclePhase.equals("contextInitializing");
    }

    @Override
    public String executeScript(String name, String scriptContent) {
        if (name.contains("/" + DatabaseUtils.getDatabaseType() + "/")) {
            try {
                DatabaseUtils.executeScript(new StringReader(scriptContent));
                return INSTALLED;
            } catch (SQLException | IOException e) {
                logger.error("Execution of script failed with error: " + e.getMessage(), e);
                return FAILED;
            }
        }
        return SKIPPED;
    }
}
