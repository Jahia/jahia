/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.admin.components;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pluto.util.assemble.Assembler;
import org.apache.pluto.util.assemble.AssemblerConfig;
import org.apache.pluto.util.assemble.war.WarAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * .
 * User: jahia
 * Date: 15 avr. 2009
 * Time: 12:31:07
 */
public class AssemblerTask {
    
    private static final Logger logger = LoggerFactory.getLogger(AssemblerTask.class);
    
    private File webapp;
    private File tempDir;

    public AssemblerTask(File tempDir, File webapp) {
        this.tempDir = tempDir;
        this.webapp = webapp;
    }

    public File getWebapp() {
        return webapp;
    }

    public File getTempDir() {
        return tempDir;
    }

    public File execute() throws Exception {
        long timer = System.currentTimeMillis();
        logger.info("Got a command to prepare " + getWebapp() + " WAR file to be deployed into the Pluto container");
        validateArgs();
        
        if (!needRewriting(getWebapp())) {
            logger.info("No rewriting is needed for the web.xml. Skipping.");
            File destFile =  new File(tempDir, getWebapp().getName());
            FileUtils.copyFile(getWebapp(), destFile, true);
            return destFile;
        }


        final File tempDir = getTempDir();
        final AssemblerConfig config = new AssemblerConfig();
        config.setSource(getWebapp());
        config.setDestination(tempDir);

        WarAssembler assembler = new WarAssembler();
        assembler.assemble(config);

        File destFile = new File(tempDir, getWebapp().getName());
        
        logger.info("Done assembling WAR file {} in {} ms.", destFile, (System.currentTimeMillis() - timer));

        return destFile;
    }

    private boolean needRewriting(File source) throws FileNotFoundException, IOException {
        final JarInputStream jarIn = new JarInputStream(new FileInputStream(source));
        String webXml = null;
        JarEntry jarEntry;
        try {
            // Read the source archive entry by entry
            while ((jarEntry = jarIn.getNextJarEntry()) != null) {
                if (Assembler.SERVLET_XML.equals(jarEntry.getName())) {
                    webXml = IOUtils.toString(jarIn);
                }
                jarIn.closeEntry();
            }
        } finally {
            jarIn.close();
        }

        return webXml == null || !webXml.contains(Assembler.DISPATCH_SERVLET_CLASS);
    }

    private void validateArgs() throws Exception {
        if (webapp != null) {
            if (!webapp.exists()) {
                throw new Exception("webapp " + webapp.getAbsolutePath() + " does not exist");
            }
            return;
        }
    }

}


