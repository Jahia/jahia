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
package org.jahia.taglibs.search.facets;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents facet data for using in a JSP view.
 *
 * @author Sergiy Shyrkov
 *
 * @since 7.2.3.1
 */
public class FacetData {

    private Collection<String> activeValues = Collections.emptySet();

    private String parameterNameRegex;

    private String valueParameterName;

    /**
     * Initializes an instance of this class.
     */
    FacetData() {
        super();
    }

    /**
     * Returns a collection of currently active values for this face, based on corresponding request parameter values.
     *
     * @return a collection of currently active values for this facet or an empty collections if there are no active values
     */
    public Collection<String> getActiveValues() {
        return activeValues;
    }

    /**
     * The regular expression for the request parameter names of this facet (used in calculating the drilldown URL).
     *
     * @return regular expression for the request parameter names of this facet (used in calculating the drilldown URL)
     */
    public String getParameterNameRegex() {
        return parameterNameRegex;
    }

    /**
     * Returns the name of the request parameter, this facet is using to submit its active values.
     *
     * @return the name of the request parameter, this facet is using to submit its active values
     */
    public String getValueParameterName() {
        return valueParameterName;
    }

    void setActiveValues(Collection<String> activeValues) {
        this.activeValues = activeValues != null ? activeValues : Collections.emptySet();
    }

    void setParameterNameRegex(String parameterNameRegex) {
        this.parameterNameRegex = parameterNameRegex;
    }

    void setValueParameterName(String valueParameterName) {
        this.valueParameterName = valueParameterName;
    }

}
