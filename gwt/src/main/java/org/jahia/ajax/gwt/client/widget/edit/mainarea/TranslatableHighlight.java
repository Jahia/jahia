/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.edit.mainarea;

/**
 * Highlight of translatable modules
 *
 * User: kevan
 */
public class TranslatableHighlight extends Selection {
    public TranslatableHighlight(Module m) {
        super(m);
    }

    @Override
    public void configure() {
        this.currentContainer = module.getContainer();
        if (module instanceof ListModule) {
            top.setStyleName("translatable-top-list");
            bottom.setStyleName("translatable-bottom-list");
            left.setStyleName("translatable-left-list");
            right.setStyleName("translatable-right-list");
        } else if (module instanceof AreaModule) {
            top.setStyleName("translatable-top-area");
            bottom.setStyleName("translatable-bottom-area");
            left.setStyleName("translatable-left-area");
            right.setStyleName("translatable-right-area");
        } else {
            top.setStyleName("translatable-top-simple");
            bottom.setStyleName("translatable-bottom-simple");
            left.setStyleName("translatable-left-simple");
            right.setStyleName("translatable-right-simple");
        }
        top.setStyleAttribute("z-index", "995");
        bottom.setStyleAttribute("z-index", "995");
        left.setStyleAttribute("z-index", "995");
        right.setStyleAttribute("z-index", "995");
    }
}
