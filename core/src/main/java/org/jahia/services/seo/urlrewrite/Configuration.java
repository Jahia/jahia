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
