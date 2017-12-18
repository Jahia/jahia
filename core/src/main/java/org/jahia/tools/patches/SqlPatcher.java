/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

import static org.jahia.tools.patches.GroovyPatcher.rename;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;
import org.jahia.utils.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * Utility class for applying SQL-based patches on Jahia startup.
 * 
 * @author Sergiy Shyrkov
 */
public final class SqlPatcher {

    private static final Logger logger = LoggerFactory.getLogger(SqlPatcher.class);

    public static void apply(String varDir, ApplicationContext ctx) {
        try {
            new SqlPatcher(varDir, ctx).execute();
        } catch (IOException e) {
            logger.error("Error executing SQL patches", e);
        }
    }

    private ApplicationContext ctx;
    
    private String varDir;

    private SqlPatcher(String varDir, ApplicationContext ctx) {
        super();
        this.varDir = varDir;
        this.ctx = ctx;
    }

    private void execute() throws IOException {
        Resource[] patches = getPatches();
        if (patches.length == 0) {
            logger.debug("No SQL patches to execute were found");
            return;
        }
        long timer = System.currentTimeMillis();
        logger.info("Found {} SQL patches to execute:\n{}", patches.length, StringUtils.join(patches));

        for (Resource r : patches) {
            execute(r);
        }

        logger.info("Execution of SQL patches took {} ms", System.currentTimeMillis() - timer);
    }

    private void execute(Resource r) {
        try {
            logger.info("Executing script {}", r);
            DatabaseUtils.executeScript(new InputStreamReader(r.getInputStream(), Charsets.UTF_8));
            logger.info("Script {} executed successfully", r);
            rename(r, ".installed");
        } catch (Exception e) {
            logger.error("Execution of SQL script " + r + " failed with error: " + e.getMessage(), e);
            rename(r, ".failed");
        }
    }

    private Resource[] getPatches() throws IOException {
        String folder = "file:" + varDir + "/patches/sql/" + DatabaseUtils.getDatabaseType();
        if (ctx.getResource(folder).exists()) {
            return ctx.getResources(folder + "/**/*.sql");
        } else {
            return new Resource[] {};
        }
    }

}
