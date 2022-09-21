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
package org.jahia.settings;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;

/**
 * Properties factory bean that retrieves the matching system properties.
 * 
 * @author Sergiy Shyrkov
 */
public class SystemPropertiesFactoryBean extends PropertiesFactoryBean {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentVariablesPropertiesFactoryBean.class);

    private String prefix;

    @Override
    protected Properties createProperties() throws IOException {
        Properties props = new Properties();

        Properties sysProps = System.getProperties();
        for (Object keyObj : sysProps.keySet()) {
            String key = keyObj.toString();
            if (StringUtils.isNotEmpty(prefix) && key.startsWith(prefix)) {
                String value = sysProps.getProperty(key);
                if (value != null) {
                    props.put(StringUtils.substringAfter(key, prefix), value);
                }
            }
        }

        logger.info("Detected the following system properties, which will be considered in the configuration: {}",
                (Object) props.keySet().toArray());

        return props;
    }

    protected String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
