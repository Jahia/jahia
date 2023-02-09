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
package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;

/**
 *
 * User: toto
 * Date: Sep 14, 2010
 * Time: 12:29:03 PM
 *
 */
public class GWTJahiaWorkflowType  extends BaseModelData implements Serializable {

    public GWTJahiaWorkflowType() {
    }

    public GWTJahiaWorkflowType(String name) {
        setName(name);
    }

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public void setDisplayName(String displayName) {
        set("displayName", displayName);
    }

    public String getDisplayName() {
        return get("displayName");
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) || (o != null && this.getClass() == o.getClass()
                && ((GWTJahiaWorkflowType) o).getName().equals(getName()));
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }



}
