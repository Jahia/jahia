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
package org.jahia.services.content.rules;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Facade object for extracted metadata property to be used in rules.
 * User: toto
 * Date: 8 janv. 2008
 * Time: 15:57:55
 */
public class ExtractedVariable {
    private String correspondingNodeTypeName;
    private String correspondingPropertyName;
    private String name;
    private String node;
    private Object value;

    public ExtractedVariable(String nodePath, String name, Object value) {
        this.node = nodePath;
        this.name = name;
        this.value = value;
    }

    public ExtractedVariable(String nodePath, String name, Object value, String correspondingNodeTypeName, String correspondingPropertyName) {
        this(nodePath, name, value);
        this.correspondingNodeTypeName = correspondingNodeTypeName;
        this.correspondingPropertyName = correspondingPropertyName;
    }

    /**
     * @return the correspondingNodeTypeName
     */
    public String getCorrespondingNodeTypeName() {
        return correspondingNodeTypeName;
    }

    /**
     * @return the correspondingPropertyName
     */
    public String getCorrespondingPropertyName() {
        return correspondingPropertyName;
    }

    public String getName() {
        return name;
    }

    public String getNode() {
        return node;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
