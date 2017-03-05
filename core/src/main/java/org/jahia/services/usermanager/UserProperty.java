/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.usermanager;

import java.io.Serializable;

/**
 * @deprecated
 */
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
        this.name = name;
        this.value = value;
        this.readOnly = readOnly;
        this.display = TEXT_FIELD;
    }

    public UserProperty(String name, String value, boolean readOnly, String display) {
        this.name = name;
        this.value = value;
        this.readOnly = readOnly;
        this.display = display;
    }

    protected UserProperty(UserProperty copy) {
        this.name = copy.name;
        this.value = copy.value;
        this.readOnly = copy.readOnly;
        this.display = copy.display;
    }

    public Object clone() {
        return new UserProperty(this);
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
