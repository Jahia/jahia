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
package org.jahia.ajax.gwt.client.widget.tripanel;

import com.extjs.gxt.ui.client.widget.Component;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;

/**
 * This class represents a part of the universal tree browser. Its purpose is to define
 * linkage mechanism in order to have interaction between components.
 *
 * @author rfelden
 * @version 19 juin 2008 - 14:37:33
 */
public abstract class LinkableComponent {

    /**
     * This is the link target, it deals with communication between components.
     */
    private ManagerLinker m_linker ;

    /**
     * Set the linker, should be used by the linker itself in order lay a callback in each component.
     * @param linker the linker
     */
    public void initWithLinker(ManagerLinker linker) {
        m_linker = linker ;
    }

    /**
     * Get the linker shared by every component.
     * @return the linker
     */
    public ManagerLinker getLinker() {
        return m_linker ;
    }

    /**
     * Get the UI component used by the subclass since it is not directly a subclass of a widget
     * (multiple inheritance is not supported in Java, damn).
     * @return the ui component
     */
    public abstract Component getComponent() ;

}
