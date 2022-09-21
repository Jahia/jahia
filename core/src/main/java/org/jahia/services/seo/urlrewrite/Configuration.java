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
package org.jahia.services.seo.urlrewrite;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.tika.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.tuckey.web.filters.urlrewrite.Conf;

/**
 * Jahia specific UrlRewriteFilter configuration.
 * 
 * @author Sergiy Shyrkov
 */
public class Configuration extends Conf {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    /**
     * Initializes an instance of this class.
     */
    public Configuration() {
        super();
        super.initialise();
    }

    Configuration(InputStream is, String fileName) {
        super(is, fileName);
        // now call initialize
        super.initialise();
    }

    private Configuration(ServletContext context, InputStream is, Resource[] confLocations) {
        super(context, is, confLocations[0].getFilename(), confLocations[0].getDescription());
        IOUtils.closeQuietly(is);
        for (int i = 1; i < confLocations.length; i++) {
            Resource resource = confLocations[i];
            InputStream stream = null;
            try {
                stream = resource.getInputStream();
                loadDom(stream);
            } catch (Exception e) {
                logger.error("Error loading URL rewrite rules from " + resource.getDescription(), e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        // now call initialize
        super.initialise();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param context
     *            current servlet context
     * @param confLocations
     *            configuration files locations
     * @throws IOException
     */
    public Configuration(ServletContext context, Resource[] confLocations) throws IOException {
        this(context, confLocations[0].getInputStream(), confLocations);
    }

    @Override
    public void initialise() {
        // prevent it from being executed until all rules are loaded
    }
}
