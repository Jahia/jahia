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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.widget.content.ContentViews;

/**
 *
 * User: toto
 * Date: Sep 30, 2009
 * Time: 6:29:20 PM
 *
 */
public class SwitchViewActionItem extends BaseActionItem {
    private String view;

    public void setView(String view) {
        this.view = view;
    }

    public void onComponentSelection() {
        if (linker instanceof ManagerLinker) {
            TopRightComponent trc = ((ManagerLinker)linker).getTopRightObject();
            if (trc instanceof ContentViews) {
                if ("list".equals(view)) {
                    ((ContentViews)trc).switchToListView();
                } else if ("thumbs".equals(view)) {
                    ((ContentViews)trc).switchToThumbView();
                } else if ("detailed".equals(view)) {
                    ((ContentViews)trc).switchToDetailedThumbView();
                }
            }
        }
    }
}
