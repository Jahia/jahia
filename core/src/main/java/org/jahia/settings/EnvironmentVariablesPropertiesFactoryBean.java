/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.settings;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
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

        setSystemProperties(props);

        return props;
    }

    protected Properties processProperties(Map<String, String> availableProps) {
        Properties props = new Properties();

        for (Map.Entry<String, String> entry : availableProps.entrySet()) {
            String key = entry.getKey();
            String effectiveKey = null;
            String value = entry.getValue();
            if (value != null) {
                if (StringUtils.isNotEmpty(getPrefix()) && key.toLowerCase().startsWith(getPrefix())
                        && key.length() > getPrefix().length()) {
                    effectiveKey = key.substring(getPrefix().length());
                }
                if (effectiveKey != null) {
                    props.put(convertKey(effectiveKey), value);
                }
            }
        }

        return props;
    }

    protected void setSystemProperties(Properties props) {
        for (Object keyObj : props.keySet()) {
            String key = keyObj.toString();

            logger.info("Setting system property: {}", key);
            JahiaContextLoaderListener.setSystemProperty(key, props.getProperty(key));
        }
    }
}
