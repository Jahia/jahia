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
package org.jahia.services.usermanager;

import java.io.Serializable;

/**
 * @deprecated
 */
@Deprecated(since = "7.2.0.0", forRemoval = true)
public class UserProperty implements Serializable {

    private static final long serialVersionUID = -4066934002153945776L;

    public static final String CHECKBOX = "checkbox";
    public static final String SELECT_BOX = "selectbox";
    public static final String TEXT_FIELD = "textfield";

    private String name;
    private String value;
    private boolean readOnly;
    private String display;

    public UserProperty(String name, String value, boolean readOnly) {
        this(name, value, readOnly, TEXT_FIELD);
    }

    public UserProperty(String name, String value, boolean readOnly, String display) {
        this.name = name;
        this.value = value;
        this.readOnly = readOnly;
        this.display = display;
    }

    /**
     * Copy constructor
     *
     * @param copy the property to create the copy from
     */
    public UserProperty(UserProperty copy) {
        this(copy.name, copy.value, copy.readOnly, copy.display);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

}
