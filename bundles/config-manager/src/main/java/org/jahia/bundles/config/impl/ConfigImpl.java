/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.config.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.jahia.bundles.config.ConfigUtil;
import org.jahia.bundles.config.Format;
import org.jahia.services.modulemanager.spi.Config;
import org.jahia.services.modulemanager.util.PropertiesManager;
import org.jahia.services.modulemanager.util.PropertiesValues;
import org.osgi.service.cm.Configuration;

import java.io.*;
import java.util.*;

import static org.jahia.bundles.config.ConfigUtil.flatten;

/**
 * Config implementation
 */
public class ConfigImpl implements Config {
    private Configuration conf;

    private String identifier;

    private Map<String, String> props;

    private Map<String, String> contentProps;
    private String content;

    private Format format;

    private PropertiesManager propertiesManager;

    /**
     * Build ConfigImpl from an existing configuration and identifier
     *
     * @param conf       the CM conf
     * @param identifier the identifier
     */
    public ConfigImpl(Configuration conf, String identifier) {
        this(conf, identifier, Format.CFG);
    }

    /**
     * Build ConfigImpl from an existing configuration and identifier, specify format
     *
     * @param conf       the CM conf
     * @param identifier the identifier
     * @param format     the format
     */
    public ConfigImpl(Configuration conf, String identifier, Format format) {
        this.conf = conf;
        this.props = (conf.getProperties() != null) ? ConfigUtil.getMap(conf.getProperties()) : new HashMap<>();
        this.propertiesManager = new PropertiesManager(this.props);
        this.identifier = identifier;
        this.format = format;
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
            if (format == Format.CFG) {
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
            } else if (format == Format.YAML) {
                StringWriter out = new StringWriter();
                YAMLMapper yamlMapper = new YAMLMapper();
                yamlMapper.writeValue(out, getValues().getStructuredMap());
                content = out.getBuffer().toString();
            }
        }
        return content;
    }

    public void setContent(String content) throws IOException {
        if (format == Format.CFG) {
            Properties p = new Properties();
            p.load(new StringReader(content));

            this.content = content;
            this.props.clear();
            p.stringPropertyNames().forEach(e -> props.put(e, p.getProperty(e)));
            this.contentProps = new HashMap<>(props);
        } else if (format == Format.YAML) {
            YAMLMapper yamlMapper = YAMLMapper.builder().build();
            final Map<String, String> ht = new HashMap<>();
            try (Reader r = new StringReader(content)) {
                Map<String, Object> m = yamlMapper.readValue(r, new TypeReference<Map<String, Object>>() {
                });
                flatten(ht, "", m);
            }
            this.props = ht;
            this.contentProps = new HashMap<>(props);
        }
    }

    @Override
    public String getFormat() {
        return format.name();
    }

    public void setFormat(String format) {
        if (this.format != Format.valueOf(format)) {
            this.format = Format.valueOf(format);
            this.content = null;
        }
    }

    @Override
    public String toString() {
        return props.toString();
    }

}
