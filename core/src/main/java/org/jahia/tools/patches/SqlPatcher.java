/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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

    public static void apply(ApplicationContext ctx) {
        try {
            new SqlPatcher(ctx).execute();
        } catch (IOException e) {
            logger.error("Error executing SQL patches", e);
        }
    }

    private ApplicationContext ctx;

    private SqlPatcher(ApplicationContext ctx) {
        super();
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
        String folder = "/WEB-INF/var/patches/sql/" + DatabaseUtils.getDatabaseType();
        if (ctx.getResource(folder).exists()) {
            return ctx.getResources(folder + "/**/*.sql");
        } else {
            return new Resource[] {};
        }
    }

}
