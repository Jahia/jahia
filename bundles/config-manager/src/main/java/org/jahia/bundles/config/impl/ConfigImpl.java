/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.config.impl;

import org.jahia.services.modulemanager.spi.Config;
import org.jahia.services.modulemanager.util.PropertiesManager;
import org.jahia.services.modulemanager.util.PropertiesValues;
import org.osgi.service.cm.Configuration;

import java.io.*;
import java.util.*;

/**
 * Config implementation
 */
public class ConfigImpl implements Config {
    private Configuration conf;

    private String identifier;

    private Map<String, String> props;

    private Map<String, String> contentProps;
    private String content;

    private PropertiesManager propertiesManager;

    /**
     * Build ConfigImpl from an existing configuration and identifier
     *
     * @param conf the CM conf
     * @param identifier the identifier
     */
    public ConfigImpl(Configuration conf, String identifier) {
        this.conf = conf;
        this.props = (conf.getProperties() != null) ? getMap(conf.getProperties()) : new HashMap<>();
        this.propertiesManager = new PropertiesManager(this.props);
        this.identifier = identifier;
    }

    public Configuration getConfiguration() {
        return conf;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, String> getRawProperties() {
        return props;
    }

    public PropertiesValues getValues() {
        return propertiesManager.getValues();
    }

    public String getContent() throws IOException {
        if (content == null || !props.equals(contentProps)) {
            this.contentProps = new HashMap<>(props);
            StringWriter out = new StringWriter();

            try (BufferedWriter writer = new BufferedWriter(out)) {
                Map<String, String> p = new TreeMap<>(getRawProperties());

                for (Map.Entry<String, String> entry : p.entrySet()) {
                    if (entry.getValue() != null) {
                        writer.write(entry.getKey() + " = " + entry.getValue());
                        writer.newLine();
                    }
                }
            }
            content = out.getBuffer().toString();
        }
        return content;
    }

    public void setContent(String content) throws IOException {
        Properties p = new Properties();
        p.load(new StringReader(content));

        this.content = content;
        this.props.clear();
        p.stringPropertyNames().forEach(e -> props.put(e, p.getProperty(e)));
        this.contentProps = new HashMap<>(props);
    }

    @Override
    public String toString() {
        return props.toString();
    }

    private Map<String, String> getMap(Dictionary<String, ?> d) {
        Map<String, String> m = new HashMap<>();
        Enumeration<String> en = d.keys();
        while (en.hasMoreElements()) {
            String key = en.nextElement();
            if (!key.startsWith("felix.") && !key.startsWith("service.")) {
                m.put(key, d.get(key).toString());
            }
        }
        return m;
    }
}
