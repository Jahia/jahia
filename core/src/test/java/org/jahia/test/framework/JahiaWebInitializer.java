/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
