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
package org.jahia.services.content.nodetypes.initializers;

import java.util.List;
import java.util.Locale;

import javax.jcr.Value;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

/**
 * Defines a default value initializer for node properties, which support internationalization.
 * 
 * @author Sergiy Shyrkov
 */
public interface I15dValueInitializer extends ValueInitializer {

    public static final String DEFAULT_VALUE = "__I15D_DEFAULT_VALUE__";

    /**
     * Returns an array of default values for the specified property, considering parameters and the locale.
     * 
     * @param declaringPropertyDefinition
     *            the definition of the property to compute default values for
     * @param params
     *            the optional parameters for this value initializer
     * @param locale
     *            the current node locale to consider for language-dependent values
     * @return an array of default values for the specified property, considering parameters and the locale
     */
    Value[] getValues(ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params, Locale locale);

}
