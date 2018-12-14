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
package org.jahia.settings;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties factory bean that retrieves matching environment variables, converts their keys to appropriate DX ones and sets corresponding
 * system properties.
 * 
 * @author Sergiy Shyrkov
 */
public class EnvironmentVariablesPropertiesFactoryBean extends SystemPropertiesFactoryBean {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentVariablesPropertiesFactoryBean.class);

    @Override
    protected String convertKey(String key) {
        // We replace underscore with dots
        // Special case with two underscores (like an escaped one) will result in a single underscore after conversion
        return StringUtils.replace(StringUtils.replaceChars(key, '_', '.'), "..", "_");
    }

    @Override
    protected Properties createProperties() throws IOException {
        Properties props = processProperties(System.getenv());

        logger.info("Detected the following environment variables, which are converted into Java system properties"
                + " and will be considered in the configuration: {}", (Object) props.keySet().toArray());

        setSystemProperties(props, true);

        return props;
    }

}
