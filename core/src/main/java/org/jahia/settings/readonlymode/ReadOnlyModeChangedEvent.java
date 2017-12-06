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
package org.jahia.settings.readonlymode;

import org.springframework.context.ApplicationEvent;

/**
 * This event is broadcasted to notify corresponding Spring service about the change of the read-only mode.
 * 
 * @author Sergiy Shyrkov
 */
public class ReadOnlyModeChangedEvent extends ApplicationEvent {

    private static final long serialVersionUID = -9011075112888785206L;

    private boolean readOnlyModeIsOn;

    /**
     * Initializes an instance of this class.
     * 
     * @param source the event source object
     * @param readOnlyModeIsOn <code>true</code> in case the read-only mode is enabled; <code>false</code> if it is disabled
     * 
     */
    public ReadOnlyModeChangedEvent(Object source, boolean readOnlyModeIsOn) {
        super(source);
        this.readOnlyModeIsOn = readOnlyModeIsOn;
    }

    /**
     * Returns <code>true</code> in case the read-only mode is enabled; <code>false</code> if it is disabled.
     * 
     * @return <code>true</code> in case the read-only mode is enabled; <code>false</code> if it is disabled
     */
    public boolean isReadOnlyModeIsOn() {
        return readOnlyModeIsOn;
    }

}
