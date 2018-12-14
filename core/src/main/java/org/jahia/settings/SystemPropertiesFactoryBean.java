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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;

/**
 * Properties factory bean that retrieves matching system properties.
 * 
 * @author Sergiy Shyrkov
 */
public class SystemPropertiesFactoryBean extends PropertiesFactoryBean {

    private static final Logger logger = LoggerFactory.getLogger(SystemPropertiesFactoryBean.class);

    private static Map<String, String> toMap(Properties properties) {
        Map<String, String> map = new HashMap<>(properties.size());
        for (Object keyObj : properties.keySet()) {
            String key = keyObj.toString();
            map.put(key, properties.getProperty(key));
        }
        return map;
    }

    private boolean isPrefixMandatory;

    private String prefix;

    protected String convertKey(String key) {
        return key;
    }

    @Override
    protected Properties createProperties() throws IOException {
        Properties props = processProperties(toMap(System.getProperties()));

        logger.info("Detected the following system properties, which will be considered in the configuration: {}",
                (Object) props.keySet().toArray());

        setSystemProperties(props, false);

        return props;
    }

    protected Properties processProperties(Map<String, String> availableProps) {
        Properties props = new Properties();

        for (Map.Entry<String, String> entry : availableProps.entrySet()) {
            String key = entry.getKey();
            String effectiveKey = null;
            String value = entry.getValue();
            if (value != null) {
                if (StringUtils.isNotEmpty(prefix) && key.toLowerCase().startsWith(prefix)
                        && key.length() > prefix.length()) {
                    effectiveKey = key.substring(prefix.length());
                } else if (!isPrefixMandatory) {
                    // in this case we consider all properties no matter the prefix
                    effectiveKey = key;
                }

                if (effectiveKey != null) {
                    props.put(convertKey(effectiveKey), value);
                }
            }
        }

        return props;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix != null ? prefix.toLowerCase() : prefix;
    }

    public void setPrefixMandatory(boolean isPrefixMandatory) {
        this.isPrefixMandatory = isPrefixMandatory;
    }

    protected void setSystemProperties(Properties props, boolean overwriteIfPresent) {
        for (Object keyObj : props.keySet()) {
            String key = keyObj.toString();

            if (overwriteIfPresent || System.getProperty(key) == null) {
                logger.info("Setting system property: {}", key);
                JahiaContextLoaderListener.setSystemProperty(key, props.getProperty(key));
            }
        }
    }
}
