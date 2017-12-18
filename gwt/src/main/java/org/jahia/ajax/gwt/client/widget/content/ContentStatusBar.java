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
package org.jahia.ajax.gwt.client.widget.content;

import org.jahia.ajax.gwt.client.widget.tripanel.BottomBar;
import org.jahia.ajax.gwt.client.widget.tripanel.MyStatusBar;
import com.extjs.gxt.ui.client.widget.Component;

/**
 * 
 *
 * @author rfelden
 * @version 23 juin 2008 - 17:35:03
 */
public class ContentStatusBar extends BottomBar {

    private MyStatusBar m_component ;

    public ContentStatusBar() {
        m_component = new MyStatusBar() ;
        m_component.setHeight("19px");
    }

    public Component getComponent() {
        return m_component ;
    }

    public void clear() {
        m_component.clear();
    }

    public void setIconStyle(String style) {
        m_component.setIconStyle(style);
    }

    public void setMessage(String info) {
        m_component.setIconStyle(info) ;
    }

    public void showBusy() {
        m_component.showBusy();
    }

    public void showBusy(String message) {
        m_component.showBusy(message);
    }

}
