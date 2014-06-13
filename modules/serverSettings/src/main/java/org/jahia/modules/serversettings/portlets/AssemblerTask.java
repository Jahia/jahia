/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.modules.serversettings.portlets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pluto.util.assemble.Assembler;
import org.apache.pluto.util.assemble.AssemblerConfig;
import org.apache.pluto.util.assemble.war.WarAssembler;
import org.jahia.settings.SettingsBean;
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

        boolean isJBoss = SettingsBean.getInstance().getServer().startsWith("jboss");

        if (!needRewriting(getWebapp())) {
            logger.info("No rewriting is needed for the web.xml. Skipping.");
            File destFile =  new File(tempDir, getWebapp().getName());
            FileUtils.copyFile(getWebapp(), destFile, true);
            return isJBoss ? JBossPortletHelper.process(destFile) : destFile;
        }


        final File tempDir = getTempDir();
        final AssemblerConfig config = new AssemblerConfig();
        config.setSource(getWebapp());
        config.setDestination(tempDir);

        WarAssembler assembler = new WarAssembler();
        assembler.assemble(config);

        File destFile = new File(tempDir, getWebapp().getName());

        if (isJBoss) {
            destFile = JBossPortletHelper.process(destFile);
        }

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

