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
package org.jahia.ajax.gwt.client.data.toolbar;

import org.jahia.ajax.gwt.client.widget.contentengine.ButtonItem;

import java.io.Serializable;
import java.util.List;

/**
 * GWT part of engine configuration
 * see EngineConfiguration for core part
 */

public class GWTEngineConfiguration implements Serializable {

    private List<GWTEngineTab> engineTabs;

    private List<ButtonItem> creationButtons;
    private List<ButtonItem> editionButtons;
    private List<ButtonItem> commonButtons;

    public List<GWTEngineTab> getEngineTabs() {
        return engineTabs;
    }

    public void setEngineTabs(List<GWTEngineTab> engineTabs) {
        this.engineTabs = engineTabs;
    }

    public List<ButtonItem> getCreationButtons() {
        return creationButtons;
    }

    public void setCreationButtons(List<ButtonItem> creationButtons) {
        this.creationButtons = creationButtons;
    }

    public List<ButtonItem> getEditionButtons() {
        return editionButtons;
    }

    public void setEditionButtons(List<ButtonItem> editionButtons) {
        this.editionButtons = editionButtons;
    }

    public List<ButtonItem> getCommonButtons() {
        return commonButtons;
    }

    public void setCommonButtons(List<ButtonItem> commonButtons) {
        this.commonButtons = commonButtons;
    }

}
