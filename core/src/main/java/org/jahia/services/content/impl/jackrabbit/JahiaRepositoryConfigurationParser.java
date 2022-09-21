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
package org.jahia.services.content.impl.jackrabbit;

import java.util.Properties;

import org.apache.jackrabbit.core.config.ConfigurationException;
import org.apache.jackrabbit.core.config.RepositoryConfigurationParser;
import org.apache.jackrabbit.core.util.db.ConnectionFactory;
import org.jahia.utils.PlaceholderUtils;

/**
 * DX specific repository configuration parser that supports nested value placeholders.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaRepositoryConfigurationParser extends RepositoryConfigurationParser {

    public JahiaRepositoryConfigurationParser(Properties variables) {
        super(variables);
    }

    public JahiaRepositoryConfigurationParser(Properties variables, ConnectionFactory connectionFactory) {
        super(variables, connectionFactory);
    }

    @Override
    protected RepositoryConfigurationParser createSubParser(Properties variables) {
        // overlay the properties
        Properties props = new Properties(getVariables());
        props.putAll(variables);
        return new JahiaRepositoryConfigurationParser(props, connectionFactory);
    }

    @Override
    protected String replaceVariables(String value) throws ConfigurationException {
        try {
            return PlaceholderUtils.PLACEHOLDER_HELPER_STRICT.replacePlaceholders(value, getVariables());
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }
}
