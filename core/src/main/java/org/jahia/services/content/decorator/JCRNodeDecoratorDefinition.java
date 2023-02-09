/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.decorator;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.templates.JahiaModuleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class JCRNodeDecoratorDefinition implements JahiaModuleAware {

    private static Logger logger = LoggerFactory.getLogger(JCRNodeDecoratorDefinition.class);

    private Map<String, Class> decorators = null;
    private Map<String, String> decoratorsAsString = new HashMap<String, String>();
    private JahiaTemplatesPackage module;

    public Map<String, Class> getDecorators() {
        if (decorators == null && decoratorsAsString != null) {
            decorators = new HashMap<String, Class>();
            for (Map.Entry<String, String> decorator : decoratorsAsString.entrySet()) {
                try {
                    this.decorators.put(decorator.getKey(), module.getClassLoader().loadClass(decorator.getValue()));
                } catch (Exception e) {
                    logger.error("Unable to instanciate decorator: " + decorator.getValue(), e);
                }
            }
        }
        return decorators;
    }

    @Override
    public void setJahiaModule(JahiaTemplatesPackage module) {
        this.module = module;
    }

    public void setDecorators(Map<String, String> decorators) {
        decoratorsAsString = decorators;
    }
}
