/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.framework;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.zip.JahiaArchiveFileHandler;
import org.jahia.utils.zip.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class JahiaWebInitializer implements ApplicationContextInitializer<GenericWebApplicationContext> {

    private static final transient Logger logger = LoggerFactory
            .getLogger(JahiaWebInitializer.class);    
    
    @Override
    public void initialize(GenericWebApplicationContext webAppContext) {
        try{
            Resource[] resources = webAppContext
                    .getResources("classpath*:org/jahia/config/jahiaunittest.properties");
            PropertiesFactoryBean propertiesFactory = new PropertiesFactoryBean();
            propertiesFactory.setLocations(resources);
            propertiesFactory.afterPropertiesSet();
            Properties properties = propertiesFactory.getObject();
                
            String jackrabbitHome = (String) properties
                    .get("jahia.jackrabbit.home");
            if (jackrabbitHome != null) {
                jackrabbitHome = webAppContext.getResource(jackrabbitHome)
                        .getURL().getPath();
                File repoHome = new File(jackrabbitHome);
                if (!repoHome.exists()) {
                    repoHome.mkdirs();
                }
                
                if (resources[0] != null && ResourceUtils.isJarURL(resources[0].getURL())) {
                    URL jarUrl = ResourceUtils.extractJarFileURL(resources[0]
                            .getURL());
                    try {
                        new JahiaArchiveFileHandler(jarUrl.getPath()).unzip(
                                "./target/test-repo", new PathFilter() {

                                    @Override
                                    public boolean accept(String path) {
                                        // TODO Auto-generated method stub
                                        return path.startsWith("WEB-INF");
                                    }
                                });
                    } catch (JahiaException e) {
                        logger.error("Unable to extract JAR");
                    }
                } else if (resources[0] != null) {
                    FileUtils.copyDirectory(new File(StringUtils.substringBefore(resources[0].getURI().getPath(), "org"), "WEB-INF"), new File(
                            "./target/test-repo", "WEB-INF"));
                }
            }
        } catch (IOException e) {

        }
    }
}
