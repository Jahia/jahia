/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
